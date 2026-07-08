package com.fpt.sealhackathon.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fpt.sealhackathon.entity.RoundTrack;

public interface RoundTrackRepository extends JpaRepository<RoundTrack, UUID> {

    @EntityGraph(attributePaths = {
            "event",
            "round"
    })
    @Query("""
            SELECT rt
            FROM RoundTrack rt
            WHERE
                (:eventId IS NULL OR rt.event.id = :eventId)
            AND (:roundId IS NULL OR rt.round.id = :roundId)
            AND (
                COALESCE(:keyword,'') = ''
                OR LOWER(rt.name) LIKE LOWER(CONCAT('%',:keyword,'%'))
                OR LOWER(rt.challengeTitle) LIKE LOWER(CONCAT('%',:keyword,'%'))
                OR LOWER(rt.challengeDescription) LIKE LOWER(CONCAT('%',:keyword,'%'))
            )
            ORDER BY rt.displayOrder
            """)
    List<RoundTrack> roundTrackFilter(
            @Param("eventId") UUID eventId,
            @Param("roundId") UUID roundId,
            @Param("keyword") String keyword);

    List<RoundTrack> findByRound_Id(UUID roundId);
}
