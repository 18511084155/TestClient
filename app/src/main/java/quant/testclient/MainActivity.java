package quant.testclient;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cz.injectlibrary.Id;
import com.cz.loglibrary.JLog;

import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import quant.testclient.bus.RxBus;
import quant.testclient.event.NetWorkChangedEvent;
import quant.testclient.model.What;
import quant.testclient.natives.NativeRuntime;
import quant.testclient.receive.NetStatusReceiver;
import quant.testclient.service.NotificationService;
import quant.testclient.service.SocketService;
import quant.testclient.sharedprefs.Prefs;
import quant.testclient.sharedprefs.Setting;
import quant.testclient.utils.DeviceUtils;
import quant.testclient.utils.ResUtils;
import quant.testclient.utils.StringUtils;

public class MainActivity extends AppCompatActivity implements Handler.Callback{
    private static final String TAG = "MainActivity";
    @Id(R.id.tv_id)
    private TextView localIp;
    @Id(R.id.et_server_address)
    private EditText serverEditor;
    @Id(R.id.tv_connect_state)
    private TextView connectState;
    @Id(R.id.tv_adb_state)
    private TextView adbState;
    @Id(R.id.tv_log_info)
    private TextView logTextView;
    @Id(R.id.btn_connect)
    private Button connectButton;
    @Id(R.id.tv_status)
    private TextView statusView;
    private AlertDialog alertDialog;
    private AlertDialog wifiDialog;
    private ProgressDialog progressDialog;
    private ServiceConnection serviceConnection;
    private NetStatusReceiver netWorkReceiver;
    private Messenger messenger;
    private Messenger reply;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        //开启守护服务
        initDaemonService();

