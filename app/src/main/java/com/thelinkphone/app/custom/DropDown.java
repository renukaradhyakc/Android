package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class DropDown extends FrameLayout {

    public interface OnItemSelectedListener {
        void onItemSelected(int position, String label);
    }

    private final Context context;
    private final boolean lightTheme;
    private OnItemSelectedListener externalListener;
    private String[] allItems = new String[]{};
    private String selectedLabel = "";
    private int selectedPosition = -1;
    private PopupWindow popupWindow;
    private FilteredListAdapter adapter;
    private boolean isPopupShowing = false;

    public DropDown(Context context, boolean lightTheme) {
        super(context);
        this.context = context;
        this.lightTheme = lightTheme;
        buildClosedView();
    }

    private void togglePopup() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else {
            openPopup();
        }
    }

    private void buildClosedView() {
        removeAllViews();
        setBackground(buildBorderedBackground());
        int hPad  = dp(context, 14);
        int vPad  = dp(context, 12);
        setPadding(hPad, vPad, hPad, vPad);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvLabel = new TextView(context);
        tvLabel.setText(selectedLabel.isEmpty() ? "Select..." : selectedLabel);
        tvLabel.setTextColor(lightTheme ? Color.parseColor("#1C1C1E") : Color.WHITE);
        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        container.addView(tvLabel, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        ChevronView chevron = new ChevronView(context, lightTheme);
        chevron.setExpanded(false);
        chevron.setOnClickListener(v -> togglePopup());

        int chevronSize = dp(context, 20);
        container.addView(chevron, new LinearLayout.LayoutParams(chevronSize, chevronSize));

        addView(container, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        setOnClickListener(v -> togglePopup());
    }

    public void setItems(String[] items) {
        this.allItems = items != null ? items : new String[]{};
        if (allItems.length > 0 && selectedPosition < 0) {
            selectedPosition = 0;
            selectedLabel = allItems[0];
        }
        buildClosedView();
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.externalListener = listener;
    }

    public String getSelectedLabel() {
        return selectedLabel;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    private void openPopup() {
        LinearLayout popupWrapper = new LinearLayout(context);
        popupWrapper.setOrientation(LinearLayout.VERTICAL);
        popupWrapper.setBackground(buildPopupBorder());
        popupWrapper.setPadding(dp(context,1), dp(context,1), dp(context,1), dp(context,1));
        LinearLayout closedStateVisual = new LinearLayout(context);
        closedStateVisual.setOrientation(LinearLayout.HORIZONTAL);
        closedStateVisual.setGravity(Gravity.CENTER_VERTICAL);
        int padding = dp(context, 14);
        closedStateVisual.setPadding(padding, dp(context, 12), padding, dp(context, 12));
        closedStateVisual.setBackgroundColor(lightTheme ? Color.WHITE : Color.parseColor("#3A3A3C"));

        TextView tvSelectedLabel = new TextView(context);
        tvSelectedLabel.setText(selectedLabel.isEmpty() ? "Select..." : selectedLabel);
        tvSelectedLabel.setTextColor(lightTheme ? Color.parseColor("#1C1C1E") : Color.WHITE);
        tvSelectedLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        closedStateVisual.addView(tvSelectedLabel, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        ChevronView chevronInPopup = new ChevronView(context, lightTheme);
        chevronInPopup.setExpanded(true);
        int chevronSize = dp(context, 20);
        closedStateVisual.addView(chevronInPopup, new LinearLayout.LayoutParams(chevronSize, chevronSize));

        popupWrapper.addView(closedStateVisual, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        closedStateVisual.setClickable(true);
        closedStateVisual.setOnClickListener(v -> {
            if (popupWindow != null) {
                popupWindow.dismiss();
            }
        });

        View divider = new View(context);
        divider.setBackgroundColor(lightTheme ? Color.parseColor("#E5E5EA") : Color.parseColor("#545458"));
        popupWrapper.addView(divider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(context, 1)));

        // PART 3: Search field
        EditText searchField = new EditText(context);
        searchField.setHint("Search...");
        searchField.setTextColor(lightTheme ? Color.BLACK : Color.WHITE);
        searchField.setHintTextColor(lightTheme ? Color.parseColor("#C7C7CC") : Color.parseColor("#8E8E93"));
        searchField.setBackgroundColor(lightTheme ? Color.WHITE : Color.parseColor("#3A3A3C"));
        int searchPad = dp(context, 12);
        searchField.setPadding(searchPad, searchPad, searchPad, searchPad);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWrapper.addView(searchField, searchParams);

        View divider2 = new View(context);
        divider2.setBackgroundColor(lightTheme ? Color.parseColor("#E5E5EA") : Color.parseColor("#545458"));
        popupWrapper.addView(divider2, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(context, 1)));

        ListView listView = new ListView(context);
        listView.setBackgroundColor(Color.TRANSPARENT);
        listView.setDivider(null);
        listView.setSelector(android.R.color.transparent); // No default selector
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWrapper.addView(listView, listParams);

        adapter = new FilteredListAdapter(context, allItems, lightTheme, selectedPosition);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = adapter.getItem(position);
            for (int i = 0; i < allItems.length; i++) {
                if (allItems[i].equals(selectedItem)) {
                    selectedPosition = i;
                    break;
                }
            }
            selectedLabel = selectedItem;
            if (externalListener != null) {
                externalListener.onItemSelected(selectedPosition, selectedLabel);
            }
            buildClosedView();
            if (popupWindow != null) popupWindow.dismiss();
        });

        searchField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
                int itemCount = adapter.getCount();
                int itemHeight = dp(context, 50);
                int maxHeight = dp(context, 250);
                int listHeight = Math.min(itemCount * itemHeight, maxHeight);
                listView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, listHeight));
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        searchField.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (popupWindow != null) popupWindow.dismiss();
                return true;
            }
            return false;
        });

        int itemCount = allItems.length;
        int itemHeight = dp(context, 50);
        int maxHeight = dp(context, 250);
        int initialListHeight = Math.min(itemCount * itemHeight, maxHeight);

        int popupWidth = getWidth() > 0 ? getWidth() : dp(context, 280);
        popupWindow = new PopupWindow(popupWrapper, popupWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        popupWindow.setClippingEnabled(false);
        popupWindow.setElevation(dp(context, 8));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOnDismissListener(() -> {
            isPopupShowing = false;
        });
        isPopupShowing = true;
        popupWindow.showAsDropDown(this, 0, -getHeight() + dp(context, 2));

        searchField.requestFocus();
    }

    private GradientDrawable buildBorderedBackground() {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(context, 10));
        bg.setColor(lightTheme ? Color.WHITE : Color.parseColor("#3A3A3C"));
        bg.setStroke(dp(context, 2),
                lightTheme ? Color.parseColor("#C7C7CC") : Color.parseColor("#636366"));
        return bg;
    }

    private GradientDrawable buildPopupBorder() {
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setCornerRadii(new float[]{
                dp(context,10), dp(context,10),
                dp(context,10), dp(context,10),
                dp(context,12), dp(context,12),
                dp(context,12), dp(context,12)
        });
        border.setColor(lightTheme ? Color.WHITE : Color.parseColor("#3A3A3C"));
        border.setStroke(dp(context, 2),
                lightTheme ? Color.parseColor("#C7C7CC") : Color.parseColor("#636366")); // Match closed state stroke
        return border;
    }

    private int dp(Context context, int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }

    private class ChevronView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private boolean expanded = false;

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
            invalidate();
        }

        ChevronView(Context context, boolean lightTheme) {
            super(context);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(lightTheme ? Color.parseColor("#8E8E93") : Color.parseColor("#AEAEB2"));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float inset = w * 0.2f;
            float tipY  = h * 0.65f;
            float topY  = h * 0.35f;

            canvas.save();

            if (expanded) {
                canvas.rotate(180, w/2f, h/2f);
            }

            Path path = new Path();
            path.moveTo(inset, topY);
            path.lineTo(w - inset, topY);
            path.lineTo(w / 2f, tipY);
            path.close();
            canvas.drawPath(path, paint);
            canvas.restore();
        }
    }

    private class FilteredListAdapter extends ArrayAdapter<String> {
        private final String[] original;
        private String[] filtered;
        private final boolean lightTheme;
        private final int selectedPosition;

        FilteredListAdapter(Context context, String[] items, boolean lightTheme, int selectedPosition) {
            super(context, 0, items);
            this.original = items;
            this.filtered = items;
            this.lightTheme = lightTheme;
            this.selectedPosition = selectedPosition;
        }

        @Override
        public int getCount() {
            return filtered.length;
        }

        @Override
        public String getItem(int position) {
            return filtered[position];
        }

        @Override
        public android.view.View getView(int position, android.view.View convertView, ViewGroup parent) {
            TextView tv = (TextView) convertView;
            if (tv == null) {
                tv = new TextView(context);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                int pad = dp(context, 14);
                tv.setPadding(pad, pad, pad, pad);
            }
            tv.setText(filtered[position]);

            if (selectedPosition >= 0 && selectedPosition < original.length &&
                    filtered[position].equals(original[selectedPosition])) {
                tv.setBackgroundColor(Color.parseColor("#6366F1"));
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackgroundColor(lightTheme ? Color.WHITE : Color.parseColor("#3A3A3C"));
                tv.setTextColor(lightTheme ? Color.parseColor("#1C1C1E") : Color.WHITE);
            }
            return tv;
        }

        public android.widget.Filter getFilter() {
            return new android.widget.Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (constraint == null || constraint.length() == 0) {
                        results.values = original;
                        results.count = original.length;
                    } else {
                        String query = constraint.toString().toLowerCase();
                        String[] filteredArray = new String[original.length];
                        int count = 0;
                        for (String item : original) {
                            if (item.toLowerCase().contains(query)) {
                                filteredArray[count++] = item;
                            }
                        }
                        String[] result = new String[count];
                        System.arraycopy(filteredArray, 0, result, 0, count);
                        results.values = result;
                        results.count = count;
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filtered = (String[]) results.values;
                    notifyDataSetChanged();
                }
            };
        }
    }

    public void setSelectedPosition(int position) {
        if (position >= 0 && position < allItems.length) {
            selectedPosition = position;
            selectedLabel = allItems[position];
            buildClosedView();
        }
    }
}