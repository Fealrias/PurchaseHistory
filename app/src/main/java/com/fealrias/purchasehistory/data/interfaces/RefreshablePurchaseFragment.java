package com.fealrias.purchasehistory.data.interfaces;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import com.fealrias.purchasehistory.data.filters.PurchaseFilter;
import com.fealrias.purchasehistory.data.filters.PurchaseFilterSingleton;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;

@AndroidEntryPoint
public abstract class RefreshablePurchaseFragment extends Fragment {
    @Inject
    protected PurchaseFilterSingleton filterViewModel;
    @Getter
    protected final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);
    @Setter
    private View loadingScreen;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.configureLoadingRow();
        filterViewModel.getFilter().observe(getViewLifecycleOwner(), this::beforeRefresh);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        filterViewModel.getFilter().removeObserver(this::beforeRefresh);
    }

    public void beforeRefresh(PurchaseFilter filter) {
        Boolean refreshing = isRefreshing.getValue();
        if (refreshing == null || !refreshing) {
            refresh(filter);
        }
    }

    public abstract void refresh(PurchaseFilter filter);

    private void configureLoadingRow() {
        if (loadingScreen != null) {
            loadingScreen.setVisibility(View.GONE);
            isRefreshing.observe(getActivity(), (isRefreshing) ->
                    loadingScreen.setVisibility(isRefreshing ? View.VISIBLE : View.GONE));
        }
    }
}
