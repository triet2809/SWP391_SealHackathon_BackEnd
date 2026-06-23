package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RoundRepository extends JpaRepository<Round, UUID> {

    List<Round> findByEventIdOrderBySequenceNumberAsc(UUID eventId);

    boolean existsByEventIdAndSequenceNumber(UUID eventId, Integer sequenceNumber);

    boolean existsByEventIdAndSequenceNumberAndIdNot(UUID eventId, Integer sequenceNumber, UUID id);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM events WHERE id = :eventId)", nativeQuery = true)
    boolean existsEventById(@Param("eventId") UUID eventId);
}
