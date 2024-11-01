package de.interaapps.punyshort.controller.user;

import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.exceptions.PermissionsDeniedException;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.requests.auth.CreateAccessTokenRequest;
import de.interaapps.punyshort.model.responses.ActionResponse;
import de.interaapps.punyshort.model.responses.user.keys.CreateAccessTokenResponse;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Delete;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;

import java.util.List;
import java.util.stream.Collectors;

@PathPrefix("/v1/access-tokens")
public class AccessTokenController extends HttpController {
    @Post
    @With("auth")
    public CreateAccessTokenResponse addKey(@Body CreateAccessTokenRequest request, @Attrib("user") User user, @Attrib("token") AccessToken requestAccessToken) {
        if (requestAccessToken != null)
            requestAccessToken.checkPermission("access_tokens:create");

        CreateAccessTokenResponse response = new CreateAccessTokenResponse();
        AccessToken accessToken = new AccessToken();
        AccessToken userAccessToken = Repo.get(AccessToken.class).where("userId", user.getId()).order("createdAt", true).first();

        if (userAccessToken.type == AccessToken.Type.USER && user.type != User.Type.ADMIN) {
            throw new PermissionsDeniedException();
        }

        if (userAccessToken != null) {
            if (request.type != AccessToken.Type.USER) {
                accessToken.type = request.type;
            }
            accessToken.userId = userAccessToken.userId;
            accessToken.save();

            response.success = true;
            response.key = accessToken.getKey();
        }
        return response;
    }

    @Get
    @With("auth")
    public List<String> getKeys(@Attrib("user") User user, @Attrib("token") AccessToken requestAccessToken) {
        if (requestAccessToken != null)
            requestAccessToken.checkPermission("access_tokens:read");

        return Repo.get(AccessToken.class).where("type", AccessToken.Type.API).where("userId", user.getId()).all().stream().map(AccessToken::getKey).collect(Collectors.toList());
    }

    @Delete("/{key}")
    @With("auth")
    public ActionResponse delete(@Attrib("user") User user, @Path("key") String key, @Attrib("token") AccessToken requestAccessToken) {
        if (requestAccessToken != null)
            requestAccessToken.checkPermission("access_tokens:delete");

        Repo.get(AccessToken.class).where("key", key).where("userId", user.getId()).all().forEach(AccessToken::delete);
        return new ActionResponse(true);
    }

}
