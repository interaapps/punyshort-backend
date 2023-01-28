package de.interaapps.punyshort.model.responses.links;

import de.interaapps.punyshort.model.responses.ActionResponse;

public class FollowLinkResponse extends ActionResponse {
    public FollowLinkResponse(String link) {
        this.link = link;
    }

    public String link;

}
