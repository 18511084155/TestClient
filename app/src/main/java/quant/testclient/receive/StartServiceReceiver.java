package quant.testclient.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import java.util.concurrent.Executors;

import quant.testclient.natives.NativeRuntime;
import quant.testclient.service.NotificationService;
import quant.testclient.utils.PackageUtils;
import quant.testclient.utils.PhoneFileUtils;
import quant.testclient.utils.ResUtils;

/**
 * Created by ldfs on 16/2/19.
 */
public class StartServiceReceiver extends BroadcastReceiver {
    private final String serviceName = ResUtils.getPackageName()+"."+ NotificationService.class.getSimpleName();
    private String processName = ResUtils.getPackageName()+":daemon";
    private String TAG = StartServiceReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(TAG, "手机开机了~~");
            startService(context);
        } else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            Log.i(TAG, "屏幕关闭了~~");
            if (!PackageUtils.isProessRunning(context,processName)) {
                startService(context);
            }
        }else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            Log.i(TAG, "屏幕开启了~~");
            if (PackageUtils.isProessRunning(context,processName)) {
                stopService();
            }
        }

    }
    private void startService(Context context){
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                NativeRuntime.getInstance().startService(context.getPackageName() + "/" + serviceName, PhoneFileUtils.createRootPath(context));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void stopService(){
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Log.i(TAG, "服务关闭了~~");
                NativeRuntime.getInstance().stopService();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
