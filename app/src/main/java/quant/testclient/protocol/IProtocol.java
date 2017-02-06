package quant.testclient.protocol;

/**
 * Created by cz on 2017/2/6.
 */

public interface IProtocol<P extends IProcessor> {
    P create(int what);
}
