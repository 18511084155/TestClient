package quant.testclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import quant.testclient.Constant;
import quant.testclient.MessageWhat;
import quant.testclient.R;
import quant.testclient.utils.IOUtils;
import quant.testclient.utils.ResUtils;
import quant.testclient.utils.StringUtils;


/**
 * Created by cz on 2017/2/3.
 */
public class SocketService extends Service {
    public static final String TAG="SocketService";
    private volatile Looper serviceLooper;
    private volatile ServiceHandler serviceHandler;
    private PrintWriter printWriter;
    private Messenger currentReply;
    private final Runnable examiner;
    //socket 是否连接检测对象
    private Handler handler;
    private Socket socket;
    private int reconnectCount;
    private boolean interrupt;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            if(MessageWhat.CONNECT==msg.what){
                //连接 socket,连接完自动尝试连接 adb,一旦连接成功,此线程会被阻塞
                if(null!=msg.obj&& StringUtils.validateAddress(msg.obj.toString())){
                    try {
                        Bundle data = msg.getData();
                        String id=null;
                        if(null!=data) id=data.getString("id");
                        connectSocket(msg.obj.toString(),id);
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
            switch (msg.what){
                case MessageWhat.CONNECT:
                    //发送给 handleService连接 socket
                    if(null!=msg.obj&& StringUtils.validateAddress(msg.obj.toString())){
                        ensureSocket(msg.obj.toString());
                        serviceHandler.sendMessage(Message.obtain(msg));
                    }
                    break;
                case MessageWhat.CONNECT_ADB:
                    //连接 adb
                    break;
                case MessageWhat.DISCONNECT:
                    //中断socket 连接
                    if(socketIsConnect(socket)){
                        interrupt =false;
                        closeSocket();
                        sendMessage(MessageWhat.DISCONNECT,!socketIsConnect(socket));
                    }
                    break;
                case MessageWhat.DISCONNECT_ADB:
                    //中断 adb 连接
                    break;
            }
            return true;
        });
    }


    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("socketThread");
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
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
                } else {
                    sendMessage(MessageWhat.LOG,ResUtils.getString(R.string.already_connect,address));
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
     * @param id 设备 id
     */
    public void connectSocket(String address,String id) throws RemoteException {
        interrupt =true;
        BufferedReader br = null;
        while (interrupt) {
            Log.e(TAG, "connect:" + socketIsConnect(socket));
            if (socketIsConnect(socket)) {
                String line;
                try {
                    if (null == br) {
                        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    }
                    while ((line = br.readLine()) != null) {
                        if (TextUtils.isEmpty(line)) continue;
                        //处理数据
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (null != socket) {
                    sendMessage(MessageWhat.LOG, ResUtils.getString(R.string.reconnect_count_value,++reconnectCount));
                } else {
                    sendMessage(MessageWhat.LOG,ResUtils.getString(R.string.start_connect));
                }
                IOUtils.closeStream(br);
                //连接断开,重联
                try {
                    socket = new Socket(address, Constant.PORT);
                    socket.setKeepAlive(true);
                    printWriter = new PrintWriter(socket.getOutputStream());
                    //向服务器写入设备
                    printWriter.write(id);
                    sendMessage(MessageWhat.LOG,ResUtils.getString(R.string.connecting));
                    //重试后连接成功
                    if (0 < reconnectCount) {
                        sendMessage(MessageWhat.LOG,"Adb connected!reconnect count:" + reconnectCount);
                        reconnectCount = 0;
                    }
                    sendMessage(MessageWhat.CONNECT_COMPLETE);
                    sendMessage(MessageWhat.LOG,ResUtils.getString(R.string.server_address_value,socket.getInetAddress().getHostAddress()));
//                    //socket连接时,开启一个handler不断检测socket状态,此线程己被阻塞,因此可能存在假死
                    handler.removeCallbacks(examiner);
                    handler.postDelayed(examiner, 1000);
                } catch (IOException e) {
                    e.printStackTrace();
                    sendMessage(MessageWhat.CONNECT_FAILED);
                    sendMessage(MessageWhat.LOG,ResUtils.getString(R.string.connect_failed_value,socket.getInetAddress().getHostAddress()));
                }
            }
            //延持
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
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
        boolean result = null!=socket;
        try {
            //发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
            if (null != socket) {
                socket.sendUrgentData(0);
                result = true;
            }
        } catch (Exception se) {
            result = false;
            se.printStackTrace();
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
        } catch (IOException e) {
            e.printStackTrace();
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
    public void sendMessage(int what,Object obj) {
        if(null!=currentReply){
            Message msg = Message.obtain(null, what,obj);
            try {
                currentReply.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        serviceLooper.quit();
        startService(new Intent(this,getClass()));
    }
}
