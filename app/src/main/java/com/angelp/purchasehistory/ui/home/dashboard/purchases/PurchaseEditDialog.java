package com.angelp.purchasehistory.ui.home.dashboard.purchases;

import static com.angelp.purchasehistory.data.Constants.Arguments.PURCHASE_EDIT_DIALOG_ID_KEY;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.components.form.CreateCategoryDialog;
import com.angelp.purchasehistory.components.form.DatePickerFragment;
import com.angelp.purchasehistory.components.form.TimePickerFragment;
import com.angelp.purchasehistory.data.filters.PurchaseFilterSingleton;
import com.angelp.purchasehistory.databinding.FragmentPurchaseEditDialogBinding;
import com.angelp.purchasehistory.ui.home.qr.CategorySpinnerAdapter;
import com.angelp.purchasehistory.util.AfterTextChangedWatcher;
import com.angelp.purchasehistory.util.Utils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AndroidEntryPoint
public class PurchaseEditDialog extends DialogFragment {
    private final String TAG = this.getClass().getSimpleName();

    @Inject
    PurchaseClient purchaseClient;
    @Inject
    PurchaseFilterSingleton filterViewModel;

    private PurchaseDTO purchase;
    private FragmentPurchaseEditDialogBinding binding;
    private TimePickerFragment timePicker;
    private DatePickerFragment datePicker;
    private CreateCategoryDialog categoryDialog;
    private CategorySpinnerAdapter categoryAdapter;
    private List<CategoryView> allCategories = new ArrayList<>();
    private Consumer<PurchaseView> onSuccess;
    private Long purchaseId;

    public PurchaseEditDialog() {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseDialogStyle);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(getTag(), "onCreateView: View created");
        binding = FragmentPurchaseEditDialogBinding.inflate(inflater, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.purchaseId = bundle.getLong(PURCHASE_EDIT_DIALOG_ID_KEY, 0L);
            binding.title.dialogTitle.setText(getString(R.string.edit_purchase_id, purchaseId.toString()));
        }
        timePicker = new TimePickerFragment(purchase.getTime());
        datePicker = new DatePickerFragment(purchase.getDate());
        categoryDialog = new CreateCategoryDialog((newCategory) -> new Handler(Looper.getMainLooper()).post(() -> {
            if (newCategory != null) {
                categoryAdapter.add(newCategory);
                binding.purchaseEditCategorySpinner.setSelection(categoryAdapter.getPosition(newCategory));
            }
        }));

        timePicker.getTimeResult().observe(getViewLifecycleOwner(), (v) -> {
            purchase.setTime(v);
            binding.purchaseEditTimeInput.setText(v.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)));
        });
        datePicker.getDateResult().observe(getViewLifecycleOwner(), (v) -> {
            purchase.setDate(v);
            binding.purchaseEditDateInput.setText(v.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
        });
        binding.purchaseEditClearButton.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().runOnUiThread(this::resetForm);
        });
        binding.purchaseEditSaveButton.setOnClickListener((view) -> {
            Log.i(getTag(), "Edit button clicked");
            onSubmit(purchase, purchaseId);
        });
        binding.purchaseEditDeleteButton.setOnClickListener((view) -> new Thread(() -> sendDelete(purchaseId)).start());
        binding.purchaseEditTimeInput.setOnClickListener((v) -> timePicker.show(getParentFragmentManager(), "timePicker"));
        binding.purchaseEditDateInput.setOnClickListener((v) -> datePicker.show(getParentFragmentManager(), "datePicker"));
        binding.purchaseEditCategoryAddButton.setOnClickListener((v) -> categoryDialog.show(getParentFragmentManager(), "createCategoryDialog"));
        new Thread(() -> {
            allCategories = purchaseClient.getAllCategories();
            categoryAdapter = new CategorySpinnerAdapter(requireContext(), allCategories);
            requireActivity().runOnUiThread(() -> {
                binding.purchaseEditCategorySpinner.setAdapter(categoryAdapter);
                int index = Utils.findIndex(allCategories, (category) -> category.getId() != null && category.getId().equals(purchase.getCategoryId()));
                if (index >= 0)
                    binding.purchaseEditCategorySpinner.setSelection(index);
            });
        }).start();
        binding.purchaseEditPriceInput.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String str = binding.purchaseEditPriceInput.getText().toString();
                if (Utils.isInvalidCurrency(str)) {
                    binding.purchaseEditPriceInput.setError(getString(R.string.error_invalid_price));
                    binding.purchaseEditSaveButton.setEnabled(false);
                } else {
                    if (str.trim().isEmpty()) purchase.setPrice(new BigDecimal(BigInteger.ZERO));
                    else purchase.setPrice(new BigDecimal(str));
                    binding.purchaseEditSaveButton.setEnabled(true);
                    binding.purchaseEditPriceInput.setError(null);
                }
            }
        });
        binding.purchaseEditNoteInput.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (binding.purchaseEditNoteInput.hasFocus()) {
                    String str = binding.purchaseEditNoteInput.getText().toString();
                    purchase.setNote(str);
                }
            }
        });
        binding.purchaseEditCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CategoryView categoryView = allCategories.get(position);
                purchase.setCategoryId(categoryView.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                purchase.setCategoryId(null);
            }
        });
        fillEditForm(purchase);
        return binding.getRoot();
    }

    private void sendDelete(Long purchaseId) {
        boolean isSuccess = purchaseClient.deletePurchase(purchaseId);
        if (isSuccess) {
            filterViewModel.refresh();
            dismiss();
        }
    }

    private void fillEditForm(PurchaseDTO view) {
        new Thread(() -> {
            if (view.getPrice() != null)
                binding.purchaseEditPriceInput.setText(String.format(view.getPrice().toString()));
            if (view.getTimestamp() != null) {
                timePicker.setValue(view.getTime());
                datePicker.setValue(view.getDate());
            }
            if (view.getBillId() != null)
                binding.purchaseEditBillIdValue.setText(view.getBillId());
            if (view.getStoreId() != null)
                binding.purchaseEditStoreIdValue.setText(view.getStoreId());
            if (view.getNote() != null)
                binding.purchaseEditNoteInput.setText(view.getNote());
            if (view.getCategoryId() != null) {
                for (int i = 0; i < allCategories.size(); i++) {
                    if (view.getCategoryId().equals(allCategories.get(i).getId())) {
                        binding.purchaseEditCategorySpinner.setSelection(i);
                        break;
                    }
                }
            }
        }).start();

    }


    private void onSubmit(PurchaseDTO data, Long id) {
        if (id != null) {
            new Thread(() -> {
                PurchaseView purchaseView = purchaseClient.editPurchase(data, id);
                if (purchaseView != null) {
                    PurchaseHistoryApplication.getInstance().alert("Updated purchase #" + purchaseView.getBillId() + ". Cost:" + purchaseView.getPrice());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            resetForm();
                            if (onSuccess != null) {
                                onSuccess.accept(purchaseView);
                                filterViewModel.refresh();
                            }
                        });
                    }
                    dismiss();
                } else
                    PurchaseHistoryApplication.getInstance().alert("Failed to register purchase #");
            }).start();
        }
    }

    private void resetForm() {
        this.purchase = new PurchaseDTO();
        this.purchaseId = null;
        binding.purchaseEditCategorySpinner.setSelection(0);
        binding.purchaseEditPriceInput.getText().clear();
        binding.purchaseEditNoteInput.getText().clear();
        binding.purchaseEditDateInput.setText(R.string.date);
        binding.purchaseEditTimeInput.setText(R.string.time);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.dismiss();
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
