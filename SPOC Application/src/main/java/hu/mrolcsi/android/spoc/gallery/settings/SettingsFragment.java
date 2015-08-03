package hu.mrolcsi.android.spoc.gallery.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.view.View;
import android.widget.FrameLayout;
import com.github.machinarius.preferencefragment.PreferenceFragment;
import hu.mrolcsi.android.spoc.common.fragment.ISPOCFragment;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.main.GalleryActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.16.
 * Time: 14:10
 */

public final class SettingsFragment extends PreferenceFragment implements ISPOCFragment {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        addPreferencesFromResource(R.xml.preferences);
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

        findPreference(getString(R.string.settings_key_about)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AboutDialog dialog = new AboutDialog();
                dialog.show(getChildFragmentManager(), AboutDialog.TAG);
                return true;
            }
        });
    }

    @Override
    public int getNavigationItemId() {
        return R.id.navigation_settings;
    }

    @Override
    public String getTitle() {
        return getString(R.string.navigation_settings);
    }

    @Override
    public String getTagString() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean onBackPressed() {
        ((GalleryActivity) getActivity()).restoreFragmentFromStack();
        return true;
    }
}
