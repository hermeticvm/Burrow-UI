package com.hamsterbase.burrowui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

public class NavigationBar extends LinearLayout {
    private ListView mListView;
    private Button mBackButton;
    private ImageButton mUpButton;
    private ImageButton mDownButton;
    private View mUpDownContainer;
    private OnBackClickListener mOnBackClickListener;

    public interface OnBackClickListener {
        void onBackClick();
    }

    public void setOnBackClickListener(OnBackClickListener listener) {
        mOnBackClickListener = listener;
    }

    public NavigationBar(Context context) {
        super(context);
        init(context);
    }

    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NavigationBar(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(Color.WHITE);
        LayoutInflater.from(context).inflate(R.layout.navigation_bar, this, true);

        mBackButton = findViewById(R.id.back_button);
        mUpButton = findViewById(R.id.up_button);
        mDownButton = findViewById(R.id.down_button);
        mUpDownContainer = findViewById(R.id.up_down_container);

        setupClickListeners();
    }

    private void setupClickListeners() {
        mBackButton.setOnClickListener(v -> {
            if (mOnBackClickListener != null) {
                mOnBackClickListener.onBackClick();
            }
        });

        mUpButton.setOnClickListener(v -> {
            if (mListView != null) {
                mListView.smoothScrollBy(-500, 0);
            }
        });

        mDownButton.setOnClickListener(v -> {
            if (mListView != null) {
                mListView.smoothScrollBy(500, 0);
            }
        });
    }

    public void setListView(ListView listView) {
        mListView = listView;
        if (mListView != null) {
            mUpDownContainer.setVisibility(VISIBLE);
        } else {
            mUpDownContainer.setVisibility(GONE);
        }
    }
}
