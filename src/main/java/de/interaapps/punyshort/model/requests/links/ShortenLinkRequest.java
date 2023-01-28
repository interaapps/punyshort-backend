package de.interaapps.punyshort.model.requests.links;

import org.javawebstack.validator.Rule;

public class ShortenLinkRequest {
    @Rule("string")
    public String domain;

    @Rule("string")
    public String path;

    @Rule({"string(5)", "required"})
    public String longLink;

    public String type;
}
