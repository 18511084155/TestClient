package quant.testclient.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.cz.loglibrary.JLog;

import java.util.ArrayList;
import java.util.List;

import quant.testclient.service.impl.AbsAccessibilityItem;
import quant.testclient.service.impl.ApkInstallAccessbilityItem;


public class ClientService extends AccessibilityService {
    private List<AbsAccessibilityItem> accessibilityItems;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        ensureAccessibilityItem();
        //设置根节点
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){
            for(AbsAccessibilityItem item:accessibilityItems){
                item.setRootInActiveWindow(getRootInActiveWindow());
            }
        }
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                //通知栏事件
                List<CharSequence> texts = event.getText();
                    if (!texts.isEmpty()) {
                        for (CharSequence text : texts) {
                            for(AbsAccessibilityItem item:accessibilityItems){
                                item.onNotification(event,text.toString());
                            }
                        }
                    }
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                for(AbsAccessibilityItem item:accessibilityItems){
                    item.onWindowChanged(event);
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:// 界面内容改变调用
                for(AbsAccessibilityItem item:accessibilityItems){
                    item.onWindowContentChanged(event);
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:// 界面状态变化调用
                for(AbsAccessibilityItem item:accessibilityItems){
                    item.onWindowStatusChanged(event);
                }
                break;
            default:
                for(AbsAccessibilityItem item:accessibilityItems){
                    item.onDefaultEvent(eventType,event);
                }
        }
    }


    @Override
    public void onInterrupt() {
        Toast.makeText(this, "抢红包服务关闭", Toast.LENGTH_SHORT).show();
    }


    private void ensureAccessibilityItem(){
        if(null==accessibilityItems){
            accessibilityItems=new ArrayList<>();
            Runnable backAction=null;
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){
                backAction=()->performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            }
            //小米服务
            accessibilityItems.add(new ApkInstallAccessbilityItem(backAction));
        }
    }

    @Override
    protected void onServiceConnected() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR2){
            AccessibilityServiceInfo info = getServiceInfo();
            info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
            info.notificationTimeout = 100;
            setServiceInfo(info);
        }
        super.onServiceConnected();
        Toast.makeText(this, "辅助服务启动", Toast.LENGTH_SHORT).show();
        JLog.e("辅助服务己启动!");
    }
}
