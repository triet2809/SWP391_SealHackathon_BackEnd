package com.fpt.sealhackathon.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fpt.sealhackathon.entity.Campus;

public interface CampusRepository extends JpaRepository<Campus, UUID> {

    @Query("""
                SELECT c
                FROM Campus c
                WHERE
                    (:universityId IS NULL OR c.university.id = :universityId)
                    AND (
                        COALESCE(:keyword, '') = ''
                        OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(c.city) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
                ORDER BY c.name
            """)
    List<Campus> campusFilter(
            @Param("universityId") UUID universityId,
            @Param("keyword") String keyword);

}
