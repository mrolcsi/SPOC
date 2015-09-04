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
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import hu.mrolcsi.android.spoc.common.fragment.SPOCFragment;
import hu.mrolcsi.android.spoc.common.helper.LocationFinderTask;
import hu.mrolcsi.android.spoc.common.loader.database.ImageTableLoader;
import hu.mrolcsi.android.spoc.database.models.Image;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.BuildConfig;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.main.GalleryActivity;
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
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.13.
 * Time: 19:52
 */

public class ImagePagerFragment extends SPOCFragment implements CursorLoader.OnLoadCompleteListener<Cursor> {

    public static final String ARG_SELECTED_POSITION = "SPOC.Gallery.Pager.SelectedPosition";
    public static final String ARG_LOADER_ID = "SPOC.Gallery.Pager.LoaderId";

    private ViewPager vpDetailsPager;
    private RelativeLayout rlInfo;
    private TextView tvLocation;
    private TextView tvDateTaken;

    private ImagePagerAdapter mAdapter;
    private Loader<Cursor> mLoader;
    private int mCurrentPageIndex = -1;
    private LocationFinderTask mLocationFinderTask;

    @Override
    @TargetApi(18)
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (Build.VERSION.SDK_INT >= 18) {
            WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
            params.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_ROTATE;
            getActivity().getWindow().setAttributes(params);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_imagepager, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        vpDetailsPager = (ViewPager) view.findViewById(R.id.vpDetailsPager);
        rlInfo = (RelativeLayout) view.findViewById(R.id.rlInfo);

        addOnFullscreenChangeListener(new OnFullscreenChangeListener() {
            @Override
            public void onFullScreenChanged(boolean isFullscreen) {
                if (isFullscreen) {
                    //hide
                    ViewCompat.animate(rlInfo).translationY(rlInfo.getHeight()).setInterpolator(new AccelerateInterpolator(2)).start();
                } else {
                    //show
                    ViewCompat.animate(rlInfo).translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                    updateInfo();
                }
            }
        });

        //add delay to make sure views are inflated and on screen
        //and for visual clarity
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleFullScreen();
            }
        }, 100);

        tvLocation = (TextView) view.findViewById(R.id.tvLocation);
        tvDateTaken = (TextView) view.findViewById(R.id.tvDateTaken);

        if (BuildConfig.DEBUG) {
            ImageButton btnRefresh = (ImageButton) view.findViewById(R.id.btnRefresh);
            btnRefresh.setVisibility(View.VISIBLE);
            btnRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateInfo();
                }
            });
        }

        vpDetailsPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mLocationFinderTask != null && !mLocationFinderTask.isCancelled())
                    mLocationFinderTask.cancel(true);
                updateInfo();
            }
        });

        if (savedInstanceState != null) mCurrentPageIndex = savedInstanceState.getInt(ARG_SELECTED_POSITION);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.details, menu);
    }

    @Override
    public void onStart() {
        super.onStart();

        int loaderId = ImageTableLoader.ID;
        Bundle loaderArgs = null;
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_LOADER_ID)) {
                loaderId = getArguments().getInt(ARG_LOADER_ID);
            }
            loaderArgs = getArguments().getBundle(ThumbnailsFragment.ARG_QUERY_BUNDLE);
            final String[] projection = new String[]{"_id", Image.COLUMN_FILENAME, Image.COLUMN_DATE_TAKEN};
            loaderArgs.putStringArray(ImageTableLoader.ARG_PROJECTION, projection);
        }
        mLoader = getLoaderManager().restartLoader(loaderId, loaderArgs, new ImageTableLoader(getActivity(), this));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isFullscreen()) toggleFullScreen();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(ARG_SELECTED_POSITION, vpDetailsPager.getCurrentItem());
    }

    @Override
    public boolean onBackPressed() {
        ((GalleryActivity) getActivity()).restoreFragmentFromStack();
        return true;
    }

    private void updateInfo() {
        if (mAdapter == null || !isAdded()) return;

        final SingleImageFragment item = (SingleImageFragment) mAdapter.getItem(vpDetailsPager.getCurrentItem());
        String imagePath = item.getImagePath();

        if (imagePath == null) return;

        try {
            final ExifInterface exif = new ExifInterface(imagePath);

            final SimpleDateFormat sdf = new SimpleDateFormat(getString(hu.mrolcsi.android.spoc.common.R.string.spoc_exifParser), Locale.US);

            final String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);
            if (dateString != null) {
                final Date date = sdf.parse(dateString);
                tvDateTaken.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date));
            } else {
                final long l = new File(imagePath).lastModified();
                final String s = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(l));
                tvDateTaken.setText(s);
            }

            final Cursor cursorWithImage = getActivity().getContentResolver().query(Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, String.valueOf(item.getImageId())), new String[]{Image.COLUMN_LOCATION}, null, null, null);
            if (cursorWithImage.moveToFirst()) {
                if (cursorWithImage.getString(0) != null) {
                    tvLocation.setText(cursorWithImage.getString(0));
                } else {
                    float latLong[] = new float[2];
                    if (exif.getLatLong(latLong)) {
                        mLocationFinderTask = new LocationFinderTask(getActivity().getApplicationContext()) {
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                tvLocation.setText(Html.fromHtml(getString(R.string.details_message_lookingUpLocation)));
                            }

                            @Override
                            protected void onPostExecute(List<Address> addresses) {
                                super.onPostExecute(addresses);

                                if (!isAdded() || isCancelled()) return;

                                if (addresses == null) {
                                    tvLocation.setText(Html.fromHtml(getString(R.string.details_message_unknownLocation)));
                                } else {
                                    final String locality = addresses.get(0).getLocality();
                                    final String countryName = addresses.get(0).getCountryName();
                                    final String locationText = locality + ", " + countryName;

                                    //update db
                                    ContentValues values = new ContentValues();
                                    values.put(Image.COLUMN_LOCATION, locationText);

                                    getActivity().getContentResolver().update(Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, String.valueOf(item.getImageId())), values, null, null);

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
        } catch (IOException | ParseException e) {
            Log.w(getClass().getName(), e);
        }
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        if (mAdapter == null) {
            try {
                mAdapter = new ImagePagerAdapter(getChildFragmentManager(), data);
                vpDetailsPager.setAdapter(mAdapter);
            } catch (NullPointerException e) {
                Log.w(getClass().getSimpleName(), e.toString() + ": Premature loading?");
            }
        } else {
            mAdapter.changeCursor(data);
        }

        if (mCurrentPageIndex < 0 && data != null && getArguments() != null && getArguments().containsKey(ARG_SELECTED_POSITION)) {
            mCurrentPageIndex = getArguments().getInt(ARG_SELECTED_POSITION);
        }
        vpDetailsPager.setCurrentItem(mCurrentPageIndex);
    }
}
