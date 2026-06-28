package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.RoundParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoundParticipantRepository extends JpaRepository<RoundParticipant, UUID> {
    List<RoundParticipant> findByRoundTrackId(UUID roundTrackId);
    Optional<RoundParticipant> findByTeamId(UUID teamId);
}