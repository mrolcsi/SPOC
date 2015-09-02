package hu.mrolcsi.android.spoc.common.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import hu.mrolcsi.android.spoc.common.R;
import hu.mrolcsi.android.spoc.common.helper.ListHelper;
import hu.mrolcsi.android.spoc.common.utils.FileUtils;
import hu.mrolcsi.android.spoc.database.DatabaseHelper;
import hu.mrolcsi.android.spoc.database.models.Contact;
import hu.mrolcsi.android.spoc.database.models.Image;
import hu.mrolcsi.android.spoc.database.models.Label;
import hu.mrolcsi.android.spoc.database.models.LabelType;
import hu.mrolcsi.android.spoc.database.models.binders.Label2Image;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.04.
 * Time: 10:53
 */

public class DatabaseBuilderService extends IntentService {

    public static final String TAG = "SPOC.Common.DatabaseBuilder";
    public static final String BROADCAST_ACTION_IMAGES_READY = "SPOC.Common.DatabaseBuilder.BROADCAST_READY";
    public static final String BROADCAST_ACTION_FINISHED = "SPOC.Common.DatabaseBuilder.BROADCAST_FINISHED";
    public static final String ARG_FIRST_START = "SPOC.Common.FIRST_START";

    public DatabaseBuilderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(getClass().getSimpleName(), "DatabaseBuilder started.");

        Intent progressIntent;

        DatabaseHelper.init(getApplicationContext());

        cleanUpImages();

        updateImagesFromMediaStore();

        updateImagesFromWhiteList();

        //everything else should be done in the background

        progressIntent = new Intent(BROADCAST_ACTION_IMAGES_READY);
        LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);

        final boolean isFirstStart = intent.getBooleanExtra(ARG_FIRST_START, false);

        if (isFirstStart) {
            Intent cacheIntent = new Intent(getApplicationContext(), CacheBuilderService.class);
            startService(cacheIntent);
        }

        updateLocations();

        //TODO: cleanUpContacts();

        //TODO: updateContactsFromContactsProvider();

        //TODO: updateContactsFromFacebook();

        generateLabels();

        progressIntent = new Intent(BROADCAST_ACTION_FINISHED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);

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

    private void updateLocations() {
        Log.v(getClass().getSimpleName(), "Updating locations...");

        final List<Image> images = DatabaseHelper.getInstance().getImagesDao().queryForAll();

        Geocoder geocoder = new Geocoder(this);

        ExifInterface exif;
        float[] latLong = new float[2];
        List<Address> addresses;
        String locality;
        String countryName;

        for (final Image image : images) {
            if (image.getLocation() == null) {
                try {
                    exif = new ExifInterface(image.getFilename());
                    exif.getLatLong(latLong);

                    addresses = geocoder.getFromLocation(latLong[0], latLong[1], 1);
                    if (addresses != null && addresses.size() > 0) {
                        locality = addresses.get(0).getLocality();
                        countryName = addresses.get(0).getCountryName();

                        image.setLocation(locality + ", " + countryName);
                        DatabaseHelper.getInstance().getImagesDao().update(image);
                    }
                } catch (IOException e) {
                    Log.w(getClass().getSimpleName(), e);
                }
            }
        }

        Log.v(getClass().getSimpleName(), "Location update done.");
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

    private void generateLabels() {
        Log.v(getClass().getSimpleName(), "Generating labels...");

        final List<Image> images = DatabaseHelper.getInstance().getImagesDao().queryForAll();

        for (Image image : images) {
            generateLabelsFromDate(image);
            generateLabelsFromLocation(image);
            //generateLabelsFromPeople(image);
        }

        Log.v(getClass().getSimpleName(), "Labels generated.");
    }

    private void createLabel(Image image, String labelName, LabelType type) {
        try {
            final PreparedQuery<Label> preparedQuery = DatabaseHelper.getInstance().getLabelsDao().queryBuilder().selectColumns(new String[]{Label.COLUMN_NAME}).where().eq(Label.COLUMN_NAME, labelName).prepare();
            Label label = DatabaseHelper.getInstance().getLabelsDao().queryForFirst(preparedQuery);
            if (label == null) {
                label = new Label(labelName, Calendar.getInstance().getTime(), type);
                DatabaseHelper.getInstance().getLabelsDao().create(label);
            }
            Label2Image binder = new Label2Image(label, image, Calendar.getInstance().getTime());
            DatabaseHelper.getInstance().getLabels2ImagesDao().createOrUpdate(binder);
        } catch (SQLException e) {
            Log.w(getClass().getSimpleName(), e);
        }
    }

    private void generateLabelsFromDate(Image image) {

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(image.getDateTaken());

        final String year = String.valueOf(calendar.get(Calendar.YEAR));
        createLabel(image, year, LabelType.DATE_NUMERIC);

        final String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        createLabel(image, month, LabelType.DATE_NUMERIC);

        final String dayOfMonth = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        createLabel(image, dayOfMonth, LabelType.DATE_NUMERIC);

        final String monthText = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        createLabel(image, monthText, LabelType.DATE_TEXT);

        final String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        createLabel(image, dayOfWeek, LabelType.DATE_TEXT);
    }

    private void generateLabelsFromLocation(Image image) {
        if (image.getLocation() == null) return;

        final String[] locationStrings = image.getLocation().split(", ");

        createLabel(image, locationStrings[0], LabelType.LOCATION_LOCALITY);
        createLabel(image, locationStrings[1], LabelType.LOCATION_COUNTRY);
    }

    private void generateLabelsFromPeople(Image image) {
        //TODO
    }
}
