package com.fealrias.purchasehistory.ui.home.profile;

import static com.fealrias.purchasehistory.util.AndroidUtils.openCsvFile;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.fealrias.purchasehistory.PurchaseHistoryApplication;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.Constants;
import com.fealrias.purchasehistory.databinding.FragmentProfileBinding;
import com.fealrias.purchasehistory.ui.home.settings.SettingsActivity;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistory.web.clients.PurchaseClient;
import com.fealrias.purchasehistory.web.clients.UserClient;
import com.fealrias.purchasehistorybackend.models.views.outgoing.UserAnalytics;
import com.fealrias.purchasehistorybackend.models.views.outgoing.UserView;
import com.fealrias.purchasehistorybackend.models.views.outgoing.analytics.CategoryAnalyticsEntry;

import java.util.Optional;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    @Inject
    UserClient userClient;
    @Inject
    PurchaseClient purchaseClient;
    private FragmentProfileBinding binding;

    private final ActivityResultLauncher<Intent> downloadCSV = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() != null && result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    new Thread(() -> {
                        boolean success = purchaseClient.getExportedCsv(requireContext(), uri);
                        if (success) {
                            openCsvFile(requireActivity(), uri);
                        }

                    }).start();
                } else {
                    Log.i("DOWNLOAD", "downloadCSV failed");
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Set up user info preview
        UserView user = PurchaseHistoryApplication.getInstance().getLoggedUser().getValue(); // Assume this method fetches the user info
        if (user != null) {
            binding.username.setText(getString(R.string.username_param, user.getUsername()));
            binding.email.setText(getString(R.string.email_param, user.getEmail()));
        }
        new Thread(() -> {
            UserAnalytics userAnalytics = userClient.getUserAnalytics(Constants.getFilter30Days());
            requireActivity().runOnUiThread(() -> {
                binding.averageSpendingPerPurchase.setText(getString(R.string.average_spending_per_purchase, AndroidUtils.formatCurrency(userAnalytics.getAverageSpendingPerPurchase())));
                binding.totalSpending.setText(getString(R.string.total_spending, AndroidUtils.formatCurrency(userAnalytics.getTotalSpending())));
                binding.totalNumberOfPurchases.setText(getString(R.string.total_number_of_purchases, String.valueOf(userAnalytics.getTotalNumberOfPurchases())));
                CategoryAnalyticsEntry category = userAnalytics.getMostFrequentlyPurchasedCategory();
                if (category != null) {
                    int color = AndroidUtils.getColor(category.getCategory());
                    Drawable background = binding.mostFrequentlyPurchasedCategoryName.getBackground().mutate();
                    background.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                    binding.mostFrequentlyPurchasedCategoryName.setTextColor(requireContext().getColor(R.color.text));
                    binding.mostFrequentlyPurchasedCategoryName.setText(category.getCategory().getName());

                    binding.mostFrequentlyPurchasedCategoryCount.setText(getString(R.string.most_frequently_purchased_category_count, category.getCount().toString()));
                    binding.mostFrequentlyPurchasedCategorySum.setText(getString(R.string.most_frequently_purchased_category_sum, AndroidUtils.formatCurrency(category.getSum())));
                } else {
                    binding.categoryAnalytics.setVisibility(View.GONE);
                }
            });
        }).start();

        // Set up button click listeners
        NavController navController = NavHostFragment.findNavController(this);
        binding.editButton.setOnClickListener(v -> {
                    if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.navigation_profile)
                        navController.navigate(R.id.action_navigation_profile_to_navigation_edit_profile);
                }
        );
        // Set up button click listeners
        binding.btnChangePassword.setOnClickListener(v -> {
                    if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.navigation_profile)
                        navController.navigate(R.id.action_navigation_profile_to_navigation_change_password);
                }
        );
        binding.editCategoryButton.setOnClickListener((v) -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            intent.putExtra(Constants.Arguments.ACTIVITY_NAVIGATE_TO, Constants.SettingsLocations.EDIT_CATEGORY);
            startActivity(intent);
        });
        binding.downloadSvgButton.setOnClickListener(v -> downloadUserData());
        binding.shareLinkButton.setOnClickListener(v -> shareToken());
        binding.logoutButton.setOnClickListener(v -> AndroidUtils.logout(v.getContext(), purchaseClient));

        binding.settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        binding.deleteAccountButton.setOnClickListener(v -> showDeleteAccountConfirmation());

        return binding.getRoot();
    }

    private void shareToken() {
        new Thread(() -> {
            Optional<String> token = userClient.getReferralToken();
            token.ifPresent((t) -> AndroidUtils.shareString(t, "Sharing referral link for your purchase history", requireContext()));
        }).start();
    }


    private void downloadUserData() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "purchase_history_data.csv");
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        downloadCSV.launch(intent);
    }

    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(requireContext(), R.style.BaseDialogStyle)
                .setTitle(R.string.delete_account)
                .setMessage(R.string.delete_account_description)
                .setPositiveButton(R.string.yes, (dialog, which) -> new Thread(() -> {
                    boolean b = userClient.deleteAccount();
                    if (b) AndroidUtils.logout(getContext(), purchaseClient);
                }).start())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}