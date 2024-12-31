package com.hamsterbase.burrowui.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;

import com.hamsterbase.burrowui.SettingsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppManagementService {

    private static AppManagementService instance;
    private final Context context;
    private final Map<String, Drawable> iconCache;
    private final Map<String, UserHandle> userCache;
    private final LauncherApps launcherApps;
    private final UserManager userManager;
    private final PackageManager packageManager;

    private AppManagementService(Context context) {
        this.context = context.getApplicationContext();
        this.iconCache = new HashMap<>();
        this.userCache = new HashMap<>();
        this.launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        this.packageManager = context.getPackageManager();
        initUserCache();
    }

    public static synchronized AppManagementService getInstance(Context context) {
        if (instance == null) {
            instance = new AppManagementService(context);
        }
        return instance;
    }

    private void initUserCache() {
        List<UserHandle> users = userManager.getUserProfiles();
        for (UserHandle user : users) {
            userCache.put(String.valueOf(user.hashCode()), user);
        }
    }

    public List<AppInfo> listApps() {


        List<AppInfo> appInfoList = new ArrayList<>();
        UserHandle currentUser = android.os.Process.myUserHandle();
        int currentUserId = currentUser.hashCode();

        List<ApplicationInfo> applications = packageManager.getInstalledApplications(0);


        for (ApplicationInfo appInfo : applications) {
            Intent launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName);
            appInfoList.add(new AppInfo(
                    appInfo.loadLabel(packageManager).toString(),
                    appInfo.packageName,
                    null,
                    launchIntent != null ? launchIntent.getComponent().toString() : null
            ));
        }

        List<UserHandle> users = userManager.getUserProfiles();
        for (UserHandle user : users) {
            if (user.hashCode() != currentUserId) {
                List<LauncherActivityInfo> activities = launcherApps.getActivityList(null, user);
                for (LauncherActivityInfo activityInfo : activities) {
                    appInfoList.add(new AppInfo(
                            activityInfo.getLabel().toString(),
                            activityInfo.getApplicationInfo().packageName,
                            String.valueOf(user.hashCode()),
                            activityInfo.getComponentName().toString()
                    ));
                }
            }
        }

        return appInfoList;
    }


    public Drawable getIcon(String packageName, String userId) {
        String cacheKey = packageName + (userId != null ? ":" + userId : "");

        Drawable cachedIcon = iconCache.get(cacheKey);
        if (cachedIcon != null) {
            return cachedIcon;
        }

        return loadIconFromLauncherApps(packageName, userId);
    }

    private Drawable loadIconFromLauncherApps(String packageName, String userId) {
        if (userId == null) {
            try {
                return packageManager.getApplicationIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        }
        UserHandle userHandle = userCache.get(userId);
        if (userHandle == null) {
            return null;
        }
        try {
            if (iconCache.get(packageName) == null) {
                iconCache.put(packageName, copyIcon(packageManager.getApplicationIcon(packageName)));
            }
        } catch (PackageManager.NameNotFoundException e) {
            //
        }
        List<LauncherActivityInfo> activities = launcherApps.getActivityList(packageName, userHandle);
        if (!activities.isEmpty()) {
            String cacheKey = packageName + ":" + userId;
            Drawable icon = activities.get(0).getIcon(0);
            iconCache.put(cacheKey, icon);
            return icon;
        }
        return null;
    }

    public Drawable copyIcon(Drawable icon) {
        if (icon instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
            return new BitmapDrawable(context.getResources(), bitmap.copy(bitmap.getConfig(), true));
        }
        return icon.getConstantState().newDrawable().mutate();
    }


    public boolean isAppSelected(AppInfo app, List<SettingsManager.SelectedItem> selectedItems) {
        for (SettingsManager.SelectedItem item : selectedItems) {
            if (isSelectItemEqualWith(app, item)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSelectItemEqualWith(AppInfo app, SettingsManager.SelectedItem item) {
        if (item.getType().equals("application")
                && app.getPackageName().equals(item.getMeta().get("packageName"))) {
            if (app.getComponentName() != null && !app.getComponentName().equals(item.getMeta().get("componentName"))) {
                return false;
            }
            if (app.getComponentName() == null && item.getMeta().get("componentName") != null) {
                return false;
            }
            if (app.getUserId() == null) {
                return item.getMeta().get("userId") == null;
            } else {
                return app.getUserId().equals(item.getMeta().get("packageName"));
            }
        }
        return false;
    }

    public SettingsManager.SelectedItem to(AppInfo app) {
        Map<String, String> meta = new HashMap<>();
        meta.put("packageName", app.getPackageName());
        meta.put("userId", app.getUserId());
        meta.put("componentName", app.getComponentName());
        return new SettingsManager.SelectedItem("application", meta);
    }

    public void launchApp(AppInfo app) {
        UserHandle currentUser = android.os.Process.myUserHandle();
        if (app.getUserId() != null) {
            currentUser = userCache.get(app.getUserId());
        }
        List<LauncherActivityInfo> activities = launcherApps.getActivityList(app.getPackageName(), currentUser);
        String packageName = app.getPackageName();
        // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
        String className = "io.iftech.android.podcast.app.home.main.view.MainActivity";
        // LAUNCHER Intent
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // 设置ComponentName参数1:packagename参数2:MainActivity路径
        ComponentName cn = new ComponentName(packageName, className);

        intent.setComponent(cn);
        context.startActivity(intent);
    }


    private void doStartApplicationWithPackageName(String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = context.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            context.startActivity(intent);
        }
    }
}
