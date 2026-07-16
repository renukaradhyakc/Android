package com.thelinkphone.app.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thelinkphone.app.ActivityHome;
import com.thelinkphone.app.R;
import com.thelinkphone.app.adapter.AdapterRecentSearch;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemRecentGroup;
import com.thelinkphone.app.repository.RecentsRepository;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.ReadContact;
import com.thelinkphone.app.utils.RecentSearchUtils;

import java.util.ArrayList;

public class FragmentRecentsSearch extends BaseFragment {
    private ViewFragmentRecentsSearch view;

    public static FragmentRecentsSearch newInstance(boolean missedOnly) {
        FragmentRecentsSearch fragment = new FragmentRecentsSearch();
        android.os.Bundle args = new android.os.Bundle();
        args.putBoolean(ViewFragmentRecentsSearch.ARG_MISSED_ONLY, missedOnly);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        boolean missedOnly = getArguments() != null && getArguments().getBoolean(ViewFragmentRecentsSearch.ARG_MISSED_ONLY, false);
        if (this.view == null) {
            this.view = new ViewFragmentRecentsSearch(layoutInflater.getContext(), missedOnly);
        }
        return this.view;
    }

    public class ViewFragmentRecentsSearch extends RelativeLayout {
        private final boolean theme;
        private final boolean missedOnly;
        private final EditText etSearch;
        private final LinearLayout historyContainer;
        private final RecyclerView rvResults;
        private final AdapterRecentSearch adapterRecentSearch;
        private final TextW tvNoResults;
        private static final String ARG_MISSED_ONLY = "missed_only";

        public ViewFragmentRecentsSearch(Context context, boolean missedOnly) {
            super(context);
            this.missedOnly = missedOnly;
            this.theme = MyShare.getTheme(context);
            int contentMargin = dp(16);

            if (theme) setBackgroundColor(-1);
            else setBackgroundColor(Color.parseColor("#2C2C2C"));

            // --- Top bar: EditText + Cancel ---
            LinearLayout topBar = new LinearLayout(context);
            topBar.setId(View.generateViewId());
            topBar.setOrientation(LinearLayout.HORIZONTAL);
            topBar.setGravity(Gravity.CENTER_VERTICAL);
            topBar.setPadding(contentMargin, dp(12), contentMargin, dp(12));

            TextW tvScopeLabel = new TextW(context);
            tvScopeLabel.setId(View.generateViewId());
            tvScopeLabel.setText(missedOnly ? "Searching in Missed Calls" : "Searching in All Calls");
            tvScopeLabel.setupText(400, 3.2f);
            tvScopeLabel.setTextColor(Color.parseColor("#8A8A8E"));
            tvScopeLabel.setPadding(contentMargin, 0, contentMargin, dp(8));
            LayoutParams scopeLabelParams = new LayoutParams(-1, -2);
            scopeLabelParams.addRule(3, topBar.getId());
            addView(tvScopeLabel, scopeLabelParams);

            EditText etSearch = new EditText(context);
            this.etSearch = etSearch;
            etSearch.setHint(getString(R.string.search));
            etSearch.setSingleLine(true);
            etSearch.setTextColor(theme ? Color.BLACK : Color.WHITE);
            etSearch.setHintTextColor(Color.parseColor("#8A8A8E"));
            etSearch.setBackgroundColor(Color.TRANSPARENT);
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setCornerRadius(dp(22));
            bg.setColor(theme ? Color.parseColor("#F0F0F0") : Color.parseColor("#3A3A3C"));
            etSearch.setBackground(bg);
            etSearch.setPadding(dp(16), dp(10), dp(16), dp(10));
            LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            topBar.addView(etSearch, etParams);

            TextW tvCancel = new TextW(context);
            tvCancel.setText(R.string.cancel); // ASSUMPTION: R.string.cancel exists; your DialogAddFav
            // flow likely already has one — tell me the actual name if different
            tvCancel.setTextColor(Color.parseColor("#007AFF"));
            tvCancel.setupText(400, 4.0f);
            tvCancel.setPadding(dp(12), 0, 0, 0);
            tvCancel.setOnClickListener(v -> closeSearch());
            topBar.addView(tvCancel, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            LayoutParams topBarParams = new LayoutParams(-1, -2);
            addView(topBar, topBarParams);

            // --- History container (shown when query is empty) ---
            LinearLayout historyContainer = new LinearLayout(context);
            this.historyContainer = historyContainer;
            historyContainer.setId(View.generateViewId());
            historyContainer.setOrientation(LinearLayout.VERTICAL);
            LayoutParams historyParams = new LayoutParams(-1, -2);
            historyParams.addRule(3, topBar.getId());
            addView(historyContainer, historyParams);

            // --- Results list (shown when query is non-empty) ---
            RecyclerView rvResults = new RecyclerView(context);
            this.rvResults = rvResults;
            rvResults.setId(View.generateViewId());
            rvResults.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
            ArrayList<ItemRecentGroup> resultsBacking = new ArrayList<>();
            AdapterRecentSearch adapterRecentSearch = new AdapterRecentSearch(resultsBacking, theme, missedOnly, this::onResultTapped);
            this.adapterRecentSearch = adapterRecentSearch;
            rvResults.setAdapter(adapterRecentSearch);
            rvResults.setVisibility(View.GONE);
            LayoutParams rvParams = new LayoutParams(-1, -1);
            rvParams.addRule(3, topBar.getId());
            addView(rvResults, rvParams);

            TextW tvNoResults = new TextW(context);
            this.tvNoResults = tvNoResults;
            tvNoResults.setText(R.string.no_results); // ASSUMPTION: add this string ("No results found")
            // or tell me the name if one already exists
            tvNoResults.setupText(400, 3.8f);
            tvNoResults.setGravity(Gravity.CENTER);
            tvNoResults.setTextColor(Color.parseColor("#aaaaaa"));
            tvNoResults.setVisibility(View.GONE);
            LayoutParams noResultsParams = new LayoutParams(-1, -1);
            noResultsParams.addRule(3, topBar.getId());
            addView(tvNoResults, noResultsParams);

            renderHistory();

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    onQueryChanged(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            etSearch.requestFocus();
            android.os.Handler h = new android.os.Handler();
            h.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }, 100);
        }

