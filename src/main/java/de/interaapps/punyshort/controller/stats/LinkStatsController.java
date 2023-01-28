package de.interaapps.punyshort.controller.stats;

import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.exceptions.NotFoundException;
import de.interaapps.punyshort.helper.DatabaseHelper;
import de.interaapps.punyshort.helper.RequestHelper;
import de.interaapps.punyshort.model.database.ShortenLink;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.database.stats.*;
import de.interaapps.punyshort.model.responses.PaginatedResponse;
import de.interaapps.punyshort.model.responses.PaginationData;
import de.interaapps.punyshort.model.responses.links.ShortenLinkResponse;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.util.List;

@PathPrefix("/v1/shorten-links/{id}/stats")
@With("auth")
public class LinkStatsController extends HttpController {
    @Get("/countries")
    public PaginatedResponse<ShortenLinkCountriesStats> countries(Exchange exchange, @Path("id") String id, @Attrib("user") User user) {
        ShortenLink shortenLink = ShortenLink.get(id);
        if (shortenLink == null)
            throw new NotFoundException();
        shortenLink.checkUserAccess(user);


        Query<ShortenLinkCountriesStats> countryQuery = Repo.get(ShortenLinkCountriesStats.class).where("linkId", id);

        RequestHelper.defaultNavigation(exchange, countryQuery);
        RequestHelper.orderBy(countryQuery, exchange, "count", true);

        PaginationData pagination = RequestHelper.pagination(countryQuery, exchange);
        return new PaginatedResponse<>(countryQuery.all(), pagination);
    }

    @Get("/referrers")
    public PaginatedResponse<ShortenLinkReferrerStats> referrersCountries(Exchange exchange, @Path("id") String id, @Attrib("user") User user) {
        ShortenLink shortenLink = ShortenLink.get(id);
        if (shortenLink == null)
            throw new NotFoundException();
        shortenLink.checkUserAccess(user);

        Query<ShortenLinkReferrerStats> referrersQuery = Repo.get(ShortenLinkReferrerStats.class).where("linkId", id);

        RequestHelper.defaultNavigation(exchange, referrersQuery);
        RequestHelper.orderBy(referrersQuery, exchange, "count", true);

        PaginationData pagination = RequestHelper.pagination(referrersQuery, exchange);
        return new PaginatedResponse<>(referrersQuery.all(), pagination);
    }

    @Get("/operating-systems")
    public PaginatedResponse<ShortenLinkOperatingSystemStats> operatingSystemscountries(Exchange exchange, @Path("id") String id, @Attrib("user") User user) {
        ShortenLink shortenLink = ShortenLink.get(id);
        if (shortenLink == null)
            throw new NotFoundException();
        shortenLink.checkUserAccess(user);

        Query<ShortenLinkOperatingSystemStats> operatingSystemsQuery = Repo.get(ShortenLinkOperatingSystemStats.class).where("linkId", id);

        RequestHelper.defaultNavigation(exchange, operatingSystemsQuery);
        RequestHelper.orderBy(operatingSystemsQuery, exchange, "count", true);

        PaginationData pagination = RequestHelper.pagination(operatingSystemsQuery, exchange);
        return new PaginatedResponse<>(operatingSystemsQuery.all(), pagination);
    }

    @Get("/browsers")
    public PaginatedResponse<ShortenLinkBrowserStats> browserscountries(Exchange exchange, @Path("id") String id, @Attrib("user") User user) {
        ShortenLink shortenLink = ShortenLink.get(id);
        if (shortenLink == null)
            throw new NotFoundException();
        shortenLink.checkUserAccess(user);

        Query<ShortenLinkBrowserStats> browsersQuery = Repo.get(ShortenLinkBrowserStats.class).where("linkId", id);

        RequestHelper.defaultNavigation(exchange, browsersQuery);
        RequestHelper.orderBy(browsersQuery, exchange, "count", true);

        PaginationData pagination = RequestHelper.pagination(browsersQuery, exchange);
        return new PaginatedResponse<>(browsersQuery.all(), pagination);
    }

    @Get("/dates")
    public PaginatedResponse<ShortenLinkClickPerDateStats> datescountries(Exchange exchange, @Path("id") String id, @Attrib("user") User user) {
        ShortenLink shortenLink = ShortenLink.get(id);
        if (shortenLink == null)
            throw new NotFoundException();
        shortenLink.checkUserAccess(user);

        Query<ShortenLinkClickPerDateStats> perDayQuery = Repo.get(ShortenLinkClickPerDateStats.class).where("linkId", id);

        RequestHelper.defaultNavigation(exchange, perDayQuery);
        RequestHelper.orderBy(perDayQuery, exchange, "count", true);

        PaginationData pagination = RequestHelper.pagination(perDayQuery, exchange);
        return new PaginatedResponse<>(perDayQuery.all(), pagination);
    }

    @Get("/total")
    public int total(Exchange exchange, @Path("id") String id, @Attrib("user") User user) {
        ShortenLink shortenLink = ShortenLink.get(id);
        if (shortenLink == null)
            throw new NotFoundException();
        shortenLink.checkUserAccess(user);
        return DatabaseHelper.sum(Repo.get(ShortenLinkClickPerDateStats.class).where("linkId", id), "count");
    }
}
