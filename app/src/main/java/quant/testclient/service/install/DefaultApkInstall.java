package quant.testclient.service.install;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cz.loglibrary.JLog;

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
        if(!installing){
            JLog.e("开始查找节点");
            callback.findNodes("正在通过USB",item-> {
                JLog.e("查找到节点:"+item.getText());
                callback.findNodes(new String[]{"继续安装","安装","允许"},it->!it.isClickable(),node->{
                    JLog.e("点击节点:"+node.getText());
                    installing=true;
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                });
            });
        }
    }
}
