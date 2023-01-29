package de.interaapps.punyshort.exceptions;

public class InternalErrorException extends RuntimeException {
    public InternalErrorException() {
        super("Resource not found");
    }
}
