package com.hamsterbase.burrowui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hamsterbase.burrowui.service.AppInfo;
import com.hamsterbase.burrowui.service.AppManagementService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView timeTextView;
    private TextView dateTextView;
    private TextView amPmTextView;
    private LinearLayout appLinearLayout;
    private List<AppInfo> selectedApps;
    private List<AppInfo> filteredApps;
    private SettingsManager settingsManager;
    private AppManagementService appManagementService;
    private Handler handler;
    private Runnable updateTimeRunnable;
    private BroadcastReceiver batteryReceiver;
    private String batteryText = "";

    private LinearLayout searchContainer;
    private EditText searchEditText;
    private ImageView searchToggleButton;
    private ImageView clearSearchButton;

    // Alphabetical index views
    private TextView[] letterViews;
    private ScrollView appScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Prevent keyboard from resizing layout
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_main);

        timeTextView = findViewById(R.id.timeTextView);
        dateTextView = findViewById(R.id.dateTextView);
        amPmTextView = findViewById(R.id.amPmTextView);
        appLinearLayout = findViewById(R.id.appLinearLayout);
        appScrollView = findViewById(R.id.appScrollList);

        ScrollView appList = findViewById(R.id.appScrollList);
        appList.setOverScrollMode(View.OVER_SCROLL_NEVER);
        appList.setVerticalScrollBarEnabled(false);

        TextView debugTextView = findViewById(R.id.debugTextView);
        if (BuildConfig.DEBUG) {
            debugTextView.setVisibility(View.VISIBLE);
            String debugInfo = "Debug: " + Build.MODEL + " - " + Build.VERSION.RELEASE;
            debugTextView.setText(debugInfo);
        } else {
            debugTextView.setVisibility(View.GONE);
        }

        settingsManager = new SettingsManager(this);
        appManagementService = AppManagementService.getInstance(this);

        // Initialize search components
        searchContainer = findViewById(R.id.searchContainer);
        searchEditText = findViewById(R.id.searchEditText);
        searchToggleButton = findViewById(R.id.searchToggleButton);
        clearSearchButton = findViewById(R.id.clearSearchButton);

        handler = new Handler(Looper.getMainLooper());
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000); // Update every second
            }
        };

        loadApps();
        setupAlphabeticalIndex();
        displaySelectedApps();
        setupSearchFunctionality();

        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            private boolean isLongPress = false;
            private Handler longPressHandler = new Handler();
            private static final long LONG_PRESS_TIMEOUT = 600;

            private Runnable longPressRunnable = new Runnable() {
                @Override
                public void run() {
                    isLongPress = true;
                    openSettingsActivity();
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isLongPress = false;
                        longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        return true;

                    case MotionEvent.ACTION_UP:
                        longPressHandler.removeCallbacks(longPressRunnable);
                        if (isLongPress) {
                            return true;
                        }
                        return false;

                    case MotionEvent.ACTION_CANCEL:
                        longPressHandler.removeCallbacks(longPressRunnable);
                        return false;
                }
                return false;
            }
        });

        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateBatteryStatus(intent);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApps();
        setupAlphabeticalIndex();
        displaySelectedApps();
        handler.post(updateTimeRunnable);

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateTimeRunnable);
        unregisterReceiver(batteryReceiver);
    }

    private void updateTime() {
        if (settingsManager.isUse24HourFormat()) {
            // 24-hour format
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentTime = sdf.format(new Date());
            timeTextView.setText(currentTime);
            amPmTextView.setVisibility(View.GONE);
        } else {
            // 12-hour format
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
            String currentTime = sdf.format(new Date());
            timeTextView.setText(currentTime);
            
            // Get AM/PM separately using English locale to ensure "AM"/"PM" instead of "上午"/"下午"
            SimpleDateFormat amPmSdf = new SimpleDateFormat("a", Locale.ENGLISH);
            String amPm = amPmSdf.format(new Date());
            amPmTextView.setText(amPm);
            amPmTextView.setVisibility(View.VISIBLE);
        }
        
        String dateFormat = settingsManager.getDateFormat();
        SimpleDateFormat dateSdf = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        String currentDate = dateSdf.format(new Date()).concat(batteryText);
        dateTextView.setText(currentDate);
    }

    private void updateBatteryStatus(Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level * 100 / (float) scale;
        batteryText = String.format(Locale.getDefault(), " %.0f%%", batteryPct);
    }

    private void loadApps() {
        List<AppInfo> allApps = appManagementService.listApps();
        selectedApps = new ArrayList<>();
        List<SettingsManager.SelectedItem> selectedItems = settingsManager.getSelectedItems();
        for (SettingsManager.SelectedItem item : selectedItems) {
            if (item.getType().equals("application")) {
                for (AppInfo app : allApps) {
                    if (appManagementService.isSelectItemEqualWith(app, item)) {
                        selectedApps.add(app);
                    }
                }
            }
        }
        
        // Sort apps alphabetically
        selectedApps.sort((app1, app2) -> {
            String label1 = app1.getLabel() != null ? app1.getLabel() : "";
            String label2 = app2.getLabel() != null ? app2.getLabel() : "";
            return label1.compareToIgnoreCase(label2);
        });
    }

    private void displaySelectedApps() {
        appLinearLayout.removeAllViews();
        for (AppInfo app : selectedApps) {
            addAppToLayout(app);
        }

        if (settingsManager.isShowSettingsIcon()) {
            addSettingsAppToLayout();
        }
        
        // Update alphabetical index visibility only if letterViews is initialized
        if (letterViews != null) {
            updateAlphabeticalIndex();
        }
    }

    private void addAppToLayout(AppInfo app) {
        View appView = getLayoutInflater().inflate(R.layout.app_item, null);
        ImageView iconView = appView.findViewById(R.id.appIcon);
        TextView nameView = appView.findViewById(R.id.appName);

        // Get custom icon size
        int iconSize = settingsManager.getIconSize();
        
        // Set icon size
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
            (int) (iconSize * getResources().getDisplayMetrics().density),
            (int) (iconSize * getResources().getDisplayMetrics().density)
        );
        iconView.setLayoutParams(iconParams);
        
        // Set font size proportionally (scale from 14sp at 44dp)
        float fontSize = 14f * (iconSize / 44f);
        nameView.setTextSize(fontSize);
        
        iconView.setImageDrawable(appManagementService.getIcon(app.getPackageName(), app.getUserId()));
        nameView.setText(app.getLabel());
        appView.setOnClickListener(v -> appManagementService.launchApp(app));
        appLinearLayout.addView(appView);
    }

    private void addSettingsAppToLayout() {
        View settingsAppView = getLayoutInflater().inflate(R.layout.app_item, null);
        ImageView iconView = settingsAppView.findViewById(R.id.appIcon);
        TextView nameView = settingsAppView.findViewById(R.id.appName);

        // Get custom icon size
        int iconSize = settingsManager.getIconSize();
        
        // Set icon size
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
            (int) (iconSize * getResources().getDisplayMetrics().density),
            (int) (iconSize * getResources().getDisplayMetrics().density)
        );
        iconView.setLayoutParams(iconParams);
        
        // Set font size proportionally (scale from 14sp at 44dp)
        float fontSize = 14f * (iconSize / 44f);
        nameView.setTextSize(fontSize);

        iconView.setImageResource(R.drawable.ic_settings);
        nameView.setText(R.string.launcher_settings);
        settingsAppView.setOnClickListener(v -> openSettingsActivity());
        appLinearLayout.addView(settingsAppView);
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    private void setupAlphabeticalIndex() {
        // Initialize letter views
        letterViews = new TextView[26];
        letterViews[0] = findViewById(R.id.letterA);
        letterViews[1] = findViewById(R.id.letterB);
        letterViews[2] = findViewById(R.id.letterC);
        letterViews[3] = findViewById(R.id.letterD);
        letterViews[4] = findViewById(R.id.letterE);
        letterViews[5] = findViewById(R.id.letterF);
        letterViews[6] = findViewById(R.id.letterG);
        letterViews[7] = findViewById(R.id.letterH);
        letterViews[8] = findViewById(R.id.letterI);
        letterViews[9] = findViewById(R.id.letterJ);
        letterViews[10] = findViewById(R.id.letterK);
        letterViews[11] = findViewById(R.id.letterL);
        letterViews[12] = findViewById(R.id.letterM);
        letterViews[13] = findViewById(R.id.letterN);
        letterViews[14] = findViewById(R.id.letterO);
        letterViews[15] = findViewById(R.id.letterP);
        letterViews[16] = findViewById(R.id.letterQ);
        letterViews[17] = findViewById(R.id.letterR);
        letterViews[18] = findViewById(R.id.letterS);
        letterViews[19] = findViewById(R.id.letterT);
        letterViews[20] = findViewById(R.id.letterU);
        letterViews[21] = findViewById(R.id.letterV);
        letterViews[22] = findViewById(R.id.letterW);
        letterViews[23] = findViewById(R.id.letterX);
        letterViews[24] = findViewById(R.id.letterY);
        letterViews[25] = findViewById(R.id.letterZ);

        // Set click listeners for each letter
        for (int i = 0; i < letterViews.length; i++) {
            final int index = i;
            letterViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scrollToLetter(index);
                }
            });
        }
    }

    private void scrollToLetter(int letterIndex) {
        if (selectedApps == null || selectedApps.isEmpty()) {
            return;
        }

        // Convert letter index to character
        char targetLetter = (char) ('A' + letterIndex);

        // Find the first app that starts with the target letter
        for (int i = 0; i < selectedApps.size(); i++) {
            AppInfo app = selectedApps.get(i);
            String label = app.getLabel();
            if (label != null && !label.isEmpty()) {
                char firstChar = Character.toUpperCase(label.charAt(0));
                if (firstChar == targetLetter) {
                    // Get the actual view at this position
                    View childView = appLinearLayout.getChildAt(i);
                    if (childView != null) {
                        // Scroll to the actual position of the view
                        int y = childView.getTop();
                        appScrollView.smoothScrollTo(0, y);
                    }
                    break;
                }
            }
        }
    }

    private void updateAlphabeticalIndex() {
        if (selectedApps == null || selectedApps.isEmpty()) {
            // Hide all letters if no apps
            for (TextView letterView : letterViews) {
                letterView.setAlpha(0.3f);
            }
            return;
        }

        // Create a set of first letters that exist in the app list
        boolean[] hasAppsForLetter = new boolean[26];
        List<AppInfo> appsToCheck = filteredApps != null ? filteredApps : selectedApps;
        for (AppInfo app : appsToCheck) {
            String label = app.getLabel();
            if (label != null && !label.isEmpty()) {
                char firstChar = Character.toUpperCase(label.charAt(0));
                if (firstChar >= 'A' && firstChar <= 'Z') {
                    hasAppsForLetter[firstChar - 'A'] = true;
                }
            }
        }

        // Update letter visibility based on whether apps exist for that letter
        for (int i = 0; i < letterViews.length; i++) {
            if (hasAppsForLetter[i]) {
                letterViews[i].setAlpha(1.0f);
            } else {
                letterViews[i].setAlpha(0.3f);
            }
        }
    }

    private void setupSearchFunctionality() {
        // Initialize filtered apps
        filteredApps = new ArrayList<>(selectedApps);

        // Set up search toggle button
        searchToggleButton.setOnClickListener(v -> toggleSearch());

        // Set up clear search button
        clearSearchButton.setOnClickListener(v -> clearSearch());

        // Set up search edit text
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Handle keyboard done action
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void toggleSearch() {
        if (searchContainer.getVisibility() == View.VISIBLE) {
            hideSearch();
        } else {
            showSearch();
        }
    }

    private void showSearch() {
        searchContainer.setVisibility(View.VISIBLE);
        searchToggleButton.setVisibility(View.GONE);
        // Don't auto-focus - let user tap to focus
        // searchEditText.requestFocus();
        
        // Show keyboard only when user taps the input field
        // InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideSearch() {
        searchContainer.setVisibility(View.GONE);
        searchToggleButton.setVisibility(View.VISIBLE);
        searchEditText.setText("");
        clearSearch();
        
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    private void clearSearch() {
        searchEditText.setText("");
        filteredApps = new ArrayList<>(selectedApps);
        displaySelectedApps();
        clearSearchButton.setVisibility(View.GONE);
    }

    private void filterApps(String query) {
        if (query.isEmpty()) {
            filteredApps = new ArrayList<>(selectedApps);
            clearSearchButton.setVisibility(View.GONE);
        } else {
            filteredApps = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (AppInfo app : selectedApps) {
                String label = app.getLabel() != null ? app.getLabel() : "";
                String packageName = app.getPackageName() != null ? app.getPackageName() : "";
                
                if (label.toLowerCase().contains(lowerQuery) || 
                    packageName.toLowerCase().contains(lowerQuery)) {
                    filteredApps.add(app);
                }
            }
            clearSearchButton.setVisibility(View.VISIBLE);
        }
        displayFilteredApps();
    }

    private void displayFilteredApps() {
        appLinearLayout.removeAllViews();
        for (AppInfo app : filteredApps) {
            addAppToLayout(app);
        }

        if (settingsManager.isShowSettingsIcon() && filteredApps.size() > 0) {
            addSettingsAppToLayout();
        }
        
        if (letterViews != null) {
            updateAlphabeticalIndex();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        if (searchContainer.getVisibility() == View.VISIBLE) {
            hideSearch();
        } else {
            super.onBackPressed();
        }
    }
}
