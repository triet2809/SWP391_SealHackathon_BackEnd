package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.MentorFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface MentorFeedbackRepository extends JpaRepository<MentorFeedback, UUID> {
}
