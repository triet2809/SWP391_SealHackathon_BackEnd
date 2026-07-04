package vn.edu.fpt.seal.modules.submission.mapper;

import vn.edu.fpt.seal.modules.submission.dto.SubmissionResponse;
import vn.edu.fpt.seal.modules.submission.entity.Submission;

public final class SubmissionMapper {
    private SubmissionMapper() {}
    public static SubmissionResponse toResponse(Submission s) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .roundId(s.getRound().getId())
                .teamId(s.getTeam().getId())
                .trackId(s.getTeam().getTrack().getId())
                .teamName(s.getTeam().getName())
                .repoUrl(s.getRepoUrl())
                .demoUrl(s.getDemoUrl())
                .slideUrl(s.getSlideUrl())
                .reportUrl(s.getReportUrl())
                .apiMetadata(s.getApiMetadata())
                .projectName(s.getProjectName())
                .version(s.getVersion())
                .reviewStatus(s.getReviewStatus())
                .submittedAt(s.getSubmittedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
