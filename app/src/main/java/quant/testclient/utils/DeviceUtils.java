package quant.testclient.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

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
}
