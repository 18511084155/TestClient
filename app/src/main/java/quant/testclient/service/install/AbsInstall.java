package quant.testclient.service.install;

import android.view.accessibility.AccessibilityEvent;

import quant.testclient.service.AccessibilityCallback;

/**
 * Created by cz on 2017/3/7.
 */

public abstract class AbsInstall {
    protected final AccessibilityCallback callback;
    protected boolean installing;

    public AbsInstall(AccessibilityCallback callback) {
        this.callback = callback;
    }

    public abstract void onWindowContentChanged(AccessibilityEvent event);
}
