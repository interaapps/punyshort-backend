package de.interaapps.punyshort.links;

import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.requests.links.ShortenLinkRequest;
import de.interaapps.punyshort.model.responses.ActionResponse;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.verbs.Post;

@PathPrefix("/v1/shorten-links")
public class ShortenLinkController {
   @Post
   @With({"blocked-check"})
   public ActionResponse create(@Body ShortenLinkRequest request, @Attrib("user") User user, @Attrib("token") AccessToken token) {


        return new ActionResponse(true);
   }
}
