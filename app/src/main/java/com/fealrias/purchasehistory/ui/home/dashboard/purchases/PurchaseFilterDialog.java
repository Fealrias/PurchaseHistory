package com.fealrias.purchasehistory.ui.home.dashboard.purchases;

import static com.fealrias.purchasehistory.data.Constants.getDefaultFilter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.components.form.DatePickerFragment;
import com.fealrias.purchasehistory.data.Constants;
import com.fealrias.purchasehistory.data.filters.PurchaseFilter;
import com.fealrias.purchasehistory.data.filters.PurchaseFilterSingleton;
import com.fealrias.purchasehistory.data.filters.TimeButton;
import com.fealrias.purchasehistory.databinding.FragmentPurchaseFilterDialogBinding;
import com.fealrias.purchasehistory.ui.home.qr.CategorySpinnerAdapter;
import com.fealrias.purchasehistory.web.clients.PurchaseClient;
import com.fealrias.purchasehistorybackend.models.views.outgoing.CategoryView;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AndroidEntryPoint
public class PurchaseFilterDialog extends DialogFragment {
    private final String TAG = this.getClass().getSimpleName();
    private boolean containCategory;

    @Inject
    PurchaseClient purchaseClient;
    @Inject
    PurchaseFilterSingleton filterViewModel;

    private FragmentPurchaseFilterDialogBinding binding;
    private DatePickerFragment datePickerFrom;
    private DatePickerFragment datePickerTo;
    private ArrayAdapter<CategoryView> categoryAdapter;
    private PurchaseFilter filter;
    private List<CategoryView> categoryOptions = new ArrayList<>();
    private List<TimeButton> timeButtons = new ArrayList<>();

