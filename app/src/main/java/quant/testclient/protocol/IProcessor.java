package quant.testclient.protocol;

import quant.testclient.callback.ServiceCallback;

/**
 * Created by cz on 2017/2/6.
 */

public abstract class IProcessor {
    public final ServiceCallback callback;
    public final String address;
    public final String message;

    public IProcessor(ServiceCallback callback,String address,String message) {
        this.callback = callback;
        this.address=address;
        this.message =message;
    }

    /**
     * 发送本地消息
     * @param what
     * @param obj
     */
    public void sendMessage(int what,Object obj) {
        if(null!=callback){
            callback.sendMessage(what,obj);
        }
    }

    /**
     * 发送远程消息
     * @param what
     * @param message
     */
    public void sendSocketMessage(int what,String address, String message) {
        if(null!=callback){
            callback.sendSocketMessage(what,address,message);
        }
    }

    /**
     * 处理任务
     */
    public abstract void process();


}
