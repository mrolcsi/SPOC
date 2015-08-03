package hu.mrolcsi.android.spoc.gallery.imagedetails;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import hu.mrolcsi.android.spoc.common.fragment.SPOCFragment;
import hu.mrolcsi.android.spoc.common.loader.MediaStoreLoader;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.main.GalleryActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.13.
 * Time: 19:52
 */

public class ImagePagerFragment extends SPOCFragment implements CursorLoader.OnLoadCompleteListener<Cursor> {

    public static final String ARG_SELECTED_POSITION = "SPOC.Gallery.Pager.SelectedPosition";
    public static final String ARG_LOADER_ID = "SPOC.Gallery.Pager.LoaderId";
    private ViewPager vpDetailsPager;
    private ImageDetailsAdapter mAdapter;
    private Loader<Cursor> mLoader;
    private int mCurrentPageIndex = -1;

    @Override
    @TargetApi(18)
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        
        if (Build.VERSION.SDK_INT > 18) {
            WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
            params.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_ROTATE;
            getActivity().getWindow().setAttributes(params);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mAdapter = new ImageDetailsAdapter(getChildFragmentManager());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_imagepager, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        vpDetailsPager = (ViewPager) view.findViewById(R.id.vpDetailsPager);

        toggleFullScreen();

        if (savedInstanceState != null) mCurrentPageIndex = savedInstanceState.getInt(ARG_SELECTED_POSITION);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.details, menu);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getArguments() != null && getArguments().containsKey(ARG_LOADER_ID)) {
            int loaderId = getArguments().getInt(ARG_LOADER_ID);
            getLoaderManager().initLoader(loaderId, null, new MediaStoreLoader(getActivity(), this));

            mLoader = getLoaderManager().getLoader(loaderId);
            if (!mLoader.isStarted())
                mLoader.startLoading();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isFullscreen()) toggleFullScreen();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(ARG_SELECTED_POSITION, vpDetailsPager.getCurrentItem());
    }

    @Override
    public boolean onBackPressed() {
        ((GalleryActivity) getActivity()).restoreFragmentFromStack();
        return true;
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        mAdapter = new ImageDetailsAdapter(getChildFragmentManager(), data);
        try {
            vpDetailsPager.setAdapter(mAdapter);
        } catch (NullPointerException e) {
            Log.w(getClass().getName(), "Caught NullPointerException. Premature loading?");
        }

        if (mCurrentPageIndex < 0 && data != null && getArguments() != null && getArguments().containsKey(ARG_SELECTED_POSITION)) {
            mCurrentPageIndex = getArguments().getInt(ARG_SELECTED_POSITION);
        }

        vpDetailsPager.setCurrentItem(mCurrentPageIndex);
    }
}
