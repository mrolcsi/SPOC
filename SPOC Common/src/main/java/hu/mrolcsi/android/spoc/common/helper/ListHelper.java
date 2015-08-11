package hu.mrolcsi.android.spoc.common.helper;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.10.
 * Time: 13:27
 */

public class ListHelper {
    public static final String PREF_WHITELIST = "SPOC.Gallery.Settings.DirectoryList.Directories9";
    public static final String PREF_BLACKLIST = "SPOC.Gallery.Settings.DirectoryList.Directories2";

    private final Context context;
    private List<String> mBlacklist;
    private List<String> mWhitelist;

    public ListHelper(Context context) {
        this.context = context;
    }

    public List<String> getBlacklist() {
        if (mBlacklist == null)
            mBlacklist = getListFromSharedPrefs(PREF_BLACKLIST);
        return mBlacklist;
    }

    public List<String> getWhitelist() {
        if (mWhitelist == null)
            mWhitelist = getListFromSharedPrefs(PREF_WHITELIST);
        return mWhitelist;
    }

    private List<String> getListFromSharedPrefs(String prefKey) {
        String listString = PreferenceManager.getDefaultSharedPreferences(context).getString(prefKey, "[]");
        List<String> list = new ArrayList<>();


        try {
            JSONArray jWhitelist = new JSONArray(listString);
            for (int i = 0; i < jWhitelist.length(); i++) {
                list.add(jWhitelist.optString(i));
            }
        } catch (JSONException e) {
            Log.w(getClass().getName(), e);
        }
        return list;
    }

    private boolean contains(List<String> list, String item) {
        for (String s : list) {
            if (item.contains(s)) return true;
        }
        return false;
    }

    public boolean isInBlacklist(String filename) {
        return contains(getBlacklist(), filename);
    }

    public boolean isInWhitelist(String filename) {
        return contains(getWhitelist(), filename);
    }
}
