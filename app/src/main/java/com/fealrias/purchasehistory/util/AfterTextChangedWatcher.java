package com.fealrias.purchasehistory.util;

import android.text.TextWatcher;

public abstract class AfterTextChangedWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //ignore
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //ignore
    }

}
