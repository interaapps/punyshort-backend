package de.interaapps.punyshort.model.responses.workspaces;

import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.database.domains.Domain;
import de.interaapps.punyshort.model.database.domains.DomainUser;
import de.interaapps.punyshort.model.database.workspaces.Workspace;
import de.interaapps.punyshort.model.database.workspaces.WorkspaceDomain;
import de.interaapps.punyshort.model.database.workspaces.WorkspaceUser;
import de.interaapps.punyshort.model.responses.domains.DomainResponse;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

public class WorkspaceResponse {
    public String id;
    public String name;
    public String slug;
    public Timestamp createdAt;

    public List<WorkspaceUserResponse> users;
    public List<DomainResponse> domains;

    public WorkspaceResponse(Workspace workspace) {
        id = workspace.id;
        name = workspace.name;
        slug = workspace.slug;
        createdAt = workspace.createdAt;


        users = WorkspaceUser.getByWorkspace(workspace.id)
            .all()
            .stream()
            .map(WorkspaceUserResponse::new)
            .collect(Collectors.toList());

        domains = Domain.getByWorkspace(workspace.id)
            .all()
            .stream()
            .map(d -> new DomainResponse(d, false))
            .collect(Collectors.toList());
    }
}
