package hu.mrolcsi.android.spoc.gallery.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.bumptech.glide.Glide;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.GlideHelper;

import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.30.
 * Time: 9:02
 */

public class CacheBuilderService extends IntentService {

    public static final String TAG = "SPOC.Gallery.CacheBuilderService";
    public static final String BROADCAST_ACTION = "SPOC.Gallery.CacheBuilderService.BROADCAST";
    public static final String EXTENDED_DATA_COUNT = "SPOC.Gallery.CacheBuilderService.COUNT";
    public static final String EXTENDED_DATA_POSITION = "SPOC.Gallery.CacheBuilderService.POSITION";

    @SuppressWarnings("unused") //needed by manifest xml
    public CacheBuilderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        if (BuildConfig.DEBUG) {
//            Log.w(getClass().getSimpleName(), "DELETING DISK CACHE - Don't forget tot remove this!");
//            Glide.get(getApplicationContext()).clearDiskCache();
//        }

        //prepare parameters
        int thumbnailSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int screenWidth, screenHeight;
        if (Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
            screenHeight = size.y;
        } else {
            //noinspection deprecation
            screenWidth = display.getWidth();
            //noinspection deprecation
            screenHeight = display.getHeight();
        }

        //prepare query
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
        String sortOrder = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";

        Intent progressIntent = new Intent(BROADCAST_ACTION);

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, projection, null, null, sortOrder);
            if (cursor == null) return;

            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String filename;

            Handler handler = new Handler(getApplication().getMainLooper());
            final Runnable clearMemoryRunnable = new Runnable() {
                @Override
                public void run() {
                    Glide.get(getApplicationContext()).clearMemory();
                }
            };

            //TODO: only cache newly received images
            //  > compare with DB, if it's in DB, it's cached.

            while (cursor.moveToNext()) {
                filename = cursor.getString(columnIndex);

                //cache thumbnails
                try {
                    GlideHelper.cacheThumbnail(getApplicationContext(), filename, thumbnailSize);
                } catch (InterruptedException | ExecutionException e) {
                    Log.w(getClass().getName(), e);
                }

                //cache big images
                try {
                    GlideHelper.cacheBigImage(getApplicationContext(), filename, screenWidth, screenHeight);
                } catch (InterruptedException | ExecutionException e) {
                    Log.w(getClass().getName(), e);
                }

                //reduce garbage every Nth iteration
                // N = 1:   too frequent, too much CPU work
                // N = 10:  looks good
                // N > 10:  OutOfMemoryErrors
                if (cursor.getPosition() % 10 == 0) //best results with N=10
                    handler.post(clearMemoryRunnable);

//                try {
//                    Thread.sleep(150);
//                } catch (InterruptedException e) {
//                    Log.w(getClass().getName(), e);
//                }

                //send progress to receiver
                progressIntent
                        .putExtra(EXTENDED_DATA_COUNT, cursor.getCount())
                        .putExtra(EXTENDED_DATA_POSITION, cursor.getPosition() + 1);
                LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);
            }

            handler.post(clearMemoryRunnable);

        } finally {
            if (cursor != null)
                cursor.close();
        }
    }
}
