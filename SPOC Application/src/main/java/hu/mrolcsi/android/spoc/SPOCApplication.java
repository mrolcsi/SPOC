package hu.mrolcsi.android.spoc;

import android.app.Application;
import android.os.Build;
import android.util.Log;
import hu.mrolcsi.android.spoc.gallery.BuildConfig;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:07
 */

public class SPOCApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG)
            Log.i(getClass().getSimpleName(), "API Level: " + Build.VERSION.SDK_INT);

        //TODO: scan MediaStore & white-listed folders for Images
        //  update library if needed
        //TODO: do this in a background service
    }
}
