package vn.edu.fpt.seal.modules.judge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.AccountStatus;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.judge.dto.*;
import vn.edu.fpt.seal.modules.judge.entity.RoundJudge;
import vn.edu.fpt.seal.modules.judge.mapper.RoundJudgeMapper;
import vn.edu.fpt.seal.modules.judge.repository.RoundJudgeRepository;
import vn.edu.fpt.seal.modules.round.entity.Round;
import vn.edu.fpt.seal.modules.round.repository.RoundRepository;
import vn.edu.fpt.seal.modules.user.entity.User;
import vn.edu.fpt.seal.modules.user.repository.UserRepository;

import java.util.*;

@Service @RequiredArgsConstructor
public class RoundJudgeService {
    private final RoundJudgeRepository roundJudgeRepository;
    private final RoundRepository roundRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<RoundJudgeResponse> list(UUID roundId, UUID userId, Pageable pageable) {
        if (roundId != null) return roundJudgeRepository.findByRoundId(roundId, pageable).map(RoundJudgeMapper::toResponse);
        if (userId != null) return roundJudgeRepository.findByUserId(userId, pageable).map(RoundJudgeMapper::toResponse);
        return roundJudgeRepository.findAll(pageable).map(RoundJudgeMapper::toResponse);
    }

    @Transactional
    public RoundJudgeResponse assign(AssignRoundJudgeRequest req) {
        Round round = roundRepository.findById(req.roundId()).orElseThrow(() -> ApiException.notFound("Round not found: " + req.roundId()));
        User user = userRepository.findById(req.userId()).orElseThrow(() -> ApiException.notFound("User not found: " + req.userId()));
        if (user.getStatus() != AccountStatus.approved) throw ApiException.badRequest("Only approved users can be assigned as judges");
        boolean hasJudgeRole = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> "judge".equalsIgnoreCase(r.getName()));
        if (!hasJudgeRole) throw ApiException.badRequest("Assigned user must have judge role");
        if (roundJudgeRepository.existsByRoundIdAndUserId(round.getId(), user.getId())) throw ApiException.conflict("Judge already assigned to this round");
        return RoundJudgeMapper.toResponse(roundJudgeRepository.save(RoundJudge.builder().round(round).user(user).build()));
    }

    @Transactional public void remove(UUID id) { roundJudgeRepository.delete(roundJudgeRepository.findById(id).orElseThrow(() -> ApiException.notFound("Round judge assignment not found: " + id))); }
    @Transactional public void removeByRoundAndUser(UUID roundId, UUID userId) { roundJudgeRepository.delete(roundJudgeRepository.findByRoundIdAndUserId(roundId, userId).orElseThrow(() -> ApiException.notFound("Round judge assignment not found"))); }

    @Transactional(readOnly = true)
    public List<JudgeSubmissionResponse> submissions(UUID judgeId) {
        return roundJudgeRepository.findSubmissionRowsForJudge(judgeId).stream().map(row -> JudgeSubmissionResponse.builder()
                .roundJudgeId(row.getRoundJudgeId()).judgeId(row.getJudgeId()).roundId(row.getRoundId()).roundName(row.getRoundName())
                .teamId(row.getTeamId()).teamName(row.getTeamName()).submissionId(row.getSubmissionId()).submittedAt(row.getSubmittedAt()).build()).toList();
    }
}
