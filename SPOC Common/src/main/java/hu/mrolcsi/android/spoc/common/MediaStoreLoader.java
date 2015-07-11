package hu.mrolcsi.android.spoc.common;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.11.
 * Time: 17:50
 */

public class MediaStoreLoader implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int ID = 0;
    private final Context context;
    private final Loader.OnLoadCompleteListener<Cursor> onLoadCompleteListener;

    public MediaStoreLoader(Context context, Loader.OnLoadCompleteListener<Cursor> onLoadCompleteListener) {
        this.context = context;
        this.onLoadCompleteListener = onLoadCompleteListener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(context);

        loader.setUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        loader.setProjection(new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA});
        loader.setSortOrder(MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (onLoadCompleteListener != null) {
            onLoadCompleteListener.onLoadComplete(loader, data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
