package hu.mrolcsi.android.spoc.common.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static void hideSoftKeyboard(Activity activity, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    public static String[] mergeStringArrays(String array1[], String array2[]) {
        if (array1 == null || array1.length == 0)
            return array2;
        if (array2 == null || array2.length == 0)
            return array1;
        List array1List = Arrays.asList(array1);
        List array2List = Arrays.asList(array2);
        List result = new ArrayList(array1List);
        List tmp = new ArrayList(array1List);
        tmp.retainAll(array2List);
        result.removeAll(tmp);
        result.addAll(array2List);
        return ((String[]) result.toArray(new String[result.size()]));
    }

}
