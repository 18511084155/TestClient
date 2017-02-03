package quant.testclient;

/**
 * Created by cz on 2017/2/3.
 */

public interface MessageWhat {
    int LOG=1;//日志
    int CONNECT=2;//连接 socket连接完自动尝试连接 adb
    int DISCONNECT=3;//中断连接
    int CONNECT_COMPLETE=4;
    int CONNECT_FAILED=5;

    int CONNECT_ADB=6;//连接 adb
    int DISCONNECT_ADB=7;//中断 adb
    int CONNECT_ADB_COMPLETE=8;
    int CONNECT_ADB_FAILED=9;
}
