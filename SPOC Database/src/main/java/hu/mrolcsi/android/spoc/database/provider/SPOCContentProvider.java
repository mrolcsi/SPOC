package hu.mrolcsi.android.spoc.database.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import hu.mrolcsi.android.spoc.database.DatabaseHelper;
import hu.mrolcsi.android.spoc.database.model.Contact;
import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.database.model.Label;
import hu.mrolcsi.android.spoc.database.model.Views;
import hu.mrolcsi.android.spoc.database.model.binder.Label2Image;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.11.
 * Time: 10:05
 */

public final class SPOCContentProvider extends ContentProvider {

    public static final String AUTHORITY = "hu.mrolcsi.android.spoc.database.provider";
    private static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri IMAGES_URI = Uri.withAppendedPath(CONTENT_URI, Image.TABLE_NAME);
    public static final Uri LABELS_URI = Uri.withAppendedPath(CONTENT_URI, Label.TABLE_NAME);
    public static final Uri LABELS_2_IMAGES_URI = Uri.withAppendedPath(CONTENT_URI, Label2Image.TABLE_NAME);
    public static final Uri SEARCH_URI = CONTENT_URI.buildUpon().appendPath(Image.TABLE_NAME).appendPath("search").build();
    public static final Uri CONTACTS_URI = Uri.withAppendedPath(CONTENT_URI, Contact.TABLE_NAME);

    private static final int IMAGES_LIST = 10;
    private static final int IMAGE_BY_ID = 11;
    private static final int IMAGE_BY_MEDIASTORE_ID = 12;
    private static final int IMAGE_SEARCH = 13;
    private static final int IMAGES_WITH_DAY_TAKEN = 14;
    private static final int IMAGES_BY_DAY_TAKEN = 15;
    private static final int IMAGES_BY_DAY_TAKEN_COUNT = 16;
    private static final int IMAGES_WITH_LABELS = 17;
    private static final int IMAGES_WITH_LABELS_COUNT = 18;

    private static final int LABELS_LIST = 20;
    private static final int LABEL_BY_ID = 21;
    private static final int LABEL_BY_NAME = 22;
    private static final int LABELS_BY_IMAGE_ID = 23;
    private static final int LABELS_2_IMAGES = 24;

