package hu.mrolcsi.android.spoc.gallery.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.view.View;
import android.widget.FrameLayout;
import com.github.machinarius.preferencefragment.PreferenceFragment;
import hu.mrolcsi.android.spoc.common.ISPOCFragment;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.navigation.NavigationActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.16.
 * Time: 14:10
 */

public class SettingsFragment extends PreferenceFragment implements ISPOCFragment {

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
        view.setLayoutParams(lp);

        findPreference("prefAbout").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
        ((NavigationActivity) getActivity()).restoreFragmentFromStack();
        return true;
    }
}
