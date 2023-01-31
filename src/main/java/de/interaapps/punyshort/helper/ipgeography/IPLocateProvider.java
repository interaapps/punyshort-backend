package de.interaapps.punyshort.helper.ipgeography;

import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpclient.HTTPClient;

public class IPLocateProvider implements IPGeographyProvider {
    @Override
    public String fetch(String ip) {
        AbstractObject data = new HTTPClient().get("https://www.iplocate.io/api/lookup/" + ip).data().object();
        return data.string("country_code", "UNKN");
    }
}
