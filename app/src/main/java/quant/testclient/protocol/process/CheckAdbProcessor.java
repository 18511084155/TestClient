package quant.testclient.protocol.process;

import android.text.TextUtils;

import quant.testclient.R;
import quant.testclient.callback.ServiceCallback;
import quant.testclient.model.What;
import quant.testclient.protocol.IProcessor;
import quant.testclient.utils.ResUtils;
import quant.testclient.utils.ShellService;

/**
 * Created by cz on 2017/2/6.
 * 检测 adb 是否连接,分为2种
 * 1:如果为无线连接,申请 root 权限,申请不到,若 usb 也未连接,则弹出提示
 * 2:为有线连接,检测连接状态.
 */

public class CheckAdbProcessor extends IProcessor {

    public CheckAdbProcessor(ServiceCallback callback,String address, String message) {
        super(callback, address,message);
    }

    @Override
    public void process() {
        sendMessage(What.Socket.LOG, ResUtils.getString(R.string.start_connect_adb_address,address));
        if(!TextUtils.isEmpty(address)){
            if(ShellService.isRoot()){
                //己 root,设备连接端口
                 if(0<=ShellService.setConnectPort()){
                     //设备端口成功
                     sendMessage(What.Socket.LOG, ResUtils.getString(R.string.set_connect_port_complete));
                     sendSocketMessage(What.ADB.CONNECT,address,message);
                 } else {
                     //设置失败
                     sendMessage(What.Socket.LOG, ResUtils.getString(R.string.set_connect_port_failed,message));
                     sendSocketMessage(What.ADB.CHECK_ADB,address,message);
                 }
            } else {
                //未 root,弹出提示窗
                sendMessage(What.ADB.ALERT_ADB_DEBUG,address);
            }
        }
    }
}
