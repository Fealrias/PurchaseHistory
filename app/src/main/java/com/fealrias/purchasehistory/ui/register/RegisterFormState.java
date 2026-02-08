package com.fealrias.purchasehistory.ui.register;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class RegisterFormState {
    @Nullable
    private final Integer usernameError;
    @Nullable
    private final Integer passwordError;
    @Nullable
    private final Integer emailError;
    private final boolean isDataValid;

    RegisterFormState(@Nullable Integer usernameError, @Nullable Integer passwordError, @Nullable Integer emailError) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.emailError = emailError;
        this.isDataValid = false;
    }

    RegisterFormState(boolean isDataValid) {
        this.usernameError = null;
        this.passwordError = null;
        this.emailError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getUsernameError() {
        return usernameError;
    }

    @Nullable
    Integer getPasswordError() {
        return passwordError;
    }

    @Nullable
    Integer getEmailError() {
        return emailError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}