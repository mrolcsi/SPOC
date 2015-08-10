package hu.mrolcsi.android.spoc.common.loader;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.04.
 * Time: 10:44
 */

public class ContactsLoader extends LoaderBase {

    public static final int ID = 23;

    public ContactsLoader(Context context, Loader.OnLoadCompleteListener<Cursor> onLoadCompleteListener) {
        super(context, onLoadCompleteListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(context);

        loader.setUri(ContactsContract.Contacts.CONTENT_URI);
        loader.setProjection(new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME});
        loader.setSortOrder(MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        return loader;
    }
}
