package quant.testclient.prefs;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import java.util.Set;

import quant.testclient.utils.ResUtils;


/**
 * 配置项管理对象
 *
 * @author momo
 * @version 从prefernceName中取值, 根据角标取值, 并设置.
 * @Date 2014/11/28
 */
public class Prefs {
    // 配置项名称
    private static final String DEFAULT_PREFERENCE = "config";
    private static final String DEFAULT_KEY = "config";
    private static final String DEFAULT_VALUE = "-1";
    private static final int DEFAULT_INT_VALUE = -1;

    public static SharedPreferences getSharedPrefs() {
        Context context = ResUtils.getContext();
        return context.getSharedPreferences(ResUtils.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
    }

    public static SharedPreferences.Editor getPrefsEditor() {
        SharedPreferences sharedPreferences = getSharedPrefs();
        return sharedPreferences.edit();
    }

    public static void remove(String key) {
        SharedPreferences.Editor editor = getPrefsEditor();
        editor.remove(key).commit();
    }


    public static String getDefaultValue() {
        SharedPreferences sharedPreferences = getSharedPrefs();
        return sharedPreferences.getString(DEFAULT_KEY, null);
    }


    /**
     * 根据角标获取 默认值 -1;
     *
     * @param key
     * @return
     */
    public static int getInt(String key) {
        SharedPreferences sharedPreferences = getSharedPrefs();
        return sharedPreferences.getInt(String.valueOf(key), DEFAULT_INT_VALUE);
    }

    public static int getInt(String key, int value) {
        int intValue = getInt(key);
        return DEFAULT_INT_VALUE == intValue ? value : intValue;
    }


    /**
     * 根据角标获取 默认值 -1;
     *
     * @param key
     * @return
     */
    public static long getLong(String key) {
        SharedPreferences sharedPreferences = getSharedPrefs();
        return sharedPreferences.getLong(key, DEFAULT_INT_VALUE);
    }


    /**
     * 获得boolean值
     *
     * @param key
     * @return
     */
    public static boolean getBoolean(String key) {
        return 1 == getInt(key);
    }


    /**
     * 根据角标获取 默认值 null;
     *
     * @param key
     * @return
     */
    public static String getString(String key) {
        SharedPreferences sharedPreferences = getSharedPrefs();
        String value = sharedPreferences.getString(String.valueOf(key), null);
        if (DEFAULT_VALUE.equals(value)) {
            value = null;
        }
        return value;
    }

    public static String getString(String key, String defaultValue) {
        String value = getString(key);
        return TextUtils.isEmpty(value) ? defaultValue : value;
    }

    public static void putInt(String key, int value) {
        SharedPreferences.Editor editor = getPrefsEditor();
        editor.putInt(key, value).commit();
    }

    public static void putLong(String key, long value) {
        SharedPreferences.Editor editor = getPrefsEditor();
        editor.putLong(key, value).commit();
    }

    public static void putString(String key, String value) {
        SharedPreferences.Editor editor = getPrefsEditor();
        editor.putString(key, value).commit();
    }


    public static void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = getPrefsEditor();
        editor.putInt(key, value ? 1 : 0).commit();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Set<String> getStringSet(String key) {
        SharedPreferences sharedPrefs = getSharedPrefs();
        return sharedPrefs.getStringSet(key, null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void putSet(String key, Set<String> items) {
        SharedPreferences.Editor editor = getPrefsEditor();
        editor.putStringSet(key, items).commit();
    }

    public static void putFloat(String key, float value) {
        SharedPreferences.Editor editor = getPrefsEditor();
        editor.putFloat(key, value).commit();
    }

    public static float getFloat(String key) {
        SharedPreferences sharedPrefs = getSharedPrefs();
        return sharedPrefs.getFloat(key, 0f);
    }
}
