package hu.mrolcsi.android.spoc.gallery.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import hu.mrolcsi.android.spoc.gallery.R;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.30.
 * Time: 9:30
 */

public class CacheBuilderReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID = 32;
    private final NotificationCompat.Builder mBuilder;
    private final NotificationManager mNotificationManager;

    public CacheBuilderReceiver(Context context) {

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
        mBuilder.setOngoing(true);
        mBuilder.setCategory(NotificationCompat.CATEGORY_PROGRESS);
        mBuilder.setSmallIcon(R.drawable.ic_notification_small);
        mBuilder.setContentTitle(context.getString(R.string.service_cache_title));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int totalCount = intent.getIntExtra(CacheBuilderService.EXTENDED_DATA_COUNT, -1);
        if (totalCount < 0) return;

        int position = intent.getIntExtra(CacheBuilderService.EXTENDED_DATA_POSITION, -1);
        if (position < 0) return;

        //notify user about progress
        if (position < totalCount) {
            mBuilder.setProgress(totalCount, position, false);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            mBuilder.setContentText(String.format("%1$d/%2$d\t(%3$.0f%%)", position, totalCount, (float) position / totalCount * 100f));
        } else {
            mBuilder.setProgress(0, 0, false);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }
}
