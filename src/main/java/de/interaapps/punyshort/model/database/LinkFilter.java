package de.interaapps.punyshort.model.database;

import de.interaapps.punyshort.exceptions.PermissionsDeniedException;
import de.interaapps.punyshort.model.database.domains.Domain;
import org.apache.commons.lang3.RandomStringUtils;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.*;

import java.sql.Timestamp;

@Dates
@Table("link_filters")
public class LinkFilter extends Model {
    @Column
    public int id;

    @Column
    public String filter;

    @Column
    public String reason;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    @Column
    public Timestamp deletedAt;
}
