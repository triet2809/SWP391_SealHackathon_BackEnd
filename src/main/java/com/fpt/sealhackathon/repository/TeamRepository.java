package com.fpt.sealhackathon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpt.sealhackathon.entity.Team;

import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID>{
    
}
