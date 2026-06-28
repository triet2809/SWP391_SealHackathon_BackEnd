package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.dto.enums.TeamMemberStatus;
import com.fpt.sealhackathon.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    /*
     * Find the active leader of a team.
     * Native query required: PostgreSQL custom types (team_member_role, team_member_status)
     * have no implicit cast from varchar — JPQL parameter binding fails with
     * "operator does not exist: team_member_role = character varying".
     * CAST resolves the type mismatch explicitly.
     */
    @Query(value = """
            SELECT * FROM team_members
            WHERE team_id = :teamId
              AND role = CAST('leader' AS team_member_role)
              AND status IN (
                  CAST('invited' AS team_member_status),
                  CAST('accepted' AS team_member_status)
              )
            LIMIT 1
            """, nativeQuery = true)
    Optional<TeamMember> findActiveLeaderByTeamId(@Param("teamId") UUID teamId);

    /*
     * Find a specific user's active membership in a team.
     */
    @Query(value = """
            SELECT * FROM team_members
            WHERE team_id = :teamId
              AND user_id = :userId
              AND status IN (
                  CAST('invited' AS team_member_status),
                  CAST('accepted' AS team_member_status)
              )
            LIMIT 1
            """, nativeQuery = true)
    Optional<TeamMember> findActiveByTeamIdAndUserId(
            @Param("teamId") UUID teamId,
            @Param("userId") UUID userId
    );

    /*
     * Check whether a user is already active in any team for the given event.
     */
    @Query(value = """
            SELECT COUNT(*) > 0 FROM team_members tm
            JOIN teams t ON t.id = tm.team_id
            WHERE t.event_id = :eventId
              AND tm.user_id = :userId
              AND tm.status IN (
                  CAST('invited' AS team_member_status),
                  CAST('accepted' AS team_member_status)
              )
            """, nativeQuery = true)
    boolean existsActiveInEvent(
            @Param("eventId") UUID eventId,
            @Param("userId") UUID userId
    );

    /*
     * Same check excluding a specific team_member row.
     */
    @Query(value = """
            SELECT COUNT(*) > 0 FROM team_members tm
            JOIN teams t ON t.id = tm.team_id
            WHERE t.event_id = :eventId
              AND tm.user_id = :userId
              AND tm.status IN (
                  CAST('invited' AS team_member_status),
                  CAST('accepted' AS team_member_status)
              )
              AND tm.id <> :excludeMemberId
            """, nativeQuery = true)
    boolean existsActiveInEventExcluding(
            @Param("eventId") UUID eventId,
            @Param("userId") UUID userId,
            @Param("excludeMemberId") UUID excludeMemberId
    );

    /*
     * Count members with a specific status in a team.
     * Status value passed as plain string — CAST handles the type.
     */
    @Query(value = """
            SELECT COUNT(*) FROM team_members
            WHERE team_id = :teamId
              AND status = CAST(:status AS team_member_status)
            """, nativeQuery = true)
    long countByTeamIdAndStatus(
            @Param("teamId") UUID teamId,
            @Param("status") String status
    );

    /*
     * Count active slots (invited + accepted) in a team.
     */
    @Query(value = """
            SELECT COUNT(*) FROM team_members
            WHERE team_id = :teamId
              AND status IN (
                  CAST('invited' AS team_member_status),
                  CAST('accepted' AS team_member_status)
              )
            """, nativeQuery = true)
    long countActiveByTeamId(@Param("teamId") UUID teamId);

    /*
     * List all members of a team, optionally filtered by status.
     * When status is null, all statuses are returned.
     */
    @Query(value = """
            SELECT * FROM team_members
            WHERE team_id = :teamId
              AND (CAST(:status AS team_member_status) IS NULL
                   OR status = CAST(:status AS team_member_status))
            ORDER BY joined_at ASC
            """, nativeQuery = true)
    List<TeamMember> findByTeamIdFiltered(
            @Param("teamId") UUID teamId,
            @Param("status") String status
    );

    /*
     * Fetch a single member with team, event and user eagerly loaded.
     * JPQL used here — no enum parameters, only UUID comparison, so no cast issue.
     */
    @Query("""
            SELECT tm FROM TeamMember tm
            JOIN FETCH tm.user
            JOIN FETCH tm.team t
            JOIN FETCH t.event
            WHERE tm.id = :memberId
            """)
    Optional<TeamMember> findByIdWithTeamAndUser(@Param("memberId") UUID memberId);

    /*
     * Bulk-delete all member rows of a team. Used by team deletion so the child
     * rows are removed before the parent team. The caller must clear the
     * persistence context afterwards, since bulk deletes bypass it.
     */
    @Modifying
    @Query("DELETE FROM TeamMember tm WHERE tm.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") UUID teamId);
}