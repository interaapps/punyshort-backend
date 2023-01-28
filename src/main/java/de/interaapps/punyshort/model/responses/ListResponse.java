package de.interaapps.punyshort.model.responses;

import java.util.ArrayList;
import java.util.List;

public class ListResponse<T> extends ActionResponse {
    public List<T> data = new ArrayList<>();

    public ListResponse(List<T> data) {
        this.data = data;
    }
}
