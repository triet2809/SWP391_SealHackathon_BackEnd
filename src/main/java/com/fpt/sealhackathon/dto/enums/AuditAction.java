package com.fpt.sealhackathon.dto.enums;

/**
 * Mirrors the PostgreSQL {@code audit_action} enum.
 * Only the actions used by this module are declared; constant names match the DB values.
 */
public enum AuditAction {
    LOCK_TEAM,
    DISQUALIFY_TEAM
}
