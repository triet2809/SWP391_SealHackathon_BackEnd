package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    boolean existsBySeasonNameAndSeasonYear(String seasonName, Integer seasonYear);

    boolean existsBySeasonNameAndSeasonYearAndIdNot(String seasonName, Integer seasonYear, UUID id);
}
