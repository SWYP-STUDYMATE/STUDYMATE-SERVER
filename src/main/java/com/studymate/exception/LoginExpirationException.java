package com.studymate.exception;

public class LoginExpirationException extends RuntimeException {
    public LoginExpirationException() {
        super("TOKEN EXPIRED");
    }
}
