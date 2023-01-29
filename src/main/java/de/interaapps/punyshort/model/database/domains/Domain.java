package de.interaapps.punyshort.model.database.domains;

import de.interaapps.punyshort.Punyshort;
import de.interaapps.punyshort.exceptions.InternalErrorException;
import de.interaapps.punyshort.exceptions.PermissionsDeniedException;
import de.interaapps.punyshort.helper.DNSHelper;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.javawebstack.abstractdata.AbstractArray;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpclient.HTTPClient;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Filterable;
import org.javawebstack.orm.annotation.Searchable;
import org.javawebstack.orm.query.Query;

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
    public AbstractObject dnsSettings = new AbstractObject();

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

    public boolean userHasAccess(User user) {
        return Repo.get(DomainUser.class).where("domain", id).where("userId", user.id).first() == null;
    }
    public void checkUserAccess(User user) {
        if (userHasAccess(user)) {
            throw new PermissionsDeniedException();
        }
    }

    public static Domain get(String id, boolean isActiveRequired) {
        Query<Domain> query = Repo.get(Domain.class).where("id", id);
        if (isActiveRequired)
            query.where("isActive", true);
        return query.first();
    }

    public static Domain get(String id) {
        return get(id, true);
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

    public void updateStatus() {
        Domain domain = Domain.get(id, false);

        if (domain.dnsSettings == null)
            domain.dnsSettings = new AbstractObject();

        switch (domain.dnsType) {
            case INTERNAL:
            case CUSTOM_PROXY:
                domain.isActive = true;
                break;
            case CNAME:
                if (!domain.dnsSettings.has("txt-entry"))
                    break;
                if (DNSHelper.getTxtRecord("punyshort-check."+domain.name).equals(domain.dnsSettings.string("txt-entry"))) {
                    domain.isActive = true;
                }
                break;
            case CLOUDFLARE:
                if (!domain.dnsSettings.has("cf-api-key") || !domain.dnsSettings.has("cf-zone-id"))
                    break;

                domain.updateStatus();

                domain.isActive = true;
                break;
        }

        domain.save();
    }

    public void updateCloudflare() {
        HTTPClient cloudflare = new HTTPClient("https://api.cloudflare.com/client/v4")
                .bearer(dnsSettings.string("cf-api-key", "ERR"));

        AbstractObject zones = cloudflare.get("/zones/" + dnsSettings.string("cf-zone-id", "ERR") + "/dns_records").data().object();

        for (AbstractObject entry : zones.array("result").toObjectList()) {
            if (name.equalsIgnoreCase(entry.string("name"))) {
                if (cloudflare.put("/zones/" + dnsSettings.string("cf-zone-id", "ERR") + "/dns_records/" + entry.string("id"))
                        .contentType("application/json")
                        .jsonBody(new AbstractObject()
                                .set("type", "A")
                                .set("proxied", false)
                                .set("ttl", 1)
                                .set("name", name)
                                .set("content", Punyshort.getInstance().getConfig().get("punyshort.default.redirect.proxy")))
                        .status() > 400) {
                    throw new InternalErrorException();
                }
                isActive = true;
                return;
            }
        }

        if (cloudflare.post("/zones/" + dnsSettings.string("cf-zone-id", "ERR") + "/dns_records")
                .contentType("application/json")
                .jsonBody(new AbstractObject()
                        .set("type", "A")
                        .set("proxied", false)
                        .set("ttl", 1)
                        .set("name", name)
                        .set("content", Punyshort.getInstance().getConfig().get("punyshort.default.redirect.proxy")))
                .status() > 400) {
            throw new InternalErrorException();
        }
    }
}
