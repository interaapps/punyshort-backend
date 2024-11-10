package de.interaapps.punyshort.helper;

import de.interaapps.punyshort.model.database.ShortenLink;
import de.interaapps.punyshort.model.database.ShortenLinkTag;
import de.interaapps.punyshort.model.responses.PaginationData;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.orm.query.Query;

import java.util.HashMap;
import java.util.Map;

public class RequestHelper {

    public static PaginationData pagination(Query<?> query, Exchange exchange) {
        int total = query.count();
        query.select();
        int page = 0;
        if (exchange.query("page") != null)
            page = Integer.parseInt(exchange.query("page")) - 1;

        int limit = 10;

        if (exchange.query("page_limit") != null)
            limit = exchange.query("page_limit", Integer.class);

        if (limit > 0) {
            query.limit(limit).offset(page * limit);
        }

        PaginationData paginationData = new PaginationData();
        paginationData.limit = limit;
        paginationData.total = total;
        paginationData.page = page + 1;
        return paginationData;
    }

    public static Map<String, String> getQueryFilter(AbstractObject params) {
        Map<String, String> filters = new HashMap<>();
        params.forEach((key, value) -> {
            if ((key.startsWith("filter_")) || key.startsWith("filter%5B") && key.endsWith("%5D") || key.startsWith("filter[") && key.endsWith("]")) {
                filters.put(key
                                .replace("filter[", "")
                                .replace("filter%5B", "")
                                .replace("%5D", "")
                                .replace("]", "")
                                .replace("filter_", "")
                                .replace("%5D", ""),
                        value.string()
                );
            }
        });
        return filters;
    }

    public static void queryFilter(Query<?> query, AbstractObject params) {
        Map<String, String> filters = getQueryFilter(params);

        if (!filters.isEmpty())
            query.filter(filters);
    }


    public static void orderBy(Query<?> query, Exchange exchange, String defaultValue, boolean descDefault) {
        query.order(exchange.query("order_by", defaultValue), exchange.query("order_desc", descDefault ? "true" : "false").equalsIgnoreCase("true"));
    }

    public static void defaultNavigation(Exchange exchange, Query<?> query) {
        query.search(exchange.query("search"));
        queryFilter(query, exchange.getQueryParameters());
    }

    public static void filterTags(Query<ShortenLink> query, AbstractObject params) {
        if (params.has("filter_tags")) {
            String[] filterTags = params.get("filter_tags").string().split(",");

            for (String filterTag : filterTags) {
                query.whereExists(ShortenLinkTag.class, (pasteTagQuery) -> pasteTagQuery.where("tag", filterTag).where(ShortenLinkTag.class, "linkId", "=", ShortenLink.class, "id"));
            }
        }
    }
}
