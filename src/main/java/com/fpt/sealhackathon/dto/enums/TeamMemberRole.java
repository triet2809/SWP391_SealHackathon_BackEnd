package com.fpt.sealhackathon.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Mirrors the PostgreSQL {@code team_member_role} enum.
 * Constant names are lowercase to match DB stored values exactly.
 */
public enum TeamMemberRole {

    leader,
    member;

    @JsonValue
    public String toJson() {
        return this.name();
    }

    @JsonCreator
    public static TeamMemberRole fromString(String value) {
        if (value == null) return null;
        return TeamMemberRole.valueOf(value.toLowerCase());
    }
}