package com.example.song_be.exception;

public class NotSellerAccessException extends RuntimeException {
    public NotSellerAccessException(String message) {
        super(message);
    }
}
