package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.dto.enums.TeamStatus;
import com.fpt.sealhackathon.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    /*
     * Check team name uniqueness within an event (case-insensitive).
     * Mirrors the functional index: uq_teams_event_lower_name
     */
    boolean existsByEvent_IdAndNameIgnoreCase(UUID eventId, String name);

    /*
     * Same check excluding a specific team — used on update (PUT /teams/{id}).
     */
    boolean existsByEvent_IdAndNameIgnoreCaseAndIdNot(UUID eventId, String name, UUID excludeTeamId);

    /*
     * List teams by event with optional status filter and name search.
     *
     * Uses a native query with explicit CAST because PostgreSQL's team_status
     * custom type has no implicit cast from varchar, causing:
     *   "operator does not exist: team_status = character varying"
     * when JPQL binds the enum parameter as a plain string.
     *
     * TeamStatus constants are lowercase (e.g. waiting_for_members) so
     * TeamStatus.name() already produces the correct DB string value.
     *
     * Every :search occurrence is wrapped in CAST(... AS text). With the JDBC
     * url's stringtype=unspecified, an uncast NULL string param has no inferable
     * type and PostgreSQL fails with "could not determine data type of parameter".
     */
    @Query(
            value = """
                    SELECT * FROM teams t
                    WHERE t.event_id = :eventId
                      AND (CAST(:#{#status?.name()} AS team_status) IS NULL
                           OR t.status = CAST(:#{#status?.name()} AS team_status))
                      AND (CAST(:search AS text) IS NULL
                           OR LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')))
                    ORDER BY t.created_at ASC
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM teams t
                    WHERE t.event_id = :eventId
                      AND (CAST(:#{#status?.name()} AS team_status) IS NULL
                           OR t.status = CAST(:#{#status?.name()} AS team_status))
                      AND (CAST(:search AS text) IS NULL
                           OR LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')))
                    """,
            nativeQuery = true
    )
    Page<Team> findByEventIdFiltered(
            @Param("eventId") UUID eventId,
            @Param("status") TeamStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    /*
     * Fetch team with its members eagerly in one query.
     * Avoids N+1 when building TeamDetailResponse.
     */
    @Query("""
            SELECT DISTINCT t FROM Team t
            LEFT JOIN FETCH t.members tm
            LEFT JOIN FETCH tm.user
            WHERE t.id = :teamId
            """)
    Optional<Team> findByIdWithMembers(@Param("teamId") UUID teamId);

    /*
     * Find team by event and profile — used by resubmission to prevent duplicates.
     */
    Optional<Team> findByEvent_IdAndTeamProfile_Id(UUID eventId, UUID teamProfileId);

    /*
     * Lightweight existence check by event and profile.
     */
    boolean existsByEvent_IdAndTeamProfile_Id(UUID eventId, UUID teamProfileId);

    /*
     * Coordinator lock. Native UPDATE because locked_at/locked_reason are
     * trigger-managed and mapped read-only on the entity.
     * clearAutomatically refreshes the persistence context after the write.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE teams
            SET locked_at = NOW(),
                locked_reason = :reason
            WHERE id = :teamId
            """, nativeQuery = true)
    void lockTeam(@Param("teamId") UUID teamId, @Param("reason") String reason);

    /*
     * Coordinator disqualify. Native UPDATE because status is trigger-managed
     * and mapped read-only on the entity.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE teams
            SET status = CAST('disqualified' AS team_status),
                disqualified_reason = :reason
            WHERE id = :teamId
            """, nativeQuery = true)
    void disqualifyTeam(@Param("teamId") UUID teamId, @Param("reason") String reason);
}