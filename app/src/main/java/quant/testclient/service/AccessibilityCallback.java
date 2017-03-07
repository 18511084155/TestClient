package quant.testclient.service;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;

import quant.testclient.callback.Condition;
import rx.functions.Action1;

/**
 * Created by cz on 2017/3/7.
 */

public interface AccessibilityCallback {
    ArrayList<AccessibilityNodeInfo> findNodeByClass(AccessibilityNodeInfo rootNode, String clazz);

    void onWindowContentChanged(AccessibilityEvent event);

    void findNodes(String text, Action1<AccessibilityNodeInfo> action);

    void findNodes(String[] textArray, Condition<AccessibilityNodeInfo> condition, Action1<AccessibilityNodeInfo> action);
}
