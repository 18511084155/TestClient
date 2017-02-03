package quant.testclient.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import quant.testclient.utils.DeviceUtils;


public class NetStatusReceiver extends BroadcastReceiver {
    public static final String MOBILE_INFO = "mobile";
    public static final String WIFI_INFO = "WIFI";
    public static final String NONE = "none";
    public static final int NO_WORK = 10;
    public static final int MOBILE = 11;
    public static final int WIFI = 12;
    public static int status;// 当前网络状态
    public static int lastStatus;// 上一次网络状态

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            status = NO_WORK;
            if (info != null && info.isAvailable()) {
                if (MOBILE_INFO.equals(info.getTypeName())) {
                    status = MOBILE;
                } else if (WIFI_INFO.equals(info.getTypeName())) {
                    status = WIFI;
                }
            } else {
                status = NO_WORK;
            }
            if (0 == lastStatus) {
                lastStatus = status;
            }
            if (lastStatus != status && WIFI != status) {
                //当前网络不为wifi,重启wifi
                DeviceUtils.resetWifi(context);
            }
            lastStatus = status;
        }
    }
}
