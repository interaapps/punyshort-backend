package de.interaapps.punyshort.controller.auth;

import de.interaapps.punyshort.Punyshort;
import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.exceptions.AuthenticationException;
import de.interaapps.punyshort.exceptions.NotFoundException;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.requests.auth.InteraAppsExternalAccessRequest;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;
import org.javawebstack.passport.strategies.oauth2.OAuth2Provider;
import org.javawebstack.webutils.config.Config;

import java.util.Map;

@PathPrefix("/v1/auth")
public class InteraAppsExternalAccessController extends HttpController {
    @Post("/iaea")
    public String iaea(@Body InteraAppsExternalAccessRequest request, Exchange exchange) {
        Map<String, OAuth2Provider> providers = Punyshort.getInstance().getOAuth2Strategy().getProviders();

        if (providers.containsKey("interaapps")) {
            Config config = Punyshort.getInstance().getConfig();

            if (config.get("oauth2.interaapps.id").equals(request.appId) && config.get("oauth2.interaapps.secret").equals(request.appSecret)) {
                User user = Repo.get(User.class).where("authId", request.userId).where("authProvider", User.AuthenticationProvider.INTERAAPPS).first();

                if (user != null) {
                    AccessToken accessToken = new AccessToken();
                    accessToken.userId = user.id;
                    accessToken.type = AccessToken.Type.ACCESS_TOKEN;
                    request.appScopeList.forEach(accessToken::addScope);

                    accessToken.save();
                    return accessToken.getKey();
                }
            } else {
                throw new AuthenticationException();
            }
        }

        throw new NotFoundException();
    }
}
