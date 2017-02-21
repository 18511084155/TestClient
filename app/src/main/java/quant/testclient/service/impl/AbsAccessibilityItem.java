package quant.testclient.service.impl;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cz.loglibrary.JLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import quant.testclient.callback.Condition;
import rx.functions.Action1;

/**
 * Created by cz on 16/1/12.
 * 抽象的事件处理
 */
public abstract class AbsAccessibilityItem {
    static final int TIME_OUT=2*1000;
    static final String EDIT_CLASS = "android.widget.EditText";
    static final String LIST_VIEW = "android.widget.AbsListView";
    static final String TEXT_VIEW = "android.widget.TextView";
    static final String IMAGE_VIEW = "android.widget.ImageView";
    static final String TAB_HOST = "android.widget.TabHost";
    static final String BUTTON = "android.widget.Button";
    protected static final Handler handler;
    private AccessibilityNodeInfo rootNode;
    private Runnable findAction;
    private Runnable backAction;
    private int scanTime;//检测时间

    static {
        handler = new Handler(Looper.getMainLooper());
    }

    public AbsAccessibilityItem(Runnable backAction) {
        this.backAction = backAction;
    }

    /**
        * 处理通知
                *
        * @param text
                * @return
        */
        public abstract void onNotification(AccessibilityEvent event, String text);

        /**
         * 界面变化判断
         *
         * @param event
         */
    public abstract void onWindowStatusChanged(AccessibilityEvent event);

    /**
     * 当窗体内容变化
     * @param event
     */
    public abstract void onWindowContentChanged(AccessibilityEvent event);

    public abstract void onDefaultEvent(int type,AccessibilityEvent event);


    public void onWindowChanged(AccessibilityEvent event){
        //移除所有查找事件
    }


    /**
     * 设置当前根节点
     *
     * @return
     */
    public void setRootInActiveWindow(AccessibilityNodeInfo info) {
        this.rootNode = info;
    }

    protected AccessibilityNodeInfo getRootInActiveWindow() {
        return rootNode;
    }


    /**
     * 发送通知
     *
     * @param event
     * @param ui
     */
    protected void sendNotification(AccessibilityEvent event, String ui) {
        if (null != event.getParcelableData() && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
            }
        }
    }


    public ArrayList<AccessibilityNodeInfo> findNodeByClass(String clazz) {
        return findNodeByClass(null, clazz);
    }

    /**
     * 根据类名查找指定节点
     *
     * @param clazz
     * @return
     */
    public ArrayList<AccessibilityNodeInfo> findNodeByClass(AccessibilityNodeInfo rootNode, String clazz) {
        if (null == rootNode) {
            rootNode = getRootInActiveWindow();
        }
        ArrayList<AccessibilityNodeInfo> findNodes = new ArrayList<>();
        LinkedList<AccessibilityNodeInfo> nodeInfos = new LinkedList<>();
        nodeInfos.add(rootNode);
        while (!nodeInfos.isEmpty()) {
            AccessibilityNodeInfo node = nodeInfos.removeFirst();
            if (null != node) {
                int childCount = node.getChildCount();
                if (0 < childCount) {
                    for (int i = 0; i < childCount; i++) {
                        AccessibilityNodeInfo child = node.getChild(i);
                        nodeInfos.add(child);
                    }
                }
                CharSequence className = node.getClassName();
                if (!TextUtils.isEmpty(className) && !TextUtils.isEmpty(clazz) && clazz.equals(className.toString())) {
                    findNodes.add(node);
                }
            }
        }
        return findNodes;
    }

    protected void findNodes(String text, Action1<AccessibilityNodeInfo> action){
        findNodes(new String[]{text},null,action);
    }

    /**
     * 等待一个节点
     *
     * @param textArray
     */
    protected void findNodes(String[] textArray, Condition<AccessibilityNodeInfo> condition, Action1<AccessibilityNodeInfo> action) {
        removeAction();
        findAction = () -> {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (null != rootNode) {
                List<AccessibilityNodeInfo> findNodes =null;
                for(String text:textArray){
                    findNodes = rootNode.findAccessibilityNodeInfosByText(text);
                    JLog.e("findNode:"+text+findNodes);
                    if (!findNodes.isEmpty()) {
                        for(Iterator<AccessibilityNodeInfo> iterator = findNodes.iterator(); iterator.hasNext();){
                            AccessibilityNodeInfo node = iterator.next();
                            if(null!=condition&&condition.condition(node)){
                                iterator.remove();
                            }
                        }
                        if(!findNodes.isEmpty()){
                            break;
                        }
                    }
                }
                JLog.e("findResult:"+findNodes);
                if(null!=findNodes&&!findNodes.isEmpty()){
                    removeAction();
                    if(null!=action){
                        action.call(findNodes.get(0));
                    }
                } else if(scanTime<TIME_OUT){
                    //以每100毫秒重新检测
                    scanTime+=100;
                    handler.postDelayed(findAction, 100);
                } else {
                    removeAction();
                }
            }
        };
        handler.post(findAction);
    }

    /**
     * 移除事件
     */
    protected void removeAction() {
        scanTime=0;
        handler.removeCallbacks(findAction);
    }


    /**
     * 返回
     */
    protected void goBack() {
        if (null != backAction) {
            backAction.run();
        }
    }

}
