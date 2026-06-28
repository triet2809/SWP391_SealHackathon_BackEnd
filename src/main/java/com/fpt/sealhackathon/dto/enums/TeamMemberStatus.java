package com.fpt.sealhackathon.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Mirrors the PostgreSQL {@code team_member_status} enum.
 * Constant names are lowercase to match DB stored values exactly.
 */
public enum TeamMemberStatus {

    invited,
    accepted,
    declined,
    removed;

    @JsonValue
    public String toJson() {
        return this.name();
    }

    @JsonCreator
    public static TeamMemberStatus fromString(String value) {
        if (value == null) return null;
        return TeamMemberStatus.valueOf(value.toLowerCase());
    }
}