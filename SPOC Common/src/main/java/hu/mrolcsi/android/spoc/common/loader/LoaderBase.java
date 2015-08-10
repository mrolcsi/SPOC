package hu.mrolcsi.android.spoc.common.loader;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.10.
 * Time: 14:53
 */

public abstract class LoaderBase implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_PROJECTION = "projection";
    public static final String ARG_SELECTION = "selection";
    public static final String ARG_SELECTION_ARGS = "selectionArgs";
    public static final String ARG_SORT_ORDER = "sortOrder";
    protected final Context context;
    protected final Loader.OnLoadCompleteListener<Cursor> onLoadCompleteListener;

    public LoaderBase(Context context, Loader.OnLoadCompleteListener<Cursor> onLoadCompleteListener) {
        this.context = context;
        this.onLoadCompleteListener = onLoadCompleteListener;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (onLoadCompleteListener != null) {
            onLoadCompleteListener.onLoadComplete(loader, data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(getClass().getSimpleName(), "onLoaderReset");
    }
}
