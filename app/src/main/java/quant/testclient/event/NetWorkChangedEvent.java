package quant.testclient.event;

/**
 * Created by cz on 2017/2/7.
 */

public class NetWorkChangedEvent {
    public final int currentType;
    public final int lastType;

    public NetWorkChangedEvent(int currentType, int lastType) {
        this.currentType = currentType;
        this.lastType = lastType;
    }
}
