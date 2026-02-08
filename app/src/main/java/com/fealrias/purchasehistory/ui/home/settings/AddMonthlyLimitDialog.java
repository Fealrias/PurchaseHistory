package com.fealrias.purchasehistory.ui.home.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.Constants;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistory.web.clients.SettingsClient;
import com.fealrias.purchasehistorybackend.models.views.incoming.MonthlyLimitDTO;
import com.fealrias.purchasehistorybackend.models.views.outgoing.MonthlyLimitView;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.function.Consumer;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddMonthlyLimitDialog extends DialogFragment {

    private final Consumer<MonthlyLimitView> monthlyLimitViewConsumer;
    @Inject
    SettingsClient settingsClient;

    public AddMonthlyLimitDialog(Consumer<MonthlyLimitView> consumer) {
        monthlyLimitViewConsumer = consumer;
    }

    @NotNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.BaseDialogStyle);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_monthly_limit, null);
        EditText limitValue = view.findViewById(R.id.editTextMonthlyLimit);
        Spinner currency = view.findViewById(R.id.monthlyCurrencySpinner);
        currency.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, Constants.CURRENCY_LIST));
        EditText limitLabel = view.findViewById(R.id.editTextMonthlyLimitLabel);
        builder.setView(view);
        View title = getLayoutInflater().inflate(R.layout.dialog_title, null);
        ((TextView) title.findViewById(R.id.dialogTitle)).setText(R.string.add_monthly_limit);
        builder.setCustomTitle(title);
        Button buttonSave = view.findViewById(R.id.buttonSave);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonSave.setOnClickListener((v) -> {
            try {
                AndroidUtils.validateNumber(limitValue.getText().toString());

                MonthlyLimitDTO monthlyLimitDTO = new MonthlyLimitDTO();
                monthlyLimitDTO.setValue(new BigDecimal(limitValue.getText().toString()));
                monthlyLimitDTO.setLabel(limitLabel.getText().toString());
                monthlyLimitDTO.setCurrency(currency.getSelectedItem().toString());
                new Thread(() -> {
                    MonthlyLimitView result = settingsClient.addMonthlyLimit(monthlyLimitDTO);
                    monthlyLimitViewConsumer.accept(result);
                    dismiss();
                }).start();

            } catch (IllegalArgumentException e) {
                limitValue.setError(getString(R.string.invalid_number));
            }
        });
        buttonCancel.setOnClickListener((v) -> {
            Dialog dialog = getDialog();
            if (dialog != null) dialog.cancel();
        });
        return builder.create();
    }
}
