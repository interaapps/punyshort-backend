package de.interaapps.punyshort.helper;

import de.interaapps.punyshort.Punyshort;
import de.interaapps.punyshort.exceptions.PermissionsDeniedException;
import de.interaapps.punyshort.model.database.User;
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

    public static void queryFilter(Query<?> query, AbstractObject params) {
        Map<String, String> filters = new HashMap<>();
        params.forEach((key, value) -> {
            if ((key.startsWith("filter_") && key.endsWith("]")) || key.startsWith("filter%5B") && key.endsWith("%5D")) {
                filters.put(key
                                .replace("filter[", "")
                                .replace("]", "")
                                .replace("filter%5B", "")
                                .replace("%5D", ""),
                        value.string()
                );
            }
        });
        if (filters.size() > 0)
            query.filter(filters);
    }


    public static void orderBy(Query<?> query, Exchange exchange, String defaultValue, boolean descDefault) {
        query.order(exchange.query("order_by", defaultValue), exchange.query("order_desc", descDefault ? "true" : "false").equalsIgnoreCase("true"));
        System.out.println(exchange.query("order_by", defaultValue));
        System.out.println(exchange.query("order_desc", descDefault ? "true" : "false").equalsIgnoreCase("true"));
    }

    public static void defaultNavigation(Exchange exchange, Query<?> query) {
        query.search(exchange.query("search"));
        queryFilter(query, exchange.getQueryParameters());
    }
}
