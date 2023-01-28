package de.interaapps.punyshort.auth;

import de.interaapps.punyshort.Punyshort;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.responses.user.SessionResponse;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.orm.Repo;
import org.javawebstack.passport.strategies.oauth2.OAuth2Profile;
import org.javawebstack.passport.strategies.oauth2.OAuth2Strategy;

public class OAuth2Callback implements OAuth2Strategy.HttpCallbackHandler {


    public Object handle(Exchange exchange, org.javawebstack.passport.strategies.oauth2.OAuth2Callback callback, String name) {
        User.AuthenticationProvider provider = de.interaapps.punyshort.model.database.User.AuthenticationProvider.getProviderByClass(callback.getProvider().getClass());
        OAuth2Profile profile = callback.getProfile();

        User user = Repo.get(User.class).where("authId", profile.getId()).where("authProvider", provider).first();

        if (user == null) {
            user = new de.interaapps.punyshort.model.database.User();
            int i = 1;
            final String uniqueName = profile.getName().replaceAll("[^a-zA-Z0-9]", "");
            user.uniqueName = uniqueName;
            while (Repo.get(de.interaapps.punyshort.model.database.User.class).where("uniqueName", user.uniqueName).first() != null) {
                user.uniqueName = uniqueName + i++;
            }
            user.authId = profile.getId();
            user.authProvider = provider;
        }
        // On every login the username, avatar and e-mail gets updated
        user.name = profile.getName();
        user.avatar = profile.getAvatar();
        user.eMail = profile.getMail();
        user.save();

        AccessToken accessToken = new AccessToken();
        accessToken.refreshToken = callback.getRefreshToken();
        accessToken.accessToken = callback.getAccessToken();
        accessToken.userId = user.id;
        accessToken.type = AccessToken.Type.USER;
        accessToken.save();

        if (exchange.rawRequest().getParameter("popup") != null) {
            return new SessionResponse(accessToken.getKey());
        }

        exchange.redirect(Punyshort.getInstance().getConfig().get("FRONTEND_BASE_URL") + "/auth?key=" + accessToken.getKey());
        return "";
    }
}
