package quant.testclient.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.List;

public class PackageUtils {

    private static String packageName;

    static {
        packageName = getPackageName();
    }

    /**
     * 判断进程是否运行
     * @return
     */
    public static boolean isProessRunning(Context context, String proessName) {

        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> lists = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : lists) {
            if (info.processName.equals(proessName)) {
                isRunning = true;
            }
        }
        return isRunning;
    }

    public static String getPackageName() {
        if (packageName == null) {
            Context context = ResUtils.getContext();
            if (null!=context) {
                packageName = context.getPackageName();
            } else {
                try {
                    final Class<?> activityThreadClass = PackageUtils.class.getClassLoader().loadClass("android.app.ActivityThread");
                    final Method currentPackageName = activityThreadClass.getDeclaredMethod("currentPackageName");
                    packageName = (String) currentPackageName.invoke(null);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return packageName;
    }

    public static ApplicationInfo getAppInfo() {
        ApplicationInfo appInfo = null;
        try {
            Context appContext = ResUtils.getContext();
            appInfo = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appInfo;
    }

    /**
     * 获得string mataData数据
     *
     * @param key
     * @return
     */
    public static String getStringMataData(String key) {
        ApplicationInfo appInfo = getAppInfo();
        String value = null;
        if (null != appInfo) {
            value = appInfo.metaData.getString(key);
        }
        return value;
    }

    /**
     * 获得boolean mataData数据
     *
     * @param key
     * @return
     */
    public static boolean getBooleanMataData(String key) {
        ApplicationInfo appInfo = getAppInfo();
        boolean value = false;
        if (null != appInfo) {
            try {
                value = appInfo.metaData.getBoolean(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    /**
     * 获得软件版本
     *
     * @return
     */
    public static String getAppVersion() {
        String appVersion = "";
        try {
            Context context = ResUtils.getContext();
            appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appVersion;
    }

    /**
     * 获得软件版本
     *
     * @return
     */
    public static int getAppVersionCode() {
        int appVersionCode = -1;
        try {
            Context context = ResUtils.getContext();
            appVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appVersionCode;
    }

    /**
     * 获得软件名称
     *
     * @return
     */
    public static String getApplicationName() {
        String appName = null;
        try {
            Context context = ResUtils.getContext();
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            CharSequence label = packageManager.getApplicationLabel(applicationInfo);
            if (!TextUtils.isEmpty(label)) {
                appName = label.toString();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }




    /**
     * 检查某个应用是否安装
     *
     * @param packageName
     */
    public static boolean appIsInstall(String packageName) {
        boolean install = false;
        Context context = ResUtils.getContext();
        if (!TextUtils.isEmpty(packageName) && null != context) {
            try {
                install = (null != context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES));
            } catch (PackageManager.NameNotFoundException e) {
                install = false;
            }
        }
        return install;
    }

    /**
     * 获得启动activity对象
     *
     * @return
     */
    public static String getLancherActivity() {
        Context context = ResUtils.getContext();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(getPackageName());
        ComponentName component = launchIntent.getComponent();
        return component.getClassName();
    }


    /**
     * 启用设置界面
     *
     * @param context
     */
    public static void startSetting(Context context) {
        if (null != context) {
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                context.startActivity(intent);
            } catch (Exception e) {
            }
        }
    }
}
