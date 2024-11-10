package de.interaapps.punyshort.model.database.workspaces;

import de.interaapps.punyshort.model.database.domains.Domain;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;

import java.sql.Timestamp;

@Dates
@Table("workspace_domains")
public class WorkspaceDomain extends Model {
    @Column(id = true)
    public int id;

    @Column(size = 8)
    public String workspaceId;

    @Column(size = 8)
    public String domainId;

    @Column
    public boolean active = true;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;


    public Domain getDomain() {
        return Domain.get(domainId);
    }

    public Workspace getWorkspace() {
        return Workspace.getById(workspaceId);
    }
}
