package com.hamsterbase.burrowui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.View;
import android.widget.ImageView;

public class SettingsActivity extends Activity implements NavigationBar.OnBackClickListener {

    private ImageView showSettingsIconSwitch;
    private SettingsManager settingsManager;
    private View selectAppsSection;
    private View settingsIconSection;
    private View aboutSection;
    private View donateSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_settings);
        showSettingsIconSwitch = findViewById(R.id.showSettingsIconSwitch);
        selectAppsSection = findViewById(R.id.selectAppsSection);
        settingsIconSection = findViewById(R.id.settingsIconSection);
        aboutSection = findViewById(R.id.aboutSection);
        donateSection = findViewById(R.id.donateSection);

        settingsManager = new SettingsManager(this);

        setupShowSettingsIconSwitch();

        selectAppsSection.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AppSelectionActivity.class);
            startActivity(intent);
        });

        aboutSection.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        donateSection.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/hamsterbase"));
            startActivity(intent);
        });

        NavigationBar navigationBar = findViewById(R.id.navigation_bar);
        navigationBar.setOnBackClickListener(this);
    }

    private void setupShowSettingsIconSwitch() {
        boolean showSettingsIcon = settingsManager.isShowSettingsIcon();
        updateSwitchImage(showSettingsIcon);
        settingsIconSection.setOnClickListener(v -> {
            boolean newState = !settingsManager.isShowSettingsIcon();
            updateSwitchImage(newState);
            settingsManager.setShowSettingsIcon(newState);
        });
    }

    private void updateSwitchImage(boolean isOn) {
        showSettingsIconSwitch.setImageResource(isOn ? R.drawable.ic_switch_on : R.drawable.ic_switch_off);
    }

    @Override
    public void onBackClick() {
        finish();
    }
}
