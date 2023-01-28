package de.interaapps.punyshort.controller.user;

import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.responses.user.UserResponse;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.verbs.Get;

@PathPrefix("/v1/user")
public class UserController extends HttpController {

    @Get
    public UserResponse getUser(@Attrib("user") User user) {
        return new UserResponse(user);
    }


}
