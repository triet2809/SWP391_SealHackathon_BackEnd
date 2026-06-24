package com.fpt.sealhackathon.exception;

/**
 * Ngoại lệ dùng khi access token hoặc refresh token không hợp lệ.
 */
public class TokenInvalidException extends RuntimeException {

    public TokenInvalidException(String message) {
        super(message);
    }
}
