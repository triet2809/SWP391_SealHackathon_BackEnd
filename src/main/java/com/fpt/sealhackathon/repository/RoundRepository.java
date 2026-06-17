package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RoundRepository extends JpaRepository<Round, UUID> {

    List<Round> findByTrackIdOrderBySequenceNumberAsc(UUID trackId);

    boolean existsByTrackIdAndNameIgnoreCase(UUID trackId, String name);

    boolean existsByTrackIdAndSequenceNumber(UUID trackId, Integer sequenceNumber);

    boolean existsByTrackIdAndNameIgnoreCaseAndIdNot(UUID trackId, String name, UUID id);

    boolean existsByTrackIdAndSequenceNumberAndIdNot(UUID trackId, Integer sequenceNumber, UUID id);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tracks WHERE id = :trackId)", nativeQuery = true)
    boolean existsTrackById(@Param("trackId") UUID trackId);
}