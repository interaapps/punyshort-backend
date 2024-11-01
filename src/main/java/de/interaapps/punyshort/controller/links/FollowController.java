package de.interaapps.punyshort.controller.links;

import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.exceptions.NotFoundException;
import de.interaapps.punyshort.helper.ipgeography.*;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.ShortenLink;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.database.cache.IPAddressCountryCodeCache;
import de.interaapps.punyshort.model.database.domains.Domain;
import de.interaapps.punyshort.model.database.stats.*;
import de.interaapps.punyshort.model.requests.links.FollowLinkRequest;
import de.interaapps.punyshort.model.responses.links.ShortenLinkResponse;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;

import java.sql.Date;
import java.util.Calendar;
import java.util.Random;
import java.util.regex.Pattern;

@PathPrefix("/v1/follow")
public class FollowController extends HttpController {

    public static IPGeographyProvider[] IP_GEOGRAPHY_PROVIDERS = {
            new IP2CProvider(),
            new IPApiProvider(),
            new IPLocateProvider(),
            new IPInfoProvider()
    };

    private int threads = 0;

    @Post
    @With("auth")
    public ShortenLinkResponse follow(@Body FollowLinkRequest request, @Attrib("user") User user, @Attrib("token") AccessToken accessToken) {
        Domain domain = Domain.byName(request.domain);
        ShortenLink shortenLink = ShortenLink.get(domain, request.path);

        if (user.type != User.Type.ADMIN && accessToken.type != AccessToken.Type.ADMIN_REDIRECT_PROXY_INSTANCE) {
            domain.checkUserAccess(user);
        }

        if (shortenLink == null)
            throw new NotFoundException();

        final ShortenLinkClickStats shortenLinkClickStats = new ShortenLinkClickStats();

        shortenLinkClickStats.linkId = shortenLink.id;

        shortenLinkClickStats.save();

        try {
            IPAddressCountryCodeCache ipAddressCountryCodeCache = Repo.get(IPAddressCountryCodeCache.class).where("ip", request.ip).first();

            if (ipAddressCountryCodeCache == null) {
                if (threads < 50) {
                    threads++;
                    new Thread(() -> {
                        IPGeographyProvider provider = IP_GEOGRAPHY_PROVIDERS[new Random().nextInt(IP_GEOGRAPHY_PROVIDERS.length-1)];

                        IPAddressCountryCodeCache newIpAddressCache = new IPAddressCountryCodeCache();
                        newIpAddressCache.ip = request.ip;
                        newIpAddressCache.countryCode = Pattern.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$\n", request.ip) ? provider.fetch(newIpAddressCache.ip) : "UNKN";
                        newIpAddressCache.save();
                        pushCountryStats(shortenLinkClickStats, newIpAddressCache, true);
                        System.out.println("Using " + provider.getClass().getName() + " checking ip " + request.ip + " " + "newIpAddressCache.countryCode");
                        threads--;
                    }).start();
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
