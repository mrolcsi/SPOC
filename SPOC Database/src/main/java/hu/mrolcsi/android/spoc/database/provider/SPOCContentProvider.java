package hu.mrolcsi.android.spoc.database.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import hu.mrolcsi.android.spoc.database.DatabaseHelper;
import hu.mrolcsi.android.spoc.database.models.Image;
import hu.mrolcsi.android.spoc.database.models.Label;
import hu.mrolcsi.android.spoc.database.models.binders.Label2Image;

import java.util.ArrayList;

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

    private static final int IMAGES_LIST = 10;
    private static final int IMAGE_BY_ID = 11;
    private static final int IMAGE_BY_MEDIASTORE_ID = 12;
    private static final int LABELS_LIST = 20;
    private static final int LABEL_BY_ID = 21;
    private static final int LABEL_BY_NAME = 22;
    private static final int LABELS_BY_IMAGE_ID = 30;
    private static final int LABELS_2_IMAGES = 40;
    //and so on
    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME, IMAGES_LIST);
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME + "/#", IMAGE_BY_ID);
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME + "/" + Image.COLUMN_MEDIASTORE_ID + "/#", IMAGE_BY_MEDIASTORE_ID);

        URI_MATCHER.addURI(AUTHORITY, Label.TABLE_NAME, LABELS_LIST);
        URI_MATCHER.addURI(AUTHORITY, Label.TABLE_NAME + "/#", LABEL_BY_ID);
        URI_MATCHER.addURI(AUTHORITY, Label.TABLE_NAME + "/" + Label.COLUMN_NAME + "/*", LABEL_BY_NAME);
        URI_MATCHER.addURI(AUTHORITY, Label.TABLE_NAME + "/" + Label2Image.COLUMN_IMAGE_ID + "/#", LABELS_BY_IMAGE_ID);

        URI_MATCHER.addURI(AUTHORITY, Label2Image.TABLE_NAME, LABELS_2_IMAGES);
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
        final SQLiteDatabase db = dbHelper.getReadableDatabase();

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        boolean useAuthority = false;

        switch (URI_MATCHER.match(uri)) {
            case IMAGES_LIST:
                builder.setTables(Image.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = Image.COLUMN_DATE_TAKEN + " DESC";
                }
                break;
            case IMAGE_BY_ID:
                builder.setTables(Image.TABLE_NAME);
                builder.appendWhere("_id" + " = " + uri.getLastPathSegment());
                break;
            case IMAGE_BY_MEDIASTORE_ID:
                builder.setTables(Image.TABLE_NAME);
                builder.appendWhere(Image.COLUMN_MEDIASTORE_ID + " = " + uri.getLastPathSegment());
                break;
            case LABELS_LIST:
                builder.setTables(Label.TABLE_NAME);
                break;
            case LABEL_BY_ID:
                builder.setTables(Label.TABLE_NAME);
                builder.appendWhere("_id = " + uri.getLastPathSegment());
                break;
            case LABEL_BY_NAME:
                builder.setTables(Label.TABLE_NAME);
                builder.appendWhere(Label.COLUMN_NAME + " = '" + uri.getLastPathSegment() + "'");
                break;
            case LABELS_BY_IMAGE_ID:
                if (projection == null) {
                    projection = new String[]{Label.TABLE_NAME + ".*"};
                }
                builder.setTables(Label.TABLE_NAME + "INNER JOIN " + Label2Image.TABLE_NAME + " ON " + Label.TABLE_NAME + "._id" + " = " + Label2Image.COLUMN_LABEL_ID);
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

    @Override
    public String getType(Uri uri) {
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case IMAGES_LIST:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.hu.mrolcsi.android.spoc.database.images";
            case IMAGE_BY_ID:
            case IMAGE_BY_MEDIASTORE_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.hu.mrolcsi.android.spoc.database.images";
            case LABELS_LIST:
            case LABELS_BY_IMAGE_ID:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.hu.mrolcsi.android.spoc.database.labels";
            case LABEL_BY_ID:
            case LABEL_BY_NAME:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.hu.mrolcsi.android.spoc.database.labels";
            case LABELS_2_IMAGES:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.hu.mrolcsi.android.spoc.database.labels2images";
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
            default:
                throw new IllegalArgumentException("Unsupported Uri for insertion: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int delCount = 0;

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
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (delCount > 0 && !inBatchMode) getContext().getContentResolver().notifyChange(uri, null);

        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updateCount = 0;

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
        return null;
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
        }
    }
}
