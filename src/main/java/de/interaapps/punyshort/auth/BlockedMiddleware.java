package de.interaapps.punyshort.auth;

import de.interaapps.punyshort.exceptions.BlockedException;
import de.interaapps.punyshort.model.database.User;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.handler.Middleware;

public class BlockedMiddleware implements Middleware {
    @Override
    public Object handle(Exchange exchange) {
        User user = exchange.attrib("user");

        if (user != null && user.type == User.Type.BLOCKED)
            throw new BlockedException();

        return null;
    }
}
