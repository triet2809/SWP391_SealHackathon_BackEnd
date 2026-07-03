package vn.edu.fpt.seal.modules.judge.dto;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record JudgeSubmissionResponse(UUID roundJudgeId, UUID judgeId, UUID roundId, String roundName, UUID teamId, String teamName, UUID submissionId, LocalDateTime submittedAt) {}
