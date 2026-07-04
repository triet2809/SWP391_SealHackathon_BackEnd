package vn.edu.fpt.seal.modules.submission.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SubmissionResponse(
        UUID id,
        UUID roundId,
        UUID teamId,
        UUID trackId,
        String teamName,
        String repoUrl,
        String demoUrl,
        String slideUrl,
        String reportUrl,
        String apiMetadata,
        String projectName,
        String version,
        String reviewStatus,
        LocalDateTime submittedAt,
        LocalDateTime updatedAt
) {}
