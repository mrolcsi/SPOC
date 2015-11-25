package hu.mrolcsi.android.spoc.gallery.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import hu.mrolcsi.android.spoc.common.service.CacheBuilderService;
import hu.mrolcsi.android.spoc.common.service.DatabaseBuilderService;
import hu.mrolcsi.android.spoc.gallery.R;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.05.
 * Time: 9:33
 */

public final class SplashScreenActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver = new FirstTimeSetupReceiver();
    private boolean isFirstStart;

    private ProgressBar progressBar;
    private ProgressBar progressCircle;
    private TextView tvProgress;
    private TextView tvPercent;
    private TextView tvMessage;
    private Button btnBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isFirstStart = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(DatabaseBuilderService.ARG_FIRST_START, true);
        //isFirstStart = true;
        if (isFirstStart) {
            Log.i(getClass().getSimpleName(), "It's a First Start!");
        }

        setContentView(R.layout.activity_splash);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressCircle = (ProgressBar) findViewById(R.id.progressCircle);
        tvProgress = (TextView) findViewById(R.id.tvProgress);
        tvPercent = (TextView) findViewById(R.id.tvPercent);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        btnBackground = (Button) findViewById(R.id.btnBackground);
        btnBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(SplashScreenActivity.this, GalleryActivity.class);
                startActivity(galleryIntent);
                finish();
            }
        });

        Intent serviceIntent = new Intent(this, DatabaseBuilderService.class);
        serviceIntent.putExtra(DatabaseBuilderService.ARG_FIRST_START, isFirstStart);
        startService(serviceIntent);

        IntentFilter serviceIntentFilter = new IntentFilter(DatabaseBuilderService.BROADCAST_ACTION_IMAGES_READY);
        serviceIntentFilter.addAction(CacheBuilderService.BROADCAST_ACTION_CACHING);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, serviceIntentFilter);

        if (!isFirstStart) {
            Intent galleryIntent = new Intent(this, GalleryActivity.class);
            startActivity(galleryIntent);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        isFirstStart = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        }
    }

    class FirstTimeSetupReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DatabaseBuilderService.BROADCAST_ACTION_IMAGES_READY) && !isFirstStart) {
                Intent galleryIntent = new Intent(context, GalleryActivity.class);
                startActivity(galleryIntent);
                finish();
            }

            if (intent.getAction().equals(CacheBuilderService.BROADCAST_ACTION_CACHING)) {
                int totalCount = intent.getIntExtra(CacheBuilderService.EXTENDED_DATA_COUNT, -1);
                if (totalCount < 0) return;

                int progress = intent.getIntExtra(CacheBuilderService.EXTENDED_DATA_POSITION, -1);
                if (progress < 0) return;

                progressCircle.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                tvProgress.setVisibility(View.VISIBLE);
                tvPercent.setVisibility(View.VISIBLE);
                tvMessage.setVisibility(View.VISIBLE);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnBackground.setVisibility(View.VISIBLE);
                    }
                }, 10000);

                if (progress < totalCount) {
                    progressBar.setMax(totalCount);
                    progressBar.setProgress(progress);
                    tvProgress.setText(progress + "/" + totalCount);
                    tvPercent.setText(String.format("%.0f%%", (float) progress / totalCount * 100f));
                } else {
                    Intent galleryIntent = new Intent(context, GalleryActivity.class);
                    startActivity(galleryIntent);
                    finish();
                }
            }
        }
    }
}
