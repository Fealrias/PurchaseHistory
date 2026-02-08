package com.fealrias.purchasehistory.data.filters;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.fealrias.purchasehistory.data.Constants;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PurchaseFilterSingleton {

    private final MutableLiveData<PurchaseFilter> filterLiveData = new MutableLiveData<>(Constants.getDefaultFilter());

    @Inject
    public PurchaseFilterSingleton() {
    }

    public LiveData<PurchaseFilter> getFilter() {
        return filterLiveData;
    }

    @NotNull
    public PurchaseFilter getFilterValue() {
        PurchaseFilter value = filterLiveData.getValue();
        return value == null ? Constants.getDefaultFilter() : value;
    }

    public void updateFilter(@NotNull PurchaseFilter newFilter) {
        filterLiveData.postValue(newFilter);
    }

    public void refresh() {
        filterLiveData.postValue(getFilterValue());
    }
}