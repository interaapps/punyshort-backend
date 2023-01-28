package de.interaapps.punyshort.model.responses.domains;

import de.interaapps.punyshort.model.database.ShortenLink;
import de.interaapps.punyshort.model.database.domains.Domain;

public class DomainResponse {
    public String id;
    public String name;
    public boolean isPublic;
    public boolean isActive;

    public DomainResponse(Domain domain) {
        id = domain.id;
        name = domain.name;
        isPublic = domain.isPublic;
        isActive = domain.isActive;
    }
}
