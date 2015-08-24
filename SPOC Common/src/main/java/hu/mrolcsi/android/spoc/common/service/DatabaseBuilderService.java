package hu.mrolcsi.android.spoc.common.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import hu.mrolcsi.android.spoc.common.R;
import hu.mrolcsi.android.spoc.common.helper.ListHelper;
import hu.mrolcsi.android.spoc.common.utils.FileUtils;
import hu.mrolcsi.android.spoc.database.DatabaseHelper;
import hu.mrolcsi.android.spoc.database.models.Contact;
import hu.mrolcsi.android.spoc.database.models.Image;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.04.
 * Time: 10:53
 */

public class DatabaseBuilderService extends IntentService {

    public static final String TAG = "SPOC.Common.DatabaseBuilder";
    public static final String BROADCAST_ACTION_FINISHED = "SPOC.Common.DatabaseBuilder.BROADCAST_FINISHED";
    public static final String ARG_FIRST_START = "SPOC.Common.FIRST_START";

    public DatabaseBuilderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(getClass().getSimpleName(), "DatabaseBuilder started.");

        DatabaseHelper.init(getApplicationContext());

        cleanUpImages();

        updateImagesFromMediaStore();

        updateImagesFromWhiteList();

        //cleanUpContacts();

        //updateContactsFromContactsProvider();

        //updateContactsFromFacebook();

        Intent progressIntent = new Intent(BROADCAST_ACTION_FINISHED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);

        final boolean isFirstStart = intent.getBooleanExtra(ARG_FIRST_START, false);

        if (isFirstStart) {
            Intent cacheIntent = new Intent(getApplicationContext(), CacheBuilderService.class);
            startService(cacheIntent);
        }

        Log.v(getClass().getSimpleName(), "DatabaseBuilder finished.");
    }

    private void cleanUpImages() {
        //TODO: delete not existing images from db
        Log.v(getClass().getSimpleName(), "Cleaning up images...");
        final List<Image> toDelete = new ArrayList<>();

        final ListHelper listHelper = new ListHelper(getApplicationContext());


        for (Image image : DatabaseHelper.getInstance().getImagesDao()) {
            if (!new File(image.getFilename()).exists() || listHelper.isInBlacklist(image.getFilename())) {
                toDelete.add(image);
            }
        }

        final int numRowsDeleted = DatabaseHelper.getInstance().getImagesDao().delete(toDelete);
        Log.v(getClass().getSimpleName(), "Image clean up done. Deleted " + numRowsDeleted + " images.");
    }

    private void cleanUpContacts() {
        //TODO: delete invalid contacts from db
        Log.v(getClass().getSimpleName(), "Cleaning up contacts...");
        try {
            //delete everything for now
            final int deletedRows = DatabaseHelper.getInstance().getContactsDao().deleteBuilder().delete();
            Log.v(getClass().getSimpleName(), "Contacts clean up done. Deleted " + deletedRows + " contacts");
        } catch (SQLException e) {
            Log.w(getClass().getName(), e);
        }
    }

    private void updateImagesFromMediaStore() {
        Log.v(getClass().getSimpleName(), "Updating images from MediaStore...");
        //prepare query
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns.DATE_TAKEN};

        Cursor cursor = null;
        final SQLiteDatabase db = DatabaseHelper.getInstance().getWritableDatabase();
        final RuntimeExceptionDao<Image, Integer> imagesDao = DatabaseHelper.getInstance().getImagesDao();

        final ListHelper listHelper = new ListHelper(getApplicationContext());

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

                if (new File(filename).exists() && !listHelper.isInBlacklist(filename)) {
                    if (images.size() > 0) {
                        image = images.get(0);
                        image.setFilename(filename);
                        image.setDateTaken(new Date(dateTaken));
                    } else {
                        image = new Image(filename, id, new Date(dateTaken));
                    }
                    try {
                        imagesDao.createOrUpdate(image);
                    } catch (SQLiteConstraintException e) {
                        Log.w(getClass().getName(), e);
                        Log.w(getClass().getSimpleName(), "filename = " + filename);
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            if (cursor != null) cursor.close();
            db.endTransaction();
        }
        Log.v(getClass().getSimpleName(), "Update from MediaStore done.");
    }

    private void updateImagesFromWhiteList() {
        Log.v(getClass().getSimpleName(), "Updating images from Whitelist...");
        final List<String> whitelist = new ListHelper(getApplicationContext()).getWhitelist();
        final FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File parent, String filename) {
                for (String format : FileUtils.ACCEPTED_FORMATS) {
                    if (filename.contains(format)) return true;
                }
                return false;
            }
        };

        final RuntimeExceptionDao<Image, Integer> imagesDao = DatabaseHelper.getInstance().getImagesDao();
        final SQLiteDatabase db = DatabaseHelper.getInstance().getWritableDatabase();
        final SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.spoc_exifParser), Locale.getDefault());

        db.beginTransaction();
        try {
            File dir;
            for (String s : whitelist) {
                dir = new File(s);
                for (File file : dir.listFiles(filter)) {

                    List<Image> images = imagesDao.queryForEq(Image.COLUMN_FILENAME, file.getAbsolutePath());
                    Image image;
                    if (images.size() == 0) {
                        image = new Image();

                        image.setFilename(file.getAbsolutePath());

                        Date date;
                        if (file.getAbsolutePath().contains("jpg") || file.getAbsolutePath().contains("jpeg")) {
                            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                            String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);
                            date = sdf.parse(dateString);
                        } else {
                            date = new Date(file.lastModified());
                        }
                        image.setDateTaken(date);

                        imagesDao.createOrUpdate(image);
                    }
                }
            }

            db.setTransactionSuccessful();
        } catch (IOException | ParseException e) {
            Log.w(getClass().getName(), e);
        } finally {
            db.endTransaction();
        }
        Log.v(getClass().getSimpleName(), "Update from Whitelist done.");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void updateContactsFromContactsProvider() {
        //TODO: collect contacts from ContactProvider > createOrUpdate
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID
        };

        Cursor cursor = null;
        final SQLiteDatabase db = DatabaseHelper.getInstance().getWritableDatabase();
        final RuntimeExceptionDao<Contact, Integer> contactsDao = DatabaseHelper.getInstance().getContactsDao();

        db.beginTransaction();
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);

            int indexId = cursor.getColumnIndex(projection[0]);
            int indexKey = cursor.getColumnIndex(projection[1]);
            int indexName = cursor.getColumnIndex(projection[2]);
            int indexPhoto = cursor.getColumnIndex(projection[3]);

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
        Log.v(getClass().getSimpleName(), "Updating contacts from Facebook...");
        //TODO: update contacts from Facebook
        Log.v(getClass().getSimpleName(), "Update from Facebook done.");
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
