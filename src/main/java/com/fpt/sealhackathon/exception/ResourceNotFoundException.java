package com.fpt.sealhackathon.exception;

/**
 * Ngoại lệ dùng khi không tìm thấy tài nguyên theo tiêu chí truy vấn.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
