package de.interaapps.punyshort.helper.ipgeography;

import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpclient.HTTPClient;

public class IP2CProvider implements IPGeographyProvider {
    @Override
    public String fetch(String ip) {
        String[] split = new HTTPClient().get("https://ip2c.org/" + ip).string().split(";");

        if (split.length < 2)
            return "UNKN";

        if (split[1].length() != 2)
            return "UNKN";

        return split[1];
    }
}
