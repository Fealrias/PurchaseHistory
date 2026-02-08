package com.fealrias.purchasehistory.ui.legal;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.fealrias.purchasehistory.R;
import com.fealrias.purchasehistory.databinding.ActivityAboutUsBinding;
import com.fealrias.purchasehistory.ui.feedback.FeedbackActivity;
import lombok.NonNull;

public class AboutUsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAboutUsBinding binding = ActivityAboutUsBinding.inflate(getLayoutInflater());
        binding.aboutUsDesc2.setOnClickListener((v) -> {
            Intent intent = new Intent(this, FeedbackActivity.class);
            startActivity(intent);
        });
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_turn_left);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(binding.getRoot());
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}