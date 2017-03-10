package quant.testclient;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.widget.ListView;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.pressHome();

        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(intent);

        if(Build.BRAND.equalsIgnoreCase("meizu")){
            UiScrollable uiScrollable1 = new UiScrollable(new UiSelector().className(ListView.class));
            uiScrollable1.scrollIntoView(new UiSelector().text("无障碍"));
            UiObject listViewItem = uiScrollable1.getChildByText(new UiSelector().className(android.widget.TextView.class.getName()),"无障碍");
            SystemClock.sleep(1000);
            if(listViewItem.click()){
                UiScrollable uiScrollable2 = new UiScrollable(new UiSelector().className(ListView.class));
                listViewItem = uiScrollable2.getChildByText(new UiSelector().className(android.widget.TextView.class.getName()),"客户端");
                if(listViewItem.click()){
                    UiObject y=device.findObject(new UiSelector().checkable(true));
                    if(!y.isChecked()&&y.click()){
                        UiObject z=device.findObject(new UiSelector().text("确定"));
                        z.click();
                        SystemClock.sleep(1000);
                        device.pressBack();
//                        device.pressBack();
//                        uiScrollable2.scrollToBeginning(2000);
//                        device.pressHome();
                    }
                }
            }
        } else if(Build.BRAND.equalsIgnoreCase("xiaomi")){
            UiObject x=device.findObject(new UiSelector().text("客户端"));
            if(x.click()){
                SystemClock.sleep(1000);
                UiObject y=device.findObject(new UiSelector().checkable(true));
                if(!y.isChecked()&&y.click()){
                    UiObject z=device.findObject(new UiSelector().text("确定"));
                    if(z.click()){
                        device.pressBack();
                        device.pressBack();
                    }
                }
            }
        }

    }
}

