package hu.mrolcsi.android.spoc.gallery.main;

import android.os.Bundle;
import android.support.v4.content.CursorLoader;

import hu.mrolcsi.android.spoc.common.loader.ImagesTableLoader;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.21.
 * Time: 15:35
 */

public final class HomeFragment extends ThumbnailsFragment {
    public static final int LOADER_ID = 10;

    @Override
    protected void setupImagesAdapter() {
        mAdapter = new ThumbnailsAdapter(getActivity());
        twList.setAdapter(mAdapter);
    }

    @Override
    protected CursorLoader setupLoader() {
        mLoaderId = IMAGES_LOADER_ID;
        Bundle loaderArgs = null;
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_LOADER_ID)) {
                mLoaderId = getArguments().getInt(ARG_LOADER_ID);
            }
            loaderArgs = getArguments().getBundle(ThumbnailsFragment.ARG_QUERY_BUNDLE);
        }
        return (CursorLoader) getLoaderManager().restartLoader(mLoaderId, loaderArgs, new ImagesTableLoader(getActivity(), this));
    }
}
