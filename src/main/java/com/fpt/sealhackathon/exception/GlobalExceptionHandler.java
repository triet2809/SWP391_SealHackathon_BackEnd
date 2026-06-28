package com.fpt.sealhackathon.exception;

import com.fpt.sealhackathon.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Translates exceptions to {@link ErrorResponse} for all Team Management endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * Handle all domain rule violations thrown by the service layer.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex,
                                                        HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ErrorResponse.builder()
                        .status(code.getHttpStatus().value())
                        .error(code.name())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

    /*
     * Handle @Valid / @Validated constraint violations on request DTOs.
     * Returns field-level errors in the fieldErrors map.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (existing, duplicate) -> existing
                ));

        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.builder()
                        .status(400)
                        .error("VALIDATION_ERROR")
                        .message("Request validation failed")
                        .path(request.getRequestURI())
                        .fieldErrors(fieldErrors)
                        .build());
    }

    /*
     * Handle unreadable JSON body — e.g. invalid enum value for ChangeRoleRequest.role.
     * Translates to INVALID_ROLE when the body cannot be deserialised.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                          HttpServletRequest request) {
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.builder()
                        .status(400)
                        .error("INVALID_ROLE")
                        .message("Invalid request body: " + ex.getMostSpecificCause().getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

    /*
     * Handle DB constraint violations not caught by service-layer validation.
     * Primarily catches uq_team_members_one_active_leader and uq_teams_event_lower_name
     * on concurrent writes that bypass the pre-insert checks.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                             HttpServletRequest request) {
        String msg = ex.getMostSpecificCause().getMessage();
        ErrorCode code;
        String detail;

        if (msg != null && msg.contains("uq_team_members_one_active_leader")) {
            code = ErrorCode.FORBIDDEN_NOT_LEADER;
            detail = "Team already has an active leader";
        } else if (msg != null && msg.contains("uq_teams_event_lower_name")) {
            code = ErrorCode.TEAM_NAME_DUPLICATE;
            detail = "A team with this name already exists in this event";
        } else if (msg != null && msg.contains("violates foreign key constraint")) {
            code = ErrorCode.TEAM_NOT_FOUND;
            detail = "Operation blocked by a related record: " + msg;
        } else {
            // Unknown constraint — return 409 with the raw DB message for debugging
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.builder()
                            .status(409)
                            .error("CONSTRAINT_VIOLATION")
                            .message(msg != null ? msg : "A database constraint was violated")
                            .path(request.getRequestURI())
                            .build());
        }

        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ErrorResponse.builder()
                        .status(code.getHttpStatus().value())
                        .error(code.name())
                        .message(detail)
                        .path(request.getRequestURI())
                        .build());
    }
    /*
     * Handle unauthenticated requests — principal is "anonymousUser" or missing.
     * Thrown by SecurityUtils.getCallerId() when no valid JWT is present.
     * Returns 401 UNAUTHORIZED instead of 500.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleUnauthenticated(IllegalStateException ex,
                                                               HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .status(401)
                        .error("UNAUTHORIZED")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }
}