package de.interaapps.punyshort.model.database.domains;

import de.interaapps.punyshort.exceptions.PermissionsDeniedException;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.javawebstack.abstractdata.AbstractArray;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Filterable;
import org.javawebstack.orm.annotation.Searchable;

import java.sql.Timestamp;

@Dates
public class Domain extends Model {
    @Column(id = true, size = 8)
    public String id;

    @Column
    @Searchable
    @Filterable
    public String name;

    @Column
    @Searchable
    @Filterable
    public DNSType dnsType;

    @Column
    public AbstractObject dnsSettings;

    @Column
    @Filterable
    public boolean isPublic = false;

    @Column
    @Filterable
    public boolean isActive = false;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public Domain() {
        id = RandomStringUtils.random(8, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
    }

    public void checkUserAccess(User user) {
        if (Repo.get(DomainUser.class).where("domain", id).where("userId", user.id).first() == null) {
            throw new PermissionsDeniedException();
        }
    }

    public static Domain get(String id) {
        return Repo.get(Domain.class).where("id", id).where("isActive", true).first();
    }

    public static Domain byName(String domainName) {
        return Repo.get(Domain.class).where("name", domainName).where("isActive", true).first();
    }

    public enum DNSType {
        INTERNAL,
        CNAME,
        CLOUDFLARE,
        CUSTOM_PROXY
    }
}
