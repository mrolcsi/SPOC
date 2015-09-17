package hu.mrolcsi.android.spoc.gallery.imagedetails;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Address;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;
import hu.mrolcsi.android.spoc.common.helper.LocationFinderTask;
import hu.mrolcsi.android.spoc.common.loader.ImageTableLoader;
import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.utils.SystemUiHider;
import hu.mrolcsi.android.spoc.gallery.main.ThumbnailsFragment;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ImagePagerActivity extends AppCompatActivity implements ImageTableLoader.LoaderCallbacks {

    public static final String ARG_SELECTED_POSITION = "SPOC.Gallery.Pager.SelectedPosition";
    public static final String ARG_LOADER_ID = "SPOC.Gallery.Pager.LoaderId";
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;
    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    Handler mHideHandler = new Handler();
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };
    private ViewPager vpDetailsPager;
    private RelativeLayout rlInfo;
    private TextView tvLocation;
    private TextView tvDateTaken;

    private ImagePagerAdapter mAdapter;
    private int mCurrentPageIndex = -1;
    private LocationFinderTask mLocationFinderTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_imagepager);
        setupActionBar();

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
            // Cached values.
            int mControlsHeight;
            int mShortAnimTime;

            @Override
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
            public void onVisibilityChange(boolean visible) {

                if (mControlsHeight == 0) {
                    mControlsHeight = controlsView.getHeight();
                }
                if (mShortAnimTime == 0) {
                    mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
                }
                ViewCompat.animate(controlsView).translationY(visible ? 0 : mControlsHeight).setDuration(mShortAnimTime);

                if (getSupportActionBar() != null) {
                    if (visible) {
                        getSupportActionBar().show();
                    } else {
                        getSupportActionBar().hide();
                    }
                }

                //noinspection PointlessBooleanExpression
                if (visible && AUTO_HIDE) {
                    // Schedule a hide().
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
                }
            }
        });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        if (Build.VERSION.SDK_INT >= 18) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_ROTATE;
            getWindow().setAttributes(params);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        vpDetailsPager = (ViewPager) findViewById(R.id.fullscreen_content);
        rlInfo = (RelativeLayout) findViewById(R.id.fullscreen_content_controls);

        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvDateTaken = (TextView) findViewById(R.id.tvDateTaken);

        vpDetailsPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mLocationFinderTask != null && !mLocationFinderTask.isCancelled())
                    mLocationFinderTask.cancel(true);
                updateInfo();
            }
        });

        if (savedInstanceState != null) mCurrentPageIndex = savedInstanceState.getInt(ARG_SELECTED_POSITION);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        int loaderId = ImageTableLoader.ID;
        Bundle loaderArgs = null;

        if (getIntent() != null) {
            if (getIntent().hasExtra(ARG_LOADER_ID)) {
                loaderId = getIntent().getIntExtra(ARG_LOADER_ID, ImageTableLoader.ID);
            }
            loaderArgs = getIntent().getBundleExtra(ThumbnailsFragment.ARG_QUERY_BUNDLE);

            final String[] projection = new String[]{"_id", Image.COLUMN_FILENAME, Image.COLUMN_DATE_TAKEN, Image.COLUMN_LOCATION};
            loaderArgs.putStringArray(ImageTableLoader.ARG_PROJECTION, projection);
        }
        getSupportLoaderManager().restartLoader(loaderId, loaderArgs, new ImageTableLoader(this, this));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(ARG_SELECTED_POSITION, vpDetailsPager.getCurrentItem());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void updateInfo() {
        if (mAdapter == null) return;

        final Fragment item = mAdapter.getItem(vpDetailsPager.getCurrentItem());
        String imagePath = item.getArguments().getString(SingleImageFragment.ARG_IMAGE_PATH);

        if (imagePath == null) return;

        try {
            final ExifInterface exif = new ExifInterface(imagePath);

            final SimpleDateFormat dateParser = new SimpleDateFormat(getString(hu.mrolcsi.android.spoc.common.R.string.spoc_exifParser), Locale.US);

            final String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);
            if (dateString != null) {
                final Date date = dateParser.parse(dateString);
                tvDateTaken.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date));
            } else {
                final long l = new File(imagePath).lastModified();
                final String s = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(l));
                tvDateTaken.setText(s);
            }

            String imageLocation = item.getArguments().getString(SingleImageFragment.ARG_IMAGE_LOCATION);
            if (!TextUtils.isEmpty(imageLocation)) {
                tvLocation.setText(imageLocation);
            } else {
                final Cursor cursorWithImage = getContentResolver().query(Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, String.valueOf(item.getArguments().getLong(SingleImageFragment.ARG_IMAGE_ID))), new String[]{Image.COLUMN_LOCATION}, null, null, null);
                if (cursorWithImage.moveToFirst()) {
                    if (cursorWithImage.getString(0) != null) {
                        tvLocation.setText(cursorWithImage.getString(0));
                    } else {
                        float latLong[] = new float[2];
                        if (exif.getLatLong(latLong)) {
                            mLocationFinderTask = new LocationFinderTask(getApplicationContext()) {
                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    tvLocation.setText(Html.fromHtml(getString(R.string.details_message_lookingUpLocation)));
                                }

                                @Override
                                protected void onPostExecute(List<Address> addresses) {
                                    super.onPostExecute(addresses);

                                    if (isCancelled()) return;

                                    if (addresses == null) {
                                        tvLocation.setText(Html.fromHtml(getString(R.string.details_message_unknownLocation)));
                                    } else {
                                        final String locality = addresses.get(0).getLocality();
                                        final String countryName = addresses.get(0).getCountryName();
                                        final String locationText = locality + ", " + countryName;

                                        //update db
                                        ContentValues values = new ContentValues();
                                        values.put(Image.COLUMN_LOCATION, locationText);

                                        getContentResolver().update(Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, String.valueOf(item.getArguments().getLong(SingleImageFragment.ARG_IMAGE_ID))), values, null, null);

                                        tvLocation.setText(locationText);
                                    }
                                }
                            };
                            mLocationFinderTask.execute(latLong[0], latLong[1]);
                        } else {
                            tvLocation.setText(getString(R.string.not_available));
                        }
                    }
                }
                cursorWithImage.close();
            }
        } catch (IOException | ParseException e) {
            Log.w(getClass().getName(), e);
        }
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        if (mAdapter == null) {
            try {
                mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), data);
                vpDetailsPager.setAdapter(mAdapter);
            } catch (NullPointerException e) {
                Log.w(getClass().getSimpleName(), e.toString() + ": Premature loading?");
            }
        } else {
            mAdapter.changeCursor(data);
        }

        if (mCurrentPageIndex < 0 && data != null && getIntent() != null && getIntent().hasExtra(ARG_SELECTED_POSITION)) {
            mCurrentPageIndex = getIntent().getIntExtra(ARG_SELECTED_POSITION, 0);
        }
        vpDetailsPager.setCurrentItem(mCurrentPageIndex);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
        }
    }
}
