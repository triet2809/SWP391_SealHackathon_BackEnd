package com.fpt.sealhackathon.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fpt.sealhackathon.entity.University;

public interface UniversityRepository extends JpaRepository<University, UUID> {

    @EntityGraph(attributePaths = "campuses")
    @Query("""
            SELECT DISTINCT u
            FROM University u
            LEFT JOIN u.campuses c
            WHERE
            (
                COALESCE(:keyword, '') = ''
                OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.shortName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.country) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND
            (
                :campusIds IS NULL OR c.id IN :campusIds
            )
            """)
    List<University> universityFilter(
            @Param("keyword") String keyword,
            @Param("campusIds") List<UUID> campusIds);
}
