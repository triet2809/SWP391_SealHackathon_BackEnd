package vn.edu.fpt.seal.modules.round.mapper;

import vn.edu.fpt.seal.modules.round.dto.RoundResponse;
import vn.edu.fpt.seal.modules.round.entity.Round;

public final class RoundMapper {

    private RoundMapper() {
    }

    public static RoundResponse toResponse(Round r) {
        return RoundResponse.builder()
                .id(r.getId())
                .trackId(r.getTrack().getId())
                .eventId(r.getTrack().getEvent().getId())
                .name(r.getName())
                .sequenceNumber(r.getSequenceNumber())
                .submissionDeadline(r.getSubmissionDeadline())
                .topNToPromote(r.getTopNToPromote())
                .resultPublishedAt(r.getResultPublishedAt())
                .appealDeadline(r.getAppealDeadline())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
