package com.hamsterbase.burrowui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSelectionActivity extends Activity implements NavigationBar.OnBackClickListener {
    private List<ResolveInfo> allApps;
    private Set<String> selectedApps = new HashSet<>();
    private ListView appListView;
    private AppAdapter appAdapter;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_app_selection);
        appListView = findViewById(R.id.appListView);

        settingsManager = new SettingsManager(this);
        loadApps();
        appAdapter = new AppAdapter();
        appListView.setAdapter(appAdapter);

        NavigationBar navigationBar = findViewById(R.id.navigation_bar);
        navigationBar.setListView(appListView);
        navigationBar.setOnBackClickListener(this);
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> tempApps = pm.queryIntentActivities(mainIntent, 0);
        allApps = new ArrayList<>();
        String currentPackageName = getPackageName();

        for (ResolveInfo info : tempApps) {
            if (!info.activityInfo.packageName.equals(currentPackageName)) {
                allApps.add(info);
            }
        }

        selectedApps = settingsManager.getSelectedApps();
    }

    public void onBackClick() {
        finish();
    }

    private class AppAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return allApps.size();
        }

        @Override
        public Object getItem(int position) {
            return allApps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(AppSelectionActivity.this).inflate(R.layout.settings_app_item, parent, false);
                holder = new ViewHolder();
                holder.appIcon = convertView.findViewById(R.id.appIcon);
                holder.appName = convertView.findViewById(R.id.appName);
                holder.appCheckImage = convertView.findViewById(R.id.appCheckImage);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ResolveInfo app = allApps.get(position);
            holder.appIcon.setImageDrawable(app.loadIcon(getPackageManager()));
            holder.appName.setText(app.loadLabel(getPackageManager()));

            boolean isSelected = selectedApps.contains(app.activityInfo.packageName);
            holder.appCheckImage.setImageResource(isSelected ? R.drawable.ic_checked : R.drawable.ic_unchecked);

            convertView.setOnClickListener(v -> {
                boolean newState = !selectedApps.contains(app.activityInfo.packageName);
                if (newState) {
                    selectedApps.add(app.activityInfo.packageName);
                } else {
                    selectedApps.remove(app.activityInfo.packageName);
                }
                holder.appCheckImage.setImageResource(newState ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                settingsManager.setSelectedApps(selectedApps);
            });

            return convertView;
        }

        private class ViewHolder {
            ImageView appIcon;
            TextView appName;
            ImageView appCheckImage;
        }
    }
}
