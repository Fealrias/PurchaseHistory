package com.fealrias.purchasehistory.ui.home.profile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.databinding.FragmentChangePasswordBinding;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistory.web.clients.UserClient;
import com.fealrias.purchasehistory.web.clients.WebException;
import com.fealrias.purchasehistorybackend.models.views.incoming.UpdatePasswordDTO;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class ChangePasswordFragment extends Fragment {
    @Inject
    UserClient userClient;
    private FragmentChangePasswordBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        binding.saveButton.setOnClickListener(v -> {
            String newPassword = binding.newPassword.getText().toString().trim();
            String newPasswordConfirm = binding.newPasswordConfirm.getText().toString().trim();
            String currentPassword = binding.currentPasswordEdit.getText().toString().trim();

            if (validateInputs(newPassword, newPasswordConfirm, currentPassword)) {
                new Thread(() -> attemptPasswordChange(newPassword, currentPassword)).start();
            }
        });

        return view;
    }

    private void attemptPasswordChange(String newPassword, String oldPassword) {
        try {
            Log.d("ChangePasswordFragment", "Attempting to change password");
            userClient.updatePassword(new UpdatePasswordDTO(oldPassword, newPassword));
            Log.i("ChangePasswordFragment", "Password changed successfully");
            AndroidUtils.showSuccessAnimation(getView());
            new Handler(Looper.getMainLooper()).post(() -> NavHostFragment.findNavController(this).popBackStack());
        } catch (WebException e) {
            Log.e("ChangePasswordFragment", "Password change failed: " + e.getMessage());
            showError(binding.currentPasswordEdit, getResources().getString(e.getErrorResource()));
        }

    }

    private boolean validateInputs(String newPassword, String newPasswordConfirm, String oldPassword) {
        if (!newPassword.isEmpty() && AndroidUtils.isPasswordInvalid(newPassword)) {
            showError(binding.newPassword, getResources().getString(R.string.invalid_password));
            return false;
        }
        if (!newPasswordConfirm.isEmpty() && AndroidUtils.isPasswordInvalid(newPasswordConfirm)) {
            showError(binding.newPasswordConfirm, getResources().getString(R.string.invalid_password));
            return false;
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            showError(binding.newPasswordConfirm, getResources().getString(R.string.invalid_password_match));
            return false;
        }
        if (!oldPassword.isEmpty() && AndroidUtils.isPasswordInvalid(oldPassword)) {
            showError(binding.currentPasswordEdit, getResources().getString(R.string.invalid_password));
            return false;
        }
        return true;
    }

    private void showError(EditText edit, String message) {
        new Handler(Looper.getMainLooper()).post(() -> edit.setError(message));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}