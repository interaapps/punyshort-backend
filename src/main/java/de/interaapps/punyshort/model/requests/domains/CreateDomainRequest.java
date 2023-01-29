package de.interaapps.punyshort.model.requests.domains;

import de.interaapps.punyshort.model.database.domains.Domain;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.validator.Rule;

public class CreateDomainRequest {
    @Rule({"string(2)", "required"})
    public String name;

    @Rule({"required"})
    public Domain.DNSType dnsType = Domain.DNSType.CNAME;

    public AbstractObject dnsSettings;
    public boolean isPublic;
}
