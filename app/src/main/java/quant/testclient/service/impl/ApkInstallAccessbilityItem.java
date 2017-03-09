package quant.testclient.service.impl;

import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import java.util.HashMap;
import java.util.Map;

import quant.testclient.service.install.AbsInstall;
import quant.testclient.service.install.DefaultApkInstall;
import quant.testclient.service.install.OppoApkInstall;
import quant.testclient.service.install.XiaoMiApkInstall;

/**
 * Created by cz on 2017/2/21.
 */

public class ApkInstallAccessbilityItem extends AbsAccessibilityItem  {
    private static final String BRAND_OPPO="oppo";
    private static final String BRAND_XIAOMI="xiaomi";
    public static final String BRAND_DEFAULT="default";
    private final Map<String,AbsInstall> apkInstallItems=new HashMap<>();

    public ApkInstallAccessbilityItem(Runnable backAction) {
        super(backAction);
        apkInstallItems.put(BRAND_OPPO,new OppoApkInstall(this));
        apkInstallItems.put(BRAND_XIAOMI,new XiaoMiApkInstall(this));
        apkInstallItems.put(BRAND_DEFAULT,new DefaultApkInstall(this));
    }

    @Override
    public void onNotification(AccessibilityEvent event, String text) {

    }

    @Override
    protected int getTimeOut() {
        return 10*1000;
    }

    @Override
    public void onWindowStatusChanged(AccessibilityEvent event) {
    }

    @Override
    public void onWindowContentChanged(AccessibilityEvent event) {
        AbsInstall absInstall = apkInstallItems.get(Build.BRAND.toLowerCase());
        if(null==absInstall){
            //默认为空,取默认处理
            absInstall = apkInstallItems.get(BRAND_DEFAULT);
        }
        //直接处理
        if(null!=absInstall){
            absInstall.onWindowContentChanged(event);
        }
    }

    @Override
    public void onDefaultEvent(int type, AccessibilityEvent event) {
    }

}
