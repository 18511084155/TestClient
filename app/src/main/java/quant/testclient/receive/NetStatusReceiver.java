package quant.testclient.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import quant.testclient.bus.RxBus;
import quant.testclient.event.NetWorkChangedEvent;


public class NetStatusReceiver extends BroadcastReceiver {
    public static final int TYPE_NONE   = -1;
    public int lastStatus=TYPE_NONE;// 上一次网络状态

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            int status=TYPE_NONE;
            if (info != null && info.isAvailable()) {
                status=info.getType();
            }
            RxBus.post(new NetWorkChangedEvent(status,lastStatus));
            lastStatus = status;
        }
    }
}
