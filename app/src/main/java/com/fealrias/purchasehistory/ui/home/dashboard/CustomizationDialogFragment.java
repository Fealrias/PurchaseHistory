package com.fealrias.purchasehistory.ui.home.dashboard;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.model.DashboardComponent;
import com.fealrias.purchasehistory.databinding.DialogCustomizationBinding;

import java.util.List;
import java.util.function.Consumer;

public class CustomizationDialogFragment extends DialogFragment {
    private final List<DashboardComponent> selectedFragments;
    private final Consumer<List<DashboardComponent>> onSave;

    public CustomizationDialogFragment(List<DashboardComponent> selectedFragments, Consumer<List<DashboardComponent>> onSave) {
        this.selectedFragments = selectedFragments;
        setStyle(STYLE_NO_TITLE, R.style.BaseDialogStyle);
        this.onSave = onSave;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        com.fealrias.purchasehistory.databinding.DialogCustomizationBinding binding = DialogCustomizationBinding.inflate(inflater, container, false);
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        CustomizationAdapter adapter = new CustomizationAdapter(selectedFragments);
        recyclerView.setAdapter(adapter);

        Button saveButton = binding.saveButton;
        saveButton.setOnClickListener(v -> {
            onSave.accept(adapter.getFragments());
            dismiss();
        });

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
