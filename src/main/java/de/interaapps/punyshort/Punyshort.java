package de.interaapps.punyshort;

import de.interaapps.punyshort.auth.*;
import de.interaapps.punyshort.controller.HttpController;
import de.interaapps.punyshort.exceptions.AuthenticationException;
import de.interaapps.punyshort.exceptions.NotFoundException;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.LinkFilter;
import de.interaapps.punyshort.model.database.User;
import de.interaapps.punyshort.model.responses.ExceptionResponse;
import org.javawebstack.httpserver.HTTPServer;
import org.javawebstack.orm.ORM;
import org.javawebstack.orm.ORMConfig;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.exception.ORMConfigurationException;
import org.javawebstack.orm.mapper.AbstractDataTypeMapper;
import org.javawebstack.orm.wrapper.SQLDriverFactory;
import org.javawebstack.orm.wrapper.SQLDriverNotFoundException;
import org.javawebstack.passport.Passport;
import org.javawebstack.passport.strategies.oauth2.OAuth2Strategy;
import org.javawebstack.passport.strategies.oauth2.providers.*;
import org.javawebstack.webutils.config.Config;
import org.javawebstack.webutils.config.EnvFile;
import org.javawebstack.webutils.middleware.CORSPolicy;
import org.javawebstack.webutils.middleware.MultipartPolicy;
import org.javawebstack.webutils.middleware.SerializedResponseTransformer;
import org.javawebstack.webutils.middlewares.RateLimitMiddleware;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Punyshort {

    private static Punyshort instance;

    public static Punyshort getInstance() {
        return instance;
    }

    private List<Pattern> linkFilters = new ArrayList<>();

    private Config config;
    private Passport passport;
    private OAuth2Strategy oAuth2Strategy;
    private HTTPServer httpServer;

    public Punyshort() {
        config = new Config();
        httpServer = new HTTPServer();
        passport = new Passport("/v1/auth");

        setupConfig();
        oAuth2Strategy = new OAuth2Strategy(getConfig().get("server.name", "http://localhost"));
        oAuth2Strategy.setHttpCallbackHandler(new OAuth2Callback());

        if (!getConfig().get("oauth2.interaapps.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Strategy.use("interaapps", new InteraAppsOAuth2Provider(getConfig().get("oauth2.interaapps.id"), getConfig().get("oauth2.interaapps.secret")).setScopes("user:read", "contacts.accepted:read"));
        if (!getConfig().get("oauth2.google.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Strategy.use("google", new GoogleOAuth2Provider(getConfig().get("oauth2.google.id"), getConfig().get("oauth2.google.secret")));
        if (!getConfig().get("oauth2.discord.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Strategy.use("discord", new DiscordOAuth2Provider(getConfig().get("oauth2.discord.id"), getConfig().get("oauth2.discord.secret")));
        if (!getConfig().get("oauth2.github.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Strategy.use("github", new GitHubOAuth2Provider(getConfig().get("oauth2.github.id"), getConfig().get("oauth2.github.secret")));
        if (!getConfig().get("oauth2.twitch.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Strategy.use("twitch", new TwitchOAuth2Provider(getConfig().get("oauth2.twitch.id"), getConfig().get("oauth2.twitch.secret")));

        passport.use("oauth2", oAuth2Strategy);

        setupModels();
        setupServer();
    }

    protected void setupConfig() {
        Map<String, String> map = new HashMap<>();
        map.put("HTTP_SERVER_PORT", "http.server.port");
        map.put("HTTP_SERVER_CORS", "http.server.cors");

        map.put("DATABASE_DRIVER", "database.driver");
        map.put("DATABASE_NAME", "database.name");
        map.put("DATABASE_USER", "database.user");
        map.put("DATABASE_PASSWORD", "database.password");
        map.put("DATABASE_HOST", "database.host");
        map.put("DATABASE_PORT", "database.port");

        map.put("INTERAAPPS_AUTH_KEY", "interaapps.auth.key");
        map.put("INTERAAPPS_AUTH_ID", "interaapps.auth.id");
        map.put("AUTH_PROVIDER", "auth.provider");

        map.put("SERVER_NAME", "server.name");

        map.put("RATELIMITER_MILLIS", "ratelimiter.millis");
        map.put("RATELIMITER_LIMIT", "ratelimiter.limit");

        map.put("OAUTH2_INTERAAPPS_CLIENT_ID", "oauth2.interaapps.id");
        map.put("OAUTH2_INTERAAPPS_CLIENT_SECRET", "oauth2.interaapps.secret");
        map.put("OAUTH2_TWITCH_CLIENT_ID", "oauth2.twitch.id");
        map.put("OAUTH2_TWITCH_CLIENT_SECRET", "oauth2.twitch.secret");
        map.put("OAUTH2_GITHUB_CLIENT_ID", "oauth2.github.id");
        map.put("OAUTH2_GITHUB_CLIENT_SECRET", "oauth2.github.secret");
        map.put("OAUTH2_GOOGLE_CLIENT_ID", "oauth2.google.id");
        map.put("OAUTH2_GOOGLE_CLIENT_SECRET", "oauth2.google.secret");
        map.put("OAUTH2_DISCORD_CLIENT_ID", "oauth2.discord.id");
        map.put("OAUTH2_DISCORD_CLIENT_SECRET", "oauth2.discord.secret");

        map.put("PUNYSHORT_DEFAULT_REDIRECT_PROXY", "punyshort.default.redirect.proxy");

        File file = new File(".env");
        if (file.exists()) {
            config.add(new EnvFile(file).withVariables(), map);
        } else {
            config.add(new EnvFile().withVariables(), map);
        }
    }

    protected void setupModels() {
        try {
            SQLDriverFactory sqlDriverFactory = new SQLDriverFactory(new HashMap<String, String>() {{
                put("file", config.get("database.file", "sb.sqlite"));
                put("host", config.get("database.host", "localhost"));
                put("port", config.get("database.port", "3306"));
                put("name", config.get("database.name", "app"));
                put("user", config.get("database.user", "root"));
                put("password", config.get("database.password", ""));
            }});

            Handler handler = new ConsoleHandler();
            handler.setLevel(Level.ALL);
            Logger.getLogger("ORM").addHandler(handler);
            Logger.getLogger("ORM").setLevel(Level.ALL);
            ORMConfig ormConfig = new ORMConfig()
                    .setTablePrefix("punyshort_")
                    .addTypeMapper(new AbstractDataTypeMapper());

            ORM.register(User.class.getPackage(), sqlDriverFactory.getDriver(config.get("database.driver", "none")), ormConfig);
            ORM.autoMigrate();
        } catch (SQLDriverNotFoundException | ORMConfigurationException e) {
            e.printStackTrace();
            System.exit(0);
        }

        linkFilters = Repo.get(LinkFilter.class).all().stream().map(filter -> Pattern.compile(filter.filter)).collect(Collectors.toList());
    }

    protected void setupServer() {
        httpServer.port(config.getInt("http.server.port", 80));

        if (config.has("http.server.cors")) {
            httpServer.beforeInterceptor(new CORSPolicy(config.get("http.server.cors")));
        }

        httpServer.beforeInterceptor(new MultipartPolicy(config.get("http.server.tmp", null)));
        httpServer.beforeInterceptor(e -> {
            e.header("Access-Control-Expose-Headers", "ETag");
            return false;
        });
        httpServer.responseTransformer(new SerializedResponseTransformer().ignoreStrings());

        oAuth2Strategy.getProviders().forEach((name, authService) -> {
            System.out.println("ADDED " + name + " as auth provider");
        });

        httpServer.exceptionHandler((exchange, throwable) -> {
            if (throwable instanceof AuthenticationException) {
                exchange.status(401);
            } else if (throwable instanceof NotFoundException) {
                exchange.status(404);
            } else {
                exchange.status(500);
            }
            return new ExceptionResponse(throwable);
        });
        httpServer.middleware("redirection-proxy", new RedirectionProxyMiddleware());
        httpServer.middleware("auth", new AuthMiddleware());
        httpServer.middleware("admin", new AdminMiddleware());


        httpServer.middleware("blocked-check", new BlockedMiddleware());


        if (getConfig().has("ratelimiter.millis")) {
            httpServer.middleware("rate-limiter", new RateLimitMiddleware(getConfig().getInt("ratelimiter.millis", 5000), getConfig().getInt("ratelimiter.limit", 5)).createAutoDeadRateLimitsRemover(1000 * 60 * 10));
        } else {
            httpServer.middleware("rate-limiter", e -> null);
        }

        httpServer.beforeInterceptor(exchange -> {
            exchange.header("Server", "InteraApps-Punyshort");

            exchange.attrib("loggedIn", false);
            exchange.attrib("user", null);
            String accessTokenKey = null;
            if (exchange.header("x-auth-key") != null)
                accessTokenKey = exchange.header("x-auth-key");
            if (exchange.bearerAuth() != null)
                accessTokenKey = exchange.bearerAuth();

            if (accessTokenKey != null) {
                AccessToken accessToken = Repo.get(AccessToken.class).where("key", accessTokenKey).first();
                if (oAuth2Strategy.getProviders().size() > 0 && accessToken != null) {
                    User user = Repo.get(User.class).get(accessToken.userId);

                    if (user != null) {
                        exchange.attrib("loggedIn", true);
                        exchange.attrib("user", user);
                        exchange.attrib("token", accessToken);
                    }
                }
            }

            return false;
        });


        httpServer.controller(HttpController.class, HttpController.class.getPackage());

        passport.createRoutes(httpServer);

        httpServer.get("/api/{*:path}", e -> {
            throw new NotFoundException();
        });
    }

    public void start() {
        httpServer.start();
    }

    public static void main(String[] args) {
        instance = new Punyshort();
        instance.start();
    }

    public Passport getPassport() {
        return passport;
    }

    public OAuth2Strategy getOAuth2Strategy() {
        return oAuth2Strategy;
    }

    public Config getConfig() {
        return config;
    }

    public List<Pattern> getLinkFilters() {
        return linkFilters;
    }
}
