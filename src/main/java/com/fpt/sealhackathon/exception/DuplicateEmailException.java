package com.fpt.sealhackathon.exception;

/**
 * Ngoại lệ dùng khi email đăng ký đã tồn tại trong hệ thống.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
