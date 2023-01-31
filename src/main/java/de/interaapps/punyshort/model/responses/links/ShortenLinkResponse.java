package de.interaapps.punyshort.model.responses.links;

import de.interaapps.punyshort.model.database.ShortenLink;
import de.interaapps.punyshort.model.database.domains.Domain;

public class ShortenLinkResponse {
    public String id;
    public String longLink;
    public Domain domain;
    public String path;
    public String fullLink;
    public ShortenLink.Type type;
    public CompactStats compactStats;

    public ShortenLinkResponse(ShortenLink shortenLink, Domain domain) {
        this.domain = domain;
        this.id = shortenLink.id;
        this.path = shortenLink.path;
        this.type = shortenLink.type;
        this.longLink = shortenLink.longLink;
        this.fullLink = "https://" + domain.name + "/" + this.path;
    }

    public ShortenLinkResponse(ShortenLink shortenLink) {
        this(shortenLink, Domain.get(shortenLink.domain));
    }

    public static class CompactStats {
        public int total;
        public String mostVisitingCountry;
        public int clicksToday;

        public CompactStats(int total, String mostVisitedCountry, int clicksToday) {
            this.total = total;
            this.mostVisitingCountry = mostVisitedCountry;
            this.clicksToday = clicksToday;
        }

    }
}
