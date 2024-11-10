package de.interaapps.punyshort.model.database.workspaces;

import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.database.domains.Domain;
import de.interaapps.punyshort.model.database.domains.DomainUser;
import org.apache.commons.lang3.RandomStringUtils;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Filterable;
import org.javawebstack.orm.annotation.Searchable;
import org.javawebstack.orm.annotation.Table;

import java.sql.Timestamp;
import java.util.List;

@Table("workspaces")
public class Workspace extends Model {
    @Column(id = true, size = 8)
    public String id;

    @Column(size = 20)
    @Searchable
    @Filterable
    public String slug;

    @Column
    @Searchable
    @Filterable
    public String name;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public Workspace() {
        id = RandomStringUtils.random(8, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
    }

    public List<WorkspaceUser> getUsers() {
        return Repo.get(WorkspaceUser.class).where("workspaceId", id).where("state", WorkspaceUser.State.ACCEPTED).all();
    }

    public List<WorkspaceUser> getInvitedUsers() {
        return Repo.get(WorkspaceUser.class).where("workspaceId", id).where("state", WorkspaceUser.State.INVITED).all();
    }

    public List<WorkspaceDomain> getDomains() {
        return Repo.get(WorkspaceDomain.class).where("workspaceId", id).all();
    }

    public WorkspaceDomain getDomain(String domainId) {
        return Repo.get(WorkspaceDomain.class).where("workspaceId", id).where("domainId", domainId).first();
    }

    public WorkspaceDomain getDomain(Domain domain) {
        return getDomain(domain.id);
    }

    public static Workspace getById(String id) {
        return Repo.get(Workspace.class).where("id", id).first();
    }

    public static Workspace bySlug(String slug) {
        return Repo.get(Workspace.class).where("slug", slug).first();
    }


    public void removeUser(User user) {
        Repo.get(WorkspaceUser.class).where("workspaceId", id).where("userId", user.getId()).delete();
    }

    public void removeDomain(Domain domain) {
        Repo.get(WorkspaceDomain.class).where("workspaceId", id).where("domainId", domain.id).delete();
    }

    public void addUser(User user, WorkspaceUser.Role role, WorkspaceUser.State state) {
        WorkspaceUser domainUser = new WorkspaceUser();
        domainUser.userId = user.id;
        domainUser.role = role;
        domainUser.workspaceId = id;
        domainUser.state = state;
        domainUser.save();
    }

    public void addDomain(Domain domain) {
        WorkspaceDomain workspaceDomain = new WorkspaceDomain();
        workspaceDomain.workspaceId = id;
        workspaceDomain.domainId = domain.id;
        workspaceDomain.save();
    }

    public WorkspaceUser getUser(String userId) {
        return Repo.get(WorkspaceUser.class).where("workspaceId", id).where("userId", userId).first();
    }

    public WorkspaceUser getUser(User user) {
        return getUser(user.getId());
    }
}
