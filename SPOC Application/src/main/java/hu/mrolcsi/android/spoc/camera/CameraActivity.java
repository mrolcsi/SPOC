package hu.mrolcsi.android.spoc.camera;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.utils.DialogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class CameraActivity extends AppCompatActivity {

    public static final String TAG = "SPOC.Camera";
    public static final int PREVIEW_TIME = 1000;

    private static int STROKE_WIDTH;
    private ImageView imgCrosshair;

    private Camera mCamera;
    private CameraPreview mPreview;
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

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            ((GradientDrawable) imgCrosshair.getDrawable().mutate()).setStroke(STROKE_WIDTH, Color.RED);
        }
    };
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            path = new File(path, "SPOC");
            path.mkdirs();

            File pictureFile = new File(path, Calendar.getInstance().getTimeInMillis() + ".jpg");

            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions.");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(bytes);
                fos.close();

                Toast.makeText(CameraActivity.this, "Picture taken.", Toast.LENGTH_SHORT).show();

                mHandler.postDelayed(mResumePreview, PREVIEW_TIME);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };
    private Camera.AutoFocusCallback mFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean b, Camera camera) {
            isFocused = b;
            if (b) {
                ((GradientDrawable) imgCrosshair.getDrawable().mutate()).setStroke(STROKE_WIDTH, Color.GREEN);
            } else {
                ((GradientDrawable) imgCrosshair.getDrawable().mutate()).setStroke(STROKE_WIDTH, Color.WHITE);
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // check if device has a camera
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            initCamera();
        } else {
            // no camera on this device
            // show dialog and exit
            DialogUtils.buildErrorDialog(this)
                    .setMessage("This device does not have a camera.")
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
                    })
                    .show();
        }

        initViews();
    }

    private void initViews() {
        final ImageButton btnCapture = (ImageButton) findViewById(R.id.btnCapture);
        btnCapture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.autoFocus(mFocusCallback);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.takePicture(mShutterCallback, null, mPictureCallback);
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
    }

    private void initCamera() {
        // Create an instance of Camera
        try {
            mCamera = Camera.open();
            mPreview = new CameraPreview(CameraActivity.this, mCamera);
            FrameLayout flCameraContainer = (FrameLayout) findViewById(R.id.cameraContainer);
            flCameraContainer.addView(mPreview);
        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), e);
            DialogUtils.buildErrorDialog(this).setMessage(e.toString()).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            if (!isFocusPressed) {
                isFocusPressed = true;
                mCamera.autoFocus(mFocusCallback);
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            if (isFocused) {
                mCamera.takePicture(mShutterCallback, null, mPictureCallback);
                isFocused = false;
                isFocusPressed = false;
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        super.onStop();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            mCamera.reconnect();
        } catch (IOException e) {
            Log.w(getClass().getSimpleName(), e);
        }

        //TODO: screen brightness lock
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
    protected void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.unlock();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCamera != null) {
            mCamera.release();
        }
    }
}
