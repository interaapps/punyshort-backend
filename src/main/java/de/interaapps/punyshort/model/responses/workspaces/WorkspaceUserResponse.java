package de.interaapps.punyshort.model.responses.workspaces;

import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.database.domains.Domain;
import de.interaapps.punyshort.model.database.workspaces.Workspace;
import de.interaapps.punyshort.model.database.workspaces.WorkspaceUser;
import de.interaapps.punyshort.model.responses.domains.DomainResponse;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

public class WorkspaceUserResponse {
    public String id;
    public String name;
    public String uniqueName;
    public String email;
    public String avatar;
    public WorkspaceUser.Role role;
    public WorkspaceUser.State state;


    public WorkspaceUserResponse(WorkspaceUser workspaceUser) {
        User user = workspaceUser.getUser();
        id = user.getId();
        name = user.getName();
        uniqueName = user.getUniqueName();
        email = user.eMail;
        avatar = user.avatar;
        role = workspaceUser.role;
        state = workspaceUser.state;
    }
}
