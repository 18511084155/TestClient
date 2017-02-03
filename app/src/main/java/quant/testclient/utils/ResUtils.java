package quant.testclient.utils;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.widget.Toast;

import java.lang.reflect.Method;


/**
 * 资源帮助工具类
 */
public class ResUtils {

    private static Resources appResource;
    private static String appPackageName;
    private static Context appContext;


    static {
        appContext = getContext();
    }

    public static Context getContext() {
        if (appContext == null) {
            try {
                final Class<?> activityThreadClass = ResUtils.class.getClassLoader().loadClass("android.app.ActivityThread");
                final Method currentActivityThread = activityThreadClass.getDeclaredMethod("currentActivityThread");
                final Object activityThread = currentActivityThread.invoke(null);
                final Method getApplication = activityThreadClass.getDeclaredMethod("getApplication");
                final Application application = (Application) getApplication.invoke(activityThread);
                appContext = application.getApplicationContext();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return appContext;
    }

    public static final String getPackageName() {
        if (null == appPackageName) {
            Context context = getContext();
            appPackageName = context.getPackageName();
        }
        return appPackageName;
    }

    public static ContentResolver getContentResolver() {
        Context context = getContext();
        return context.getContentResolver();
    }

    private static final Resources resources() {
        if (null == appResource) {
            Context context = getContext();
            appResource = context.getResources();
        }
        return appResource;
    }

    private static final int identifier(String name, String type) {
        return resources().getIdentifier(name, type, getPackageName());
    }

    public static final int string(String name) {
        return identifier(name, "string");
    }

    public static final int drawable(String name) {
        return identifier(name, "drawable");
    }

    public static final int id(String name) {
        return identifier(name, "id");
    }

    public static final int attr(String name) {
        return identifier(name, "attr");
    }

    public static final int layout(String name) {
        return identifier(name, "layout");
    }

    public static final int menu(String name) {
        return identifier(name, "menu");
    }

    public static final int style(String name) {
        return identifier(name, "style");
    }

    public static int integer(String name) {
        return identifier(name, "integer");
    }

    public static int anim(String name) {
        return identifier(name, "anim");
    }

    public static int raw(String name) {
        return identifier(name, "raw");
    }

    public static int color(String name) {
        return identifier(name, "color");
    }

    public static int array(String name) {
        return identifier(name, "array");
    }

    public static int getItemId(String name) {
        return identifier(name, "id");
    }

    public static int getLayoutId(String name) {
        return identifier(name, "layout");
    }

    public static int getIdentifier(String name, String type) {
        return identifier(name, type);
    }

    public static int getDrawableId(String name) {
        return identifier(name, "drawable");
    }


    public static SharedPreferences getSharedPreferences(String name) {
        return appContext.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static Drawable getDrawable(@DrawableRes int drawable) {
        return appContext.getResources().getDrawable(drawable);
    }

    public static String getString(int resId, Object... formatArgs) {
        return appContext.getString(resId, formatArgs);
    }

    public static int getColor(int id) {
        return appContext.getResources().getColor(id);
    }

    public static String[] getStringArray(int array) {
        return appContext.getResources().getStringArray(array);
    }

    public static float getDimension(int dimension) {
        return appContext.getResources().getDimension(dimension);
    }

    public static int[] getIntArray(int id) {
        return appContext.getResources().getIntArray(id);
    }

    public static int dip2px(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, appContext.getResources().getDisplayMetrics());
    }


    public static int px2dip(float pxValue) {
        final float scale = appContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(float pxValue) {
        int value = 0;
        if (null != appContext) {
            value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, pxValue, appContext.getResources().getDisplayMetrics());
        }
        return value;
    }

    public static float px2sp(float px) {
        float scaledDensity = appContext.getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }

    public static float getDimenAttr(@AttrRes int attr) {
        int[] attribute = new int[]{attr};
        TypedArray array = appContext.obtainStyledAttributes(null, attribute);
        return array.getDimensionPixelSize(0, -1);
    }

    public static void toast(String text) {
        Toast.makeText(appContext, text, Toast.LENGTH_SHORT).show();
    }

    public static void toast(@StringRes int res, Object... params) {
        Toast.makeText(appContext, getString(res, params), Toast.LENGTH_SHORT).show();
    }


}
