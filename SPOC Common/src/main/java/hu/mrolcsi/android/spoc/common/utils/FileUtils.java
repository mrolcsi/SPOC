package hu.mrolcsi.android.spoc.common.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.util.SparseBooleanArray;

import java.io.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.15.
 * Time: 11:46
 */

public abstract class FileUtils {

    public static final String[] ACCEPTED_FORMATS = new String[]{"jpg", "jpeg", "gif", "png"};

    public static String getHumanReadableSize(long rawSize) {
        if (rawSize > 1000000000) return String.format("%.2f GB", (float) rawSize / 1000000000);
        else if (rawSize > 1000000) return String.format("%.2f MB", (float) rawSize / 1000000);
        else if (rawSize > 1000) return String.format("%.2f kB", (float) rawSize / 1000);
        else return String.format("%d B", rawSize);
    }

    public static String readRawResource(Context context, int resourceId) {
        InputStream inputStream = context.getResources().openRawResource(resourceId);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException ignored) {
        }

        return byteArrayOutputStream.toString();
    }

    public static boolean deleteFile(String filename) throws IOException {
        File file = new File(filename);

        if (!file.exists()) throw new FileNotFoundException();
        if (!file.canRead() || !file.canWrite())
            throw new IOException("File cannot be read or written. No permission to write on disk?");

        return file.delete();
    }

    public static Intent createShareIntent(String filename) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filename));
        return shareIntent;
    }

    public static class MultiFileDeleterTask extends MultiFileProcessorTask<Void> {

        public MultiFileDeleterTask(Context context, SparseBooleanArray checkedPositions) {
            super(context, checkedPositions);
        }

        protected Void processCursor(Cursor cursor) {
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String filename;

            int i = 0;
            int size = mCheckedPositions.size();

            while (cursor.moveToNext()) {

                if (mCheckedPositions.get(cursor.getPosition())) {
                    filename = cursor.getString(columnIndex);

                    try {
                        if (FileUtils.deleteFile(filename)) publishProgress(++i, size);
                    } catch (IOException e) {
                        Log.w(getClass().getName(), e);
                        // TODO: pass exception to UI
                    }
                }
            }
            return null;
        }
    }

    public static class MultiFileShareTask extends MultiFileProcessorTask<Intent> {
        public MultiFileShareTask(Context context, SparseBooleanArray checkedPositions) {
            super(context, checkedPositions);
        }

        @Override
        protected Intent processCursor(Cursor cursor) {
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String filename;

            int i = 0;
            int size = mCheckedPositions.size();

            ArrayList<Uri> uris = new ArrayList<>();

            while (cursor.moveToNext()) {

                if (mCheckedPositions.get(cursor.getPosition())) {
                    filename = cursor.getString(columnIndex);

                    uris.add(Uri.parse("file://" + filename));

                    publishProgress(++i, size);
                }
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            shareIntent.setType("image/*");

            return shareIntent;
        }
    }

    private static abstract class MultiFileProcessorTask<Result> extends AsyncTask<CursorLoader, Integer, Result> {
        protected final SparseBooleanArray mCheckedPositions;
        private final Context context;

        public MultiFileProcessorTask(Context context, SparseBooleanArray checkedPositions) {
            this.context = context;
            mCheckedPositions = checkedPositions;
        }

        @Override
        protected Result doInBackground(CursorLoader... loader) {
            //prepare query
            Uri uri = loader[0].getUri();
            String[] projection = loader[0].getProjection();
            String selection = loader[0].getSelection();
            final String[] selectionArgs = loader[0].getSelectionArgs();
            String sortOrder = loader[0].getSortOrder();

            Cursor cursor = null;
            Result result = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
                if (cursor == null) return null;

                result = processCursor(cursor);

            } finally {
                if (cursor != null) cursor.close();
            }

            return result;
        }

        protected abstract Result processCursor(Cursor cursor);
    }
}
