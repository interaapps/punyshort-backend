package de.interaapps.punyshort.model.responses.user;

import de.interaapps.punyshort.Punyshort;
import de.interaapps.punyshort.model.database.User;

import java.util.ArrayList;
import java.util.List;

public class UserResponse {

    public boolean loggedIn = false;
    public String id;
    public String name;
    public String color;
    public String profilePicture;
    public String authType;
    public List<String> authTypes = new ArrayList<>(Punyshort.getInstance().getOAuth2Strategy().getProviders().keySet());
    public User.Type type;

    public UserResponse(User user) {
        if (user == null)
            return;

        name = user.getName();
        authType = user.getAuthProvider().getName();
        // color = user.getFavouriteColor();
        color = "#f52966";
        profilePicture = user.getAvatar();
        id = user.getId();
        loggedIn = true;

        type = user.type == null ? User.Type.USER : user.type;
    }
}
