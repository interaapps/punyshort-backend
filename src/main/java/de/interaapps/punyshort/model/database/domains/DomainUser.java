package de.interaapps.punyshort.model.database.domains;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;

import java.sql.Timestamp;

@Dates
public class DomainUser extends Model {
    @Column(id = true, size = 8)
    public String id;

    @Column(size = 8)
    private String domain;

    @Column(size = 8)
    private String userId;

    @Column
    private Role role = Role.USER;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public enum Role {
        USER,
        ADMIN;
    }
}
