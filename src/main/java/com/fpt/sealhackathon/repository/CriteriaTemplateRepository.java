package com.fpt.sealhackathon.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fpt.sealhackathon.entity.CriteriaTemplate;

public interface CriteriaTemplateRepository extends JpaRepository<CriteriaTemplate, UUID> {

    @Query("""
                SELECT c
                FROM CriteriaTemplate c
                WHERE
                    COALESCE(:keyword, '') = ''
                    OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                ORDER BY c.createdAt DESC
            """)
    List<CriteriaTemplate> filter(@Param("keyword") String keyword);
}
