package hu.mrolcsi.android.spoc.gallery.settings;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.github.machinarius.preferencefragment.PreferenceFragment;
import hu.mrolcsi.android.filebrowser.BrowserDialog;
import hu.mrolcsi.android.filebrowser.option.BrowseMode;
import hu.mrolcsi.android.filebrowser.option.Style;
import hu.mrolcsi.android.spoc.common.fragment.ISPOCFragment;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.utils.DialogUtils;
import hu.mrolcsi.android.spoc.gallery.main.GalleryActivity;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.05.
 * Time: 17:33
 */

public class DirectoryListFragment extends PreferenceFragment implements ISPOCFragment {

    public static final String TAG = "SPOC.Settings.DirectoryList";
    public static final String ARG_TYPE = "SPOC.Gallery.Settings.DirectoryList.TYPE";
    public static final String PREF_DIRECTORIES = "SPOC.Gallery.Settings.DirectoryList.Directories";
    public static final int TYPE_WHITELIST = 9;
    public static final int TYPE_BLACKLIST = 2;

    private int mType;
    private PreferenceCategory mListCategory;
    private List<String> mDirectories = new ArrayList<>();
    private Preference.OnPreferenceClickListener removePreferenceClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(final Preference preference) {
            DialogUtils.buildConfirmDialog(getActivity())
                    .setMessage(R.string.settings_message_removeThisDirectory)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //noinspection SuspiciousMethodCalls
                            mDirectories.remove(preference.getSummary());
                            save();
                            load();
                        }
                    })
                    .show();
            return true;
        }
    };

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setHasOptionsMenu(true);

        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getActivity()));

        if (!getArguments().containsKey(ARG_TYPE)) throw new IllegalArgumentException();
        mType = getArguments().getInt(ARG_TYPE);


        Preference helpPref = new Preference(getActivity());
        if (mType == TYPE_WHITELIST)
            helpPref.setSummary(Html.fromHtml(getString(R.string.settings_whitelist_help)));
        if (mType == TYPE_BLACKLIST)
            helpPref.setSummary(Html.fromHtml(getString(R.string.settings_blacklist_help)));
        if (Build.VERSION.SDK_INT >= 11)
            helpPref.setIcon(R.drawable.help);

        getPreferenceScreen().addPreference(helpPref);

        Preference restartNotePref = new Preference(getActivity());
        restartNotePref.setSummary(Html.fromHtml(getString(R.string.settings_message_restartNote)));

        getPreferenceScreen().addPreference(restartNotePref);

        mListCategory = new PreferenceCategory(getActivity());
        if (mType == TYPE_WHITELIST) mListCategory.setTitle(R.string.settings_whitelist_title);
        if (mType == TYPE_BLACKLIST) mListCategory.setTitle(R.string.settings_blacklist_title);

        getPreferenceScreen().addPreference(mListCategory);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        lp.topMargin = getResources().getDimensionPixelOffset(R.dimen.abc_action_bar_default_height_material);

        lp.topMargin += getResources().getDimensionPixelOffset(R.dimen.margin_small);
        lp.bottomMargin += getResources().getDimensionPixelOffset(R.dimen.margin_small);
        lp.leftMargin += getResources().getDimensionPixelOffset(R.dimen.margin_small);
        lp.rightMargin += getResources().getDimensionPixelOffset(R.dimen.margin_small);
        view.setLayoutParams(lp);

        load();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.directorylist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.menuAdd:
                showBrowserDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        save();
    }

    private void load() {

        mListCategory.removeAll();
        mDirectories.clear();

        try {
            final String directoriesJson = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(PREF_DIRECTORIES + mType, null);
            if (!TextUtils.isEmpty(directoriesJson)) {
                JSONArray jDirectories = new JSONArray(directoriesJson);
                for (int i = 0; i < jDirectories.length(); i++) {
                    mDirectories.add(jDirectories.optString(i));
                }
            }
        } catch (JSONException e) {
            Log.w(getClass().getSimpleName(), e);
        }

        if (mDirectories.isEmpty()) {
            Preference empty = new Preference(getActivity());
            empty.setTitle(R.string.settings_message_listIsEmpty);
            if (Build.VERSION.SDK_INT >= 11)
                empty.setIcon(R.drawable.info);
            mListCategory.addPreference(empty);
        }

        for (String directory : mDirectories) {
            mListCategory.addPreference(createPreference(directory));
        }
    }

    private void save() {
        JSONArray jDirectories = new JSONArray();
        for (String directory : mDirectories) {
            jDirectories.put(directory);
        }
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(PREF_DIRECTORIES + mType, jDirectories.toString()).apply();
    }

    @TargetApi(11)
    private Preference createPreference(String directory) {
        Preference pref = new Preference(getActivity());
        pref.setSummary(directory);
        if (Build.VERSION.SDK_INT >= 11)
            pref.setIcon(hu.mrolcsi.android.filebrowser.R.drawable.browser_folder_light);
        pref.setOnPreferenceClickListener(removePreferenceClickListener);
        return pref;
    }

    public void showBrowserDialog() {
        BrowserDialog dialog = new BrowserDialog();
        dialog.setBrowseMode(BrowseMode.SELECT_DIR)
                .setTheme(Style.LIGHT_ICONS)
                .setOnDialogResultListener(new BrowserDialog.OnDialogResultListener() {
                    @Override
                    public void onPositiveResult(String path) {
                        //noinspection SuspiciousMethodCalls
                        if (!mDirectories.contains(path)) {
                            Toast.makeText(getActivity(), String.format(getString(R.string.settings_message_directoryAdded), path), Toast.LENGTH_SHORT).show();
                            mDirectories.add(path);
                            save();
                            load();
                        } else
                            Toast.makeText(getActivity(), R.string.settings_message_alreadyInTheList, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNegativeResult() {
                    }
                }).show(getChildFragmentManager(), "BrowserDialog");
    }

    @Override
    public int getNavigationItemId() {
        return R.id.navigation_settings;
    }

    @Override
    public String getTitle() {
        if (mType == TYPE_WHITELIST)
            return getString(R.string.settings_whitelist_title);
        if (mType == TYPE_BLACKLIST) return getString(R.string.settings_blacklist_title);
        else return null;
    }

    @Override
    public String getTagString() {
        return TAG;
    }

    @Override
    public boolean onBackPressed() {
        ((GalleryActivity) getActivity()).restoreFragmentFromStack();
        return true;
    }
}