        private void onQueryChanged(String query) {
            String trimmed = query == null ? "" : query.trim();
            if (trimmed.isEmpty()) {
                historyContainer.setVisibility(View.VISIBLE);
                rvResults.setVisibility(View.GONE);
                tvNoResults.setVisibility(View.GONE);
                renderHistory();
                return;
            }

            historyContainer.setVisibility(View.GONE);

            ArrayList<ItemRecentGroup> cache = RecentsRepository.getCache();
            ArrayList<ItemRecentGroup> scoped = missedOnly ? RecentSearchUtils.filterMissedGroups(cache) : cache;

            ArrayList<ItemRecentGroup> filtered = new ArrayList<>();
            if (scoped != null) {
                for (ItemRecentGroup group : scoped) {
                    if (RecentSearchUtils.matches(group, trimmed)) {
                        filtered.add(group);
                    }
                }
            }

            Log.d("SEARCH_MATCH", "query='" + trimmed + "' missedOnly=" + missedOnly + " matches=" + filtered.size());

            if (filtered.isEmpty()) {
                rvResults.setVisibility(View.GONE);
                tvNoResults.setVisibility(View.VISIBLE);
            } else {
                tvNoResults.setVisibility(View.GONE);
                rvResults.setVisibility(View.VISIBLE);
                adapterRecentSearch.updateResults(filtered, trimmed);
            }
        }

        private void renderHistory() {
            historyContainer.removeAllViews();
            ArrayList<String> history = MyShare.getSearchHistory(getContext());

            if (history.isEmpty()) {
                return;
            }

            for (String term : history) {
                LinearLayout row = new LinearLayout(getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                int pad = dp(16);
                row.setPadding(pad, dp(12), pad, dp(12));

                TextW tvTerm = new TextW(getContext());
                tvTerm.setText(term);
                tvTerm.setupText(400, 3.8f);
                tvTerm.setTextColor(theme ? Color.BLACK : Color.WHITE);
                LinearLayout.LayoutParams termParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                row.addView(tvTerm, termParams);

                TextW tvDelete = new TextW(getContext());
                tvDelete.setText("\u2715"); // ×
                tvDelete.setTextColor(Color.parseColor("#8A8A8E"));
                tvDelete.setupText(400, 4.0f);
                tvDelete.setPadding(dp(12), 0, 0, 0);
                tvDelete.setOnClickListener(v -> {
                    MyShare.removeSearchHistoryEntry(getContext(), term);
                    renderHistory();
                });
                row.addView(tvDelete, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                row.setOnClickListener(v -> {
                    etSearch.setText(term);
                    etSearch.setSelection(term.length());
                });

                historyContainer.addView(row);
            }

            TextW tvClear = new TextW(getContext());
            tvClear.setText(R.string.clear_history);
            tvClear.setTextColor(Color.parseColor("#FF3B30"));
            tvClear.setupText(400, 3.8f);
            tvClear.setGravity(Gravity.CENTER);
            int pad = dp(16);
            tvClear.setPadding(pad, dp(16), pad, dp(16));
            tvClear.setOnClickListener(v -> {
                MyShare.clearSearchHistory(getContext());
                renderHistory();
            });
            historyContainer.addView(tvClear);
        }

        private void onResultTapped(ItemRecentGroup group) {
            String committedTerm = etSearch.getText().toString().trim();
            if (!committedTerm.isEmpty()) {
                MyShare.addSearchHistoryEntry(getContext(), committedTerm);
            }

            if (FragmentRecentsSearch.this.getActivity() instanceof ActivityHome) {
                ActivityHome activity = (ActivityHome) FragmentRecentsSearch.this.getActivity();
                ItemContact contactWithNumber = ReadContact.getContactWithNumber(getContext(),
                        group.arrRecent.get(0).number);
                if (contactWithNumber != null) {
                    FragmentInfo newInstance = FragmentInfo.newInstance(contactWithNumber, group, R.string.back, false);
                    newInstance.setContactResult(FragmentRecentsSearch.this.contactResult);
                    activity.showFragment(newInstance, true);
                } else {
                    FragmentInfoAnother newInstance2 = FragmentInfoAnother.newInstance(group, false);
                    newInstance2.setContactResult(FragmentRecentsSearch.this.contactResult);
                    activity.showFragment(newInstance2, true);
                }
            }
        }

        private void closeSearch() {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            if (FragmentRecentsSearch.this.getActivity() instanceof ActivityHome) {
                ((ActivityHome) FragmentRecentsSearch.this.getActivity()).onBackPressed();
            }
        }

        private int dp(int value) {
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
        }
    }
}