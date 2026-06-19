package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {

    boolean existsByEventIdAndNameIgnoreCase(UUID eventId, String name);

    boolean existsByEventIdAndNameIgnoreCaseAndIdNot(UUID eventId, String name, UUID id);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM events WHERE id = :eventId)", nativeQuery = true)
    boolean existsEventById(@Param("eventId") UUID eventId);
}
