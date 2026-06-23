package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.CriteriaTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CriteriaTemplateRepository extends JpaRepository<CriteriaTemplate, UUID> {
}
