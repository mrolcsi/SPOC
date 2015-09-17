package hu.mrolcsi.android.spoc.common.utils;

import android.app.ActivityManager;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.08.
 * Time: 9:25
 */

public abstract class GeneralUtils {
    public static boolean isServiceRunning(ActivityManager manager, Class<?> serviceClass) {
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
