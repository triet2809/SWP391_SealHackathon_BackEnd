package com.fpt.sealhackathon.exception;

import org.springframework.http.HttpStatus;

/**
 * All application-level error codes for the Team Management API.
 * Each code carries its HTTP status and a default message.
 */
public enum ErrorCode {

    // --- 404 Not Found ---
    TEAM_NOT_FOUND              (HttpStatus.NOT_FOUND,             "Team not found"),
    EVENT_NOT_FOUND             (HttpStatus.NOT_FOUND,             "Event not found"),
    TEAM_MEMBER_NOT_FOUND       (HttpStatus.NOT_FOUND,             "Team member not found"),
    USER_NOT_FOUND              (HttpStatus.NOT_FOUND,             "User not found"),

    // --- 403 Forbidden ---
    ACCOUNT_NOT_APPROVED        (HttpStatus.FORBIDDEN,             "Account is not approved"),
    FORBIDDEN_NOT_LEADER        (HttpStatus.FORBIDDEN,             "Only the team leader can perform this action"),
    FORBIDDEN_NOT_MEMBER_OWNER  (HttpStatus.FORBIDDEN,             "You can only manage your own invitation"),
    FORBIDDEN_NOT_COORDINATOR   (HttpStatus.FORBIDDEN,             "Only a coordinator can perform this action"),

    // --- 409 Conflict ---
    REGISTRATION_NOT_OPEN       (HttpStatus.CONFLICT,              "Event registration is not open"),
    TEAM_NAME_DUPLICATE         (HttpStatus.CONFLICT,              "A team with this name already exists in this event"),
    USER_ALREADY_IN_TEAM        (HttpStatus.CONFLICT,              "User is already invited or accepted in a team for this event"),
    TEAM_FULL                   (HttpStatus.CONFLICT,              "Team has reached its maximum member capacity"),
    TEAM_LOCKED                 (HttpStatus.CONFLICT,              "Team is locked and does not allow member modifications"),
    TEAM_ALREADY_LOCKED         (HttpStatus.CONFLICT,              "Team is already locked"),
    TEAM_TERMINATED             (HttpStatus.CONFLICT,              "Team is eliminated or disqualified"),
    MEMBER_STATUS_INVALID       (HttpStatus.CONFLICT,              "This operation is not valid for the member's current status"),
    CANNOT_SELF_REMOVE          (HttpStatus.CONFLICT,              "Team leader cannot remove themselves"),
    CANNOT_CHANGE_OWN_ROLE      (HttpStatus.CONFLICT,              "Team leader cannot change their own role"),
    TEAM_NOT_DELETABLE          (HttpStatus.CONFLICT,              "Only teams in waiting_for_members status can be deleted"),

    // --- 400 Bad Request ---
    CANNOT_SELF_INVITE          (HttpStatus.BAD_REQUEST,           "You cannot invite yourself to the team"),
    INVALID_ROLE                (HttpStatus.BAD_REQUEST,           "Invalid role value"),

    // --- 422 Unprocessable ---
    TARGET_USER_NOT_APPROVED    (HttpStatus.UNPROCESSABLE_ENTITY,  "The invited user's account is not yet approved");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}