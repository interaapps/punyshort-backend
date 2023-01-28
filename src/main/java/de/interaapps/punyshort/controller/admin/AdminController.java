package de.interaapps.punyshort.controller.admin;

import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.helper.RequestHelper;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.requests.admin.EditUserRequest;
import de.interaapps.punyshort.model.responses.ActionResponse;
import de.interaapps.punyshort.model.responses.PaginatedResponse;
import de.interaapps.punyshort.model.responses.PaginationData;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Delete;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.httpserver.router.annotation.verbs.Put;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.util.List;

@PathPrefix("/v1/admin")
@With("admin")
public class AdminController extends HttpController {
    @Get("/users")
    public PaginatedResponse<User> getUsers(Exchange exchange, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        accessToken.checkPermission("admin.users:read");
        Query<User> query = Repo.get(User.class).query();

        PaginationData pagination = RequestHelper.pagination(query, exchange);
        query.search(exchange.query("search"));
        RequestHelper.queryFilter(query, exchange.getQueryParameters());

        return new PaginatedResponse<>(query.order("created_at", true).all(), pagination);
    }

    @Get("/users/{id}")
    public User getUser(@Path("id") String id, @Attrib("token") AccessToken accessToken) {
        accessToken.checkPermission("admin.users:read");
        return Repo.get(User.class).get(id);
    }

    @Delete("/users/{id}")
    public ActionResponse removeUser(@Path("id") String id, @Attrib("token") AccessToken accessToken) {
        accessToken.checkPermission("admin.users:delete");

        Repo.get(AccessToken.class).where("userId", id).delete();

        Repo.get(User.class).get(id).delete();

        return new ActionResponse(true);
    }

    @Put("/users/{id}")
    public ActionResponse editUser(@Body EditUserRequest request, @Attrib("token") AccessToken accessToken, @Path("id") String id) {
        accessToken.checkPermission("admin.users:edit");

        User user = Repo.get(User.class).get(id);

        if (request.uniqueName != null)
            user.uniqueName = request.uniqueName;

        if (request.name != null)
            user.name = request.name;

        if (request.type != null)
            user.type = request.type;

        user.save();

        return new ActionResponse(true);
    }
}
