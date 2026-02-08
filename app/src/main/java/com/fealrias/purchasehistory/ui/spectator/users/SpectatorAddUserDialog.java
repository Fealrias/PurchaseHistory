package com.fealrias.purchasehistory.ui.spectator.users;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.fealrias.purchasehistory.PurchaseHistoryApplication;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.databinding.SpectatorAddUserDialogBinding;
import com.fealrias.purchasehistory.web.clients.ObserverClient;
import com.fealrias.purchasehistorybackend.models.views.outgoing.UserView;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.function.Consumer;

@Getter
@Setter
@AndroidEntryPoint
public class SpectatorAddUserDialog extends DialogFragment {
    private final String TAG = this.getClass().getSimpleName();

    @Inject
    ObserverClient observerClient;
    private SpectatorAddUserDialogBinding binding;
    private Consumer<UserView> onSuccess;

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = SpectatorAddUserDialogBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.BaseDialogStyle);
        binding.submit.setOnClickListener((view) -> {
            binding.submit.setEnabled(false);
            String token = binding.spectatorTokenInput.getText().toString();
            onSubmit(token);
        });
        binding.cancel.setOnClickListener((view) -> {
            resetForm();
            dismiss();
        });
        builder.setView(binding.getRoot());
        return builder.create();
    }

    private void onSubmit(String token) {
        if (token != null && !token.isBlank()) {
            new Thread(() -> {
                try {
                    UserView user = observerClient.addUser(token);
                    if (user != null) {
                        PurchaseHistoryApplication.getInstance().alert("Added new user to spectating list: " + user.getUsername());
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                resetForm();
                                if (onSuccess != null) onSuccess.accept(user);
                            });
                        }
                        dismiss();
                    } else
                        binding.spectatorTokenErrorText.setText(R.string.error_spectating_list_add_user);
                } catch (RuntimeException e) {
                    if ("EXPIRED_LINK".equals(e.getMessage())) {
                        binding.spectatorTokenErrorText.setText(R.string.error_spectating_list_add_user_expired);
                    } else
                        binding.spectatorTokenErrorText.setText(R.string.error_spectating_list_add_user);
                }
                new Handler(Looper.getMainLooper()).post(() -> binding.submit.setEnabled(true));
            }).start();
        }
    }

    private void resetForm() {
        binding.spectatorTokenInput.setText("");
        binding.spectatorTokenErrorText.setText("");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.dismiss();
    }

    @Override
    public void show(@NonNull @NotNull FragmentManager manager, @Nullable String tag) {
        if (this.isAdded()) {
            Log.w(TAG, "Fragment already added");
            return;
        }
        super.show(manager, tag);
    }
}
