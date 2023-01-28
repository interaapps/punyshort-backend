package de.interaapps.punyshort.auth;

import de.interaapps.punyshort.exceptions.PermissionsDeniedException;
import de.interaapps.punyshort.model.database.User;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.handler.RequestHandler;

public class AdminMiddleware implements RequestHandler {
    public Object handle(Exchange exchange) {

        new AuthMiddleware().handle(exchange);

        User user = exchange.attrib("user");

        if (user.type != User.Type.ADMIN)
            throw new PermissionsDeniedException();

        return null;
    }
}
