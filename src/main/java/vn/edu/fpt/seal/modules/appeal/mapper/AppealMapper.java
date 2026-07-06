package vn.edu.fpt.seal.modules.appeal.mapper;

import vn.edu.fpt.seal.modules.appeal.dto.AppealResponse;
import vn.edu.fpt.seal.modules.appeal.entity.Appeal;

/** Mapper tĩnh entity -> DTO cho module khiếu nại. */
public final class AppealMapper {
    private AppealMapper() {}

    public static AppealResponse toResponse(Appeal a) {
        return AppealResponse.builder()
                .id(a.getId())
                .eventId(a.getEvent().getId())
                .roundId(a.getRound().getId())
                .roundName(a.getRound().getName())
                .teamId(a.getTeam().getId())
                .teamName(a.getTeam().getName())
                .submittedById(a.getSubmittedBy().getId())
                .submittedByName(a.getSubmittedBy().getFullName())
                .reason(a.getReason())
                .status(a.getStatus())
                .response(a.getResponse())
                .resolvedById(a.getResolvedBy() == null ? null : a.getResolvedBy().getId())
                .resolvedByName(a.getResolvedBy() == null ? null : a.getResolvedBy().getFullName())
                .resultPublishedAt(a.getResultPublishedAt())
                .appealDeadline(a.getAppealDeadline())
                .resolvedAt(a.getResolvedAt())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
