package de.interaapps.punyshort.model.responses.app;

import org.javawebstack.orm.Repo;

public class StatsResponse {

    private int createdPastes;
    private int loggedInPastes;

    public static StatsResponse create() {
        StatsResponse response = new StatsResponse();

        return response;
    }
}
