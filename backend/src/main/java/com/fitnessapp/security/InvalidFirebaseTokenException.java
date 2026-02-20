package com.fitnessapp.security;

public class InvalidFirebaseTokenException extends RuntimeException {

    public InvalidFirebaseTokenException(String message) {
        super(message);
    }
}
