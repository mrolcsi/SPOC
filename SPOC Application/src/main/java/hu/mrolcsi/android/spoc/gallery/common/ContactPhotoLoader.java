package hu.mrolcsi.android.spoc.gallery.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import hu.mrolcsi.android.spoc.database.model.Contact;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.R;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.24.
 * Time: 10:53
 */

public class ContactPhotoLoader extends AsyncTask<Void, Void, Drawable> {
    private final Context context;
    private final String lookupKey;
    private final int contactId;

    public ContactPhotoLoader(Context context, String lookupKey) {
        this.context = context;
        this.lookupKey = lookupKey;
        this.contactId = -1;
    }

    public ContactPhotoLoader(Context context, int contactId) {
        this.context = context;
        lookupKey = null;
        this.contactId = contactId;
    }

    @Override
    @TargetApi(21)
    protected Drawable doInBackground(Void... voids) {
        //contact photo
        Uri contactUri = null;
        if (contactId > 0) {
            final Cursor cursorWithContact = context.getContentResolver().query(Uri.withAppendedPath(SPOCContentProvider.CONTACTS_URI, String.valueOf(contactId)), new String[]{Contact.COLUMN_CONTACT_KEY}, null, null, null);
            if (cursorWithContact.moveToFirst()) {
                contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, cursorWithContact.getString(0));
            }
            cursorWithContact.close();
        } else if (lookupKey != null) {
            contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
        }

        if (contactUri == null) {
            return null;
        }

        final InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), contactUri);

        RoundedBitmapDrawable roundedBitmapDrawable;
        if (inputStream != null) {
            roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), inputStream);
            roundedBitmapDrawable.setCircular(true);
            return roundedBitmapDrawable;
        }

        if (Build.VERSION.SDK_INT >= 21) {
            return context.getResources().getDrawable(R.drawable.user, context.getTheme());
        } else {
            //noinspection deprecation
            return context.getResources().getDrawable(R.drawable.user);
        }
    }
}