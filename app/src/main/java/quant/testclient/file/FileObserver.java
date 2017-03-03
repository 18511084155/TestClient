package quant.testclient.file;


import android.content.Context;

/**
 * Created by czz on 2016/8/28.
 *
 */
public class FileObserver extends android.os.FileObserver {
    public final Context context;
    public FileObserver(Context context, String path) {
        super(path, android.os.FileObserver.DELETE);
        this.context=context;
    }

    @Override
    public void onEvent(int event, String path) {
        final int action = event & android.os.FileObserver.ALL_EVENTS;
        if(action==android.os.FileObserver.DELETE){
            FilePrefs.refreshPropFile(context);
        }
    }
}
