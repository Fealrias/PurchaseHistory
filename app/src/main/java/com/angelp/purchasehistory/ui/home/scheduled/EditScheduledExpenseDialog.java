package com.angelp.purchasehistory.ui.home.scheduled;

import static com.angelp.purchasehistory.util.AndroidUtils.SCHEDULED_PERIOD_LIST;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.components.form.DatePickerFragment;
import com.angelp.purchasehistory.components.form.TimePickerFragment;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.databinding.DialogEditScheduledExpenseBinding;
import com.angelp.purchasehistory.ui.home.qr.CategorySpinnerAdapter;
import com.angelp.purchasehistory.util.AfterTextChangedWatcher;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.util.Utils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistory.web.clients.ScheduledExpenseClient;
import com.angelp.purchasehistorybackend.models.enums.ScheduledPeriod;
import com.angelp.purchasehistorybackend.models.views.incoming.ScheduledExpenseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditScheduledExpenseDialog extends DialogFragment {

    private final ScheduledExpenseView scheduledExpense;
    private final Consumer<ScheduledExpenseView> consumer;
    @Inject
    ScheduledExpenseClient scheduledExpenseClient;
    @Inject
    PurchaseClient purchaseClient;
    private ArrayAdapter<String> periodAdapter;
    private ArrayAdapter<String> currencyAdapter;
    private DialogEditScheduledExpenseBinding binding;
    private DatePickerFragment datePicker;
    private TimePickerFragment timePicker;
    private CategorySpinnerAdapter categoryAdapter;
    private List<CategoryView> categoryOptions;

    public EditScheduledExpenseDialog(ScheduledExpenseView scheduledExpense, Consumer<ScheduledExpenseView> consumer) {
        this.scheduledExpense = scheduledExpense;
        this.consumer = consumer;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = getLayoutInflater();
        binding = DialogEditScheduledExpenseBinding.inflate(layoutInflater);
        periodAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, SCHEDULED_PERIOD_LIST);
        currencyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, Constants.CURRENCY_LIST);
        View title = layoutInflater.inflate(R.layout.dialog_title, null);
        ((TextView) title.findViewById(R.id.dialogTitle)).setText(getString(R.string.edit_schedule_id, scheduledExpense.getId().toString()));

        setupDateTimeButtons();
        setupNameField();
        setupPriceField();
        setupCategoryOptions();
        setupCancelButton();
        setupSaveButton();
        return createDialog(title);
    }

    private void setupCategoryOptions() {
        new Thread(() -> {
            categoryOptions = purchaseClient.getAllCategories();
            categoryOptions.add(0, Constants.getDefaultCategory(requireContext()));
            categoryAdapter = new CategorySpinnerAdapter(requireContext(), categoryOptions);
            new Handler(Looper.getMainLooper()).post(() -> {
                binding.editScheduledExpenseSpinnerCategory.setAdapter(categoryAdapter);
                setupCategorySpinner();
            });

        }).start();
    }

    private void setupNameField() {
        binding.editScheduledExpenseEditTextName.setText(scheduledExpense.getNote());
        binding.editScheduledExpenseEditTextName.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                scheduledExpense.setNote(s.toString());
            }
        });
    }

    private void setupPriceField() {
        binding.editScheduledExpenseEditTextPrice.setText(String.valueOf(scheduledExpense.getPrice()));
        binding.editScheduledExpenseEditTextPrice.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String current = s.toString();
                if (Utils.isInvalidCurrency(current)) {
                    binding.editScheduledExpenseEditTextPrice.setError(getString(R.string.error_invalid_price));
                    binding.editScheduledExpenseSaveButton.setEnabled(false);
                } else {
                    if (current.trim().isEmpty())
                        scheduledExpense.setPrice(new BigDecimal(BigInteger.ZERO));
                    else scheduledExpense.setPrice(new BigDecimal(current));
                    binding.editScheduledExpenseSaveButton.setEnabled(true);
                    binding.editScheduledExpenseEditTextPrice.setError(null);
                }
            }
        });
    }

    private void setupCategorySpinner() {
        binding.editScheduledExpenseSpinnerCategory.setAdapter(categoryAdapter);
        binding.editScheduledExpenseSpinnerCategory.setSelection(getCategoryIndex(scheduledExpense.getCategory()));
        binding.editScheduledExpenseSpinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AndroidUtils.setNextTimestampString(binding.editScheduledExpenseTextViewNextDate, scheduledExpense);
                scheduledExpense.setCategory(categoryOptions.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.editScheduledExpenseSpinnerPeriod.setAdapter(periodAdapter);
        int i = !SCHEDULED_PERIOD_LIST.contains(scheduledExpense.getPeriod().toString()) ? 0 : SCHEDULED_PERIOD_LIST.indexOf(scheduledExpense.getPeriod().toString());
        binding.editScheduledExpenseSpinnerPeriod.setSelection(i);
        binding.editScheduledExpenseSpinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                scheduledExpense.setPeriod(ScheduledPeriod.valueOf(periodAdapter.getItem(position)));
                AndroidUtils.setNextTimestampString(binding.editScheduledExpenseTextViewNextDate, scheduledExpense);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.editScheduledExpenseSpinnerCurrency.setAdapter(currencyAdapter);
        binding.editScheduledExpenseSpinnerCurrency.setSelection(Constants.CURRENCY_LIST.indexOf(scheduledExpense.getCurrency()));
        binding.editScheduledExpenseSpinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                scheduledExpense.setCurrency(Constants.CURRENCY_LIST.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupDateTimeButtons() {
        datePicker = new DatePickerFragment(scheduledExpense.getTimestamp().toLocalDate());
        timePicker = new TimePickerFragment(scheduledExpense.getTimestamp().toLocalTime());
        binding.editScheduledExpenseButtonShowDate.setOnClickListener(v -> datePicker.show(getParentFragmentManager(), "datePicker"));
        binding.editScheduledExpenseButtonShowTime.setOnClickListener(v -> timePicker.show(getParentFragmentManager(), "timePicker"));
        datePicker.getDateResult().observe(this, (v) -> {
            LocalDateTime localDateTime = scheduledExpense.getTimestamp() != null ? scheduledExpense.getTimestamp() : LocalDateTime.now();
            scheduledExpense.setTimestamp(localDateTime.with(v));
            binding.editScheduledExpenseButtonShowDate.setText(v.format(DateTimeFormatter.ISO_LOCAL_DATE));
            AndroidUtils.setNextTimestampString(binding.editScheduledExpenseTextViewNextDate, scheduledExpense);
        });
        timePicker.getTimeResult().observe(this, (v) -> {
            LocalDateTime localDateTime = scheduledExpense.getTimestamp() != null ? scheduledExpense.getTimestamp() : LocalDateTime.now();
            scheduledExpense.setTimestamp(localDateTime.with(v));
            binding.editScheduledExpenseButtonShowTime.setText(v.format(DateTimeFormatter.ofPattern("HH:mm")));
            AndroidUtils.setNextTimestampString(binding.editScheduledExpenseTextViewNextDate, scheduledExpense);
        });
    }

    private void setupSaveButton() {
        binding.editScheduledExpenseSaveButton.setOnClickListener(v -> onSubmit(getDialog()));
    }

    private void setupCancelButton() {
        binding.editScheduledExpenseDismissButton.setOnClickListener(v -> this.dismiss());
    }

    private Dialog createDialog(View title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.BaseDialogStyle);
        builder.setView(binding.getRoot());
        builder.setCustomTitle(title);
        return builder.create();
    }

    private int getCategoryIndex(CategoryView category) {
        return categoryOptions.indexOf(category);
    }

    private void onSubmit(DialogInterface dialog) {
        if (scheduledExpense.getNote().isBlank() || binding.editScheduledExpenseEditTextPrice.getText().toString().isBlank()) {
            binding.editScheduledExpenseEditTextPrice.setError(getText(R.string.error_price_empty));
            return;
        }
        if (scheduledExpense.getPrice() == null || binding.editScheduledExpenseEditTextName.getText().toString().isBlank()) {
            binding.editScheduledExpenseEditTextName.setError(getText(R.string.error_must_not_be_empty));
            return;
        }
        try {
            new Thread(() -> {
                try {
                    ScheduledExpenseDTO dto = new ScheduledExpenseDTO();
                    dto.setCategoryId(scheduledExpense.getCategory().getId());
                    dto.setPrice(scheduledExpense.getPrice());
                    dto.setTimestamp(scheduledExpense.getTimestamp());
                    dto.setPeriod(scheduledExpense.getPeriod());
                    dto.setNote(scheduledExpense.getNote());
                    dto.setEnabled(scheduledExpense.isEnabled());
                    dto.setCurrency(scheduledExpense.getCurrency());
                    ScheduledExpenseView updatedExpense = scheduledExpenseClient.editScheduledExpense(dto, scheduledExpense.getId());
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (updatedExpense != null) {
                            consumer.accept(updatedExpense);
                            dialog.dismiss();
                        } else {
                            Log.i("EditScheduledExpenseDialog", "Failed to update scheduled expense");
                        }
                    });
                } catch (RuntimeException e) {
                    Log.i("EditScheduledExpenseDialog", "Validation failed: " + e.getMessage());
                }
            }).start();
        } catch (RuntimeException e) {
            Log.i("EditScheduledExpenseDialog", "Validation failed: " + e.getMessage());
        }
    }
}
