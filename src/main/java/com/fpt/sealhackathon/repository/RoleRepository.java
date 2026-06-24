package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository truy cập dữ liệu vai trò và hỗ trợ tra cứu theo tên role.
 */
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);

    List<Role> findAllByOrderByNameAsc();
}
