package com.fealrias.purchasehistory.ui.spectator.users;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistorybackend.models.views.outgoing.UserView;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UsersAdapter extends RecyclerView.Adapter<UsersViewHolder> {


    private final List<UserView> userViews = new ArrayList<>();


    public UsersAdapter(List<UserView> userViews) {
        setUserViews(userViews);
    }

    public void setUserViews(List<UserView> userViews) {
        this.userViews.clear();
        this.userViews.addAll(userViews);
    }

    @NonNull
    @NotNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_user, parent, false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UsersViewHolder holder, int position) {
        if (userViews.size() <= position) return;
        UserView userView = userViews.get(position);
        holder.bind(userView);
    }

    private void removeUser(int index) {
        this.userViews.remove(index);
        new Handler(Looper.getMainLooper()).post(() -> notifyItemRemoved(index));
    }

    @Override
    public int getItemCount() {
        return userViews.size();
    }

}
