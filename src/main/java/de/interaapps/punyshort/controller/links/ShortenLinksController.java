package de.interaapps.punyshort.controller.links;

import de.interaapps.punyshort.Punyshort;
import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.exceptions.*;
import de.interaapps.punyshort.helper.DatabaseHelper;
import de.interaapps.punyshort.helper.RequestHelper;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.ShortenLink;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.database.domains.Domain;
import de.interaapps.punyshort.model.database.stats.ShortenLinkClickPerDateStats;
import de.interaapps.punyshort.model.database.stats.ShortenLinkCountriesStats;
import de.interaapps.punyshort.model.requests.links.ShortenLinkRequest;
import de.interaapps.punyshort.model.responses.ActionResponse;
import de.interaapps.punyshort.model.responses.PaginatedResponse;
import de.interaapps.punyshort.model.responses.PaginationData;
import de.interaapps.punyshort.model.responses.links.ShortenLinkActionResponse;
import de.interaapps.punyshort.model.responses.links.ShortenLinkResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Delete;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.httpserver.router.annotation.verbs.Put;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@PathPrefix("/v1/shorten-links")
public class ShortenLinksController extends HttpController {
    @Post
    public ShortenLinkResponse create(@Body ShortenLinkRequest request, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        ShortenLink shortenLink = new ShortenLink();

        shortenLink.type = ShortenLink.Type.SHORTEN_LINK;

        shortenLink.longLink = request.longLink;
        checkLongLink(request.longLink);


        Domain domain = getAndCheckDomainAccess(request.domain, user);
        shortenLink.domain = domain.id;

        String path = "";
        if (request.path != null) {
            path = getAndCheckPath(request.path, user, domain);
        } else {
            do {
                path = RandomStringUtils.random(8, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
            } while (ShortenLink.get(domain, path) != null);
        }

        if (path.equals("")) {
            if (!domain.isPublic) {
                if (user == null) {
                    throw new AuthenticationException();
                }

                domain.checkUserAccess(user);
            }
        }

        shortenLink.path = path;

        if (user != null) {
            accessToken.checkPermission("shorten_links:write");
            shortenLink.userId = user.id;
        }

        shortenLink.saveAndUpdateLinkCache(domain);

        return new ShortenLinkResponse(shortenLink, domain);
    }

    public Domain getAndCheckDomainAccess(String domainId, User user) {
        Domain domain = Domain.get(domainId);

        if (domain == null) {
            domain = Repo.get(Domain.class).where("is_public", true).first();

            if (domain == null) {
                System.err.println("You have no domains set up");
                throw new NoDefaultDomainFoundException();
            }
        }

        if (!domain.isPublic) {
            if (user == null) {
                throw new AuthenticationException();
            }

            domain.checkUserAccess(user);
        }
        return domain;
    }

    public void checkLongLink(String longLink) {
        if (!longLink.contains("://")) {
            throw new InvalidURLException();
        }
        for (Pattern linkFilter : Punyshort.getInstance().getLinkFilters()) {
            if (linkFilter.matcher(longLink).matches()) {
                throw new FilteredOutException();
            }
        }
    }

    public String getAndCheckPath(String path, User user, Domain domain) {
        path = path.trim();

        if (domain.isPublic && (path.length() <= 6 || path.contains("/")) && !domain.userHasAccess(user))
            throw new PathTooShortException();
        if (path.charAt(0) == '/')
            path = path.substring(1);
        if (!Pattern.matches("^([a-zA-Z0-9-_/])*$", path))
            throw new InvalidPathException();
        if (ShortenLink.get(domain, path) != null)
            throw new PathTakenException();

        return path;
    }


    @Get
    @With("auth")
    public PaginatedResponse<ShortenLinkResponse> getAll(Exchange exchange, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        accessToken.checkPermission("shorten_links:read");

        Query<ShortenLink> userLinks = Repo.get(ShortenLink.class).where("userId", user.id);

        RequestHelper.defaultNavigation(exchange, userLinks);
        RequestHelper.orderBy(userLinks, exchange, "created_at", true);

        PaginationData pagination = RequestHelper.pagination(userLinks, exchange);
        return new PaginatedResponse<>(userLinks.all().stream().map(r -> {
            ShortenLinkResponse shortenLinkResponse = new ShortenLinkResponse(r);

            if (exchange.query("show_compact_stats", "false").equalsIgnoreCase("true")) {
                ShortenLinkClickPerDateStats shortenLinkClickPerDateStats = Repo.get(ShortenLinkClickPerDateStats.class).where("linkId", r.id).where("date", new Date(Calendar.getInstance().getTime().getTime())).first();
                ShortenLinkCountriesStats countriesStats = Repo.get(ShortenLinkCountriesStats.class).where("linkId", r.id).order("count", true).first();

                shortenLinkResponse.compactStats = new ShortenLinkResponse.CompactStats(
                        DatabaseHelper.sum(Repo.get(ShortenLinkClickPerDateStats.class).where("linkId", r.id), "count"),
                        countriesStats == null ? null : countriesStats.countryCode,
                        shortenLinkClickPerDateStats == null ? 0 : shortenLinkClickPerDateStats.count
                );
            }
            return shortenLinkResponse;
        }).collect(Collectors.toList()), pagination);
    }

    @Get("/{id}")
    public ShortenLinkResponse get(@Path("id") String id) {
        ShortenLink shortenLink = ShortenLink.get(id);
        if (shortenLink == null)
            throw new NotFoundException();
        return new ShortenLinkResponse(shortenLink);
    }


    @Delete("/{id}")
    @With("auth")
    public ActionResponse delete(@Path("id") String id, @Attrib("token") AccessToken accessToken, @Attrib("user") User user) {
        ShortenLink shortenLink = ShortenLink.get(id);
        if (shortenLink == null)
            throw new NotFoundException();
        accessToken.checkPermission("shorten_links:delete");

        shortenLink.delete();
        return new ActionResponse(true);
    }

    @Put("/{id}")
    @With("auth")
    public ActionResponse edit(@Body ShortenLinkRequest request, @Path("id") String id, @Attrib("token") AccessToken accessToken, @Attrib("user") User user) {
        ShortenLink shortenLink = ShortenLink.get(id);
        if (shortenLink == null)
            throw new NotFoundException();
        accessToken.checkPermission("shorten_links:write");

        boolean checkPath = false;
        if (request.path != null && !request.path.equals(shortenLink.path)) {
            shortenLink.path = request.path;
            checkPath = true;
        }

        if (request.domain != null && !request.domain.equals(shortenLink.domain)) {
            shortenLink.domain = getAndCheckDomainAccess(request.domain, user).id;
            checkPath = true;
        }

        if (checkPath) {
            getAndCheckPath(shortenLink.path, user, Domain.get(shortenLink.domain));
        }
        if (request.longLink != null) {
            checkLongLink(request.longLink);
            shortenLink.longLink = request.longLink;
        }

        shortenLink.saveAndUpdateLinkCache();

        return new ActionResponse(true);
    }
}
