package quant.testclient.model;

/**
 * Created by cz on 2017/2/3.
 */

public class  What {
    public static class Socket {
        public static final int ID=Socket.class.getSimpleName().hashCode();
        public static final int LOG=ID+1;//日志
        public static final int CONNECT=ID+2;//连接 socket连接完自动尝试连接 adb
        public static final int DISCONNECT=ID+3;//中断连接
        public static final int CONNECT_COMPLETE=ID+4;
        public static final int CONNECT_FAILED=ID+5;
        public static final int CONNECT_STATUS=ID+6;//连接状态
    }

    public static class ADB{
        public static final int ID=ADB.class.getSimpleName().hashCode();
        public static final int CHECK_ADB=ID+1;//检测 adb 状态
        public static final int OFFLINE=ID+2;//检测 adb 状态为 offline
        public static final int CONNECT=ID+3;//连接adb指令
        public static final int SET_PORT=ID+4;//设备无线调试端口
        public static final int CONNECT_COMPLETE=ID+5;//连接成功
        public static final int CONNECT_FAILED=ID+6;//连接失败
        public static final int KILL_SERVER=ID+7;//杀死 adb 服务
        public static final int START_SERVER=ID+8;//启动 adb 服务
        public static final int LOG=ID+9;
    }


}
