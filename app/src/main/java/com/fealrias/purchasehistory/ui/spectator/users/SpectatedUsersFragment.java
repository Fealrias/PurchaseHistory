package com.fealrias.purchasehistory.ui.spectator.users;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.fealrias.purchasehistory.databinding.FragmentSpectatedUsersBinding;
import com.fealrias.purchasehistory.web.clients.ObserverClient;
import com.fealrias.purchasehistorybackend.models.views.outgoing.UserView;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.List;

@AndroidEntryPoint
public class SpectatedUsersFragment extends Fragment {
    @Inject
    ObserverClient observerClient;
    private FragmentSpectatedUsersBinding binding;
    private SpectatorAddUserDialog addUserDialog;
    private UsersAdapter usersAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSpectatedUsersBinding.inflate(inflater, container, false);
        init();

        return binding.getRoot();
    }

    private void init() {
        addUserDialog = new SpectatorAddUserDialog();

        addUserDialog.setOnSuccess((user) -> {
            int position = usersAdapter.getUserViews().size();
            usersAdapter.getUserViews().add(user);
            usersAdapter.notifyItemInserted(position);
        });
        binding.spectatorAddUser.setOnClickListener((v) -> addUserDialog.show(getParentFragmentManager(), "spectatorAddUser"));
        new Thread(() -> {
            List<UserView> users = observerClient.getObservedUsers();
            usersAdapter = new UsersAdapter(users);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            new Handler(Looper.getMainLooper()).post(() -> {
                binding.spectatedUsersList.setAdapter(usersAdapter);
                binding.spectatedUsersList.setLayoutManager(llm);
            });
        }).start();
    }
}