package com.fpt.sealhackathon.service;

import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.event.request.TeamRequest;
import com.fpt.sealhackathon.dto.event.response.TeamResponse;

@Service
public interface TeamService {
    TeamResponse createTeam(TeamRequest request);
    
}
