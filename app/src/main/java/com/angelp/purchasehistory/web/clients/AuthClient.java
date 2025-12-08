package com.angelp.purchasehistory.web.clients;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.model.UsernamePassword;
import com.angelp.purchasehistorybackend.models.views.incoming.ForgottenPasswordDTO;
import com.angelp.purchasehistorybackend.models.views.incoming.UserDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;

import okhttp3.Response;

public class AuthClient extends HttpClient {

    @Inject
    public AuthClient() {
    }

    private static void saveLoggedUser(String authorization) {
        SharedPreferences player = PurchaseHistoryApplication.getContext().getSharedPreferences("player", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = player.edit();
        editor.putString("jwt", authorization);
        Log.i("player_auth", authorization);
        editor.apply();
    }

    public Optional<UserView> login(String username, String password) {
        UserView result = null;

        try (Response res = postFormData(BACKEND_URL + "/login", new UsernamePassword(username, password).getRequestBody())) {
            String authorization = res.header("Authorization");
            Log.i("loginResult", "Response code: " + res.code());
            if (res.isSuccessful() && res.body() != null && authorization != null && !authorization.isEmpty()) {
                String body = res.body().string();
                Log.i("loginResult", "login successful: " + body);
                result = gson.fromJson(body, UserView.class);
                saveLoggedUser(authorization);
                PurchaseHistoryApplication.getInstance().getLoggedUser().postValue(result);
                PurchaseHistoryApplication.getInstance().getUserToken().postValue(authorization);
            } else {
                switch (res.code()) {
                    case 500:
                    case 501:
                    case 502:
                    case 503:
                        throw new WebException(R.string.server_something_went_wrong);
                    case 401:
                        throw new WebException(R.string.login_failed_401);
                    default:
                        throw new WebException(R.string.failed);
                }

            }
        } catch (IOException e) {
            Log.e("loginResult", "login:" + e.getMessage());
            if (e instanceof UnknownHostException hostException) {
                Log.e("Login", hostException.getMessage());
                throw new WebException(R.string.error_hostException);
            }
            else if (e instanceof SocketTimeoutException) {
                throw new WebException(R.string.server_connection_failed_long);
            }
        }
        return Optional.ofNullable(result);
    }

    public Optional<UserView> getLoggedUser() {
        UserView result = null;
        try (Response res = get(BACKEND_URL + "/users/self/get")) {
            if (res.isSuccessful() && res.body() != null) {
                String json = res.body().string();
                Log.d("httpResponse", "getLoggedUser: " + json);
                result = gson.fromJson(json, UserView.class);
                PurchaseHistoryApplication.getInstance().getLoggedUser().postValue(result);
                Log.d("jwt_valid", "JWT Valid, user is logged in");
            }
        } catch (IOException e) {
            Log.d("no_user", "JWT is invalid or missing. Redirected to login");
            if(e instanceof SocketTimeoutException) {
                throw new WebException(R.string.server_connection_failed_long);
            }
        }
        return Optional.ofNullable(result);
    }

    public void logout() {
        try (Response res = postFormData(BACKEND_URL + "/logout", new UsernamePassword("", "").getRequestBody())) {
            SharedPreferences player = PurchaseHistoryApplication.getContext().getSharedPreferences("player", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = player.edit();
            editor.clear(); //clear all stored data
            editor.apply();
        } catch (IOException e) {
            Log.i("logout", "logout: " + e.getMessage());
        }
    }

    public Optional<UserView> register(String username, String password, String email, Locale locale, String capchaToken) {
        try (Response res = post(BACKEND_URL + "/register", new UserDTO(username, password, email, locale.toLanguageTag(), capchaToken))) {
                return Optional.of(utils.getBody(res,UserView.class));
        } catch (IOException e) {
            Log.e("registerResult", "failed:" + e.getMessage());
            throw new WebException(R.string.server_connection_failed_500);
        }
    }


    public boolean forgotPassword(String email) {
        try (Response res = post(BACKEND_URL + "/forgot-password/email?email=" + email, null)) {
            String body = res.body() == null ? "" : res.body().string();
            Log.i("forgotPassword: ", "code: " + res.code() + " body:" + body);
            if (res.code() == 429) throw new WebException(R.string.tooManyRequest_429);
            if (!res.isSuccessful() && !body.isEmpty()) {
                ErrorResponse errorResponse = gson.fromJson(body, ErrorResponse.class);
                if ("NO_EMAIL_FOUND".equals(errorResponse.getDetail()))
                    throw new WebException(R.string.email_not_found_400);
            }
            return res.isSuccessful();
        } catch (IOException e) {
            Log.e("forgotPassword", "failed:" + e.getMessage());
            throw new WebException(R.string.server_connection_failed_500);
        }
    }

    public boolean changeForgotPassword(String token, String newPassword) {
        try (Response res = post(BACKEND_URL + "/forgot-password/change", new ForgottenPasswordDTO(token, newPassword))) {
            return res.isSuccessful();
        } catch (IOException e) {
            Log.e("forgotPassword", "failed:" + e.getMessage());
            throw new WebException(R.string.server_connection_failed_500);
        }
    }


}
