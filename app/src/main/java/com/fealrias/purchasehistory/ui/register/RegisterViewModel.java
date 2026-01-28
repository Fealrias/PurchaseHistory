package com.fealrias.purchasehistory.ui.register;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.model.LoginResult;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistory.web.clients.AuthClient;
import com.fealrias.purchasehistory.web.clients.WebException;
import com.fealrias.purchasehistorybackend.models.views.outgoing.UserView;

import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RegisterViewModel extends ViewModel {

    private final MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> registerResult = new MutableLiveData<>();
    final AuthClient authClient;

    @Inject
    public RegisterViewModel(AuthClient authClient) {
        this.authClient = authClient;

    }

    LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    public LiveData<LoginResult> getRegisterResult() {
        return registerResult;
    }

    public Runnable register(String username, String password, String email, Locale locale, String token) {
        return () -> {
            try {
                Optional<UserView> loggedUser = authClient.register(username, password, email, locale, token);
                if (loggedUser.isPresent()) {
                    UserView userView = loggedUser.get();
                    registerResult.postValue(new LoginResult(userView));
                } else {
                    registerResult.postValue(new LoginResult(R.string.register_failed));
                }
            } catch (WebException e) {
                registerResult.postValue(new LoginResult(e.getErrorResource()));
            }
        };
    }

    public Runnable checkIfLoggedIn() {
        return () -> {
            try {
                Optional<UserView> loggedUser = authClient.getLoggedUser();
                loggedUser.ifPresent(userView -> registerResult.postValue(new LoginResult(userView)));
            } catch (WebException ignored) {
            }
        };
    }

    public void registerDataChanged(String username, String password, String confirmPassword, String email) {
        if (AndroidUtils.isUserNameInvalid(username)) {
            registerFormState.setValue(new RegisterFormState(R.string.invalid_username, null, null));
        } else if (AndroidUtils.isPasswordInvalid(password)) {
            registerFormState.setValue(new RegisterFormState(null, R.string.invalid_password, null));
        } else if (AndroidUtils.isEmailInvalid(email)) {
            registerFormState.setValue(new RegisterFormState(null, null, R.string.invalid_email));
        }
        if (!password.equals(confirmPassword)) {
            registerFormState.setValue(new RegisterFormState(null, R.string.invalid_password_match, null));
        } else {
            registerFormState.setValue(new RegisterFormState(true));
        }
    }


}