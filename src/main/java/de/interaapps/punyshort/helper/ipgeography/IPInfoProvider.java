package de.interaapps.punyshort.helper.ipgeography;

import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpclient.HTTPClient;

public class IPInfoProvider implements IPGeographyProvider {
    @Override
    public String fetch(String ip) {
        AbstractObject data = new HTTPClient().get("https://ipinfo.io/"+ip+"/json").data().object();
        return data.string("country", "UNKN");
    }
}
