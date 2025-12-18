package com.angelp.purchasehistory.web.gcloud.GCloudService;

import android.content.Context;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CredentialManagerImpl;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class GCloudService {
    private static final String CLIENT_ID="1045435891626-k0sp8t916i6p2edk6ab8pu0q241r6q0s.apps.googleusercontent.com";
    private final GetGoogleIdOption getGoogleIdOption;
    private final CredentialManager credentialManager;
    @Inject
    public GCloudService(@ApplicationContext Context context) {
        credentialManager = new CredentialManagerImpl(context);
        String nonce = getNonce();
        getGoogleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(CLIENT_ID)
                .setAutoSelectEnabled(true)
                // nonce string to use when generating a Google ID token
                .setNonce(nonce)
                .build();
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(getGoogleIdOption)
                .build();

        ExecutorService executor = Executors.newSingleThreadExecutor();

        credentialManager.getCredentialAsync(
                context,
                request,
                new CancellationSignal(),  // Optional CancellationSignal
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        // Handle error (e.g., no credentials, user canceled)
                    }
                }
        );
    }
    private void handleSignIn(GetCredentialResponse result) {
        Credential credential = result.getCredential();

        if (credential instanceof CustomCredential &&
                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {

            try {
                GoogleIdTokenCredential googleCredential =
                        GoogleIdTokenCredential.createFrom(credential.getData());

                String idToken = googleCredential.getIdToken();
                // https://developers.google.com/identity/gsi/web/guides/verify-google-id-token

            } catch (GoogleIdTokenParsingException e) {
                // Invalid token
            }
        } else {
            // Unexpected credential type
        }
    }
    @NonNull
    private static String getNonce() {

    }

}
