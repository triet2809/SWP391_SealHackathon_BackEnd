package com.fpt.sealhackathon.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Mirrors the PostgreSQL {@code team_status} enum.
 * Constant names are lowercase to match DB stored values exactly.
 * Hibernate 7 uses the declared constant name for read/write — toString() is not consulted.
 */
public enum TeamStatus {

    waiting_for_members,
    approved_open,
    approved_full,
    eliminated,
    disqualified;

    /** Serialises to lowercase for JSON responses. */
    @JsonValue
    public String toJson() {
        return this.name();
    }

    /** Accepts both lowercase ({@code waiting_for_members}) and uppercase ({@code WAITING_FOR_MEMBERS}) from API input. */
    @JsonCreator
    public static TeamStatus fromString(String value) {
        if (value == null) return null;
        return TeamStatus.valueOf(value.toLowerCase());
    }
}