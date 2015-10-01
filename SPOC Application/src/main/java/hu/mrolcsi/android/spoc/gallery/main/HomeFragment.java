package hu.mrolcsi.android.spoc.gallery.main;

import android.support.v4.content.CursorLoader;

import hu.mrolcsi.android.spoc.common.loader.ImagesTableLoader;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.21.
 * Time: 15:35
 */

public final class HomeFragment extends ThumbnailsFragment {
    public static final int HOMESCREEN_LOADER_ID = 10;

    @Override
    protected void setupImagesAdapter() {
        mAdapter = new ThumbnailsAdapter(getActivity());
        twList.setAdapter(mAdapter);
    }

    @Override
    protected CursorLoader setupLoader() {
        return (CursorLoader) getLoaderManager().restartLoader(HOMESCREEN_LOADER_ID, null, new ImagesTableLoader(getActivity(), this));
    }

    @Override
    public int getLoaderId() {
        return HOMESCREEN_LOADER_ID;
    }
}
