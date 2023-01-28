package de.interaapps.punyshort.auth;

import de.interaapps.punyshort.exceptions.BlockedException;
import de.interaapps.punyshort.exceptions.PermissionsDeniedException;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.User;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.handler.Middleware;

public class RedirectionProxyMiddleware implements Middleware {
    @Override
    public Object handle(Exchange exchange) {
        AccessToken accessToken = exchange.attrib("token");

        if (accessToken != null && accessToken.type != AccessToken.Type.REDIRECT_PROXY_INSTANCE)
            throw new PermissionsDeniedException();

        return null;
    }
}
