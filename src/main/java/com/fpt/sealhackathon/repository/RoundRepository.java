package com.fpt.sealhackathon.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fpt.sealhackathon.entity.Round;

public interface RoundRepository extends JpaRepository<Round, UUID> {

    @EntityGraph(attributePaths = "event")
    Optional<Round> findById(UUID id);

    @EntityGraph(attributePaths = "event")
    @Query("""
            SELECT r
            FROM Round r
            WHERE
                (:eventId IS NULL OR r.event.id = :eventId)
                AND (
                    COALESCE(:keyword, '') = ''
                    OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            ORDER BY r.sequenceNumber
            """)
    List<Round> roundFilter(
            @Param("eventId") UUID eventId,
            @Param("keyword") String keyword);
}
