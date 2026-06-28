package com.fpt.sealhackathon.exception;

import lombok.Getter;

/**
 * Runtime exception for all domain rule violations in the Team Management API.
 * Caught by the global exception handler and translated to {@link com.fpt.sealhackathon.dto.response.ErrorResponse}.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }
}