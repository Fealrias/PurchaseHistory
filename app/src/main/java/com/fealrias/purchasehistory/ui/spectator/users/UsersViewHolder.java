package com.fealrias.purchasehistory.ui.spectator.users;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fealrias.purchasehistory.databinding.RecyclerViewUserBinding;
import com.fealrias.purchasehistorybackend.models.views.outgoing.UserView;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class UsersViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = this.getClass().getSimpleName();

    private final RecyclerViewUserBinding binding;

    public UsersViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        binding = RecyclerViewUserBinding.bind(itemView);
    }

    public void bind(UserView purchaseView) {
        binding.userUsernameText.setText(purchaseView.getUsername());
        binding.userEmailText.setText(purchaseView.getEmail());
        binding.userEditButton.setEnabled(false);
    }


}
