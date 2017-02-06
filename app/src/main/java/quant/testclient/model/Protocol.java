package quant.testclient.model;

/**
 * Created by cz on 2017/2/6.
 */

public class Protocol {
    public final int what;
    public final String address;
    public final String message;

    public Protocol(int what,String address, String message) {
        this.what = what;
        this.address=address;
        this.message = message;
    }

    @Override
    public String toString() {
        return "what:"+what+" address:"+address+" message:"+message;
    }
}
