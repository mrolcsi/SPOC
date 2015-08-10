package hu.mrolcsi.android.spoc.common.loader.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import hu.mrolcsi.android.spoc.common.loader.LoaderBase;
import hu.mrolcsi.android.spoc.database.DatabaseHelper;
import hu.mrolcsi.android.spoc.database.models.Image;

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

        final CursorLoader loader = new CursorLoader(context) {
            @Override
            public Cursor loadInBackground() {
                final SQLiteDatabase db = DatabaseHelper.getInstance().getReadableDatabase();
                return db.query(Image.TABLE_NAME, getProjection(), getSelection(), getSelectionArgs(), null, null, getSortOrder());
            }
        };
        loader.setSortOrder(Image.COLUMN_DATE_TAKEN + " DESC");

        if (args != null) {
            String[] projection = null;
            if (args.containsKey(ARG_PROJECTION)) {
                projection = args.getStringArray(ARG_PROJECTION);
            }

            String selection = null;
            if (args.containsKey(ARG_SELECTION)) {
                selection = args.getString(ARG_SELECTION);
            }

            String[] selectionArgs = new String[]{};
            if (args.containsKey(ARG_SELECTION_ARGS)) {
                selectionArgs = args.getStringArray(ARG_SELECTION_ARGS);
            }

            String sortOrder = Image.COLUMN_DATE_TAKEN + " DESC";
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
