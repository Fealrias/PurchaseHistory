package com.angelp.purchasehistory.ui.spectator.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.fragment.app.Fragment;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.databinding.FragmentSpectatorDashboardBinding;
import com.angelp.purchasehistory.ui.EmptyFragment;
import com.angelp.purchasehistory.ui.home.dashboard.DashboardFragment;
import com.angelp.purchasehistory.web.clients.ObserverClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@AndroidEntryPoint
public class SpectatorDashboardFragment extends Fragment {

    @Inject
    ObserverClient observerClient;
    private FragmentSpectatorDashboardBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSpectatorDashboardBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {
        new Thread(() -> {
            List<UserView> observedUsers = observerClient.getObservedUsers();
            ArrayAdapter<UserView> observedUserAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, observedUsers);
            binding.spectatorHomeUserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    UserView user = observedUsers.get(position);
                    changeObservedUser(user.getId());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    changeObservedUser(null);
                }
            });
            new Handler(Looper.getMainLooper()).post(() -> binding.spectatorHomeUserSpinner.setAdapter(observedUserAdapter));

        }).start();

    }

    private void changeObservedUser(UUID id) {
        if (id != null) {
            PurchaseFilter purchaseFilter = new PurchaseFilter();
            purchaseFilter.setUserId(id);
            if (binding.fragmentContainerView.getFragment() instanceof RefreshablePurchaseFragment) {
                RefreshablePurchaseFragment fragment = binding.fragmentContainerView.getFragment();
                fragment.refresh(purchaseFilter);
            } else {
                DashboardFragment dashboardFragment = new DashboardFragment();
                dashboardFragment.getArguments().putParcelable(Constants.Arguments.DASHBOARD_FILTER, purchaseFilter);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(binding.fragmentContainerView.getId(), dashboardFragment)
                        .commit();
            }
        } else {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(binding.fragmentContainerView.getId(), new EmptyFragment("User"))
                    .commit();
        }

    }
}