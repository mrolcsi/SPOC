package hu.mrolcsi.android.spoc.common.loader;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(context);

        loader.setUri(ContactsContract.Contacts.CONTENT_URI);
        loader.setProjection(new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME
        });

        return loader;
    }
}
