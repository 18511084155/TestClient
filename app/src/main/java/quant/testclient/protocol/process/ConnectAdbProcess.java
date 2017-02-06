package quant.testclient.protocol.process;

import quant.testclient.callback.ServiceCallback;
import quant.testclient.protocol.IProcessor;

/**
 * Created by cz on 2017/2/6.
 */

public class ConnectAdbProcess extends IProcessor {
    public ConnectAdbProcess(ServiceCallback callback,String address, String message) {
        super(callback, address,message);
    }

    @Override
    public void process() {

    }
}
