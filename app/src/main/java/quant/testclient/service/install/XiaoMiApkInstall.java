package quant.testclient.service.install;

import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import quant.testclient.service.AccessibilityCallback;

/**
 * Created by cz on 2017/3/7.
 */

public class XiaoMiApkInstall extends AbsInstall{
    private static final String APP_INSTALL_PACKAGE ="com.miui.securitycenter";

    public XiaoMiApkInstall(AccessibilityCallback callback) {
        super(callback);
    }

    @Override
    public void onWindowContentChanged(AccessibilityEvent event) {
        CharSequence packageName = event.getPackageName();
        if(!installing&&!TextUtils.isEmpty(packageName)&& APP_INSTALL_PACKAGE.equals(packageName.toString())){
            //这里记录一个事件,一直检测并执行下去
            installing=true;
            callback.findNodes(new String[]{"继续安装","允许"},it->!it.isClickable(),node->{
                installing=false;
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            });
        }
    }
}
