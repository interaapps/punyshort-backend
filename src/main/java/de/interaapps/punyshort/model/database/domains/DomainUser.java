package de.interaapps.punyshort.model.database.domains;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;

import java.sql.Timestamp;

@Dates
public class DomainUser extends Model {
    @Column(id = true)
    public int id;

    @Column(size = 8)
    public String domain;

    @Column(size = 8)
    public String userId;

    @Column
    public Role role = Role.MEMBER;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public enum Role {
        MEMBER,
        ADMIN;
    }
}
