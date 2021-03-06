package hu.mrolcsi.android.spoc.common.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import com.bumptech.glide.Glide;
import hu.mrolcsi.android.spoc.common.helper.GlideHelper;
import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.30.
 * Time: 9:02
 */

public class CacheBuilderService extends IntentService implements Thread.UncaughtExceptionHandler {

    public static final String TAG = "SPOC.Gallery.CacheBuilderService";
    public static final String BROADCAST_ACTION_CACHING = "hu.mrolcsi.android.spoc.BROADCAST_CACHING";
    public static final String EXTENDED_DATA_COUNT = "SPOC.Gallery.CacheBuilderService.COUNT";
    public static final String EXTENDED_DATA_POSITION = "SPOC.Gallery.CacheBuilderService.POSITION";

    @SuppressWarnings("unused") //needed by manifest xml
    public CacheBuilderService() {
        super(TAG);
    }

    @TargetApi(13)
    @Override
    protected void onHandleIntent(Intent intent) {
//        if (BuildConfig.DEBUG) {
//            Log.w(getClass().getSimpleName(), "DELETING DISK CACHE - Don't forget tot remove this!");
//            Glide.get(getApplicationContext()).clearDiskCache();
//        }

        long startTime = System.currentTimeMillis();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SPOCWakeLockTag");
        wakeLock.acquire();

        //prepare parameters
        int thumbnailSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, this.getResources().getDisplayMetrics());
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

        Intent progressIntent = new Intent(BROADCAST_ACTION_CACHING);

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(SPOCContentProvider.IMAGES_URI, new String[]{"_id", Image.COLUMN_FILENAME}, null, null, null);
            if (cursor == null) return;

            String filename;

            Handler handler = new Handler(getApplication().getMainLooper());
            final Runnable clearMemoryRunnable = new Runnable() {
                @Override
                public void run() {
                    Glide.get(getApplicationContext()).clearMemory();
                }
            };

            while (cursor.moveToNext()) {
                filename = cursor.getString(1);

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
//                if (cursor.getPosition() % 10 == 0) //best results with N=10
//                    handler.post(clearMemoryRunnable);

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

            progressIntent
                    .putExtra(EXTENDED_DATA_COUNT, cursor.getCount())
                    .putExtra(EXTENDED_DATA_POSITION, cursor.getCount());
            LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);

            handler.post(clearMemoryRunnable);
            
            long endTime = System.currentTimeMillis();
            Log.i(getClass().getSimpleName(), String.format("Caching done in %d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(endTime - startTime), TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime - startTime))));
        } finally {
            if (cursor != null)
                cursor.close();

            //let's assume it's finished, even when it's not
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(DatabaseBuilderService.ARG_FIRST_START, false).apply();
        }
        wakeLock.release();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.w(getClass().getSimpleName(), throwable.toString());
    }
}

