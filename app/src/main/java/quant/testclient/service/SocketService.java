package quant.testclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import quant.testclient.Constant;
import quant.testclient.R;
import quant.testclient.callback.ServiceCallback;
import quant.testclient.file.FileObserver;
import quant.testclient.file.FilePrefs;
import quant.testclient.model.Json;
import quant.testclient.model.Protocol;
import quant.testclient.model.What;
import quant.testclient.protocol.process.CheckAdbProcessor;
import quant.testclient.protocol.process.ConnectAdbProcess;
import quant.testclient.utils.DeviceUtils;
import quant.testclient.utils.IOUtils;
import quant.testclient.utils.ResUtils;
import quant.testclient.utils.StringUtils;


/**
 * Created by cz on 2017/2/3.
 */
public class SocketService extends Service implements ServiceCallback{
    public static final String TAG="SocketService";
    private volatile Looper serviceLooper;
    private volatile ServiceHandler serviceHandler;
    private final Runnable examiner;
    private Messenger currentReply;
    //socket 是否连接检测对象
    private Handler handler;
    private boolean isLooper;
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader reader;
    private FileObserver fileObserver;
    private int reconnectCount;


    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            if(What.Socket.CONNECT==msg.what){
                //连接 socket,连接完自动尝试连接 adb,一旦连接成功,此线程会被阻塞
                if(null!=msg.obj&& StringUtils.validateAddress(msg.obj.toString())){
                    try {
                        connectSocket(msg.obj.toString());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public SocketService() {
        examiner = new Runnable() {
            @Override
            public void run() {
                if (!socketIsConnect(socket)) {
                    closeSocket();
                } else {
                    //如果正在执行,反复检测
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler=new Handler(msg -> {
            this.currentReply=msg.replyTo;
            //主线程发送到子线程执行
            if(What.Socket.CONNECT==msg.what){
                //发送给 handleService连接 socket
                if(null!=msg.obj&& StringUtils.validateAddress(msg.obj.toString())){
                    ensureSocket(msg.obj.toString());
                    if(!isLooper){
                        serviceHandler.sendMessage(Message.obtain(msg));
                    }
                }
            } else if(What.ADB.CONNECT==msg.what){
                //连接 adb
                if(null!=msg.obj&& StringUtils.validateAddress(msg.obj.toString())){
                    ensureSocket(msg.obj.toString());
                    sendSocketMessage(msg.what,msg.obj.toString(),msg.obj.toString());
                }
            } else if(What.Socket.DISCONNECT==msg.what){
                //中断socket 连接
                if(socketIsConnect(socket)){
                    isLooper =false;
                    closeSocket();
                    sendMessage(What.Socket.DISCONNECT,!socketIsConnect(socket));
                }
            }
            return true;
        });
    }


    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("serviceThread");
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
        fileObserver=new FileObserver(getApplication(),FilePrefs.PROP_FOLDER.getAbsolutePath());
        fileObserver.startWatching();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Messenger(handler).getBinder();
    }

    /**
     * 检测 socket 连接状态,如果己连接,而连接 ip 不同,则关闭.否则未连接而 socket 未关闭,直接关闭
     * @param address
     */
    private void ensureSocket(String address) {
        if (null != socket) {
            if (socketIsConnect(socket)) {
                //检测连接Socket地址是否一致
                final String hostAddress = socket.getInetAddress().getHostAddress();
                //连接地址不同,重新连接
                if (!address.equals(hostAddress)){
                    closeSocket();
                } else if(isLooper){
                    sendMessage(What.Socket.LOG,ResUtils.getString(R.string.current_connecting,address));
                } else {
                    sendMessage(What.Socket.LOG,ResUtils.getString(R.string.already_connect,address));
                }
            } else if (!socket.isClosed()) {
                closeSocket();
            }
        }
    }

    /**
     * 连接socket
     *
     * @param address
     */
    public void connectSocket(String address) throws RemoteException {
        isLooper =true;
        while (isLooper) {
            Log.e(TAG, "connect:" + socketIsConnect(socket));
            if (socketIsConnect(socket)) {
                String line;
                try {
                    closeReaderStream();
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (null!=reader&&null!=(line = reader.readLine())) {
                        Log.e(TAG,"while:"+(null!=socket));
                        if(socketIsConnect(socket)){
                            //处理数据
                            processProtocol(Json.getObject(Protocol.class, line));
                        } else if(isLooper){
                            connectSocket(address);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.closeStream(reader);
                    if(isLooper) connectSocket(address);
                }
            } else {
                if (null != socket) {
                    sendMessage(What.Socket.LOG, ResUtils.getString(R.string.reconnect_count_value,reconnectCount));
                } else if(0<reconnectCount){
                    //重联多次信息
                    sendMessage(What.Socket.CONNECT_STATUS,ResUtils.getString(R.string.reconnect_count_value,reconnectCount));
                } else {
                    //第一次开始联接
                    sendMessage(What.Socket.LOG,ResUtils.getString(R.string.start_connect));
                }
                //连接断开,重联
                try {
                    closeSocket();
                    socket = new Socket(address, Constant.PORT);
                    socket.setSoTimeout(0);
                    socket.setKeepAlive(true);
                    printWriter = new PrintWriter(socket.getOutputStream());

                    sendMessage(What.Socket.CONNECT_COMPLETE);
                    sendMessage(What.Socket.LOG,ResUtils.getString(R.string.connecting));
                    sendMessage(What.Socket.LOG,ResUtils.getString(R.string.server_address_value,address));
                    //重试后连接成功
                    if (0 < reconnectCount) {
                        sendMessage(What.Socket.LOG, ResUtils.getString(R.string.reconnect_complete_value,reconnectCount));
                        reconnectCount = 0;
                    }
//                    //socket连接时,开启一个handler不断检测socket状态,此线程己被阻塞,因此可能存在假死
                    handler.removeCallbacks(examiner);
//                    handler.postDelayed(examiner, 1000);
                } catch (IOException e) {
                    closeSocket();
                    reconnectCount++;
                    sendMessage(What.Socket.CONNECT_FAILED);
                    sendMessage(What.Socket.CONNECT_STATUS,ResUtils.getString(R.string.connect_failed_value,address));
                }
            }
            //延持
            SystemClock.sleep(3000);
        }
        Log.e(TAG,"run finish!");
    }

    /**
     * 处理协议(此处都为 adb 操作)
     * @param protocol
     */
    private void processProtocol(Protocol protocol) {
        String address = DeviceUtils.getAddress();
        Log.e(TAG,"processProtocol:"+protocol);
        if(null!=protocol&&protocol.address.equals(address)){
            if(What.ADB.CHECK_ADB==protocol.what){
                //检测adb 连接
                new CheckAdbProcessor(this,protocol.address,protocol.message).process();
            } else if(What.ADB.CONNECT==protocol.what){
                new ConnectAdbProcess(this,protocol.message,protocol.message).process();
            } else if(What.ADB.CONNECT_COMPLETE==protocol.what){
                //设备连接成功
                sendMessage(protocol.what,protocol.address);
            } else if(What.ADB.CONNECT_FAILED==protocol.what){
                //设备连接失败
            } else if(What.ADB.KILL_SERVER==protocol.what){
                //杀死 adb 服务
            } else if(What.ADB.START_SERVER==protocol.what){
                //启动 adb 服务
            } else if(What.ADB.LOG==protocol.what){
                //adb 日志
                sendMessage(What.Socket.LOG,protocol.message);
            } else if(What.ADB.ADB_INTERRUPT==protocol.what){
                //adb 意外中断
                sendMessage(protocol.what,protocol.address);
            }

        }
    }

    /**
     * 判断是否断开连接
     *
     * @param socket
     * @return
     */
    public boolean socketIsConnect(Socket socket) {
        boolean result = null != socket&&socket.isConnected();
        try {
            //发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
            if (result) {
                socket.sendUrgentData(0);
            }
        } catch (Exception se) {
            result = false;
        }
        return result;
    }

    /**
     * 关闭socket
     */
    private void closeSocket() {
        try {
            if (null != socket && !socket.isClosed()) {
                printWriter.close();
                socket.close();
                socket = null;
            }
            //此读取流为单独流对象,socket关闭后才可以关闭此流,socket关闭后,操作此流,会报错
            closeReaderStream();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            handler.removeCallbacks(examiner);
        }
    }

    private void closeReaderStream() throws IOException {
        if(null!=reader){
            reader.close();
            reader=null;
        }
    }


    /**
     * 发送远程消息
     * @param what
     */
    public void sendMessage(int what){
        sendMessage(what,null);
    }

    /**
     * 发送远程消息
     * @param what
     * @param obj
     */
    @Override
    public void sendMessage(int what,Object obj) {
        if(null!=currentReply){
            try {
                currentReply.send(Message.obtain(null, what,obj));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Log.e(TAG,"result:"+(null!=currentReply)+" sendMessage:"+what+" msg:"+obj);
    }

    @Override
    public void sendSocketMessage(int what,String address, String message) {
        if (null!=printWriter) {
            printWriter.println(Json.toJson(new Protocol(what,address,message)));
            printWriter.flush();
        }
    }

    @Override
    public void onDestroy() {
        serviceLooper.quit();
        if(null!=fileObserver) fileObserver.stopWatching();
        startService(new Intent(this,getClass()));
    }
}