    public PurchaseFilterDialog() {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseDialogStyle);
    }

    public PurchaseFilterDialog(boolean containCategory) {
        this.containCategory = containCategory;
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseDialogStyle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(getTag(), "onCreateView: View created");
        if (savedInstanceState != null)
            containCategory = savedInstanceState.getBoolean("containCategory");
        binding = FragmentPurchaseFilterDialogBinding.inflate(inflater, container, false);
        filterViewModel.getFilter().observe(getViewLifecycleOwner(), this::updateFilter);
        updateFilter(filterViewModel.getFilterValue());
        setupDatePickers();
        setupCategorySpinner(containCategory);
        fillEditForm(filter);
        binding.purchaseFilterSubmitButton.setOnClickListener((view) -> {
            filterViewModel.updateFilter(filter);
            this.dismiss();
        });
        binding.dialogTitle.dialogTitle.setText(R.string.filterTitle);
        return binding.getRoot();
    }

    private void setupDatePickers() {
        datePickerFrom = new DatePickerFragment();
        datePickerTo = new DatePickerFragment();
        datePickerFrom.getDateResult().observe(getViewLifecycleOwner(), (v) -> {
            filter.setFrom(v);
            updateSelectedTimeButton();
            binding.purchaseFilterFromDate.setText(v.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
        });
        datePickerTo.getDateResult().observe(getViewLifecycleOwner(), (v) -> {
            filter.setTo(v);
            updateSelectedTimeButton();
            binding.purchaseFilterToDate.setText(v.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
        });
        binding.purchaseFilterClearButton.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().runOnUiThread(this::resetForm);
            updateSelectedTimeButton();
        });
        binding.purchaseFilterFromDate.setOnClickListener((v) -> datePickerFrom.show(getParentFragmentManager(), "datePickerFrom"));
        binding.purchaseFilterToDate.setOnClickListener((v) -> datePickerTo.show(getParentFragmentManager(), "datePickerTo"));
        timeButtons.clear();
        timeButtons.add(new TimeButton(binding.filterWeek, LocalDate.now().minusDays(7)));
        timeButtons.add(new TimeButton(binding.filterMonth, LocalDate.now().withDayOfMonth(1)));
        timeButtons.add(new TimeButton(binding.filter3month, LocalDate.now().minusMonths(3).withDayOfMonth(1)));
        timeButtons.add(new TimeButton(binding.filter6month, LocalDate.now().minusMonths(6).withDayOfMonth(1)));
        timeButtons.add(new TimeButton(binding.filterYear, LocalDate.now().withMonth(1).withDayOfMonth(1)));
        timeButtons.add(new TimeButton(binding.filterLastYear, LocalDate.now().minusYears(1).withMonth(1).withDayOfMonth(1)));
        for (TimeButton timeButton : timeButtons) {
            setSelectedButton(timeButton, filter.getFrom(), filter.getTo());
            timeButton.getButton().setOnClickListener((v) -> quickUpdateFilter(timeButton.getFrom()));
        }

    }

    private void updateSelectedTimeButton() {
        for (TimeButton timeButton : timeButtons) {
            setSelectedButton(timeButton, filter.getFrom(), filter.getTo());
        }
    }

    private void setSelectedButton(TimeButton timeButton, LocalDate from, LocalDate to) {
        if (from.equals(timeButton.getFrom()) && to.equals(LocalDate.now())) {
            timeButton.getButton().getBackground().mutate().setTint(requireContext().getColor(R.color.primaryA50));
        } else {
            timeButton.getButton().getBackground().setTintList(null);
        }
    }

    /**
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("containCategory", this.containCategory);
    }

    private void quickUpdateFilter(LocalDate from) {
        quickUpdateFilter(from, LocalDate.now());
    }

    private void quickUpdateFilter(LocalDate from, LocalDate to) {
        boolean toHasChanged = !filter.getTo().equals(to);
        boolean fromHasChanged = !filter.getFrom().equals(from);
        datePickerFrom.setValue(from);
        filter.setFrom(from);
        datePickerTo.setValue(to);
        filter.setTo(to);
        Animation shake = AnimationUtils.loadAnimation(this.getContext(), R.anim.jump);

        if (fromHasChanged) binding.purchaseFilterFromDate.startAnimation(shake);
        if (toHasChanged) binding.purchaseFilterToDate.startAnimation(shake);
    }

    private void setupCategorySpinner(boolean containCategory) {
        if (!containCategory) {
            binding.purchaseFilterCategorySpinner.setVisibility(View.GONE);
            return;
        }
        new Thread(() -> {
            categoryOptions = purchaseClient.getAllCategories();
            categoryOptions.add(0, Constants.getDefaultCategory(requireContext()));
            categoryAdapter = new CategorySpinnerAdapter(requireContext(), categoryOptions);
            new Handler(Looper.getMainLooper()).post(() -> {
                binding.purchaseFilterCategorySpinner.setAdapter(categoryAdapter);
                updateFilter(filterViewModel.getFilterValue());
            });
        }).start();
        binding.purchaseFilterCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CategoryView categoryView = categoryOptions.get(position);
                filter.setCategory(categoryView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filter.setCategory(null);
            }
        });
    }


    private void fillEditForm(PurchaseFilter view) {
        new Thread(() -> {
            if (view.getFrom() != null) {
                datePickerFrom.getDateResult().postValue(view.getFrom());
            }
            if (view.getTo() != null) {
                datePickerTo.getDateResult().postValue(view.getTo());
            }
            if (view.getCategoryId() != null) {
                for (int i = 0; i < categoryOptions.size(); i++) {
                    if (view.getCategoryId().equals(categoryOptions.get(i).getId())) {
                        binding.purchaseFilterCategorySpinner.setSelection(i, true);
                        break;
                    }
                }
            }
        }).start();

    }

    private void resetForm() {
        this.updateFilter(getDefaultFilter());
        binding.purchaseFilterCategorySpinner.setSelection(0);
        binding.purchaseFilterToDate.setText(R.string.to);
        binding.purchaseFilterFromDate.setText(R.string.from);
    }

    @Override
    public void show(@NonNull @NotNull FragmentManager manager, @Nullable String tag) {
        if (this.isAdded()) {
            Log.w(TAG, "Fragment already added");
            return;
        }
        super.show(manager, tag);
    }

    private void updateFilter(PurchaseFilter observedFilter) {
        filter = observedFilter;
        if (filter.getCategoryId() == null || categoryAdapter == null) {
            return;
        }
        for (int i = 0; i < categoryAdapter.getCount(); i++) {
            CategoryView entry = categoryAdapter.getItem(i);
            if (entry.getId() != null && entry.getId().equals(filter.getCategoryId())) {
                binding.purchaseFilterCategorySpinner.setSelection(i);
                return;
            }
        }
    }
}
