package com.fealrias.purchasehistory.data.filters;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class Sort {
    List<String> properties;
    SortDirection direction;

    public Sort() {
        properties = new ArrayList<>();
        direction = SortDirection.DESC;
    }

    public String asQueryParam() {
        StringBuilder str = new StringBuilder("direction=" + direction);
        if (properties != null)
            for (String property : properties) {
                str.append("property=").append(property);
            }
        return str.toString();
    }
}
