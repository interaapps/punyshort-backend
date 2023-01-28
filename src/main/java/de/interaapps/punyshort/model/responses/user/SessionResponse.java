package de.interaapps.punyshort.model.responses.user;

import de.interaapps.punyshort.model.responses.ActionResponse;

public class SessionResponse extends ActionResponse {
    public String session;
    public SessionResponse(String session) {
        super(true);
        this.session = session;
    }
}
