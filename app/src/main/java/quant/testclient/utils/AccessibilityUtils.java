package quant.testclient.utils;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import com.cz.loglibrary.JLog;

import quant.testclient.service.ClientService;


/**
 * Created by cz on 16/1/9.
 */
public class AccessibilityUtils {

    /**
     * 检测服务是否开启
     *
     * @param context
     * @return
     */
    public static boolean updateServiceStatus(Context context) {
        boolean serviceEnabled = false;
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/"+ ClientService.class.getName();// 包名+"/"+服务名
        try {
            accessibilityEnabled = Settings.Secure.getInt(context
                            .getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            JLog.e("Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(context
                            .getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        serviceEnabled = true;
                        break;
                    }
                }
            }
        }
        return serviceEnabled;

    }
}
