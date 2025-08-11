package com.hamsterbase.burrowui.components;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SliderSettingsItem extends LinearLayout {
    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView valueTextView;
    private SeekBar seekBar;

    public SliderSettingsItem(Context context, String title, String description, 
                            int minValue, int maxValue, int currentValue, 
                            OnValueChangeListener listener) {
        super(context);
        init(context, title, description, minValue, maxValue, currentValue, listener);
    }

    private void init(Context context, String title, String description, 
                     int minValue, int maxValue, int currentValue, 
                     OnValueChangeListener listener) {
        setOrientation(LinearLayout.VERTICAL);
        setPadding(
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()),
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()),
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()),
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics())
        );

        // Title
        titleTextView = new TextView(context);
        titleTextView.setText(title);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleTextView.setTextColor(Color.BLACK);
        addView(titleTextView);

        // Description
        descriptionTextView = new TextView(context);
        descriptionTextView.setText(description);
        descriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        descriptionTextView.setTextColor(Color.GRAY);
        descriptionTextView.setPadding(0, 
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()), 0, 0);
        addView(descriptionTextView);

        // Value display and SeekBar container
        LinearLayout sliderContainer = new LinearLayout(context);
        sliderContainer.setOrientation(LinearLayout.HORIZONTAL);
        sliderContainer.setGravity(Gravity.CENTER_VERTICAL);
        sliderContainer.setPadding(0, 
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()), 0, 0);

        // Value text
        valueTextView = new TextView(context);
        valueTextView.setText(String.valueOf(currentValue));
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        valueTextView.setTextColor(Color.BLACK);
        valueTextView.setMinWidth(
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));
        valueTextView.setGravity(Gravity.CENTER);

        // SeekBar
        seekBar = new SeekBar(context);
        seekBar.setMax(maxValue - minValue);
        seekBar.setProgress(currentValue - minValue);
        
        LinearLayout.LayoutParams seekBarParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        seekBarParams.setMarginStart(
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()));

        sliderContainer.addView(valueTextView);
        sliderContainer.addView(seekBar, seekBarParams);
        addView(sliderContainer);

        // Set listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = minValue + progress;
                valueTextView.setText(String.valueOf(value));
                if (listener != null && fromUser) {
                    listener.onValueChanged(value);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public interface OnValueChangeListener {
        void onValueChanged(int newValue);
    }
}
