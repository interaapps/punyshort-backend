package de.interaapps.punyshort.controller;

import de.interaapps.punyshort.Punyshort;
import de.interaapps.punyshort.model.responses.app.AppInfoResponse;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.verbs.Get;

@PathPrefix("/v1/app")
public class AppController extends HttpController {
    @Get("/info")
    public AppInfoResponse appInfo() {
        return new AppInfoResponse(Punyshort.getInstance());
    }
}
