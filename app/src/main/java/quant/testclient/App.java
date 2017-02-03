package quant.testclient;

import android.app.Application;
import android.util.Log;

import com.cz.loglibrary.JLog;
import com.cz.loglibrary.LogConfig;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Created by Administrator on 2016/11/7.
 */

public class App extends Application implements Thread.UncaughtExceptionHandler {
    private static final String TAG="App";
    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this);
        JLog.setLogConfig(LogConfig.get().setLogLevel(JLog.ALL));
    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        Log.e(TAG,stackTrace.toString());
    }
}
