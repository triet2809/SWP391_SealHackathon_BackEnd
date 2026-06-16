package com.fpt.sealhackathon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpt.sealhackathon.entity.TeamMember;

import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID>{
    
}
