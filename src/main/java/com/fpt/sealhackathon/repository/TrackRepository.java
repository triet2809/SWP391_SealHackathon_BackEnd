package com.fpt.sealhackathon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpt.sealhackathon.entity.Track;

public interface TrackRepository extends JpaRepository<Track, UUID>{
    
}
