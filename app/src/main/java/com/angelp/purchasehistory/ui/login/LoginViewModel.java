package com.angelp.purchasehistory.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.model.LoginResult;
import com.angelp.purchasehistory.web.clients.AuthClient;
import com.angelp.purchasehistory.web.clients.WebException;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;

import java.util.Optional;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final AuthClient authClient;

    @Inject
    public LoginViewModel(AuthClient authClient) {
        this.authClient = authClient;
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public Runnable login(String username, String password) {
        return () -> {
            try {
                Optional<UserView> loggedUser = authClient.login(username, password);

                if (loggedUser.isPresent()) {
                    UserView userView = loggedUser.get();
                    PurchaseHistoryApplication.getInstance().loggedUser.postValue(userView);
                    loginResult.postValue(new LoginResult(userView));
                } else {
                    loginResult.postValue(new LoginResult(R.string.login_failed));
                }
            } catch (WebException e) {
                loginResult.postValue(new LoginResult(e.getErrorResource()));
            }
        };
    }

    public Runnable checkIfLoggedIn() {
        return () -> {
            try {
                Optional<UserView> loggedUser = authClient.getLoggedUser();
                if (loggedUser.isPresent()) {
                    UserView userView = loggedUser.get();
                    loginResult.postValue(new LoginResult(userView));
                }
            } catch (WebException ignored) {
            }
        };
    }
}