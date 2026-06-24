package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository truy cập dữ liệu nhật ký audit phục vụ truy vết thao tác quản trị.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
