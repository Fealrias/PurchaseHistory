package com.fealrias.purchasehistory.web;

import android.util.Log;

import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.web.clients.ErrorResponse;
import com.fealrias.purchasehistory.web.clients.WebException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class WebUtils {
    private final String TAG = WebUtils.class.getSimpleName();
    private final Gson gson;

    public WebUtils(Gson gson) {
        this.gson = gson;
    }

    public <T> T getBody(Response response, Class<T> clazz) {
        ResponseBody body = response.body();
        if (body == null) throw new WebException(R.string.error_web_response);
        try {
            String json = body.string();
            body.close();
            if (!response.isSuccessful()) {
                getError(json);
            }
            return getBody(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getBody(String json, Class<T> clazz) {
        Log.d(TAG, "Received response " + json);
        try {
            return gson.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            try {
                getError(json);
            } catch (JsonSyntaxException e1) {
                Log.e(TAG, "Failed to read response\n" + json);
                throw new WebException(R.string.error_web_response);
            }
        }
        return null;
    }

    private void getError(String json) {
        Log.w(TAG, "Error received from server. Proceeding to parse it");
        ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
        throw new WebException(errorResponse);
    }
}
