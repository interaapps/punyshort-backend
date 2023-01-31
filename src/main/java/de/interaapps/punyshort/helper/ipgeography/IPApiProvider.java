package de.interaapps.punyshort.helper.ipgeography;

import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpclient.HTTPClient;

public class IPApiProvider implements IPGeographyProvider {
    @Override
    public String fetch(String ip) {
        AbstractObject data = new HTTPClient().get("http://ip-api.com/json/" + ip).data().object();
        return data.string("countryCode", "UNKN");
    }
}
