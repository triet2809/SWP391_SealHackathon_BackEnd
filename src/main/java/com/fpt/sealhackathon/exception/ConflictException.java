package com.fpt.sealhackathon.exception;

/**
 * Ngoại lệ dùng cho các xung đột dữ liệu hoặc trạng thái nghiệp vụ.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
