package hu.mrolcsi.android.spoc.common.loader;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.10.
 * Time: 14:53
 */

abstract class LoaderBase implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_URI_STRING = "uri";
    public static final String ARG_PROJECTION = "projection";
    public static final String ARG_SELECTION = "selection";
    public static final String ARG_SELECTION_ARGS = "selectionArgs";
    public static final String ARG_SORT_ORDER = "sortOrder";

    protected final Context context;
    protected final LoaderCallbacks loaderCallbacks;

    public LoaderBase(Context context, LoaderCallbacks loaderCallbacks) {
        this.context = context;
        this.loaderCallbacks = loaderCallbacks;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loaderCallbacks != null) {
            loaderCallbacks.onLoadComplete(loader, data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loaderCallbacks != null) {
            loaderCallbacks.onLoaderReset(loader);
        }
    }

    public interface LoaderCallbacks extends Loader.OnLoadCompleteListener<Cursor> {
        void onLoaderReset(Loader<Cursor> loader);
    }
}
