package com.fpt.sealhackathon.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fpt.sealhackathon.entity.Event;
import com.fpt.sealhackathon.entity.enums.EventStatus;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("""
                SELECT e
                FROM Event e
                WHERE
                    (
                        COALESCE(:keyword, '') = ''
                        OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(e.seasonName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
                    AND (:seasonYear IS NULL OR e.seasonYear = :seasonYear)
                ORDER BY e.createdAt DESC
            """)
    List<Event> eventFilter(
            @Param("keyword") String keyword,
            @Param("seasonYear") Integer seasonYear);

    boolean existsBySeasonNameAndSeasonYear(
            String seasonName,
            Integer seasonYear);

    boolean existsBySeasonNameAndSeasonYearAndIdNot(
            String seasonName,
            Integer seasonYear,
            UUID id);
}
