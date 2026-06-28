package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    /* True when the user holds the given role (via user_roles -> roles.name). */
    @Query(value = """
            SELECT COUNT(*) > 0 FROM user_roles ur
            JOIN roles r ON r.id = ur.role_id
            WHERE ur.user_id = :userId
              AND r.name = :roleName
            """, nativeQuery = true)
    boolean hasRole(@Param("userId") UUID userId, @Param("roleName") String roleName);
}