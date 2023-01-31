package de.interaapps.punyshort.helper.ipgeography;

import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpclient.HTTPClient;

public class LocalDataProvider implements IPGeographyProvider {
    @Override
    public String fetch(String ip) {

        return "UNKN";
    }
}
