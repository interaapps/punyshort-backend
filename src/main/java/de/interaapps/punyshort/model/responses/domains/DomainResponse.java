package de.interaapps.punyshort.model.responses.domains;

import de.interaapps.punyshort.model.database.domains.Domain;
import org.javawebstack.abstractdata.AbstractObject;

public class DomainResponse {
    public String id;
    public String name;
    public Domain.DNSType dnsType;
    public AbstractObject dnsSettings;
    public boolean locked = false;
    public boolean isPublic;
    public boolean isActive;

    public DomainResponse(Domain domain, boolean showInternal) {
        id = domain.id;
        name = domain.name;
        isPublic = domain.isPublic;
        locked = domain.locked;
        isActive = domain.isActive;

        if (showInternal) {
            dnsType = domain.dnsType;
            dnsSettings = domain.dnsSettings;
        }
    }

    public DomainResponse(Domain domain) {
        this(domain, false);
    }
}