    private static final int CONTACTS_LIST = 31;
    private static final int CONTACT_BY_ID = 32;
    private static final int CONTACT_BY_KEY = 33;

    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME, IMAGES_LIST);                                                                   // content://authority/images                   > SELECT * FROM images > INSERT INTO images2labels
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME + "/#", IMAGE_BY_ID);                                                            // content://authority/images/#                 > SELECT * FROM images WHERE _id = #
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME + "/" + Image.COLUMN_MEDIASTORE_ID + "/#", IMAGE_BY_MEDIASTORE_ID);              // content://authority/images/mediastore_id/#   > SELECT * FROM images WHERE mediastore_id = #
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME + "/" + Views.IMAGES_BY_DAY_DAY_TAKEN, IMAGES_WITH_DAY_TAKEN);                   // content://authority/images/day_taken         > SELECT * FROM images INNER JOIN images_by_day
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME + "/" + Views.IMAGES_BY_DAY_DAY_TAKEN + "/#", IMAGES_BY_DAY_TAKEN);              // content://authority/images/day_taken/#       > SELECT * FROM images INNER JOIN images_by_day WHERE day_taken = #
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME + "/" + Views.IMAGES_BY_DAY_DAY_TAKEN + "/count", IMAGES_BY_DAY_TAKEN_COUNT);    // content://authority/images/day_taken/count   > SELECT count(_id), day_taken FROM images INNER JOIN images_by_day GROUP BY day_taken
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME + "/" + Image.COLUMN_LOCATION + "/count", IMAGES_WITH_LABELS_COUNT);             // content://authority/images/location/count    > SELECT * FROM images_with_labels GROUP BY location
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME + "/search", IMAGES_WITH_LABELS);                                                // content://authority/images/search            > SELECT * FROM images_with_labels
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME + "/search/*", IMAGE_SEARCH);                                                    // content://authority/images/search/*          > SELECT * FROM images_with_labels WHERE column LIKE '%*%'

        URI_MATCHER.addURI(AUTHORITY, Label.TABLE_NAME, LABELS_LIST);                                                                   // content://authority/labels                   > SELECT * FROM labels > INSERT INTO labels
        URI_MATCHER.addURI(AUTHORITY, Label.TABLE_NAME + "/#", LABEL_BY_ID);                                                            // content://authority/labels/#                 > SELECT * FROM labels WHERE _id = #
        URI_MATCHER.addURI(AUTHORITY, Label.TABLE_NAME + "/" + Label.COLUMN_NAME + "/*", LABEL_BY_NAME);                                // content://authority/labels/name/*            > SELECT * FROM labels WHERE name = *
        URI_MATCHER.addURI(AUTHORITY, Label.TABLE_NAME + "/" + Label2Image.COLUMN_IMAGE_ID + "/#", LABELS_BY_IMAGE_ID);                 // content://authority/labels/image_id/#        > SELECT * FROM labels INNER JOIN labels2images WHERE image_id = #

        URI_MATCHER.addURI(AUTHORITY, Label2Image.TABLE_NAME, LABELS_2_IMAGES);                                                         // content://authority/images2labels            > INSERT INTO images2labels

        URI_MATCHER.addURI(AUTHORITY, Contact.TABLE_NAME, CONTACTS_LIST);                                                               // content://authority/contacts                 > SELECT * FROM contacts
        URI_MATCHER.addURI(AUTHORITY, Contact.TABLE_NAME + "/#", CONTACT_BY_ID);                                                        // content://authority/contacts/#               > SELECT * FROM contacts WHERE _id = #
        URI_MATCHER.addURI(AUTHORITY, Contact.TABLE_NAME + "/" + Contact.COLUMN_CONTACT_KEY + "/*", CONTACT_BY_KEY);                    // content://authority/contacts/key/*           > SELECT * FROM contacts WHERE key = *
    }

    private DatabaseHelper dbHelper;
    private boolean inBatchMode = false;

    @Override
    public boolean onCreate() {
        DatabaseHelper.init(getContext());
        dbHelper = DatabaseHelper.getInstance();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final String firstSegment = uri.getPathSegments().get(0);

        if (firstSegment.equals(Image.TABLE_NAME)) {
            return queryImages(uri, projection, selection, selectionArgs, sortOrder);
        }
        if (firstSegment.equals(Label.TABLE_NAME)) {
            return queryLabels(uri, projection, selection, selectionArgs, sortOrder);
        }
        if (firstSegment.equals(Contact.TABLE_NAME)) {
            return queryContacts(uri, projection, selection, selectionArgs, sortOrder);
        }

        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }

    private Cursor queryImages(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Image.TABLE_NAME);
        boolean useAuthority = false;

        switch (URI_MATCHER.match(uri)) {
            case IMAGES_LIST:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = Image.COLUMN_DATE_TAKEN + " DESC";
                }
                break;
            case IMAGE_BY_ID:
                builder.appendWhere("_id" + " = " + uri.getLastPathSegment());
                break;
            case IMAGE_BY_MEDIASTORE_ID:
                builder.appendWhere(Image.COLUMN_MEDIASTORE_ID + " = " + uri.getLastPathSegment());
                break;
            case IMAGE_SEARCH:
                builder.setTables(Views.IMAGES_WITH_LABELS_NAME);
                if (projection == null) {
                    projection = new String[]{"DISTINCT _id", Image.COLUMN_FILENAME};
                }
                builder.appendWhere(Image.COLUMN_FILENAME + " LIKE '%" + uri.getLastPathSegment().toLowerCase(Locale.getDefault()) + "%' OR " + Label.COLUMN_NAME + " LIKE '%" + uri.getLastPathSegment().toLowerCase() + "%'");
                break;
            case IMAGES_WITH_LABELS:
                builder.setTables(Views.IMAGES_WITH_LABELS_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = Image.COLUMN_DATE_TAKEN + " DESC";
                }
                break;
            case IMAGES_WITH_DAY_TAKEN:
                builder.setTables(Image.TABLE_NAME + " INNER JOIN " + Views.IMAGES_BY_DAY_NAME + " ON " + Views.IMAGES_BY_DAY_NAME + "._id=" + Image.TABLE_NAME + "._id");
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = Image.COLUMN_DATE_TAKEN + " DESC";
                }
                break;
            case IMAGES_BY_DAY_TAKEN:
                builder.setTables(Image.TABLE_NAME + " INNER JOIN " + Views.IMAGES_BY_DAY_NAME + " ON " + Views.IMAGES_BY_DAY_NAME + "._id=" + Image.TABLE_NAME + "._id");
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = Image.COLUMN_DATE_TAKEN + " DESC";
                }
                builder.appendWhere(Views.IMAGES_BY_DAY_DAY_TAKEN + "=" + uri.getLastPathSegment());
                break;
            case IMAGES_BY_DAY_TAKEN_COUNT:
                builder.setTables(Image.TABLE_NAME + " INNER JOIN " + Views.IMAGES_BY_DAY_NAME + " ON " + Views.IMAGES_BY_DAY_NAME + "._id=" + Image.TABLE_NAME + "._id");
                if (projection == null) {
                    projection = new String[]{"count(" + Image.TABLE_NAME + "._id)", Views.IMAGES_BY_DAY_DAY_TAKEN};
                }
                sortOrder = Views.IMAGES_BY_DAY_DAY_TAKEN + " DESC";
                return builder.query(db, projection, selection, selectionArgs, Views.IMAGES_BY_DAY_DAY_TAKEN, null, sortOrder);
            case IMAGES_WITH_LABELS_COUNT:
                builder.setTables(Views.IMAGES_WITH_LABELS_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = Image.COLUMN_DATE_TAKEN + " DESC";
                }
                return builder.query(db, projection, selection, selectionArgs, Label.COLUMN_NAME, null, sortOrder);
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        //noinspection ConstantConditions
        if (useAuthority) cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
        else cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    private Cursor queryLabels(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Label.TABLE_NAME);
        boolean useAuthority = false;

        switch (URI_MATCHER.match(uri)) {
            case LABELS_LIST:
                break;
            case LABEL_BY_ID:
                builder.appendWhere("_id = " + uri.getLastPathSegment());
                break;
            case LABEL_BY_NAME:
                builder.appendWhere(Label.COLUMN_NAME + " = '" + uri.getLastPathSegment() + "'");
                break;
            case LABELS_BY_IMAGE_ID:
                builder.setTables(Label.TABLE_NAME + "INNER JOIN " + Label2Image.TABLE_NAME + " ON " + Label.TABLE_NAME + "._id" + " = " + Label2Image.COLUMN_LABEL_ID);
                if (projection == null) {
                    projection = new String[]{Label.TABLE_NAME + ".*"};
                }
                builder.appendWhere(Label2Image.COLUMN_IMAGE_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        //noinspection ConstantConditions
        if (useAuthority) cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
        else cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    private Cursor queryContacts(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Contact.TABLE_NAME);
        boolean useAuthority = false;

        switch (URI_MATCHER.match(uri)) {
            case CONTACTS_LIST:
                break;
            case CONTACT_BY_ID:
                builder.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            case CONTACT_BY_KEY:
                builder.appendWhere(Contact.COLUMN_CONTACT_KEY + "='" + uri.getLastPathSegment() + "'");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        //noinspection ConstantConditions
        if (useAuthority) cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
        else cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case IMAGES_LIST:
            case IMAGES_WITH_DAY_TAKEN:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.hu.mrolcsi.android.spoc.database." + Image.TABLE_NAME;
            case IMAGE_BY_ID:
            case IMAGE_BY_MEDIASTORE_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.hu.mrolcsi.android.spoc.database." + Image.TABLE_NAME;
            case LABELS_LIST:
            case LABELS_BY_IMAGE_ID:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.hu.mrolcsi.android.spoc.database." + Label.TABLE_NAME;
            case LABEL_BY_ID:
            case LABEL_BY_NAME:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.hu.mrolcsi.android.spoc.database." + Label.TABLE_NAME;
            case LABELS_2_IMAGES:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.hu.mrolcsi.android.spoc.database." + Label2Image.TABLE_NAME;
            case IMAGE_SEARCH:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.hu.mrolcsi.android.spoc.database." + Views.IMAGES_WITH_LABELS_NAME;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = URI_MATCHER.match(uri);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;

        switch (match) {
            case IMAGES_LIST:
                id = db.insert(Image.TABLE_NAME, null, contentValues);
                return getUriForId(id, uri);
            case LABELS_LIST:
                id = db.insert(Label.TABLE_NAME, null, contentValues);
                return getUriForId(id, uri);
            case LABELS_2_IMAGES:
                id = db.insertWithOnConflict(Label2Image.TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                return getUriForId(id, uri);
            case CONTACTS_LIST:
                id = db.insert(Contact.TABLE_NAME, null, contentValues);
                return getUriForId(id, uri);
            default:
                throw new IllegalArgumentException("Unsupported Uri for insertion: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int delCount;

        String idStr;
        String where;

        switch (URI_MATCHER.match(uri)) {
            case IMAGES_LIST:
                delCount = db.delete(Image.TABLE_NAME, selection, selectionArgs);
                break;
            case IMAGE_BY_ID:
                idStr = uri.getLastPathSegment();
                where = "_id" + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) where += " AND " + selection;
                delCount = db.delete(Image.TABLE_NAME, where, selectionArgs);
                break;
            case LABELS_LIST:
                delCount = db.delete(Label.TABLE_NAME, selection, selectionArgs);
                break;
            case LABEL_BY_ID:
                idStr = uri.getLastPathSegment();
                where = "_id" + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) where += " AND " + selection;
                delCount = db.delete(Label.TABLE_NAME, where, selectionArgs);
                break;
            case CONTACTS_LIST:
                delCount = db.delete(Contact.TABLE_NAME, selection, selectionArgs);
                break;
            case CONTACT_BY_ID:
                idStr = uri.getLastPathSegment();
                where = "_id" + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) where += " AND " + selection;
                delCount = db.delete(Contact.TABLE_NAME, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (delCount > 0 && !inBatchMode) getContext().getContentResolver().notifyChange(uri, null);

        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updateCount;

        String idStr;
        String where;

        switch (URI_MATCHER.match(uri)) {
            case IMAGES_LIST:
                updateCount = db.update(Image.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case IMAGE_BY_ID:
                idStr = uri.getLastPathSegment();
                where = "_id" + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(Image.TABLE_NAME, contentValues, where, selectionArgs);
                break;
            case LABELS_LIST:
                updateCount = db.update(Label.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case LABEL_BY_ID:
                idStr = uri.getLastPathSegment();
                where = "_id" + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(Label.TABLE_NAME, contentValues, where, selectionArgs);
                break;
            case CONTACTS_LIST:
                updateCount = db.update(Contact.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case CONTACT_BY_ID:
                idStr = uri.getLastPathSegment();
                where = "_id" + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(Contact.TABLE_NAME, contentValues, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (updateCount > 0 && !inBatchMode) getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }

    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            if (!inBatchMode) {
                // notify all listeners of changes:
                getContext().getContentResolver().notifyChange(itemUri, null);
            }
            return itemUri;
        }
        // s.th. went wrong:
        return uri;
    }

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        inBatchMode = true;
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
            inBatchMode = false;
            getContext().getContentResolver().notifyChange(SPOCContentProvider.CONTENT_URI, null);
        }

    }
}
