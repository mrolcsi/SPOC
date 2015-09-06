package hu.mrolcsi.android.spoc.common.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import hu.mrolcsi.android.spoc.common.R;
import hu.mrolcsi.android.spoc.common.helper.ListHelper;
import hu.mrolcsi.android.spoc.common.utils.FileUtils;
import hu.mrolcsi.android.spoc.database.DatabaseHelper;
import hu.mrolcsi.android.spoc.database.models.Contact;
import hu.mrolcsi.android.spoc.database.models.Image;
import hu.mrolcsi.android.spoc.database.models.Label;
import hu.mrolcsi.android.spoc.database.models.LabelType;
import hu.mrolcsi.android.spoc.database.models.binders.Label2Image;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    private boolean mInternet;

    private Map<String, Integer> mLabelCache = new TreeMap<>();

    public DatabaseBuilderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        mInternet = activeNetworkInfo != null && activeNetworkInfo.isConnected();

        long startTime = System.currentTimeMillis();

        Log.i(getClass().getSimpleName(), "DatabaseBuilder started.");

        Intent progressIntent;

        DatabaseHelper.init(getApplicationContext());

        cleanUpImages();

        updateImagesFromMediaStore();

        updateImagesFromWhiteList();

        long endTime = System.currentTimeMillis();
        Log.i(getClass().getSimpleName(), String.format("Images updated in %d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(endTime - startTime), TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime - startTime))));

        //everything else should be done in the background

        progressIntent = new Intent(BROADCAST_ACTION_IMAGES_READY);
        LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);

        final boolean isFirstStart = intent.getBooleanExtra(ARG_FIRST_START, false);

        if (isFirstStart) {
            Intent cacheIntent = new Intent(getApplicationContext(), CacheBuilderService.class);
            startService(cacheIntent);
        }

        //TODO: cleanUpContacts();

        //TODO: updateContactsFromContactsProvider();

        //TODO: updateContactsFromFacebook();

        generateLabels();

        progressIntent = new Intent(BROADCAST_ACTION_FINISHED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);

        endTime = System.currentTimeMillis();
        Log.i(getClass().getSimpleName(), String.format("DatabaseBuilder finished in %d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(endTime - startTime), TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime - startTime))));
    }

    private void cleanUpImages() {
        //TODO: delete not existing images from db
        Log.v(getClass().getSimpleName(), "Cleaning up images...");

        final ListHelper listHelper = new ListHelper(getApplicationContext());

        final Cursor cursor = getContentResolver().query(SPOCContentProvider.IMAGES_URI, new String[]{"_id", Image.COLUMN_FILENAME}, null, null, null);
        try {
            String filename;

            final ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            while (cursor.moveToNext()) {
                filename = cursor.getString(1);
                if (!new File(filename).exists() || listHelper.isInBlacklist(filename)) {
                    ops.add(ContentProviderOperation.newDelete(Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, String.valueOf(cursor.getLong(0)))).build());
                    //numRowsDeleted += getContentResolver().delete(Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, String.valueOf(cursor.getLong(0))), null, null);
                }
            }
            getContentResolver().applyBatch(SPOCContentProvider.AUTHORITY, ops);
        } catch (RemoteException | OperationApplicationException e) {
            Log.w(getClass().getSimpleName(), e);
        } finally {
            cursor.close();
        }


        Log.v(getClass().getSimpleName(), "Image clean up done.");
    }

    private void updateImagesFromMediaStore() {
        Log.v(getClass().getSimpleName(), "Updating images from MediaStore...");

        //prepare query
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns.DATE_TAKEN};
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";

        Cursor mediaStoreCursor = null;

        final ListHelper listHelper = new ListHelper(getApplicationContext());

        try {
            mediaStoreCursor = getContentResolver().query(uri, projection, null, null, sortOrder);

            int indexID = mediaStoreCursor.getColumnIndex(projection[0]);
            int indexData = mediaStoreCursor.getColumnIndex(projection[1]);
            int indexDateTaken = mediaStoreCursor.getColumnIndex(projection[2]);

            int mediaStoreId;
            String filename;
            long dateTaken;
            String location;
            ContentValues values;
            Uri imagesByMediaStoreIdUri = Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, Image.COLUMN_MEDIASTORE_ID);

            final ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            while (mediaStoreCursor.moveToNext()) {
                mediaStoreId = mediaStoreCursor.getInt(indexID);
                filename = mediaStoreCursor.getString(indexData);
                dateTaken = mediaStoreCursor.getLong(indexDateTaken);

                if (new File(filename).exists() && !listHelper.isInBlacklist(filename)) {
                    values = new ContentValues();
                    values.put(Image.COLUMN_MEDIASTORE_ID, mediaStoreId);
                    values.put(Image.COLUMN_FILENAME, filename);
                    values.put(Image.COLUMN_DATE_TAKEN, dateTaken);


                    final Cursor imageCursor = getContentResolver().query(Uri.withAppendedPath(imagesByMediaStoreIdUri, String.valueOf(mediaStoreId)),
                            new String[]{"_id", Image.COLUMN_LOCATION},
                            null, null, null);

                    if (imageCursor.moveToFirst()) {
                        //update db with mediastore values
                        if (mInternet && TextUtils.isEmpty(imageCursor.getString(1))) {
                            location = buildLocationString(filename);
                            if (location != null) {
                                values.put(Image.COLUMN_LOCATION, location);
                            }
                        }
                        ops.add(ContentProviderOperation.newUpdate(Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, String.valueOf(imageCursor.getLong(0)))).withValues(values).build());
                        //getContentResolver().update(Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, String.valueOf(imageCursor.getLong(0))), values, null, null);
                    } else {
                        //add new db entry with mediastore values
                        if (mInternet) {
                            location = buildLocationString(filename);
                            if (location != null) {
                                values.put(Image.COLUMN_LOCATION, location);
                            }
                        }
                        ops.add(ContentProviderOperation.newInsert(SPOCContentProvider.IMAGES_URI).withValues(values).build());
                        //getContentResolver().insert(SPOCContentProvider.IMAGES_URI, values);
                    }
                    imageCursor.close();
                }
            }

            getContentResolver().applyBatch(SPOCContentProvider.AUTHORITY, ops);
        } catch (RemoteException | OperationApplicationException e) {
            Log.w(getClass().getSimpleName(), e);
        } finally {
            if (mediaStoreCursor != null) mediaStoreCursor.close();
        }
        Log.v(getClass().getSimpleName(), "Update from MediaStore done.");
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

        final SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.spoc_exifParser), Locale.getDefault());

        try {
            File dir;
            String location;
            ContentValues values;

            final ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            for (String s : whitelist) {
                dir = new File(s);
                for (File file : dir.listFiles(filter)) {
                    values = new ContentValues();
                    values.put(Image.COLUMN_FILENAME, file.getAbsolutePath());

                    Date date;
                    if (file.getAbsolutePath().contains("jpg") || file.getAbsolutePath().contains("jpeg")) { //TODO: additional extensions
                        ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                        String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);
                        date = sdf.parse(dateString);
                    } else {
                        date = new Date(file.lastModified());
                    }
                    values.put(Image.COLUMN_DATE_TAKEN, date.getTime());

                    final Cursor imageCursor = getContentResolver().query(SPOCContentProvider.IMAGES_URI,
                            new String[]{"_id"},
                            Image.COLUMN_FILENAME + " = ?",
                            new String[]{file.getAbsolutePath()},
                            null);

                    if (imageCursor.moveToFirst()) {
                        if (mInternet && TextUtils.isEmpty(imageCursor.getString(1))) {
                            location = buildLocationString(file.getAbsolutePath());
                            if (location != null) {
                                values.put(Image.COLUMN_LOCATION, location);
                            }
                        }
                        ops.add(ContentProviderOperation.newUpdate(Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, String.valueOf(imageCursor.getLong(0)))).withValues(values).build());
                        //getContentResolver().update(Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, String.valueOf(imageCursor.getLong(0))), values, null, null);
                    } else {
                        if (mInternet) {
                            location = buildLocationString(file.getAbsolutePath());
                            if (location != null) {
                                values.put(Image.COLUMN_LOCATION, location);
                            }
                        }
                        ops.add(ContentProviderOperation.newInsert(SPOCContentProvider.IMAGES_URI).withValues(values).build());
                        //getContentResolver().insert(SPOCContentProvider.IMAGES_URI, values);
                    }

                    imageCursor.close();
                }
            }

            getContentResolver().applyBatch(SPOCContentProvider.AUTHORITY, ops);
        } catch (IOException | ParseException e) {
            Log.w(getClass().getName(), e);
        } catch (RemoteException | OperationApplicationException e) {
            Log.w(getClass().getSimpleName(), e);
        }
        Log.v(getClass().getSimpleName(), "Update from Whitelist done.");
    }

    private String buildLocationString(String filename) {
        try {
            ExifInterface exif = new ExifInterface(filename);

            float[] latLong = new float[2];
            exif.getLatLong(latLong);

            Geocoder geocoder = new Geocoder(this);

            List<Address> addresses = geocoder.getFromLocation(latLong[0], latLong[1], 1);
            if (addresses != null && addresses.size() > 0) {
                String locality = addresses.get(0).getLocality();
                String countryName = addresses.get(0).getCountryName();

                return locality + ", " + countryName;
            }
        } catch (IOException e) {
            Log.w(getClass().getSimpleName(), e.toString());
        }
        return null;
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
        long startTime = System.currentTimeMillis();
        Log.v(getClass().getSimpleName(), "Generating labels...");

        final Cursor cursor = getContentResolver().query(SPOCContentProvider.IMAGES_URI,
                new String[]{"_id", Image.COLUMN_DATE_TAKEN, Image.COLUMN_LOCATION},
                null, null, null);

        while (cursor.moveToNext()) {
            generateLabelsFromDate(cursor);
            generateLabelsFromLocation(cursor);
            //generateLabelsFromPeople(cursor);
        }

        cursor.close();

        long endTime = System.currentTimeMillis();
        Log.v(getClass().getSimpleName(), String.format("Labels generated in %d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(endTime - startTime), TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime - startTime))));
    }

    private void createLabel(long imageId, String labelName, LabelType type) {

        int labelId;
        if (mLabelCache.containsKey(labelName)) {
            labelId = mLabelCache.get(labelName);
        } else {
            final Uri labelNameUri = SPOCContentProvider.LABELS_URI.buildUpon().appendPath(Label.COLUMN_NAME).appendPath(labelName).build();
            final Cursor labelCursor = getContentResolver().query(labelNameUri, new String[]{"_id"}, null, null, null);

            if (labelCursor.moveToFirst()) {
                labelId = labelCursor.getInt(0);
            } else {
                ContentValues values = new ContentValues();
                values.put(Label.COLUMN_NAME, labelName);
                values.put(Label.COLUMN_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
                values.put(Label.COLUMN_TYPE, type.toString());

                final Uri insert = getContentResolver().insert(SPOCContentProvider.LABELS_URI, values);
                labelId = Integer.parseInt(insert.getLastPathSegment());
            }
            labelCursor.close();
        }

        mLabelCache.put(labelName, labelId);

        ContentValues values = new ContentValues();
        values.put(Label2Image.COLUMN_DATE, Calendar.getInstance().getTimeInMillis());
        values.put(Label2Image.COLUMN_IMAGE_ID, imageId);
        values.put(Label2Image.COLUMN_LABEL_ID, labelId);

        getContentResolver().insert(SPOCContentProvider.LABELS_2_IMAGES_URI, values);
    }

    private void generateLabelsFromDate(Cursor cursorWithImage) {

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(cursorWithImage.getLong(1)));

        final long imageId = cursorWithImage.getLong(0);

        final String year = String.valueOf(calendar.get(Calendar.YEAR));
        createLabel(imageId, year, LabelType.DATE_NUMERIC);

        final String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        createLabel(imageId, month, LabelType.DATE_NUMERIC);

        final String dayOfMonth = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        createLabel(imageId, dayOfMonth, LabelType.DATE_NUMERIC);

        final String monthText = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        createLabel(imageId, monthText, LabelType.DATE_TEXT);

        final String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        createLabel(imageId, dayOfWeek, LabelType.DATE_TEXT);
    }

    private void generateLabelsFromLocation(Cursor cursorWithImage) {
        if (cursorWithImage.getString(2) == null) return;

        final String[] locationStrings = cursorWithImage.getString(2).split(", ");

        final long imageId = cursorWithImage.getLong(0);

        createLabel(imageId, locationStrings[0], LabelType.LOCATION_LOCALITY);
        createLabel(imageId, locationStrings[1], LabelType.LOCATION_COUNTRY);
    }

    private void generateLabelsFromPeople(Cursor cursorWithImage) {
        //TODO
    }
}
