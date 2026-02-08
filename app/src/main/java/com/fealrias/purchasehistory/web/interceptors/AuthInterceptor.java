package com.fealrias.purchasehistory.web.interceptors;

import com.fealrias.purchasehistory.PurchaseHistoryApplication;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AuthInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        String jwt = PurchaseHistoryApplication.getInstance().getUserToken().getValue();
        Request.Builder request = chain.request().newBuilder();
        if (jwt != null && !jwt.isEmpty()) {
            String prefix = "Bearer ";
            request.addHeader("Authorization", prefix + jwt);
        }
        return chain.proceed(request.build());

    }
}
