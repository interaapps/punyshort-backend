package de.interaapps.punyshort.controller;

import de.interaapps.punyshort.Punyshort;
import de.interaapps.punyshort.model.responses.app.AppInfoResponse;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.verbs.Get;

@PathPrefix("/v1/app")
public class AppController extends HttpController {
    @Get("/info")
    public AppInfoResponse appInfo() {
        return new AppInfoResponse(Punyshort.getInstance());
    }

    @Get("/default-cname")
    public AbstractObject getCNameAddress() {
        return new AbstractObject().object().set("value", Punyshort.getInstance().getConfig().get("punyshort.default.cname"));
    }

    @Get("/default-redirect-proxy")
    public AbstractObject getDefaultProxy() {
        return new AbstractObject().object().set("value", Punyshort.getInstance().getConfig().get("punyshort.default.redirect.proxy"));
    }
}
