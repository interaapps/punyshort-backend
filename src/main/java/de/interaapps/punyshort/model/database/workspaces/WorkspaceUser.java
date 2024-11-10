package de.interaapps.punyshort.model.database.workspaces;

import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.database.domains.Domain;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;
import org.javawebstack.orm.query.Query;

import java.sql.Timestamp;

@Dates
@Table("workspace_users")
public class WorkspaceUser extends Model {
    @Column(id = true)
    public int id;

    @Column(size = 8)
    public String workspaceId;

    @Column(size = 8)
    public String userId;

    @Column
    public Role role = Role.MEMBER;

    @Column
    public State state = State.INVITED;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public enum Role {
        MEMBER,
        ADMIN;
    }

    public enum State {
        INVITED,
        ACCEPTED
    }

    public User getUser() {
        return User.getById(userId);
    }

    public Workspace getWorkspace() {
        return Workspace.getById(workspaceId);
    }


    public static Query<WorkspaceUser> getByWorkspace(String workspaceId) {
        return Repo.get(WorkspaceUser.class).query()
            .where("workspaceId", workspaceId)
            .whereExists(Workspace.class, q ->
                    q.where(Workspace.class, "id", "=", WorkspaceUser.class, "workspaceId")
            );
    }
}
