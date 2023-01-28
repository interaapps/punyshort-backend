package de.interaapps.punyshort.model.requests.admin;

import de.interaapps.punyshort.model.database.User;
import org.javawebstack.validator.Rule;

public class EditUserRequest {
    @Rule("string(2,255)")
    public String name;

    @Rule("string(2,33)")
    public String uniqueName;

    public User.Type type;
}
