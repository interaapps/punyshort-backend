package de.interaapps.punyshort.controller.links;

import com.google.gson.Gson;
import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.exceptions.NotFoundException;
import de.interaapps.punyshort.model.database.ShortenLink;
import de.interaapps.punyshort.model.database.cache.IPAddressCountryCodeCache;
import de.interaapps.punyshort.model.database.domains.Domain;
import de.interaapps.punyshort.model.database.stats.*;
import de.interaapps.punyshort.model.requests.links.FollowLinkRequest;
import de.interaapps.punyshort.model.responses.links.ShortenLinkResponse;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpclient.HTTPClient;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;

import java.sql.Date;
import java.util.Calendar;

@PathPrefix("/v1/follow")
public class FollowController extends HttpController {
    private int threads = 0;

    @Post
    public ShortenLinkResponse follow(@Body FollowLinkRequest request) {
        System.out.println(new Gson().toJson(request));
        ShortenLink shortenLink = ShortenLink.get(Domain.byName(request.domain), request.path);

        if (shortenLink == null)
            throw new NotFoundException();

        final ShortenLinkClickStats shortenLinkClickStats = new ShortenLinkClickStats();

        shortenLinkClickStats.linkId = shortenLink.id;

        shortenLinkClickStats.save();

        try {
            IPAddressCountryCodeCache ipAddressCountryCodeCache = Repo.get(IPAddressCountryCodeCache.class).where("ip", request.ip).first();

            if (ipAddressCountryCodeCache == null) {
                if (threads < 50) {
                    new Thread(() -> {
                        AbstractObject data = new HTTPClient().get("https://www.iplocate.io/api/lookup/" + request.ip).data().object();

                        IPAddressCountryCodeCache newIpAddressCache = new IPAddressCountryCodeCache();
                        newIpAddressCache.ip = request.ip;
                        newIpAddressCache.countryCode = data.string("country_code", "UNKN");
                        newIpAddressCache.save();
                        pushCountryStats(shortenLinkClickStats, newIpAddressCache, true);
                    }).start();
                    threads++;
                }
            } else {
                pushCountryStats(shortenLinkClickStats, ipAddressCountryCodeCache, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Date date = new Date(Calendar.getInstance().getTime().getTime());
        pushDateStats(shortenLinkClickStats, date);

        if (request.referrer == null)
            request.userAgent = "";

        if (request.userAgent == null)
            request.userAgent = "";

        shortenLinkClickStats.operatingSystem = ShortenLinkClickStats.OperatingSystem.find(request.userAgent);
        pushOperatingSystemStats(shortenLinkClickStats, shortenLinkClickStats.operatingSystem);

        shortenLinkClickStats.browser = ShortenLinkClickStats.Browser.find(request.userAgent);
        pushBrowserStats(shortenLinkClickStats, shortenLinkClickStats.browser);

        shortenLinkClickStats.referrer = request.referrer;
        pushReferrerStats(shortenLinkClickStats, shortenLinkClickStats.referrer);

        shortenLinkClickStats.save();

        return new ShortenLinkResponse(shortenLink);
    }

    private void pushCountryStats(ShortenLinkClickStats clickStats, IPAddressCountryCodeCache ipAddressCountryCodeCache, boolean update) {
        clickStats.countryCode = ipAddressCountryCodeCache.countryCode;
        if (update)
            clickStats.save();

        ShortenLinkCountriesStats countryCodeStats = Repo.get(ShortenLinkCountriesStats.class)
                .where("linkId", clickStats.linkId)
                .where("countryCode", clickStats.countryCode)
                .first();

        if (countryCodeStats == null) {
            countryCodeStats = new ShortenLinkCountriesStats();
            countryCodeStats.linkId = clickStats.linkId;
            countryCodeStats.countryCode = clickStats.countryCode;
        }

        countryCodeStats.count++;
        countryCodeStats.save();
    }

    private void pushBrowserStats(ShortenLinkClickStats clickStats, ShortenLinkClickStats.Browser browser) {
        ShortenLinkBrowserStats browserStats = Repo.get(ShortenLinkBrowserStats.class)
                .where("linkId", clickStats.linkId)
                .where("browser", browser)
                .first();

        if (browserStats == null) {
            browserStats = new ShortenLinkBrowserStats();
            browserStats.linkId = clickStats.linkId;
            browserStats.browser = browser;
        }

        browserStats.count++;
        browserStats.save();
    }

    private void pushOperatingSystemStats(ShortenLinkClickStats clickStats, ShortenLinkClickStats.OperatingSystem browser) {
        ShortenLinkOperatingSystemStats operatingSystemStats = Repo.get(ShortenLinkOperatingSystemStats.class)
                .where("linkId", clickStats.linkId)
                .where("operatingSystem", browser)
                .first();

        if (operatingSystemStats == null) {
            operatingSystemStats = new ShortenLinkOperatingSystemStats();
            operatingSystemStats.linkId = clickStats.linkId;
            operatingSystemStats.operatingSystem = browser;
        }

        operatingSystemStats.count++;
        operatingSystemStats.save();
    }


    private void pushDateStats(ShortenLinkClickStats clickStats, Date date) {
        ShortenLinkClickPerDateStats dateStats = Repo.get(ShortenLinkClickPerDateStats.class)
                .where("linkId", clickStats.linkId)
                .where("date", date)
                .first();

        if (dateStats == null) {
            dateStats = new ShortenLinkClickPerDateStats();
            dateStats.linkId = clickStats.linkId;
            dateStats.date = date;
        }

        dateStats.count++;
        dateStats.save();
    }


    private void pushReferrerStats(ShortenLinkClickStats clickStats, String referrer) {
        ShortenLinkReferrerStats referrerStats = Repo.get(ShortenLinkReferrerStats.class)
                .where("linkId", clickStats.linkId)
                .where("referrer", referrer)
                .first();

        if (referrerStats == null) {
            referrerStats = new ShortenLinkReferrerStats();
            referrerStats.linkId = clickStats.linkId;
            referrerStats.referrer = referrer;
        }

        referrerStats.count++;
        referrerStats.save();
    }
}
