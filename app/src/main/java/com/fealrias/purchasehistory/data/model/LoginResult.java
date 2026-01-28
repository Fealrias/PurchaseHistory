package com.fealrias.purchasehistory.data.model;

import androidx.annotation.Nullable;
import com.fealrias.purchasehistorybackend.models.views.outgoing.UserView;

public class LoginResult {
    @Nullable
    private UserView success;
    @Nullable
    private Integer error;

    public LoginResult(@Nullable Integer error) {
        this.error = error;
    }

    public LoginResult(@Nullable UserView success) {
        this.success = success;
    }

    @Nullable
    public UserView getSuccess() {
        return success;
    }

    @Nullable
    public Integer getError() {
        return error;
    }

    public boolean isError() {
        return error != null;
    }
}