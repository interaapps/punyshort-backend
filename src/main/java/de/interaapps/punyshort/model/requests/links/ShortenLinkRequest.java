package de.interaapps.punyshort.model.requests.links;

import org.javawebstack.validator.Rule;

import java.util.List;

public class ShortenLinkRequest {
    @Rule("string")
    public String domain;

    @Rule("string")
    public String path;

    @Rule({"string(5)", "required"})
    public String longLink;

    public String type;

    @Rule("string")
    public String workspaceId;

    public List<String> tags = null;
}
