package com.fealrias.purchasehistory.components.form;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Calendar;

@Getter
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private final MutableLiveData<LocalDate> dateResult;
    private final String TAG = this.getClass().getSimpleName();


    public DatePickerFragment(LocalDate date) {
        dateResult = new MutableLiveData<>(date != null ? date : LocalDate.now());
    }

    public DatePickerFragment(LocalDate date, LocalDate def) {
        dateResult = new MutableLiveData<>(date != null ? date : def);
    }

    public DatePickerFragment() {
        dateResult = new MutableLiveData<>();
    }

    @Override
    public @NotNull Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker.
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it.
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), this, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(c.toInstant().toEpochMilli());
        return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        this.dateResult.postValue(LocalDate.of(year, month + 1, day));
    }

    public void setValue(LocalDate date) {
        this.dateResult.postValue(date);
    }

    @Override
    public void show(@NonNull @NotNull FragmentManager manager, @Nullable String tag) {
        if (this.isAdded()) {
            Log.w(TAG, "Fragment already added");
            return;
        }
        super.show(manager, tag);
    }
}