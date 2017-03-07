package quant.testclient.service.install;

import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cz.loglibrary.JLog;

import quant.testclient.service.AccessibilityCallback;

/**
 * Created by cz on 2017/3/7.
 */

public class OppoApkInstall extends AbsInstall{
    private static final String APP_INSTALL_PACKAGE ="com.android.packageinstaller";

    public OppoApkInstall(AccessibilityCallback callback) {
        super(callback);
    }

    @Override
    public void onWindowContentChanged(AccessibilityEvent event) {
        CharSequence packageName = event.getPackageName();
        if(!installing&&!TextUtils.isEmpty(packageName)&& APP_INSTALL_PACKAGE.equals(packageName.toString())){
            //这里记录一个事件,一直检测并执行下去
            callback.findNodes(new String[]{"继续安装","安装"},it->!it.isClickable(),node->{
                if(node.performAction(AccessibilityNodeInfo.ACTION_CLICK)){
                    installing=true;
                    CharSequence text = node.getText();
                    if("继续安装".equals(text.toString())){
                        callback.findNodes("安装",installNode-> {
                            //检测完成
                            JLog.e("installNode-complete: install继续安装:"+text);
                            if(installNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)){
                                callback.findNodes("完成", completeNote -> {
                                    installing=false;
                                    completeNote.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                });
                            }
                        });
                    } else if("安装".equals(text.toString())){
                        //检测完成
                        callback.findNodes("完成", completeNote -> {
                            installing=false;
                            completeNote.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        });
                    }
                }
            });
        }
    }
}
