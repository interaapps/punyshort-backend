package de.interaapps.punyshort.model.responses.links;

import de.interaapps.punyshort.model.database.ShortenLink;
import de.interaapps.punyshort.model.database.domains.Domain;
import de.interaapps.punyshort.model.responses.domains.DomainResponse;

import java.util.List;

public class ShortenLinkResponse {
    public String id;
    public String longLink;
    public DomainResponse domain;
    public String path;
    public String fullLink;
    public ShortenLink.Type type;
    public CompactStats compactStats;
    public List<String> tags;

    public ShortenLinkResponse(ShortenLink shortenLink, Domain domain) {
        this.domain = new DomainResponse(domain, false);
        this.id = shortenLink.id;
        this.path = shortenLink.path;
        this.type = shortenLink.type;
        this.longLink = shortenLink.longLink;
        this.fullLink = "https://" + domain.name + "/" + this.path;
        this.tags = shortenLink.getTags();
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
