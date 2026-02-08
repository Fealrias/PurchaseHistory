package com.fealrias.purchasehistory.data.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UsernamePassword {
    private String username;
    private String password;

    public RequestBody getRequestBody() {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", username)
                .addFormDataPart("password", password)
                .build();
    }
}
