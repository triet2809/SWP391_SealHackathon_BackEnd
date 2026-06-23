package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.RoundCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RoundCriteriaRepository extends JpaRepository<RoundCriteria, UUID> {

    List<RoundCriteria> findByRoundIdOrderByNameAsc(UUID roundId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM rounds WHERE id = :roundId AND event_id = :eventId)", nativeQuery = true)
    boolean existsRoundInEvent(@Param("roundId") UUID roundId, @Param("eventId") UUID eventId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM round_tracks WHERE id = :roundTrackId AND round_id = :roundId AND event_id = :eventId)", nativeQuery = true)
    boolean existsRoundTrackInRoundEvent(
            @Param("roundTrackId") UUID roundTrackId,
            @Param("roundId") UUID roundId,
            @Param("eventId") UUID eventId
    );

    @Query(value = "SELECT EXISTS (SELECT 1 FROM criteria_templates WHERE id = :templateId)", nativeQuery = true)
    boolean existsTemplateById(@Param("templateId") UUID templateId);

    boolean existsByRoundIdAndRoundTrackIdAndName(UUID roundId, UUID roundTrackId, String name);

    boolean existsByRoundIdAndRoundTrackIdAndNameAndIdNot(UUID roundId, UUID roundTrackId, String name, UUID id);

    boolean existsByRoundIdAndRoundTrackIdIsNullAndName(UUID roundId, String name);

    boolean existsByRoundIdAndRoundTrackIdIsNullAndNameAndIdNot(UUID roundId, String name, UUID id);
}
