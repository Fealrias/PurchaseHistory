package com.fealrias.purchasehistory.ui.home.scheduled;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.fealrias.purchasehistory.PurchaseHistoryApplication;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.Constants;
import com.fealrias.purchasehistory.data.model.ScheduledNotification;
import com.fealrias.purchasehistory.databinding.FragmentScheduledExpensesBinding;
import com.fealrias.purchasehistory.receivers.scheduled.NotificationHelper;
import com.fealrias.purchasehistory.web.clients.ScheduledExpenseClient;
import com.fealrias.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;
import com.google.gson.Gson;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

@AndroidEntryPoint
public class ScheduledExpensesFragment extends Fragment {

    public static final String CREATE_SCHEDULED_EXPENSE_DIALOG = "createScheduledExpenseDialog";
    private final Gson gson = new Gson();
    @Inject
    ScheduledExpenseClient scheduledExpenseClient;
    private FragmentScheduledExpensesBinding binding;
    private ScheduledExpenseAdapter adapter;
    private EditScheduledExpenseDialog editScheduledExpenseDialog;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentScheduledExpensesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (binding == null) return;
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        isLoading.observe(getViewLifecycleOwner(), loading -> {
            binding.loadingBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.recyclerViewScheduledExpenses.setVisibility(loading ? View.GONE : View.VISIBLE);
        });
        setupRecycleView();
        setupAddButton();
    }

    private void setupRecycleView() {
        new Thread(() -> {
            isLoading.postValue(true);
            List<ScheduledExpenseView> scheduledExpenses = scheduledExpenseClient.findAllForUser();

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            NotificationHelper.setupAllAlarms(requireContext(), scheduledExpenses);
            adapter = new ScheduledExpenseAdapter(scheduledExpenses, new ScheduledExpenseAdapter.OnItemClickListener() {

                @Override
                public void onSilenceToggleTrigger(ScheduledExpenseView item, boolean silenced) {
                    new Thread(() -> {
                        Context context = getContext();
                        if (context == null) return;
                        NotificationHelper.addNotificationAlarm(context, item, silenced);
                        String message = getString(silenced ? R.string.notification_silenced : R.string.notification_un_silenced, item.getNote());
                        PurchaseHistoryApplication.getInstance().alert(message);
                    }).start();
                }

                @Override
                public void onTriggerClick(ScheduledExpenseView item) {
                    new Thread(() -> sendToQRPage(item)).start();
                }

                @Override
                public void onEditClick(ScheduledExpenseView item) {
                    editScheduledExpenseDialog = new EditScheduledExpenseDialog(item, (view) -> {
                        int index = adapter.getScheduledExpenses().indexOf(view);
                        if (index == -1) return;
                        adapter.getScheduledExpenses().remove(index);
                        adapter.getScheduledExpenses().add(index, view);
                        adapter.notifyItemChanged(index, view);
                        NotificationHelper.reschedule(new ScheduledNotification(item), requireContext());
                    });
                    editScheduledExpenseDialog.show(getParentFragmentManager(), "editScheduledExpenseDialog");
                }

                @Override
                public void onDeleteClick(ScheduledExpenseView item) {
                    new Thread(() -> {
                        try {
                            Log.i("ScheduledExpense", "onDeleteClick: Deleting item" + item.getId());
                            boolean success = scheduledExpenseClient.deleteScheduledExpense(item.getId());
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (success) {
                                    int index = adapter.getScheduledExpenses().indexOf(item);
                                    NotificationHelper.deleteAlarm(requireContext(), item.getId());
                                    SharedPreferences preferences = requireContext().getSharedPreferences(Constants.Preferences.SILENCED_NOTIFICATIONS, MODE_PRIVATE);
                                    preferences.edit().remove(item.getId().toString()).apply();
                                    adapter.getScheduledExpenses().remove(index);
                                    adapter.notifyItemRemoved(index);
                                    binding.emptyScheduledExpenses.setVisibility(adapter.getScheduledExpenses().isEmpty() ? View.VISIBLE : View.GONE);
                                } else {
                                    PurchaseHistoryApplication.getInstance().alert("Error deleting scheduled expense.");
                                }
                            });

                        } catch (Exception e) {
                            PurchaseHistoryApplication.getInstance().alert("Error deleting scheduled expense: " + e.getMessage());
                        }
                    }).start();
                }
            });

            new Handler(Looper.getMainLooper()).post(() -> {
                binding.recyclerViewScheduledExpenses.setLayoutManager(linearLayoutManager);
                binding.recyclerViewScheduledExpenses.setAdapter(adapter);
                binding.emptyScheduledExpenses.setVisibility(scheduledExpenses.isEmpty() ? View.VISIBLE : View.GONE);
                isLoading.setValue(false);
            });
        }).start();
        binding.swiperefreshScheduledExpenses.setOnRefreshListener(() -> {
            refresh();
            binding.swiperefreshScheduledExpenses.setRefreshing(false);
        });
    }

    private void sendToQRPage(ScheduledExpenseView item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("scheduledNotification", new ScheduledNotification(item));
        NavOptions navOptions = new NavOptions.Builder().setLaunchSingleTop(false).build();
        new Handler(Looper.getMainLooper()).post(() -> NavHostFragment.findNavController(this).navigate(R.id.action_navigation_scheduled_expenses_to_navigation_qrscanner, bundle, navOptions));
    }

    private void refresh() {
        new Thread(() -> {
            isLoading.postValue(true);
            List<ScheduledExpenseView> allForUser = scheduledExpenseClient.findAllForUser();
            adapter.getScheduledExpenses().clear();
            adapter.getScheduledExpenses().addAll(allForUser);
            new Handler(Looper.getMainLooper()).post(() -> {
                adapter.notifyDataSetChanged();
                binding.emptyScheduledExpenses.setVisibility(allForUser.isEmpty() ? View.VISIBLE : View.GONE);
                isLoading.postValue(false);
            });
        }).start();
    }

    private void setupAddButton() {
        binding.fabAddScheduledExpense.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            } else {
                CreateScheduledExpenseDialog createDialog = new CreateScheduledExpenseDialog(this::acceptCreateScheduledExpenseResult);
                createDialog.show(getParentFragmentManager(), CREATE_SCHEDULED_EXPENSE_DIALOG);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CreateScheduledExpenseDialog createDialog = new CreateScheduledExpenseDialog(this::acceptCreateScheduledExpenseResult);
                createDialog.show(getParentFragmentManager(), CREATE_SCHEDULED_EXPENSE_DIALOG);
            } else {
                PurchaseHistoryApplication.getInstance().alert(R.string.notification_required_warning);
            }
        }
    }

    private void acceptCreateScheduledExpenseResult(ScheduledExpenseView newExpense) {
        NotificationHelper.addNotificationAlarm(getContext(), newExpense, false);
        adapter.getScheduledExpenses().add(newExpense);
        adapter.notifyItemInserted(adapter.getItemCount() - 1);
    }
}