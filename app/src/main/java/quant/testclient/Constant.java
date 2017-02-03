package quant.testclient;

import android.os.Environment;

import java.io.File;

/**
 * Created by cz on 4/21/16.
 */
public interface Constant {
    int PORT=5556;
    String CLIENT_APK = "client.apk";
    public static final File CLIENT_FILE = new File(Environment.getExternalStorageDirectory(), CLIENT_APK);
}
