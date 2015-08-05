package hu.mrolcsi.android.spoc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import hu.mrolcsi.android.spoc.gallery.BuildConfig;
import io.fabric.sdk.android.Fabric;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:07
 */

public class SPOCApplication extends Application {
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Log.i(getClass().getSimpleName(), "API Level: " + Build.VERSION.SDK_INT);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle bundle) {
                        Log.v(getClass().getSimpleName(), activity.getLocalClassName() + " created.");
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                        Log.v(getClass().getSimpleName(), activity.getLocalClassName() + " started.");
                    }

                    @Override
                    public void onActivityResumed(Activity activity) {
                        Log.v(getClass().getSimpleName(), activity.getLocalClassName() + " resumed.");
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                        Log.v(getClass().getSimpleName(), activity.getLocalClassName() + " paused.");
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                        Log.v(getClass().getSimpleName(), activity.getLocalClassName() + " stopped.");
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                        Log.v(getClass().getSimpleName(), activity.getLocalClassName() + " saved.");
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                        Log.v(getClass().getSimpleName(), activity.getLocalClassName() + " destroyed.");
                    }
                });
        }

        Fabric.with(this, new Crashlytics());

        //TODO: scan MediaStore & white-listed folders for Images
        //  update library if needed
        //TODO: do this in a background service
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.v(getClass().getSimpleName(), "onLowMemory");
        Glide.get(this).clearMemory();
    }
}
