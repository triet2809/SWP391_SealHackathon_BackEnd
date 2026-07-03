package vn.edu.fpt.seal.modules.participant.dto; import lombok.Builder; import vn.edu.fpt.seal.common.enums.RoundParticipantStatus; import java.time.LocalDateTime; import java.util.UUID;
@Builder public record RoundParticipantResponse(UUID id,UUID roundId,String roundName,UUID teamId,String teamName,RoundParticipantStatus status,String note,LocalDateTime createdAt,LocalDateTime updatedAt) {}
