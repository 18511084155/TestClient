package quant.testclient.callback;

/**
 * Created by cz on 2017/2/21.
 */

public interface Condition<T> {
    boolean condition(T t);
}
