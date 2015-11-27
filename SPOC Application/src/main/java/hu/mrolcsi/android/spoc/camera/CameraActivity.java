package hu.mrolcsi.android.spoc.camera;

import android.annotation.TargetApi;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import hu.mrolcsi.android.spoc.common.helper.FaceDetectorTask;
import hu.mrolcsi.android.spoc.common.service.DatabaseBuilderService;
import hu.mrolcsi.android.spoc.common.utils.CameraUtils;
import hu.mrolcsi.android.spoc.common.utils.LocationUtils;
import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.database.model.LabelType;
import hu.mrolcsi.android.spoc.database.model.binder.Contact2Image;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.BuildConfig;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.utils.DialogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    public static final String TAG = "SPOC.Camera";
    public static final int PREVIEW_TIME = 1000;
    public static final int LOCATION_UPDATE_INTERVAL = 1000; //millisec
    public static final float LOCATION_UPDATE_DISTANCE = 1;  //meter

    private static int STROKE_WIDTH;
    private FrameLayout flPreviewContainer;
    private ImageView imgCrosshair;
    private ImageButton btnCapture;
    private LinearLayout llIndicators;
    private ImageView imgGPSIndicator;
    private ImageView imgSaveIndicator;
    private Animation mBlinkAnim;

    private int mCameraId = 0;
    private Camera mCamera;
    private CameraCallbacks mCameraCallbacks;
    private Runnable mResumePreview = new Runnable() {
        @Override
        public void run() {
            mCamera.startPreview();
            ((GradientDrawable) imgCrosshair.getDrawable().mutate()).setStroke(STROKE_WIDTH, Color.WHITE);
        }
    };
    private Handler mHandler = new Handler();

    private boolean isFocused = false;
    private boolean isFocusPressed = false;
    private boolean isLocationFound = false;

    private LocationManager mLocationService;
    private Location mCurrentLocation;
    private LocationCallbacks mLocationListener = new LocationCallbacks();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCameraCallbacks = new CameraCallbacks(this);

        //get GPS service
        mLocationService = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        initViews();
    }

    private void initViews() {
        btnCapture = (ImageButton) findViewById(R.id.btnCapture);
        btnCapture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.autoFocus(mCameraCallbacks);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.takePicture(mCameraCallbacks, null, mCameraCallbacks);
                }
                return false;
            }
        });

        final ImageButton btnSettings = (ImageButton) findViewById(R.id.btnSettings);
        registerForContextMenu(btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openContextMenu(btnSettings);
            }
        });

        imgCrosshair = (ImageView) findViewById(R.id.imgCrosshair);
        STROKE_WIDTH = getResources().getDimensionPixelSize(R.dimen.margin_xsmall);

        llIndicators = (LinearLayout) findViewById(R.id.llIndicators);
        imgGPSIndicator = (ImageView) llIndicators.findViewById(R.id.imgGPSIndicator);
        imgSaveIndicator = (ImageView) llIndicators.findViewById(R.id.imgSaveIndicator);
        mBlinkAnim = AnimationUtils.loadAnimation(this, R.anim.blink);
    }

    private void initCamera() {
        // Create an instance of Camera

        final AlertDialog.Builder error = DialogUtils.buildErrorDialog(this)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        finish();
                    }
                });

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            try {
                mCamera = Camera.open(mCameraId);
                CameraUtils.setCameraDisplayOrientation(this, mCameraId, mCamera);
                CameraPreview mPreview = new CameraPreview(CameraActivity.this, mCamera);
                flPreviewContainer = (FrameLayout) findViewById(R.id.cameraContainer);
                flPreviewContainer.addView(mPreview);
            } catch (Exception e) {
                Log.w(getClass().getSimpleName(), e);
                error.setMessage(e.getMessage()).show();
            }
        } else {
            // no camera on this device
            // show dialog and exit
            error.setMessage("This device does not have a camera.").show();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            isFocused = false;
            isFocusPressed = false;
            mCamera.cancelAutoFocus();
            ((GradientDrawable) imgCrosshair.getDrawable().mutate()).setStroke(STROKE_WIDTH, Color.WHITE);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        initCamera();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCurrentLocation = mLocationService.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mLocationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISTANCE, mLocationListener);
        mLocationService.addGpsStatusListener(mLocationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCameraCallbacks.enable();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 11) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            if (!isFocusPressed) {
                isFocusPressed = true;
                mCamera.autoFocus(mCameraCallbacks);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            if (isFocused) {
                mCamera.takePicture(mCameraCallbacks, null, mCameraCallbacks);
                isFocused = false;
                isFocusPressed = false;
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (Build.VERSION.SDK_INT < 14) {
            mCamera.stopPreview();
        }
        CameraUtils.setCameraDisplayOrientation(this, mCameraId, mCamera);
        if (Build.VERSION.SDK_INT < 14) {
            mCamera.startPreview();
        }

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) btnCapture.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            btnCapture.setLayoutParams(layoutParams);
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) btnCapture.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
            btnCapture.setLayoutParams(layoutParams);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        mCameraCallbacks.disable();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mCamera != null) {
            mCamera.release();
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLocationService.removeUpdates(mLocationListener);
        mLocationService.removeGpsStatusListener(mLocationListener);
    }

    private void writeExif(String filename) {
        try {
            ExifInterface exif = new ExifInterface(filename);

            //save position
            if (mCurrentLocation != null) {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, LocationUtils.convert(mCurrentLocation.getLatitude()));
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, LocationUtils.latitudeRef(mCurrentLocation.getLatitude()));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, LocationUtils.convert(mCurrentLocation.getLongitude()));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, LocationUtils.longitudeRef(mCurrentLocation.getLongitude()));
            }

            //save rotation
            final int rotation = getWindowManager().getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:        //portrait
                    exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
                    break;
                case Surface.ROTATION_90:       //landscape
                    exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_NORMAL));
                    break;
                case Surface.ROTATION_180:      //reverse portrait
                    exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
                    break;
                case Surface.ROTATION_270:      //reverse landscape
                    exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
                    break;
            }

            exif.saveAttributes();

            Log.v(TAG, "Exif data saved.");
        } catch (IOException e) {
            Log.w(TAG, e);
        }
    }

    private void saveToDatabase(String filename) {
        Log.v(TAG, "Insert '" + filename + "' into database...");
        //assume it's called from a separate thread

        File file = new File(filename);

        //  insert into image table: filename, date taken, location
        ContentValues values = new ContentValues();

        //      filename
        values.put(Image.COLUMN_FILENAME, filename);

        Date date = Calendar.getInstance().getTime();
        final SimpleDateFormat sdf = new SimpleDateFormat(getString(hu.mrolcsi.android.spoc.common.R.string.spoc_exifParser), Locale.getDefault());

        //      date taken
        try {
            ExifInterface exif = new ExifInterface(filename);

            String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);
            if (!TextUtils.isEmpty(dateString)) {
                date = sdf.parse(dateString);
            } else {
                date = new Date(new File(filename).lastModified());
            }
            values.put(Image.COLUMN_DATE_TAKEN, date.getTime());
        } catch (IOException | ParseException e) {
            Log.w(getClass().getSimpleName(), e);
        }

        //      location
        final String location = DatabaseBuilderService.buildLocationString(this, filename);
        if (location != null) {
            values.put(Image.COLUMN_LOCATION, location);
        }

        final Uri insertedUri = getContentResolver().insert(SPOCContentProvider.IMAGES_URI, values);
        long imageId = Long.parseLong(insertedUri.getLastPathSegment());

        //  generate labels from filename, date, location
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        //      filename
        ops.add(DatabaseBuilderService.createLabel(this, imageId, file.getParentFile().getName(), LabelType.FOLDER));

        //      date
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        final String monthText = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        ops.add(DatabaseBuilderService.createLabel(this, imageId, monthText, LabelType.DATE_MONTH));

        final String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        ops.add(DatabaseBuilderService.createLabel(this, imageId, dayOfWeek, LabelType.DATE_DAY));

        //      location
        if (location != null) {
            final String[] locationStrings = location.split(", ");

            ops.add(DatabaseBuilderService.createLabel(this, imageId, locationStrings[0], LabelType.LOCATION_LOCALITY));
            ops.add(DatabaseBuilderService.createLabel(this, imageId, locationStrings[1], LabelType.LOCATION_COUNTRY));
        }

        try {
            getContentResolver().applyBatch(SPOCContentProvider.AUTHORITY, ops);
            Log.v(TAG, "Insert successful.");
        } catch (RemoteException | OperationApplicationException e) {
            Log.w(getClass().getSimpleName(), e);
        }

        // detect faces
        new FaceDetectorTask(this, (int) imageId, filename) {
            @Override
            protected void onPostExecute(List<Contact2Image> contact2ImageList) {
                imgSaveIndicator.setVisibility(View.GONE);
                imgSaveIndicator.clearAnimation();
            }
        }.execute();
    }

    private class CameraCallbacks extends OrientationEventListener implements Camera.ShutterCallback, Camera.AutoFocusCallback, Camera.PictureCallback {

        public CameraCallbacks(Context context) {
            super(context, SensorManager.SENSOR_DELAY_UI);
        }

        @Override
        public void onAutoFocus(boolean b, Camera camera) {
            isFocused = b;
            if (b) {
                ((GradientDrawable) imgCrosshair.getDrawable().mutate()).setStroke(STROKE_WIDTH, Color.GREEN);
            } else {
                ((GradientDrawable) imgCrosshair.getDrawable().mutate()).setStroke(STROKE_WIDTH, Color.WHITE);
            }
        }

        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            imgSaveIndicator.setVisibility(View.VISIBLE);
            imgSaveIndicator.startAnimation(mBlinkAnim);

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            path = new File(path, "SPOC");
            path.mkdirs();

            final File pictureFile = new File(path, Calendar.getInstance().getTimeInMillis() + ".jpg");

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(bytes);
                fos.close();

                Log.v(TAG, "onPictureTaken | File saved.");

                mHandler.postDelayed(mResumePreview, PREVIEW_TIME);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        writeExif(pictureFile.getAbsolutePath());
                        saveToDatabase(pictureFile.getAbsolutePath());
                    }
                }).start();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }

        @Override
        public void onShutter() {
            ((GradientDrawable) imgCrosshair.getDrawable().mutate()).setStroke(STROKE_WIDTH, Color.RED);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            //rotate camera
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return;
            Camera.CameraInfo info =
                    new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            orientation = (orientation + 45) / 90 * 90;
            int rotation;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else {  // back-facing camera
                rotation = (info.orientation + orientation) % 360;
            }

            final Camera.Parameters parameters = mCamera.getParameters();
            parameters.setRotation(rotation);
            mCamera.setParameters(parameters);

            //rotate UI
            btnCapture.setRotation(-rotation);
            for (int i = 0; i < llIndicators.getChildCount(); i++) {
                llIndicators.getChildAt(i).setRotation(-rotation);
            }

        }
    }

    private class LocationCallbacks implements LocationListener, GpsStatus.Listener {
        @Override
        public void onGpsStatusChanged(int status) {
            if (status == 1) {
                //looking for satellites
                imgGPSIndicator.setImageResource(R.drawable.gps_searching);
            } else if (status == 4 && !isLocationFound) {
                // receiving data, no fix yet
                imgGPSIndicator.setImageResource(R.drawable.gps_searching);
            } else if (status == 3) {
                // location updated
                imgGPSIndicator.setImageResource(R.drawable.gps_receiving);
            } else if (status == 2) {
                imgGPSIndicator.setImageResource(R.drawable.gps_disconnected);
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "GPS | onLocationChanged: " + location.toString());
            }

            isLocationFound = true;

            if (CameraUtils.isBetterLocation(location, mCurrentLocation)) {
                mCurrentLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "GPS | onStatusChanged: " + status);
            }

            if (status == LocationProvider.OUT_OF_SERVICE) {
                imgGPSIndicator.setImageResource(R.drawable.gps_disconnected);
            } else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                imgGPSIndicator.setImageResource(R.drawable.gps_searching);
            } else if (status == LocationProvider.AVAILABLE) {
                imgGPSIndicator.setImageResource(R.drawable.gps_receiving);
            }
        }

        @Override
        public void onProviderEnabled(String s) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "GPS | onProviderEnabled: " + s);
            }

            //imgGPSIndicator.setImageResource(R.drawable.gps_receiving);
        }

        @Override
        public void onProviderDisabled(String s) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "GPS | onProviderDisabled: " + s);
            }

            //imgGPSIndicator.setImageResource(R.drawable.gps_disconnected);
        }
    }
}
