package com.fpt.sealhackathon.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Mirrors the PostgreSQL {@code registration_mode} enum.
 * Constant names are lowercase to match DB stored values exactly.
 */
public enum RegistrationMode {

    new_team,       // "new" is a reserved keyword in Java — mapped to DB value "new"
    resubmitted,
    imported,
    manual;

    /** Returns the actual DB value, handling the new_team → "new" special case. */
    @JsonValue
    public String toJson() {
        return this == new_team ? "new" : this.name();
    }

    /** Accepts both lowercase and uppercase from API input. */
    @JsonCreator
    public static RegistrationMode fromString(String value) {
        if (value == null) return null;
        if ("new".equalsIgnoreCase(value)) return new_team;
        return RegistrationMode.valueOf(value.toLowerCase());
    }
}