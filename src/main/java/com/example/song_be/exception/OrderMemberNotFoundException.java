package com.example.song_be.exception;

public class OrderMemberNotFoundException extends RuntimeException {
    public OrderMemberNotFoundException(String message) {
        super(message);
    }
}
