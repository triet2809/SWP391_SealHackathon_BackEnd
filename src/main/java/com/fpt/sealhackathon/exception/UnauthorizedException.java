package com.fpt.sealhackathon.exception;

/**
 * Ngoại lệ dùng cho các trường hợp người dùng chưa xác thực hoặc không đủ quyền.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
