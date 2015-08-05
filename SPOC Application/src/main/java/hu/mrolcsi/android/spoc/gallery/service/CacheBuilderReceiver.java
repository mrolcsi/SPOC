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

    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;

    public CacheBuilderReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int totalCount = intent.getIntExtra(CacheBuilderService.EXTENDED_DATA_COUNT, -1);
        if (totalCount < 0) return;

        int progress = intent.getIntExtra(CacheBuilderService.EXTENDED_DATA_POSITION, -1);
        if (progress < 0) return;

        if (intent.getAction().equals(CacheBuilderService.BROADCAST_ACTION_INCREMENTAL)) {
            //show notification
            if (mNotificationManager == null)
                mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (mNotificationBuilder == null) {

                mNotificationBuilder = new NotificationCompat.Builder(context);
                mNotificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
                mNotificationBuilder.setOngoing(true);
                mNotificationBuilder.setCategory(NotificationCompat.CATEGORY_PROGRESS);
                mNotificationBuilder.setSmallIcon(R.drawable.ic_notification_small);
                mNotificationBuilder.setContentTitle(context.getString(R.string.service_cache_title));
            }

            //notify user about progress
            if (progress < totalCount) {
                mNotificationBuilder.setProgress(totalCount, progress, false);
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                mNotificationBuilder.setContentText(String.format("%1$d/%2$d\t(%3$.0f%%)", progress, totalCount, (float) progress / totalCount * 100f));
            } else {
                mNotificationBuilder.setProgress(0, 0, false);
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                mNotificationManager.cancel(NOTIFICATION_ID);
            }
        }
    }
}
