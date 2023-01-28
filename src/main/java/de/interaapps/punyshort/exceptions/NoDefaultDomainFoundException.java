package de.interaapps.punyshort.exceptions;

public class NoDefaultDomainFoundException extends RuntimeException {
    public NoDefaultDomainFoundException() {
        super("Resource not found");
    }
}
