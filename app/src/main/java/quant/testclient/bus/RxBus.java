package quant.testclient.bus;

/**
 * Created by cz on 16/3/18.
 * @update 2016/10/7 更新自动释放
 */

import com.cz.loglibrary.JLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxBus {
    private static final Subject bus = new SerializedSubject<>(PublishSubject.create());
    private static final HashMap<String,List<Subscription>> subscribeItems;
    static {
        subscribeItems=new HashMap<>();
    }

    /**
     * 发送事件
     * @param o
     */
    public static void post(Object o) {
        bus.onNext(o);
    }

    public static<T> void  subscribe(final Class<T> eventType, Action1<T> action1){
        subscribe(eventType,action1,null);
    }

    /**
     * 根据class event type观察事件
     * @param <T>
     * @param eventType
     * @return
     */
    public static<T> void  subscribe(final Class<T> eventType, Action1<T> action1, Action1<Throwable> action2) {
        String className = getCallClass();
        List<Subscription> items = subscribeItems.get(className);
        if(null==items){
            subscribeItems.put(className,items=new ArrayList<>());
        }
        JLog.e("subscribe:"+className);
        Observable observable = bus.filter(eventType::isInstance).cast(eventType).
                subscribeOn(AndroidSchedulers.mainThread());
        if(null!=action1){
            items.add(observable.subscribe(action1,new Action1<Throwable>() {
                @Override
                public void call(Throwable e) {
                    e.printStackTrace();
                    if(null!=action2){
                        action2.call(e);
                    }
                }
            }));
        }
    }

    /**
     * 取消订阅,自动回调的对象,无须用户调用
     * @param object
     */
    public static void unSubscribeItems(Object object) {
        if(null!=object){
            String name = object.getClass().getName();
            List<Subscription> items = subscribeItems.get(name);
            JLog.e("unSubscribeItems:"+name);
            if (null != items) {
                for (int i = 0; i < items.size(); i++) {
                    Subscription subscription = items.get(i);
                    if (null != subscription && !subscription.isUnsubscribed()) {
                        subscription.unsubscribe();
                    }
                }
                items.clear();
            }
        }
    }

    public static void unSubscribeAll(){
        JLog.e("unSubscribeAll");
        for(Map.Entry<String,List<Subscription>> entry:subscribeItems.entrySet()){
            List<Subscription> items = entry.getValue();
            if (null != items) {
                for (int i = 0; i < items.size(); i++) {
                    Subscription subscription = items.get(i);
                    if (null != subscription && !subscription.isUnsubscribed()) {
                        subscription.unsubscribe();
                    }
                }
                items.clear();
            }
        }
    }

    /**
     * 获取当前调用class对象,缓存作tag-event,再利用ActivityLifecycleCallbacks接口自动释放订阅对象
     * @return
     */
    protected static String getCallClass() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int length = stackTrace.length;
        String name = RxBus.class.getName();
        StackTraceElement stackTraceElement = null;
        boolean isCallStackTrace=false;
        for (int i = 0; i < length; i++) {
            stackTraceElement = stackTrace[i];
            if (name.equals(stackTraceElement.getClassName())) {
                isCallStackTrace=true;
            } else if(isCallStackTrace){
                break;
            }
        }
        String className=stackTraceElement.getClassName();
        int index = className.lastIndexOf("$");
        if(-1<index){
            className=className.substring(0,index);
        }
        return className;
    }
}