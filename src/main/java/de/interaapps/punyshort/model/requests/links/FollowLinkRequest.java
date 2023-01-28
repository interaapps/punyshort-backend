package de.interaapps.punyshort.model.requests.links;

import org.javawebstack.validator.Rule;

public class FollowLinkRequest {
    public String ip;

    public String userAgent;

    public String referrer;

    @Rule({"string(4)"})
    public String domain;

    @Rule({"string(1)"})
    public String path;
}
