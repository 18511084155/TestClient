package quant.testclient.receive;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.cz.loglibrary.JLog;

public class ScreenStatusReceiver extends BroadcastReceiver {
    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;
    private PowerManager pm;
    private PowerManager.WakeLock wl;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            JLog.e("----SCREEN_ON----");
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wake:");
            wl.setReferenceCounted(false);
            wl.acquire();
        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {

        }
    }

}