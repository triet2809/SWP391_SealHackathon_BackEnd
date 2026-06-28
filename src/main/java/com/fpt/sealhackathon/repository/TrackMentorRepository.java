package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.TrackMentor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrackMentorRepository extends JpaRepository<TrackMentor, UUID> {
    List<TrackMentor> findByRoundTrackId(UUID roundTrackId);
    boolean existsByRoundTrackIdAndUserId(UUID roundTrackId, UUID userId);
    List<TrackMentor> findByUserId(UUID userId);
}
