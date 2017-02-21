package quant.testclient.service.impl;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by cz on 2017/2/21.
 */

public class ApkInstallAccessbilityItem extends AbsAccessibilityItem {


    public ApkInstallAccessbilityItem(Runnable backAction) {
        super(backAction);
    }

    @Override
    public void onNotification(AccessibilityEvent event, String text) {

    }

    @Override
    public void onWindowStatusChanged(AccessibilityEvent event) {
        findNodes("正在通过USB",item->{
            findNodes(new String[]{"继续安装","安装","允许"},it->!it.isClickable(),node->node.performAction(AccessibilityNodeInfo.ACTION_CLICK));
        });
    }

    @Override
    public void onWindowContentChanged(AccessibilityEvent event) {
        findNodes("正在通过USB",item->{
            findNodes(new String[]{"继续安装","安装","允许"},it->!it.isClickable(),node->node.performAction(AccessibilityNodeInfo.ACTION_CLICK));
        });
    }

    @Override
    public void onDefaultEvent(int type, AccessibilityEvent event) {
    }

}
