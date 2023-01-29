package de.interaapps.punyshort.controller.domains;

import de.interaapps.punyshort.Punyshort;
import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.exceptions.AuthenticationException;
import de.interaapps.punyshort.exceptions.NotFoundException;
import de.interaapps.punyshort.exceptions.PermissionsDeniedException;
import de.interaapps.punyshort.helper.DNSHelper;
import de.interaapps.punyshort.helper.DatabaseHelper;
import de.interaapps.punyshort.helper.RequestHelper;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.ShortenLink;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.database.domains.Domain;
import de.interaapps.punyshort.model.database.domains.DomainUser;
import de.interaapps.punyshort.model.database.stats.ShortenLinkClickPerDateStats;
import de.interaapps.punyshort.model.database.stats.ShortenLinkCountriesStats;
import de.interaapps.punyshort.model.responses.ActionResponse;
import de.interaapps.punyshort.model.responses.PaginatedResponse;
import de.interaapps.punyshort.model.responses.PaginationData;
import de.interaapps.punyshort.model.responses.domains.DomainResponse;
import de.interaapps.punyshort.model.responses.links.ShortenLinkResponse;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpclient.HTTPClient;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Delete;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.sql.Date;
import java.util.Calendar;
import java.util.stream.Collectors;

@PathPrefix("/v1/domains")
public class DomainsController extends HttpController {
    @Get
    public PaginatedResponse<DomainResponse> getAll(Exchange exchange, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        Query<Domain> domains = Repo.get(Domain.class).query();

        if (exchange.query("show_public", "true").equalsIgnoreCase("true")) {
            domains.where("isPublic", true);
        }

        if (user != null) {
            accessToken.checkPermission("domains:read");

            domains.orWhereExists(DomainUser.class, q -> q.where("userId", user.id).where(Domain.class, "id", "=", DomainUser.class, "domain"));
        }

        RequestHelper.defaultNavigation(exchange, domains);
        RequestHelper.orderBy(domains, exchange, "created_at", false);

        PaginationData pagination = RequestHelper.pagination(domains, exchange);
        return new PaginatedResponse<>(domains.all().stream().map(DomainResponse::new).collect(Collectors.toList()), pagination);
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

        return new DomainResponse(domain);
    }


    @Delete("/{id}")
    @With("auth")
    public ActionResponse delete(@Path("id") String id, @Attrib("token") AccessToken accessToken, @Attrib("user") User user) {
        Domain domain = Domain.get(id, false);
        if (domain == null)
            throw new NotFoundException();
        accessToken.checkPermission("domains:delete");

        domain.delete();

        Repo.get(ShortenLink.class).where("domain", domain.id).get().forEach(ShortenLink::delete);

        return new ActionResponse(true);
    }


    @Post("/{id}/dns-check")
    @With("auth")
    public ActionResponse triggerDomainCheck(@Path("id") String id, @Attrib("token") AccessToken accessToken, @Attrib("user") User user) {
        Domain domain = Domain.get(id, false);
        if (domain == null)
            throw new NotFoundException();
        accessToken.checkPermission("domains:write");

        domain.updateStatus();

        return new ActionResponse(true);
    }
}
