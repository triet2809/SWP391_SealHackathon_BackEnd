package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {

    List<Track> findByRoundIdOrderByDisplayOrderAscNameAsc(UUID roundId);

    boolean existsByRoundIdAndName(UUID roundId, String name);

    boolean existsByRoundIdAndNameAndIdNot(UUID roundId, String name, UUID id);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM rounds WHERE id = :roundId AND event_id = :eventId)", nativeQuery = true)
    boolean existsRoundInEvent(@Param("roundId") UUID roundId, @Param("eventId") UUID eventId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM round_tracks WHERE id = :roundTrackId AND round_id = :roundId AND event_id = :eventId)", nativeQuery = true)
    boolean existsInRoundEvent(@Param("roundTrackId") UUID roundTrackId, @Param("roundId") UUID roundId, @Param("eventId") UUID eventId);
}
