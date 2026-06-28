package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.RoundTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoundTrackRepository extends JpaRepository<RoundTrack, UUID> {
}