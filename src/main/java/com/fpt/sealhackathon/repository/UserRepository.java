package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.User;
import com.fpt.sealhackathon.entity.enums.AccountStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesById(UUID id);

    @EntityGraph(attributePaths = "roles")
    List<User> findAllByStatusOrderByCreatedAtDesc(AccountStatus status);
}
