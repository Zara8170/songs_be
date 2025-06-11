package com.example.song_be.exception;

public class NotAccessChatRoom extends RuntimeException {
    public NotAccessChatRoom(String message) {
        super(message);
    }
}
