package de.interaapps.punyshort.model.responses;

import de.interaapps.punyshort.exceptions.AuthenticationException;

public class ExceptionResponse extends ActionResponse {

    public String exception;
    public boolean error = true;
    public boolean exists = false;

    public ExceptionResponse(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            if (throwable.getCause() != null)
                throwable = throwable.getCause();
        }

        if (throwable instanceof AuthenticationException)
            System.err.println("AUTH EXCEPTION");
        else
            throwable.printStackTrace();
        success = false;

        exception = throwable.getClass().getSimpleName();
    }
}
