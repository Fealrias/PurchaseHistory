package com.fealrias.purchasehistory.ui.home.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.fealrias.purchasehistory.data.Constants;
import com.fealrias.purchasehistory.data.factories.DashboardComponentsFactory;
import com.fealrias.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.fealrias.purchasehistory.data.model.DashboardComponent;
import com.fealrias.purchasehistory.databinding.FragmentDashboardCardBinding;
import com.fealrias.purchasehistory.ui.FullscreenGraphActivity;

import org.jetbrains.annotations.NotNull;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DashboardCardFragment extends Fragment {

    private DashboardComponent component;
    private int generatedId;
    private Integer marginBottom;
    private FragmentDashboardCardBinding binding;

    public DashboardCardFragment(DashboardComponent dashboardComponent) {
        this.component = dashboardComponent;
        generatedId = View.generateViewId();
        Bundle args = new Bundle();
        args.putParcelable(Constants.Arguments.ARG_COMPONENT, dashboardComponent);
        args.putInt(Constants.Arguments.VIEW_ID, generatedId);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            component = getArguments().getParcelable(Constants.Arguments.ARG_COMPONENT);
            generatedId = getArguments().getInt(Constants.Arguments.VIEW_ID);
            marginBottom = getArguments().getInt(Constants.Arguments.MARGIN_BOTTOM);
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardCardBinding.inflate(inflater, container, false);
        binding.fragmentContainerView.setId(generatedId);
        binding.title.setText(component.getTitle());
        binding.imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FullscreenGraphActivity.class);
            intent.putExtra(Constants.Arguments.ARG_COMPONENT, component);
            startActivity(intent);
        });
        RefreshablePurchaseFragment fragment = DashboardComponentsFactory.createFragment(component.getFragmentName());
        if (fragment.getArguments() != null) {
            fragment.getArguments().putInt(Constants.Arguments.ARG_MAX_SIZE, 6);
            fragment.getArguments().putBoolean(Constants.Arguments.ARG_SHOW_FILTER, false);
        }
        getChildFragmentManager().beginTransaction()
                .replace(binding.fragmentContainerView.getId(), fragment)
                .commit();
        setMarginBottomIfLast();
        return binding.getRoot();
    }

    private void setMarginBottomIfLast() {
        if (marginBottom == 0) return;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, marginBottom);
        binding.getRoot().setLayoutParams(params);
    }

    /**
     *
     */
    @Override
    public void onDetach() {
        super.onDetach();
        RefreshablePurchaseFragment fragment = component.getFragment();
        if (fragment != null)
            getChildFragmentManager().beginTransaction().remove(fragment);
    }
}