package quant.testclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cz.injectlibrary.Id;
import com.cz.loglibrary.JLog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import quant.testclient.receive.NetStatusReceiver;
import quant.testclient.service.SocketService;
import quant.testclient.sharedprefs.Prefs;
import quant.testclient.sharedprefs.Setting;
import quant.testclient.utils.DeviceUtils;
import quant.testclient.utils.ResUtils;
import quant.testclient.utils.StringUtils;

public class MainActivity extends AppCompatActivity implements Handler.Callback{
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

        //初始化 socket service
        initSocketService();
        // 网络广播接收者
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkReceiver = new NetStatusReceiver(), intentFilter);

        localIp.setText(DeviceUtils.getAddress());
        reply =new Messenger(new Handler(this));

        connectButton.setOnClickListener(v -> sendConnectMessage(serverEditor.getText()));
        connectState.setOnClickListener(v->sendMessage(MessageWhat.DISCONNECT));
        adbState.setOnClickListener(v->sendMessage(MessageWhat.DISCONNECT_ADB));
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
            Prefs.putString(Setting.SERVER_IP, address.toString());
            serverEditor.setText(address);
            serverEditor.setSelection(address.length());
            sendMessage(MessageWhat.CONNECT,address);
        }
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
                String address=Prefs.getString(Setting.SERVER_IP);
                if(!TextUtils.isEmpty(address)&& StringUtils.validateAddress(address)){
                    sendMessage(MessageWhat.CONNECT,address);
                }
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
        //日志消息
        switch (msg.what){
            case MessageWhat.LOG:
                if(null!=msg.obj) logTextView.append(msg.obj.toString()+"\n");
                break;
            case MessageWhat.CONNECT_COMPLETE:
                connectState.setEnabled(true);
                connectState.setText(R.string.connect_complete);
                break;
            case MessageWhat.CONNECT_FAILED:
                connectState.setEnabled(false);
                connectState.setText(R.string.connect_failed);
                break;
            case MessageWhat.DISCONNECT:
                connectState.setText(R.string.connect_interrupt);
                Boolean interrupt = null!=msg.obj&&Boolean.valueOf(msg.obj.toString());
                connectState.setEnabled(!interrupt);
                logTextView.append(ResUtils.getString(
                        interrupt?R.string.disconnect_complete:R.string.disconnect_failed)+"\n");
                break;
            case MessageWhat.CONNECT_ADB_COMPLETE:
                adbState.setEnabled(true);
                adbState.setText(R.string.connect_complete);
                break;
            case MessageWhat.CONNECT_ADB_FAILED:
                adbState.setEnabled(false);
                adbState.setText(R.string.connect_failed);
                break;
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
            // 设置回调用的Messenger
            msg.replyTo = reply;
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
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
        if(null!=serviceConnection) unbindService(serviceConnection);
        if(null!=netWorkReceiver) unregisterReceiver(netWorkReceiver);
        super.onDestroy();
        Intent intent = new Intent(this, getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}
