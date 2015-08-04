package hu.mrolcsi.android.spoc.common.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import hu.mrolcsi.android.spoc.database.DatabaseHelper;
import hu.mrolcsi.android.spoc.database.models.Contact;
import hu.mrolcsi.android.spoc.database.models.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.04.
 * Time: 10:53
 */

public class DatabaseBuilderService extends IntentService {

    public static final String TAG = "SPOC.Common.DatabaseBuilder";

    public DatabaseBuilderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        cleanUpImages();

        cleanUpContacts();

        updateImagesFromMediaStore();

        updateImagesFromWhiteList();

        updateContactsFromContactsProvider();

        updateContactsFromFacebook();
    }

    private void cleanUpImages() {
        //TODO: delete not existing images from db
        final List<Image> images = DatabaseHelper.getInstance().getImagesDao().queryForAll();
        final List<Image> toDelete = new ArrayList<>();
        for (Image image : images) {
            if (!new File(image.getFilename()).exists()) {
                toDelete.add(image);
            }
        }
        DatabaseHelper.getInstance().getImagesDao().delete(toDelete);
    }

    private void cleanUpContacts() {
        //TODO: delete invalid contacts from db
        DatabaseHelper.getInstance().getContactsDao();
    }

    private void updateImagesFromMediaStore() {
        //TODO: collect images from MediaStore > createOrUpdate
        //prepare query
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns.DATE_TAKEN};

        Cursor cursor = null;
        final SQLiteDatabase db = DatabaseHelper.getInstance().getWritableDatabase();
        final RuntimeExceptionDao<Image, Integer> imagesDao = DatabaseHelper.getInstance().getImagesDao();

        db.beginTransaction();
        try {
            cursor = getContentResolver().query(uri, projection, null, null, null);

            int indexID = cursor.getColumnIndex(projection[0]);
            int indexData = cursor.getColumnIndex(projection[1]);
            int indexDateTaken = cursor.getColumnIndex(projection[2]);

            int id;
            String filename;
            long dateTaken;

            Image image;
            List<Image> images;

            while (cursor.moveToNext()) {
                id = cursor.getInt(indexID);
                filename = cursor.getString(indexData);
                dateTaken = cursor.getLong(indexDateTaken);

                images = imagesDao.queryForEq(Image.COLUMN_MEDIASTORE_ID, id);
                if (images.size() > 0) {
                    image = images.get(0);
                    image.setFilename(filename);
                    image.setDateTaken(new Date(dateTaken));
                } else {
                    image = new Image(filename, id, new Date(dateTaken));
                }
                imagesDao.createOrUpdate(image);
            }
            db.setTransactionSuccessful();
        } finally {
            if (cursor != null) cursor.close();
            db.endTransaction();
        }
    }

    private void updateImagesFromWhiteList() {
        //TODO: collect images from whiteList > createOrUpdate
    }

    private void updateContactsFromContactsProvider() {
        //TODO: collect contacts from ContactProvider > createOrUpdate
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_ID};

        Cursor cursor = null;
        final SQLiteDatabase db = DatabaseHelper.getInstance().getWritableDatabase();
        final RuntimeExceptionDao<Contact, Integer> contactsDao = DatabaseHelper.getInstance().getContactsDao();

        db.beginTransaction();
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);

            int indexKey = cursor.getColumnIndex(projection[0]);
            int indexName = cursor.getColumnIndex(projection[1]);
            int indexPhoto = cursor.getColumnIndex(projection[2]);

            String key;
            String displayName;
            byte[] photo;

            List<Contact> contacts;
            Contact contact;

            while (cursor.moveToNext()) {
                key = cursor.getString(indexKey);
                displayName = cursor.getString(indexName);
                photo = queryContactImage(cursor.getInt(indexPhoto));

                contacts = contactsDao.queryForEq(Contact.COLUMN_CONTACT_KEY, key);
                if (contacts.size() > 0) {
                    contact = contacts.get(0);
                    contact.setName(displayName);
                    contact.setPhoto(photo);
                } else {
                    contact = new Contact(key, displayName, photo);
                }
                contactsDao.createOrUpdate(contact);
            }
            db.setTransactionSuccessful();
        } finally {
            if (cursor != null) cursor.close();
            db.endTransaction();
        }
    }

    private void updateContactsFromFacebook() {
        //TODO: update contacts from Facebook
    }

    private byte[] queryContactImage(int photoId) {
        Cursor c = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO},
                ContactsContract.Data._ID + "=?",
                new String[]{Integer.toString(photoId)},
                null);
        byte[] imageBytes = null;
        if (c != null) {
            if (c.moveToFirst()) {
                imageBytes = c.getBlob(0);
            }
            c.close();
        }
        return imageBytes;
    }
}
