package de.interaapps.punyshort.controller.user;

import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.helper.RequestHelper;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.database.workspaces.Workspace;
import de.interaapps.punyshort.model.database.workspaces.WorkspaceUser;
import de.interaapps.punyshort.model.responses.PaginatedResponse;
import de.interaapps.punyshort.model.responses.PaginationData;
import de.interaapps.punyshort.model.responses.user.UserResponse;
import de.interaapps.punyshort.model.responses.workspaces.WorkspaceResponse;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.util.stream.Collectors;

@PathPrefix("/v1/user")
public class UserController extends HttpController {

    @Get
    public UserResponse getUser(@Attrib("user") User user) {
        return new UserResponse(user);
    }
}
