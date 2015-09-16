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

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:23
 */

public abstract class SPOCFragment extends Fragment implements ISPOCFragment {

    public static final String ARG_NAVIGATION_POSITION = "SPOC.NavigationPosition";
    private static List<OnFullscreenChangeListener> onFullscreenChangeListeners = new ArrayList<>();
    protected View mRootView;

    private boolean isFullscreen = false;

    @Override
    public int getNavigationItemPosition() {
        if (getArguments() == null) return -1;
        return getArguments().getInt(ARG_NAVIGATION_POSITION, -1);
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
//        return isFullscreen;

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

        if (Build.VERSION.SDK_INT == 10)
            if (mRootView != null) {
                ViewGroup parentViewGroup = (ViewGroup) mRootView.getParent();
                if (parentViewGroup != null) {
                    parentViewGroup.removeAllViews();
                }
            }
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

//        if (isFullscreen) {
//            showSystemUI();
//        } else {
//            hideSystemUI();
//        }

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
            if (Build.VERSION.SDK_INT >= 19) {
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

        for (OnFullscreenChangeListener listener : onFullscreenChangeListeners) {
            listener.onFullScreenChanged(isFullscreen());
        }
    }

    // This snippet hides the system bars.
    protected void hideSystemUI() {
        isFullscreen = true;
        if (Build.VERSION.SDK_INT >= 11) {

            // Set the IMMERSIVE flag.
            // Set the content to appear under the system bars so that the content
            // doesn't resize when the system bars hide and show.

            View decorView = getActivity().getWindow().getDecorView();

            int flags = decorView.getSystemUiVisibility();

            if (Build.VERSION.SDK_INT >= 14) {
                flags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION; // hide nav bar
            }
            if (Build.VERSION.SDK_INT >= 16) {
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                flags |= View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
            }
            if (Build.VERSION.SDK_INT >= 19) {
                flags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
            }

            decorView.setSystemUiVisibility(flags);
        }

        for (OnFullscreenChangeListener listener : onFullscreenChangeListeners) {
            listener.onFullScreenChanged(true);
        }
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    protected void showSystemUI() {
        isFullscreen = false;
        if (Build.VERSION.SDK_INT >= 11) {

            View decorView = getActivity().getWindow().getDecorView();

            int flags = decorView.getSystemUiVisibility();

            if (Build.VERSION.SDK_INT >= 16) {
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }

            decorView.setSystemUiVisibility(flags);
        }

        for (OnFullscreenChangeListener listener : onFullscreenChangeListeners) {
            listener.onFullScreenChanged(true);
        }
    }

    public void addOnFullscreenChangeListener(OnFullscreenChangeListener listener) {
        onFullscreenChangeListeners.add(listener);
    }

    public void removeOnFullscreenChangeListener(OnFullscreenChangeListener listener) {
        onFullscreenChangeListeners.remove(listener);
    }

    public interface OnFullscreenChangeListener {
        void onFullScreenChanged(boolean isFullscreen);
    }
}