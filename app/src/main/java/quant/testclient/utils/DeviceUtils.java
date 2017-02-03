package quant.testclient.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.UUID;

import quant.testclient.sharedprefs.Prefs;
import quant.testclient.sharedprefs.Setting;

/**
 * 获得设备唯一id
 *
 * @author momo
 * @Date 2014/7/15
 */
public class DeviceUtils {
    private final static String TAG = DeviceUtils.class.getSimpleName();

    public static void resetWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        wifiManager.setWifiEnabled(true);
    }


    private static void setIpAssignment(String assign, WifiConfiguration wifiConf) throws Exception {
        setEnumField(wifiConf, assign, "ipAssignment");
    }

    private static void setIpAddress(InetAddress addr, int prefixLength, WifiConfiguration wifiConf) throws Exception {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null) return;
        Class<?> laClass = Class.forName("android.net.LinkAddress");
        Constructor<?> laConstructor = laClass.getConstructor(new Class[]{InetAddress.class, int.class});
        Object linkAddress = laConstructor.newInstance(addr, prefixLength);
        ArrayList<Object> mLinkAddresses = (ArrayList<Object>) getDeclaredField(linkProperties, "mLinkAddresses");
        mLinkAddresses.clear();
        mLinkAddresses.add(linkAddress);
    }


    private static Object getField(Object obj, String name) throws Exception {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    private static Object getDeclaredField(Object obj, String name) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setEnumField(Object obj, String value, String name) throws Exception {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }

    /**
     * 获得ip
     *
     * @return
     */
    public static String getAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumeration = networkInterface.getInetAddresses(); enumeration.hasMoreElements(); ) {
                    InetAddress inetAddress = enumeration.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得一个设备 id
     * 先取使用过的配置文件 id,若没有代表首次使用,则按以下顺序取
     * 先取 imei 码,取不到取 AndroidId,再取不到取 一个随机生成的 uuid
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        String id = Prefs.getString(Setting.ID);
        if(TextUtils.isEmpty(id)){
            id=getAndroidId(context);
            if (TextUtils.isEmpty(id)) {
                id = getAndroidImei(context);
            }
            if (TextUtils.isEmpty(id)) {
                id=getUUid(context);
            }
            //记录使用 id
            Prefs.putString(Setting.ID,id);
        }
        return id;
    }

    /**
     * 获得android设备id
     *
     * @return
     */
    public static String getAndroidId(Context context) {
        String id = null;
        if (TextUtils.isEmpty(id)) {
            try {
                id = Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    /**
     * 获得imei码
     *
     * @return
     */
    public static String getAndroidImei(Context context) {
        String id = null;
        try {
            if(null!=context){
                id = ((TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    private static String sID = null;

    public synchronized static String getUUid(Context context) {
        if (sID == null) {
            try {
                File installation = new File(context.getFilesDir(), "INSTALLATION");
                if (!installation.exists()){
                    writeInstallationFile(installation);
                }
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }

}
