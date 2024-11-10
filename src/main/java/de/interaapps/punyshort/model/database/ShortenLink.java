package de.interaapps.punyshort.model.database;

import de.interaapps.punyshort.exceptions.DomainNotFoundException;
import de.interaapps.punyshort.exceptions.NotFoundException;
import de.interaapps.punyshort.exceptions.PermissionsDeniedException;
import de.interaapps.punyshort.model.database.domains.Domain;
import de.interaapps.punyshort.model.database.stats.*;
import de.interaapps.punyshort.model.database.workspaces.Workspace;
import org.apache.commons.lang3.RandomStringUtils;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Dates
@SoftDelete
@Table("shorten_links")
public class ShortenLink extends Model {

    @Column(size = 8, id = true)
    public String id;

    @Column(size = 8)
    @Filterable
    public String userId;

    @Column
    @Searchable
    public String path;

    @Column
    @Searchable
    @Filterable
    public String domain;

    @Column
    @Searchable
    @Filterable
    public String fullShortenUrl;

    @Column
    @Searchable
    public String longLink;

    @Column
    @Searchable
    @Filterable
    public Type type = Type.SHORTEN_LINK;


    @Column(size = 8)
    @Filterable
    public String workspaceId = null;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    @Column
    public Timestamp deletedAt;

    public ShortenLink() {
        id = RandomStringUtils.random(8, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
        while (Repo.get(ShortenLink.class).where("id", id).first() != null)
            id = RandomStringUtils.random(8, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
    }

    public static ShortenLink get(String id) {
        return Repo.get(ShortenLink.class).where("id", id).first();
    }

    public static ShortenLink get(Domain domain, String path) {
        if (domain == null)
            throw new DomainNotFoundException();
        return Repo.get(ShortenLink.class).where("domain", domain.id).where("path", path).first();
    }

    public static ShortenLink get(String domain, String path) {
        return get(Domain.byName(domain), path);
    }

    public void checkUserAccess(User user) {
        if (workspaceId != null) {
            Workspace workspace = Workspace.getById(workspaceId);

            if (workspace != null && workspace.getUser(user) != null)
                return;
        }

        if (userId.equals(user.id))
            return;

        throw new PermissionsDeniedException();
    }

    public void saveAndUpdateLinkCache(Domain domain) {
        fullShortenUrl = "https://" + domain.name + "/" + path;
        save();
    }

    public void saveAndUpdateLinkCache() {
        saveAndUpdateLinkCache(Domain.get(domain));
    }

    public List<String> getTags() {
        return Repo.get(ShortenLinkTag.class).where("linkId", id).get().stream().map(t -> t.tag).collect(Collectors.toList());
    }

    @Override
    public void delete() {
        Repo.get(ShortenLinkBrowserStats.class).query().where("linkId", id).delete();
        Repo.get(ShortenLinkClickPerDateStats.class).query().where("linkId", id).delete();
        Repo.get(ShortenLinkClickStats.class).query().where("linkId", id).delete();
        Repo.get(ShortenLinkCountriesStats.class).query().where("linkId", id).delete();
        Repo.get(ShortenLinkReferrerStats.class).query().where("linkId", id).delete();
        Repo.get(ShortenLinkOperatingSystemStats.class).query().where("linkId", id).delete();
        super.delete();
    }

    public enum Type {
        SHORTEN_LINK
    }
}
