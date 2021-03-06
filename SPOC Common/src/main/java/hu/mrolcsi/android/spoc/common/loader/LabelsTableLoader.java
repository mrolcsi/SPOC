package hu.mrolcsi.android.spoc.common.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import hu.mrolcsi.android.spoc.database.model.Contact;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.07.
 * Time: 14:23
 */

public class LabelsTableLoader extends LoaderBase {

    public LabelsTableLoader(Context context, LoaderCallbacks loaderCallbacks) {
        super(context, loaderCallbacks);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final CursorLoader loader = new CursorLoader(context);
        loader.setUri(Uri.withAppendedPath(SPOCContentProvider.LABELS_URI, Contact.TABLE_NAME));

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
