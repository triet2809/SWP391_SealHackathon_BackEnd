package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface RoundRepository
        extends JpaRepository<Round, UUID> {
}
