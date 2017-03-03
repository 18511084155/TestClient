package quant.testclient.utils;

import android.text.TextUtils;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * adb服务
 */
public class ShellService {

    /**
     * @param comment
     * @return
     */
    public static int execRoot(String comment) {
        int exitValue = -1;
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            if (!TextUtils.isEmpty(comment)) {
                outputStream.writeBytes(comment + "\n");
                outputStream.flush();
            }
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();
            exitValue = process.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exitValue;
    }

    /**
     * 是否拥有root权限
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean isRoot() {
        int i = execRoot("echo test");
        return i == 0;
    }

    /**
     * 取消调试
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static int reset() {
        int exitValue = -1;
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("setprop service.adb.tcp.port -1\n");
            outputStream.flush();
            outputStream.writeBytes("stop adbd\n");
            outputStream.flush();
            outputStream.writeBytes("start adbd\n");
            outputStream.flush();
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();
            exitValue = process.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exitValue;
    }

    /**
     * 设置无线调试端口号
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static int setConnectPort() {
        int exitValue = -1;
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("setprop service.adb.tcp.port 5555 \n");
            outputStream.flush();
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();
            exitValue = process.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exitValue;
    }




}