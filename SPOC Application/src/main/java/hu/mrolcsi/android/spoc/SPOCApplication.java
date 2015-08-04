package hu.mrolcsi.android.spoc;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import hu.mrolcsi.android.spoc.common.service.DatabaseBuilderService;
import hu.mrolcsi.android.spoc.database.DatabaseHelper;
import hu.mrolcsi.android.spoc.gallery.BuildConfig;
import io.fabric.sdk.android.Fabric;

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

        Fabric.with(this, new Crashlytics());
        DatabaseHelper.init(this);

        Intent serviceIntent = new Intent(this, DatabaseBuilderService.class);
        startService(serviceIntent);
    }
}
