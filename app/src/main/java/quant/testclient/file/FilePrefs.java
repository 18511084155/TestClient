package quant.testclient.file;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import quant.testclient.utils.DeviceUtils;
import quant.testclient.utils.IOUtils;

/**
 * Created by cz on 9/18/16.
 */
public class FilePrefs {
    public static final File APP_FILE;// 程序SD卡目录
    public static final File PROP_FOLDER;// 缓存
    public static final File PROP_FILE;// 临时文件


    static {
        APP_FILE = new File(Environment.getExternalStorageDirectory(), "/TestClient/");
        PROP_FOLDER = new File(APP_FILE, "/prop/");
        PROP_FILE =new File(PROP_FOLDER,"client.prop");
        mkdirs(APP_FILE, PROP_FOLDER);
    }

    public static void mkdirs(File... files) {
        if (null != files) {
            for (int i = 0; i < files.length; i++) {
                if (!files[i].exists()) {
                    files[i].mkdir();
                }
            }
        }
    }

    /**
     * 刷新属性文件
     */
    public static void refreshPropFile(Context context){
        Properties p = new Properties();
        p.put("[device_address]", "["+DeviceUtils.getAddress()+"]");
        p.put("[device_imei]", "["+DeviceUtils.getAndroidImei(context)+"]");
        p.put("[device_id]", "["+DeviceUtils.getAndroidId(context)+"]");
        OutputStream fos=null;
        try {
            fos = new FileOutputStream(PROP_FILE);
            p.store(fos, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeStream(fos);
        }
    }
}
