package com.angelp.purchasehistory.ui.home.settings;

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

import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.SettingsClient;
import com.angelp.purchasehistorybackend.models.views.incoming.MonthlyLimitDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.MonthlyLimitView;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.Consumer;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditMonthlyLimitDialog extends DialogFragment {

    private final Long id;
    private final MonthlyLimitView monthlyLimitView;
    private final Consumer<MonthlyLimitView> monthlyLimitViewConsumer;
    @Inject
    SettingsClient settingsClient;

    public EditMonthlyLimitDialog(Long id, MonthlyLimitView monthlyLimit, Consumer<MonthlyLimitView> consumer) {
        this.id = id;
        monthlyLimitView = monthlyLimit;
        monthlyLimitViewConsumer = consumer;
    }

    @NotNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.BaseDialogStyle);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_monthly_limit, null);
        EditText limitValue = view.findViewById(R.id.editTextMonthlyLimit);
        EditText limitLabel = view.findViewById(R.id.editTextMonthlyLimitLabel);
        Spinner currency = view.findViewById(R.id.monthlyCurrencySpinner);
        currency.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, Constants.CURRENCY_LIST));
        String preferredCurrency = PurchaseHistoryApplication.getInstance().loggedUser.getValue().getPreferredCurrency();
        currency.setSelection(Constants.CURRENCY_LIST.indexOf(preferredCurrency));
        limitValue.setText(String.format(Locale.US,"%.2f",monthlyLimitView.getValue().floatValue()));
        limitLabel.setText(monthlyLimitView.getLabel());
        builder.setView(view);
        View title = getLayoutInflater().inflate(R.layout.dialog_title, null);
        ((TextView) title.findViewById(R.id.dialogTitle)).setText(R.string.edit_limit);
        builder.setCustomTitle(title);
        Button buttonSave = view.findViewById(R.id.buttonSave);
        Button buttonDelete = view.findViewById(R.id.buttonDelete);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonSave.setOnClickListener((v) -> {
            try {
                AndroidUtils.validateNumber(limitValue.getText().toString());
                MonthlyLimitDTO monthlyLimitDTO = new MonthlyLimitDTO();
                monthlyLimitDTO.setValue(new BigDecimal(limitValue.getText().toString()));
                monthlyLimitDTO.setLabel(limitLabel.getText().toString());
                monthlyLimitDTO.setCurrency(currency.getSelectedItem().toString());
                new Thread(() -> {
                    MonthlyLimitView monthlyLimitView1 = settingsClient.updateMonthlyLimit(id, monthlyLimitDTO);
                    monthlyLimitViewConsumer.accept(monthlyLimitView1);
                    dismiss();
                }).start();
            } catch (IllegalArgumentException e) {
                limitValue.setError(getString(R.string.invalid_number));
            }
        });
        buttonDelete.setOnClickListener((v) -> new Thread(() -> {
            settingsClient.deleteMonthlyLimit(id);
            monthlyLimitViewConsumer.accept(null);
            dismiss();
        }).start());
        buttonCancel.setOnClickListener((v) -> {
            Dialog dialog = getDialog();
            if (dialog != null) dialog.cancel();
        });
        return builder.create();
    }
}
