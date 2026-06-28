package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.RoundJudge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoundJudgeRepository extends JpaRepository<RoundJudge, UUID> {
    List<RoundJudge> findByRoundId(UUID roundId);
    boolean existsByRoundIdAndUserId(UUID roundId, UUID userId);
    List<RoundJudge> findByRoundTrackId(UUID roundTrackId);
    boolean existsByRoundTrackIdAndUserId(UUID roundTrackId, UUID userId);

}
