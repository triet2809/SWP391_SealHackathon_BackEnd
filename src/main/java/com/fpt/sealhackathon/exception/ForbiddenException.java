package com.fpt.sealhackathon.exception;

// Exception duoc nem khi user da xac thuc nhung khong du quyen thuc hien thao tac.
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
