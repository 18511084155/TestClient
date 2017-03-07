package quant.testclient.service.install;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import quant.testclient.service.AccessibilityCallback;

/**
 * Created by cz on 2017/3/7.
 */

public class DefaultApkInstall extends AbsInstall {
    public DefaultApkInstall(AccessibilityCallback callback) {
        super(callback);
    }

    @Override
    public void onWindowContentChanged(AccessibilityEvent event) {
        callback.findNodes("正在通过USB",item->
                callback.findNodes(new String[]{"继续安装","安装","允许"},it->!it.isClickable(),node->node.performAction(AccessibilityNodeInfo.ACTION_CLICK)));
    }
}
