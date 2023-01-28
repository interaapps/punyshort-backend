package de.interaapps.punyshort.controller.stats;

import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.responses.ActionResponse;
import de.interaapps.punyshort.model.responses.app.StatsResponse;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.httpserver.router.annotation.verbs.Post;

@PathPrefix("/v1/link-stats")
public class StatsController extends HttpController {
    @Post("/register")
    @With("redirection-proxy")
    public ActionResponse register(@Attrib("token") AccessToken accessToken, @Attrib("user") User user) {


        return new ActionResponse(true);
    }
}
