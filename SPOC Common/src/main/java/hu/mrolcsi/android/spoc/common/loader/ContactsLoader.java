package hu.mrolcsi.android.spoc.common.loader;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.04.
 * Time: 10:44
 */

public class ContactsLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private final Context context;
    private final Loader.OnLoadCompleteListener<Cursor> onLoadCompleteListener;

    public ContactsLoader(Context context, Loader.OnLoadCompleteListener<Cursor> onLoadCompleteListener) {
        this.context = context;
        this.onLoadCompleteListener = onLoadCompleteListener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(context);

        loader.setUri(ContactsContract.Contacts.CONTENT_URI);
        loader.setProjection(new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME});
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
        Log.v(getClass().getSimpleName(), "onLoaderReset");
    }
}
