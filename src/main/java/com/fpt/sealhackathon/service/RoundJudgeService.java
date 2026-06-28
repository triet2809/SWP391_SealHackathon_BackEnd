package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.JudgeAssignRequest;
import com.fpt.sealhackathon.entity.Round;
import com.fpt.sealhackathon.entity.RoundJudge;
import com.fpt.sealhackathon.repository.RoundJudgeRepository;
import com.fpt.sealhackathon.repository.RoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Service
public class RoundJudgeService {
    @Autowired
    private RoundJudgeRepository roundJudgeRepository;
    @Autowired
    private RoundRepository roundRepository;
    public List<RoundJudge> getJudgesByRound(UUID roundId) {
        return roundJudgeRepository.findByRoundId(roundId);
    }
    public RoundJudge assignJudge(UUID roundId, JudgeAssignRequest request) {

        if (roundJudgeRepository.existsByRoundTrackIdAndUserId(
                request.getRoundTrackId(),
                request.getUserId())) {
            return null;
        }

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("Round not found"));

        RoundJudge roundJudge = new RoundJudge();

        roundJudge.setId(UUID.randomUUID());
        roundJudge.setEventId(round.getEventId());
        roundJudge.setRoundId(roundId);
        roundJudge.setRoundTrackId(request.getRoundTrackId());
        roundJudge.setUserId(request.getUserId());
        roundJudge.setAssignedAt(LocalDateTime.now());

        return roundJudgeRepository.save(roundJudge);
    }
    public List<RoundJudge> getJudgesByTrack(UUID roundTrackId) {
        return roundJudgeRepository.findByRoundTrackId(roundTrackId);
    }
    public boolean removeJudge(UUID roundJudgeId) {

        RoundJudge roundJudge = roundJudgeRepository.findById(roundJudgeId).orElse(null);

        if (roundJudge == null) {
            return false;
        }

        roundJudgeRepository.delete(roundJudge);

        return true;
    }
}
