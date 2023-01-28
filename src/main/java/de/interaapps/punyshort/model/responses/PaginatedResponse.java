package de.interaapps.punyshort.model.responses;

import java.util.ArrayList;
import java.util.List;

public class PaginatedResponse<T> extends ActionResponse {
    public List<T> data = new ArrayList<>();
    private PaginationData pagination = new PaginationData();

    public PaginatedResponse(List<T> data, PaginationData pagination) {
        this.data = data;
        this.pagination = pagination;
    }
}
