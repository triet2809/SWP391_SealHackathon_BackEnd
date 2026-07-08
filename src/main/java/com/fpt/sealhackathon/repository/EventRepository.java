package com.fpt.sealhackathon.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fpt.sealhackathon.entity.Event;

public interface EventRepository extends JpaRepository<Event, UUID> {

        @Query(value = """
                        SELECT *
                        FROM events e
                        WHERE
                        (
                            COALESCE(:keyword, '') = ''
                            OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(e.season_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        )
                        AND (:seasonYear IS NULL OR e.season_year = :seasonYear)
                        AND (:status IS NULL OR e.status = CAST(:status AS event_status))
                        ORDER BY e.created_at DESC
                        """, nativeQuery = true)
        List<Event> eventFilter(
                        @Param("keyword") String keyword,
                        @Param("seasonYear") Integer seasonYear,
                        @Param("status") String status);

        boolean existsBySeasonNameAndSeasonYear(
                        String seasonName,
                        Integer seasonYear);

        boolean existsBySeasonNameAndSeasonYearAndIdNot(
                        String seasonName,
                        Integer seasonYear,
                        UUID id);
}
