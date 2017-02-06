package quant.testclient.callback;

/**
 * Created by cz on 2017/2/6.
 */

public interface ServiceCallback {
    /**
     * 发送本地信息
     * @param what
     * @param message
     */
    void sendMessage(int what,Object message);

    /**
     * 发送 socket 信息
     * @param what
     * @param message
     */
    void sendSocketMessage(int what,String address,String message);
}
