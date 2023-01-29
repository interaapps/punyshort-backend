package de.interaapps.punyshort.model.requests.auth;

import de.interaapps.punyshort.model.database.AccessToken;

public class CreateAccessTokenRequest {
    public AccessToken.Type type = AccessToken.Type.API;
}
