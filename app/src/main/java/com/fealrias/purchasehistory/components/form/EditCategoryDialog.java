package com.fealrias.purchasehistory.components.form;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.data.model.Category;
import com.fealrias.purchasehistory.databinding.CategoryDialogBinding;
import com.fealrias.purchasehistory.util.AfterTextChangedWatcher;
import com.fealrias.purchasehistory.util.AndroidUtils;
import com.fealrias.purchasehistory.web.clients.PurchaseClient;
import com.fealrias.purchasehistory.web.clients.WebException;
import com.fealrias.purchasehistorybackend.models.views.incoming.CategoryDTO;
import com.fealrias.purchasehistorybackend.models.views.outgoing.CategoryView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditCategoryDialog extends DialogFragment {
    private final String TAG = this.getClass().getSimpleName();
    @Inject
    PurchaseClient purchaseClient;
    final List<String> defaultCategories = List.of("Groceries", "Medicine", "Travel", "Gifts", "Fast food", "Hobbies", "Bills", "Investments", "Essentials");
    final List<String> defaultColors = List.of("#FADADD", "#AECBFA", "#D4EED1", "#FFF9C4", "#E6E6FA", "#FFDAB9", "#F5FFFA", "#F08080");
    private CategoryView defaultValue;
    private Long categoryId;
    private Consumer<CategoryView> consumer;
    private Runnable onDelete;

    private CategoryDialogBinding binding;

    public EditCategoryDialog() {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseDialogStyle);
    }

    public EditCategoryDialog(Long id, CategoryView defaultValue, Consumer<CategoryView> categoryViewProvider, Runnable onDelete) {
        this.categoryId = id;
        this.defaultValue = defaultValue;
        this.consumer = categoryViewProvider;
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseDialogStyle);
        this.onDelete = onDelete;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = getLayoutInflater();
        binding = CategoryDialogBinding.inflate(layoutInflater);
        View titleView = layoutInflater.inflate(R.layout.dialog_title, null, false);
        TextView title = titleView.findViewById(R.id.dialogTitle);
        fillNameAutocomplete();
        binding.colorPickerView.setHueSliderView(binding.hueSlider);
        binding.colorPickerView.setOnColorChangedListener((color) -> {
            if (binding.colorPickerView.hasWindowFocus()) {
                binding.categoryColorInput.setText(String.format("#%06X", (0xFFFFFF & color)));
            }
        });
        binding.categoryColorInput.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String color = s.toString();
                if (color.trim().isEmpty() || !color.startsWith("#") || color.trim().length() != 7) {
                    binding.colorBlob.setBackgroundColor(Color.WHITE);
                    return;
                }
                int colorValue = Color.parseColor(color);
                binding.colorBlob.setBackgroundColor(colorValue);
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.BaseDialogStyle);
        title.setText(getString(R.string.edit_category, categoryId.toString()));
        builder.setCustomTitle(titleView);
        builder.setView(binding.getRoot());
        binding.saveButton.setOnClickListener(v -> {
            String name = binding.categoryNameInput.getText().toString();
            String color = binding.categoryColorInput.getText().toString();
            boolean isValid = AndroidUtils.validateCategoryValues(binding.categoryNameInput, binding.categoryColorInput);
            if (isValid) {
                new Thread(() -> {
                    try {
                        Category category = purchaseClient.editCategory(categoryId.intValue(), new CategoryDTO(name, color));
                        defaultValue.setColor(category.getColor());
                        defaultValue.setName(category.getName());
                        consumer.accept(category);
                        dismiss();
                    } catch (WebException e) {
                        new Handler(Looper.getMainLooper()).post(() -> binding.categoryErrorText.setText(e.getErrorResource()));
                    }
                }).start();
            }
        });
        binding.deleteButton.setOnClickListener(v -> showDeleteAlertConfirmation());
        binding.cancelButton.setOnClickListener(v -> dismiss());
        updateUI(defaultValue);
        return builder.create();
    }

    private void showDeleteAlertConfirmation() {
        View title = getLayoutInflater().inflate(R.layout.dialog_title, null);
        ((TextView) title.findViewById(R.id.dialogTitle)).setText(R.string.delete_category);
        new AlertDialog.Builder(requireContext(), R.style.BaseDialogStyle)
                .setCustomTitle(title)
                .setMessage(getString(R.string.delete_category_confirmation, defaultValue.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> new Thread(() -> {
                    purchaseClient.deleteCategory(categoryId);
                    onDelete.run();
                }).start())

                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .show();
    }

    private void updateUI(CategoryView defaultValue) {
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.categoryColorInput.setText(defaultValue.getColor());
            binding.categoryNameInput.setText(defaultValue.getName());
        });
    }

    private void fillNameAutocomplete() {
        new Thread(() -> {
            List<CategoryView> allCategories = purchaseClient.getAllCategories();

            List<String> categoryList = new ArrayList<>();
            List<String> colorList = new ArrayList<>();
            for (String defaultCategory : defaultCategories) {
                if (allCategories.stream().noneMatch(a -> defaultCategory.equals(a.getName()))) {
                    categoryList.add(defaultCategory);
                }
            }
            for (String defaultColor : defaultColors) {
                if (allCategories.stream().noneMatch(a -> defaultColor.equals(a.getColor()))) {
                    colorList.add(defaultColor);
                }
            }
            ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, categoryList);
            new Handler(Looper.getMainLooper()).post(() -> binding.categoryNameInput.setAdapter(nameAdapter));
            ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, colorList);
            new Handler(Looper.getMainLooper()).post(() -> binding.categoryColorInput.setAdapter(colorAdapter));
        }).start();
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
