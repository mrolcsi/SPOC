package hu.mrolcsi.android.spoc.common.loader.database;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import hu.mrolcsi.android.spoc.common.loader.LoaderBase;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.10.
 * Time: 14:48
 */

public class ImageTableLoader extends LoaderBase {

    public static final int ID = 324;

    public ImageTableLoader(Context context, Loader.OnLoadCompleteListener<Cursor> onLoadCompleteListener) {
        super(context, onLoadCompleteListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final CursorLoader loader = new CursorLoader(context);
        loader.setUri(SPOCContentProvider.CONTENT_URI);

        if (args != null) {
            String[] projection = null;
            if (args.containsKey(ARG_PROJECTION)) {
                projection = args.getStringArray(ARG_PROJECTION);
            }

            String selection = null;
            if (args.containsKey(ARG_SELECTION)) {
                selection = args.getString(ARG_SELECTION);
            }

            String[] selectionArgs = null;
            if (args.containsKey(ARG_SELECTION_ARGS)) {
                selectionArgs = args.getStringArray(ARG_SELECTION_ARGS);
            }

            String sortOrder = null;
            if (args.containsKey(ARG_SORT_ORDER)) {
                sortOrder = args.getString(ARG_SORT_ORDER);
            }

            loader.setProjection(projection);
            loader.setSelection(selection);
            loader.setSelectionArgs(selectionArgs);
            loader.setSortOrder(sortOrder);
        }

        return loader;
    }

}
