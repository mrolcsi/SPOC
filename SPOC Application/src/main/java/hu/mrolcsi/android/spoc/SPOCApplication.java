package hu.mrolcsi.android.spoc;

import android.app.Application;

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

        //TODO: scan MediaStore & white-listed folders for Images
        //  update library if needed
        //TODO: do this in a background service
    }
}
