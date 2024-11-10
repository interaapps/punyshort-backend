package de.interaapps.punyshort.model.requests.workspaces;

import de.interaapps.punyshort.model.database.workspaces.WorkspaceUser;
import org.javawebstack.validator.Rule;

public class CreateWorkspaceInvitationRequest {
    @Rule({"required"})
    public String email;

    public WorkspaceUser.Role role;
}
