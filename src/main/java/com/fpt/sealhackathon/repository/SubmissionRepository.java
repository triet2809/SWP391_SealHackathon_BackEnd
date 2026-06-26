package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    List<Submission> findByTeamId(UUID teamId);
    List<Submission> findByRoundId(UUID roundId);
    boolean existsByRoundParticipantId(UUID roundParticipantId);
    Submission findByRoundParticipantId(UUID roundParticipantId);
}
