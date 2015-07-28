package hu.mrolcsi.android.spoc.common.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.14.
 * Time: 10:59
 */

public class RetainedFragment extends Fragment {

    public static final String TAG = "SPOC.Common.RetainedFragment";

    // data object we want to retain
    private Map<String, Object> retainedData = new TreeMap<>();

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public Object getRetainedData(String key) {
        return retainedData.get(key);
    }

    public void putRetainedData(String key, Object data) {
        retainedData.put(key, data);
    }
}