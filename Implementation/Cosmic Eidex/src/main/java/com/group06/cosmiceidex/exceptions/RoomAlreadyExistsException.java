package com.group06.cosmiceidex.exceptions;

public class RoomAlreadyExistsException extends RuntimeException {
    public RoomAlreadyExistsException(String message) {
        super(message);
    }
}
