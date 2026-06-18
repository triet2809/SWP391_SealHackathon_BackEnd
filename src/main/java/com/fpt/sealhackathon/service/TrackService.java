package com.fpt.sealhackathon.service;

import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.event.request.TrackRegisterRequest;
import com.fpt.sealhackathon.entity.Track;

@Service
public interface TrackService {
    Track registeTrack(TrackRegisterRequest request);
}
