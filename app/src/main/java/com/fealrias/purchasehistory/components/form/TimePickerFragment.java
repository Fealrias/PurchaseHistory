package com.fealrias.purchasehistory.components.form;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.util.Calendar;

@Getter
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private final MutableLiveData<LocalTime> timeResult;
    private final String TAG = this.getClass().getSimpleName();

    public TimePickerFragment(LocalTime timeResult) {
        this.timeResult = new MutableLiveData<>(timeResult != null ? timeResult : LocalTime.now());
    }

    public TimePickerFragment() {
        timeResult = new MutableLiveData<>();
    }

    @Override
    public @NotNull Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker.
        int hour;
        int minute;
        if (timeResult.getValue() != null) {
            hour = timeResult.getValue().getHour();
            minute = timeResult.getValue().getMinute();
        } else {
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        // Create a new instance of TimePickerDialog and return it.
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.timeResult.postValue(LocalTime.of(hourOfDay, minute));
    }

    public void setValue(LocalTime time) {
        this.timeResult.postValue(time);
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