package de.interaapps.punyshort.model.responses.app;

import de.interaapps.punyshort.Punyshort;
import org.javawebstack.webutils.config.Config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AppInfoResponse {

    public String customLogo;
    public String customName;
    public Map<String, String> customFooter;
    public boolean encryptionIsDefault;

    public AppInfoResponse(Punyshort punyshort) {
    }
}
