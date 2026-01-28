package com.fealrias.purchasehistory.ui.home.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fealrias.purchasehistory.databinding.FragmentFaqBinding;
import com.fealrias.purchasehistory.databinding.ItemFaqBinding;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import lombok.NonNull;

public class FAQFragment extends Fragment {private FragmentFaqBinding binding;
    private FaqAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFaqBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerFaq.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FaqAdapter(getSampleFaqs());
        binding.recyclerFaq.setAdapter(adapter);
    }

    private List<FaqItem> getSampleFaqs() {
        List<FaqItem> list = new ArrayList<>();
        list.add(new FaqItem("What is this app for?", "Track your purchases, set monthly budgets, get reminders for recurring payments and analyze your spending habits."));
        list.add(new FaqItem("How do scheduled notifications work?", "You can create recurring or one-time reminders for bills and subscriptions. When the time comes, you'll get a notification with quick action to add the expense."));
        list.add(new FaqItem("Can I silence a notification temporarily?", "Yes — long-press or use the silence toggle on any scheduled item to mute notifications without deleting the rule."));
        list.add(new FaqItem("Why do I need notification permission?", "The app uses notifications to remind you about upcoming scheduled expenses and recurring payments."));
        list.add(new FaqItem("Is my data safe?", "All sensitive data is stored locally on your device. We only send anonymized statistics if you explicitly enable cloud sync."));
        list.add(new FaqItem("How do I set a monthly budget?", "Go to Settings → Budgets → Set monthly limit. The app will show remaining money and progress in the Monthly Balance chart."));
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    static class FaqItem {
        String question;
        String answer;
        boolean isExpanded = false;

        FaqItem(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }
    static class FaqAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {

        private final List<FaqItem> items;

        FaqAdapter(List<FaqItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public FaqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemFaqBinding binding = ItemFaqBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new FaqViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull FaqViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class FaqViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            private final ItemFaqBinding binding;

            FaqViewHolder(ItemFaqBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            void bind(FaqItem item) {
                binding.tvQuestion.setText(item.question);
                binding.tvAnswer.setText(item.answer);
                binding.tvAnswer.setVisibility(item.isExpanded ? View.VISIBLE : View.GONE);

                binding.getRoot().setOnClickListener(v -> {
                    item.isExpanded = !item.isExpanded;
                    notifyItemChanged(getAdapterPosition());
                });

                // Rotate arrow icon
                binding.ivExpand.setRotation(item.isExpanded ? 180f : 0f);

                // Optional: subtle card elevation change on expand
                binding.getRoot();
                binding.getRoot().setCardElevation(item.isExpanded ? 8f : 2f);
            }
        }
    }
}