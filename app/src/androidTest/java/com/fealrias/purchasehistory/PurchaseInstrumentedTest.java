package com.fealrias.purchasehistory;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import org.junit.Before;
import org.junit.Rule;

@HiltAndroidTest
public class PurchaseInstrumentedTest {
    @Rule
    public final HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Before
    public void init() {
        hiltRule.inject();
    }
}
