package com.fealrias.purchasehistory.data.filters;

import androidx.paging.PagingSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;


@AllArgsConstructor
@Getter
@Setter
public class PageRequest {
    Integer pageNumber;
    Integer pageSize;
    Sort sort;

    public PageRequest() {
        this.pageNumber = 0;
        this.pageSize = 10;
        this.sort = new Sort();
    }

    public PageRequest(PagingSource.LoadParams<Integer> params) {
        // Start refresh at page 0 if undefined.
        this.pageNumber = params.getKey() == null ? 0 : params.getKey();
        this.pageSize = params.getLoadSize();
        this.sort = new Sort();
    }

    public HttpUrl buildURL(String url) {
        HttpUrl parse = HttpUrl.parse(url);
        return parse.newBuilder()
                .addQueryParameter("pageNumber", pageNumber.toString())
                .addQueryParameter("pageSize", pageSize.toString())
                .addQueryParameter("sort", sort.asQueryParam()).build();
    }

    @Override
    public @NotNull String toString() {
        StringBuilder builder = new StringBuilder();
        if (pageNumber != null)
            builder.append("pageNumber=").append(pageNumber).append("&");
        if (pageSize != null)
            builder.append("pageSize=").append(pageSize).append("&");
        if (sort != null)
            builder.append(sort.asQueryParam()).append("&");
        return builder.toString();
    }
}
