package de.interaapps.punyshort.model.database.cache;

import de.interaapps.punyshort.model.database.domains.Domain;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.*;

import java.sql.Timestamp;

@Dates
@Table("ip_address_country_code_cashe")
public class IPAddressCountryCodeCache extends Model {
    @Column(id = true)
    public int id;

    @Column(size = 45)
    @Filterable
    public String ip;

    @Column(size = 4)
    @Filterable
    public String countryCode = "UNKN";

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;
}
