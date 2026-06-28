package com.fpt.sealhackathon.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fpt.sealhackathon.entity.RoundCriteria;

public interface RoundCriteriaRepository extends JpaRepository<RoundCriteria, UUID> {

    @EntityGraph(attributePaths = {
            "event",
            "round",
            "roundTrack",
            "template"
    })
    @Query("""
                SELECT rc
                FROM RoundCriteria rc
                WHERE
                    (:roundId IS NULL OR rc.round.id = :roundId)
                AND (:eventId IS NULL OR rc.event.id = :eventId)
                AND (:roundTrackId IS NULL OR rc.roundTrack.id = :roundTrackId)
                AND (
                    COALESCE(:keyword,'') = ''
                    OR LOWER(rc.name) LIKE LOWER(CONCAT('%',:keyword,'%'))
                    OR LOWER(rc.description) LIKE LOWER(CONCAT('%',:keyword,'%'))
                )
                ORDER BY rc.createdAt DESC
            """)
    List<RoundCriteria> filter(
            @Param("eventId") UUID eventId,
            @Param("roundId") UUID roundId,
            @Param("roundTrackId") UUID roundTrackId,
            @Param("keyword") String keyword);
}
