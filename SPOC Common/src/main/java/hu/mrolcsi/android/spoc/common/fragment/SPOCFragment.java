package hu.mrolcsi.android.spoc.common.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import hu.mrolcsi.android.spoc.common.BuildConfig;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:23
 */

public abstract class SPOCFragment extends Fragment implements ISPOCFragment {

    @Override
    public int getNavigationItemId() {
        return -1;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getTagString() {
        return getClass().getName();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean isFullscreen() {
        if (Build.VERSION.SDK_INT >= 11) {
            final int systemUiVisibility = getActivity().getWindow().getDecorView().getSystemUiVisibility();
            return ((systemUiVisibility | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == systemUiVisibility);
        } else return false;
    }

    //region -- L I F E C Y C L E --

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(getTitle());

        if (BuildConfig.DEBUG)
            Log.v(getClass().getSimpleName(), "LIFECYCLE : onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (BuildConfig.DEBUG)
            Log.v(getClass().getSimpleName(), "LIFECYCLE : onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (BuildConfig.DEBUG)
            Log.v(getClass().getSimpleName(), "LIFECYCLE : onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (BuildConfig.DEBUG)
            Log.v(getClass().getSimpleName(), "LIFECYCLE : onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (BuildConfig.DEBUG)
            Log.v(getClass().getSimpleName(), "LIFECYCLE : onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG)
            Log.v(getClass().getSimpleName(), "LIFECYCLE : onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (BuildConfig.DEBUG)
            Log.v(getClass().getSimpleName(), "LIFECYCLE : onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (BuildConfig.DEBUG)
            Log.v(getClass().getSimpleName(), "LIFECYCLE : onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (BuildConfig.DEBUG)
            Log.v(getClass().getSimpleName(), "LIFECYCLE : onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG)
            Log.v(getClass().getSimpleName(), "LIFECYCLE : onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (BuildConfig.DEBUG)
            Log.v(getClass().getSimpleName(), "LIFECYCLE : onDetach");
    }

    //endregion

    /**
     * Detects and toggles immersive mode (also known as "hidey bar" mode).
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void toggleFullScreen() {

        if (Build.VERSION.SDK_INT >= 11) {
            // BEGIN_INCLUDE (get_current_ui_flags)
            // The UI options currently enabled are represented by a bitfield.
            // getSystemUiVisibility() gives us that bitfield.
            int newUiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();
            // END_INCLUDE (get_current_ui_flags)
            // BEGIN_INCLUDE (toggle_ui_flags)

            // Navigation bar hiding:  Backwards compatible to ICS.
            if (Build.VERSION.SDK_INT >= 14) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }

            // Status bar hiding: Backwards compatible to Jellybean
            if (Build.VERSION.SDK_INT >= 16) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }

            // Immersive mode: Backward compatible to KitKat.
            // Note that this flag doesn't do anything by itself, it only augments the behavior
            // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
            // all three flags are being toggled together.
            // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
            // Sticky immersive mode differs in that it makes the navigation and status bars
            // semi-transparent, and the UI flag does not get cleared when the user interacts with
            // the screen.
            if (Build.VERSION.SDK_INT >= 18) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            getActivity().getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
            //END_INCLUDE (set_ui_flags)

            final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                if (isFullscreen()) actionBar.hide();
                else actionBar.show();
            }
        }
    }
}
