package de.interaapps.punyshort.auth;

import de.interaapps.punyshort.exceptions.AuthenticationException;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.handler.RequestHandler;

public class AuthMiddleware implements RequestHandler {
    public Object handle(Exchange exchange) {
        if (exchange.attrib("user") == null)
            throw new AuthenticationException();

        return null;
    }
}
