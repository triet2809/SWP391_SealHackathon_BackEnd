package com.fpt.sealhackathon.service;

import java.util.List;
import java.util.UUID;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.campus.CampusRequest;
import com.fpt.sealhackathon.dto.campus.CampusResponse;

@Service
public interface CampusService {

    CampusResponse create(CampusRequest request);

    CampusResponse update(UUID id, CampusRequest request);

    void delete(UUID id) throws BadRequestException;

    List<CampusResponse> campusFilter(UUID universityId, String keyword);

}
