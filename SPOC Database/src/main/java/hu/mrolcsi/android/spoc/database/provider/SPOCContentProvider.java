package hu.mrolcsi.android.spoc.database.provider;

import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import hu.mrolcsi.android.spoc.database.DatabaseHelper;
import hu.mrolcsi.android.spoc.database.models.Image;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.11.
 * Time: 10:05
 */

public final class SPOCContentProvider extends ContentProvider {

    private static final String AUTHORITY = "hu.mrolcsi.android.spoc.database.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    private static final int IMAGE_LIST = 1;
    private static final int IMAGE_ID = 2;
    //and so on
    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME, IMAGE_LIST);
        URI_MATCHER.addURI(AUTHORITY, Image.TABLE_NAME + "/#", IMAGE_ID);
    }

    private DatabaseHelper dbHelper;
    private boolean inBatchMode;

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
            case IMAGE_LIST:
                builder.setTables(Image.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = Image.COLUMN_DATE_TAKEN + " DESC";
                }
                break;
            case IMAGE_ID:
                builder.setTables(Image.TABLE_NAME);
                builder.appendWhere("_id" + " = " + uri.getLastPathSegment());
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
            case IMAGE_LIST:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.hu.mrolcsi.android.spoc.database.images";
            case IMAGE_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.hu.mrolcsi.android.spoc.database.images";
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = URI_MATCHER.match(uri);
        if (match != IMAGE_LIST) { //etc
            throw new IllegalArgumentException("Unsupported Uri for insertion: " + uri);
        }

        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (match == IMAGE_LIST) {
            long id = db.insert(Image.TABLE_NAME, null, contentValues);
            return getUriForId(id, uri);
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int delCount = 0;

        switch (URI_MATCHER.match(uri)) {
            case IMAGE_LIST:
                delCount = db.delete(Image.TABLE_NAME, selection, selectionArgs);
                break;
            case IMAGE_ID:
                String idStr = uri.getLastPathSegment();
                String where = "_id" + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) where += " AND " + selection;
                delCount = db.delete(Image.TABLE_NAME, where, selectionArgs);
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

        switch (URI_MATCHER.match(uri)) {
            case IMAGE_LIST:
                updateCount = db.update(Image.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case IMAGE_ID:
                String idStr = uri.getLastPathSegment();
                String where = "_id" + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(Image.TABLE_NAME, contentValues, where, selectionArgs);
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
            if (!isInBatchMode()) {
                // notify all listeners of changes:
                getContext().getContentResolver().notifyChange(itemUri, null);
            }
            return itemUri;
        }
        // s.th. went wrong:
        throw new SQLException("Problem while inserting into uri: " + uri);
    }

    public boolean isInBatchMode() {
        return inBatchMode;
    }

    public void setInBatchMode(boolean inBatchMode) {
        this.inBatchMode = inBatchMode;
    }
}
