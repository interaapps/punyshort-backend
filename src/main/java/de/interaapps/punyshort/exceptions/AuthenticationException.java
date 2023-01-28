package de.interaapps.punyshort.exceptions;

public class AuthenticationException extends RuntimeException {
    private String message;

    public AuthenticationException(String message) {
        this.message = message;
    }

    public AuthenticationException() {
        this.message = "Not authenticated";
    }

    public String getMessage() {
        return message;
    }
}
