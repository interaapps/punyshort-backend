package de.interaapps.punyshort.controller.domains;

import de.interaapps.punyshort.Punyshort;
import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.exceptions.*;
import de.interaapps.punyshort.helper.DNSHelper;
import de.interaapps.punyshort.helper.DatabaseHelper;
import de.interaapps.punyshort.helper.RequestHelper;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.ShortenLink;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.database.cache.IPAddressCountryCodeCache;
import de.interaapps.punyshort.model.database.domains.Domain;
import de.interaapps.punyshort.model.database.domains.DomainUser;
import de.interaapps.punyshort.model.database.stats.ShortenLinkClickPerDateStats;
import de.interaapps.punyshort.model.database.stats.ShortenLinkClickStats;
import de.interaapps.punyshort.model.database.stats.ShortenLinkCountriesStats;
import de.interaapps.punyshort.model.database.workspaces.WorkspaceDomain;
import de.interaapps.punyshort.model.requests.domains.CreateDomainRequest;
import de.interaapps.punyshort.model.requests.links.FollowLinkRequest;
import de.interaapps.punyshort.model.responses.ActionResponse;
import de.interaapps.punyshort.model.responses.PaginatedResponse;
import de.interaapps.punyshort.model.responses.PaginationData;
import de.interaapps.punyshort.model.responses.domains.DomainResponse;
import de.interaapps.punyshort.model.responses.links.ShortenLinkResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpclient.HTTPClient;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Delete;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.sql.Date;
import java.util.Calendar;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@PathPrefix("/v1/domains")
public class DomainsController extends HttpController {
    @Get
    public PaginatedResponse<DomainResponse> getAll(Exchange exchange, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        Query<Domain> domains = Repo.get(Domain.class).query();

        if (exchange.query("show_public", "true").equalsIgnoreCase("true")) {
            domains.where("isPublic", true);
        }

        boolean showInternals;
        if (user != null) {
            accessToken.checkPermission("domains:read");

            domains.orWhereExists(DomainUser.class, q -> q.where("userId", user.id).where(Domain.class, "id", "=", DomainUser.class, "domain"));

            showInternals = (!exchange.query("show_public", "true").equalsIgnoreCase("true")) && exchange.query("show_internals", "true").equalsIgnoreCase("true");
        } else {
            showInternals = false;
        }

        RequestHelper.defaultNavigation(exchange, domains);
        RequestHelper.orderBy(domains, exchange, "created_at", false);

        PaginationData pagination = RequestHelper.pagination(domains, exchange);
        return new PaginatedResponse<>(domains.all().stream().map(d -> new DomainResponse(d, showInternals)).collect(Collectors.toList()), pagination);
    }

    @Post
    @With("auth")
    public DomainResponse create(@Body CreateDomainRequest request, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        accessToken.checkPermission("domains:write");
        Domain domain = new Domain();

        request.name = request.name.toLowerCase();

        if (!Pattern.matches("^[A-Za-z0-9._-]*$", request.name) || Domain.byName(request.name) != null) {
            throw new DomainInvalidOrTakenException();
        }

        if (request.dnsSettings == null)
            request.dnsSettings = new AbstractObject();

        domain.name = request.name;


        if (user.type != User.Type.ADMIN) {
            domain.isPublic = request.isPublic;
        }

        switch (request.dnsType) {
            case CNAME:
            case CUSTOM_PROXY:
                domain.dnsSettings = request.dnsSettings
                        .set("txt-entry", RandomStringUtils.random(30, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890"));
                break;
            case CLOUDFLARE:
                domain.dnsSettings = request.dnsSettings;
                break;
            case INTERNAL:
                if (user.type != User.Type.ADMIN) {
                    throw new PermissionsDeniedException();
                }
                break;
        }

        domain.dnsType = request.dnsType;

        domain.save();
        domain.addUser(user, DomainUser.Role.ADMIN);

        return new DomainResponse(domain, true);
    }


    @Get("/domain-exists")
    public String checkAllowHTTPS(@org.javawebstack.httpserver.router.annotation.params.Query("domain") String domainName) {
        Domain domain = Domain.byName(domainName);

        if (domain == null)
            throw new NotFoundException();

        return "{\"allow\": true}";
    }

    @Get("/{id}")
    public DomainResponse get(@Path("id") String id, @Attrib("token") AccessToken accessToken, @Attrib("user") User user) {
        Domain domain = Domain.get(id, false);
        if (domain == null)
            throw new NotFoundException();

        if (!domain.isPublic) {
            if (user == null)
                throw new AuthenticationException();
            accessToken.checkPermission("domains:read");
        }

        return new DomainResponse(domain, true);
    }


    @Delete("/{id}")
    @With("auth")
    public ActionResponse delete(@Path("id") String id, @Attrib("token") AccessToken accessToken, @Attrib("user") User user) {
        Domain domain = Domain.get(id, false);
        if (domain == null)
            throw new NotFoundException();
        accessToken.checkPermission("domains:delete");

        DomainUser domainUser = domain.getUser(user.id);

        if (domainUser == null || domainUser.role != DomainUser.Role.ADMIN)
            throw new PermissionsDeniedException();

        domain.delete();

        Repo.get(ShortenLink.class).where("domain", domain.id).get().forEach(ShortenLink::delete);
        Repo.get(DomainUser.class).where("domain", domain.id).delete();
        Repo.get(WorkspaceDomain.class).where("domainId", domain.id).delete();

        return new ActionResponse(true);
    }


    @Post("/{id}/dns-check")
    @With("auth")
    public ActionResponse triggerDomainCheck(@Path("id") String id, @Attrib("token") AccessToken accessToken, @Attrib("user") User user) {
        accessToken.checkPermission("domains:write");

        Domain domain = Domain.get(id, false);
        if (domain == null)
            throw new NotFoundException();

        DomainUser domainUser = domain.getUser(user.id);
        if (domainUser == null || domainUser.role != DomainUser.Role.ADMIN)
            throw new PermissionsDeniedException();


        domain.updateStatus();

        return new ActionResponse(true);
    }
}