        //初始化 socket service
        initSocketService();
        // 网络广播接收者
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkReceiver = new NetStatusReceiver(), intentFilter);

        localIp.setText(DeviceUtils.getAddress());
        reply =new Messenger(new Handler(this));

        connectButton.setOnClickListener(v -> sendConnectMessage(serverEditor.getText()));
        connectState.setOnClickListener(v->sendMessage(What.Socket.DISCONNECT));
        findViewById(R.id.iv_clear).setOnClickListener(v->logTextView.setText(null));

        //订阅网络变化
        RxBus.subscribe(NetWorkChangedEvent.class,event->{
            if(ConnectivityManager.TYPE_WIFI==event.currentType){
                //当前网络状态为 wifi
                dismissDialog(wifiDialog,progressDialog);
                connectSocketAndCheckWifi(Prefs.getString(Setting.SERVER_IP));
            } else {
                //网络切换为无网络,或者其他
                alertWifiServiceDialog();
            }
        });
    }

    /**
     * 发送连接 socket 消息
     * @param address
     */
    private void sendConnectMessage(Editable address) {
        Pattern pattern = Pattern.compile("(\\w{1,3})\\.(\\w{1,3})\\.(\\w{1,3})\\.(\\w{1,3})");
        Matcher matcher = pattern.matcher(address);
        if(TextUtils.isEmpty(address)){
            Snackbar.make(findViewById(android.R.id.content), R.string.server_address_empty,Snackbar.LENGTH_SHORT).show();
        } else if(!matcher.matches()){
            Snackbar.make(findViewById(android.R.id.content),R.string.server_address_error,Snackbar.LENGTH_SHORT).show();
        } else {
            connectSocketAndCheckWifi(address.toString());
        }
    }

    /**
     * 增加守护进程
     */
    public void initDaemonService() {
        //守护进程服务
        String serviceName = ResUtils.getPackageName()+"."+ NotificationService.class.getSimpleName();
        Executors.newSingleThreadExecutor().execute(() -> {
            String executable = "libhelper.so";
            String aliasfile = "helper";
            String parafind = "/data/data/" + getPackageName() + "/" + aliasfile;
            String retx = "false";
            NativeRuntime.getInstance().RunExecutable(getPackageName(), executable, aliasfile, getPackageName() + "/" + serviceName);
        });
    }

    /**
     * 初始化 socket 服务
     */
    private void initSocketService() {
        startService(new Intent(this, SocketService.class));
        serviceConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                JLog.e("onServiceConnected");
                messenger=new Messenger(service);
                //主动连接默认地址
                connectSocketAndCheckWifi(Prefs.getString(Setting.SERVER_IP));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                JLog.e("onServiceDisconnected:" + name);
            }
        };
        boolean bindResult = bindService(new Intent(this, SocketService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        if(!bindResult){
            new AlertDialog.Builder(this).setTitle(R.string.bind_service_failed).
                    setPositiveButton(R.string.ok,(dialog, which) -> dialog.dismiss()).show();
        }
    }

    /**
     * 连接 socket,先检测 wifi 是否开启,再获取本地 ip 地址,检测是否在一个网段内
     * @param address
     */
    private void connectSocketAndCheckWifi(String address) {
        if(!TextUtils.isEmpty(address)&& StringUtils.validateAddress(address)){
            //检测是否在同一个网段内
            if(wifiConnected()){
                if(equalsAddressSegment(address,DeviceUtils.getAddress())){
                    Prefs.putString(Setting.SERVER_IP, address.toString());
                    connectButton.setEnabled(false);
                    serverEditor.setText(address);
                    serverEditor.setSelection(address.length());
                    sendMessage(What.Socket.CONNECT,address);
                } else {
                    new AlertDialog.Builder(this).
                            setCancelable(false).
                            setTitle(R.string.app_alert).
                            setMessage(R.string.wifi_service_not_same).setPositiveButton(R.string.reset_wifi,(dialog, which) -> {
                                //前往设备重联
                                if(android.os.Build.VERSION.SDK_INT > 10) {
                                    startActivity(new Intent( android.provider.Settings.ACTION_SETTINGS));
                                } else {
                                    startActivity(new Intent( android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            }).show();
                }
            } else {
                alertWifiServiceDialog();
            }
        }
    }

    /**
     * 提示 wifi服务
     */
    private void alertWifiServiceDialog() {
        dismissDialog(wifiDialog);
        wifiDialog = new AlertDialog.Builder(this).
                setCancelable(false).
                setTitle(R.string.app_alert).
                setMessage(R.string.open_wifi_service).
                setPositiveButton(R.string.open_wifi, (dialog, which) -> {
                    //重启 wifi 再重联
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage(getString(R.string.open_wifi_ing));
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    DeviceUtils.resetWifi(getApplication());
                }).show();
    }

    private void dismissDialog(Dialog... dialogs){
        if(null!=dialogs){
            for(Dialog dialog:dialogs){
                if(null!=dialog&&dialog.isShowing()){
                    dialog.dismiss();
                }
            }
        }
    }

    /**
     * 检测网络段是否一致
     * @param address1
     * @param address2
     * @return
     */
    private boolean equalsAddressSegment(String address1, String address2) {
        boolean result=false;
        if(StringUtils.validateAddress(address1)&&StringUtils.validateAddress(address2)){
            Pattern pattern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\.\\d{1,3}");
            Matcher matcher1 = pattern.matcher(address1);
            Matcher matcher2 = pattern.matcher(address2);
            if(matcher1.find()&&matcher2.find()){
                result=matcher1.group(1).equals(matcher2.group(1));
            }
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 回调内容
     * @param msg
     * @return
     */
    @Override
    public boolean handleMessage(Message msg) {
        if(What.Socket.LOG==msg.what){
            //日志消息
            if(null!=msg.obj) logTextView.append(msg.obj.toString()+"\n");
        } else if(What.Socket.CONNECT_COMPLETE==msg.what){
            //连接正常
            connectState.setEnabled(true);
            connectButton.setEnabled(true);
            connectState.setText(R.string.connect_complete);
            statusView.setText(R.string.connect_complete);
        } else if(What.Socket.CONNECT_FAILED==msg.what){
            //连接失败
            connectState.setEnabled(false);
            connectState.setText(R.string.connect_failed);
            statusView.setText(R.string.connect_failed);
        } else if(What.Socket.DISCONNECT==msg.what){
            //主动中断连接
            connectState.setText(R.string.connect_interrupt);
            Boolean interrupt = null!=msg.obj&&Boolean.valueOf(msg.obj.toString());
            connectState.setEnabled(!interrupt);
            logTextView.append(ResUtils.getString(
                    interrupt?R.string.disconnect_complete:R.string.disconnect_failed)+"\n");
            statusView.setText(ResUtils.getString(
                    interrupt?R.string.disconnect_complete:R.string.disconnect_failed));
        } else if(What.Socket.CONNECT_STATUS==msg.what){
            //连接状态变化信息
            if(null!=msg.obj) statusView.setText(msg.obj.toString());
        }

        //adb连接
        if(What.ADB.CONNECT_COMPLETE==msg.what){
            //连接 adb 成功,若弹出警告框,则取消
            adbState.setText(R.string.connect_complete);
            statusView.setText(R.string.connect_complete);
            logTextView.append(getString(R.string.connect_adb_complete,msg.obj.toString()));
            if(null!=alertDialog&&alertDialog.isShowing()) alertDialog.dismiss();
            Log.e(TAG,"CONNECT_COMPLETE:"+msg.obj);
        } else if(What.ADB.CONNECT_FAILED==msg.what){
            //连接 adb 失败
            adbState.setText(R.string.connect_failed);
            statusView.setText(R.string.connect_failed);
        } else if(What.ADB.ADB_INTERRUPT==msg.what){
            //adb 连接中断
            adbState.setText(R.string.connect_interrupt);
            statusView.setText(R.string.connect_interrupt);
        } else if(What.ADB.ALERT_ADB_DEBUG==msg.what&&(null==alertDialog||!alertDialog.isShowing())){
            alertDialog = new AlertDialog.Builder(this).setTitle(R.string.app_alert).setMessage(R.string.debug_message).setCancelable(false).show();
        }
        return true;
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
        if(null!=messenger){
            Message msg = Message.obtain(null, what,obj);
            //设置回调用的Messenger
            msg.replyTo = reply;
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检测 wifi 是否开启
     * @return
     */
    private boolean wifiConnected() {
        boolean result = false;
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP){
            NetworkInfo networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkInfo != null) {
                result = networkInfo.isConnected();
            }
        } else {
            Network[] allNetworks = manager.getAllNetworks();
            if(null!=allNetworks){
                for(Network network:allNetworks){
                    NetworkInfo networkInfo = manager.getNetworkInfo(network);
                    if(null!=networkInfo&&ConnectivityManager.TYPE_WIFI==networkInfo.getType()){
                        result=networkInfo.isConnected();
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        //按下返回键,跳转到桌面
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        RxBus.unSubscribeItems(this);
        if(null!=serviceConnection) unbindService(serviceConnection);
        if(null!=netWorkReceiver) unregisterReceiver(netWorkReceiver);
        super.onDestroy();
        Intent intent = new Intent(this, getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}
