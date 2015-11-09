package hu.mrolcsi.android.spoc.common.helper;

import android.annotation.TargetApi;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import hu.mrolcsi.android.spoc.database.model.binder.Contact2Image;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015. 09. 15.
 * Time: 16:39
 */

public class FaceDetectorTask extends AsyncTask<Bitmap, Void, List<Contact2Image>> {

    private static final String TAG = "SPOC.FaceDetector";
    private final Context context;
    private final int imageId;
    private String filename;
    private boolean mInternalBitmap = false;

    public FaceDetectorTask(Context context, int imageId) {
        this.context = context;
        this.imageId = imageId;
    }

    public FaceDetectorTask(Context context, int imageId, String filename) {
        this.context = context;
        this.imageId = imageId;
        this.filename = filename;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    @Override
    protected List<Contact2Image> doInBackground(Bitmap... bitmaps) {

        Bitmap bitmap;
        if (bitmaps == null || bitmaps.length == 0) {
            if (filename != null) {
                bitmap = loadBitmap(filename);
            } else {
                throw new IllegalArgumentException("No ImageID nor FileName is specified.");
            }
        } else {
            bitmap = bitmaps[0];
        }

        //check if bitmap width is even
        if (bitmap.getWidth() % 2 == 1) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() + 1, bitmap.getHeight(), false);
        }

        //detect faces
        FaceDetector detector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), 5);
        FaceDetector.Face[] faces = new FaceDetector.Face[5];
        detector.findFaces(bitmap, faces);

        List<Contact2Image> contact2ImageList = new ArrayList<>();

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ContentValues values = new ContentValues();
        values.put(Contact2Image.COLUMN_IMAGE_ID, imageId);
        int fakeContact = -1;

        if (mInternalBitmap) {
            bitmap.recycle();
        }

        for (FaceDetector.Face face : faces) {
            //check for valid face
            if (face != null && face.confidence() > 0.5f) {

                if (isCancelled()) {
                    return null;
                }

                Contact2Image contact2Image = new Contact2Image();

                float x1, y1, x2, y2;

                PointF midPoint = new PointF();
                face.getMidPoint(midPoint);
                final float radius = face.eyesDistance() * 1.25f;

                x1 = midPoint.x - radius;
                y1 = midPoint.y - radius + (radius / 5f);
                x2 = midPoint.x + radius;
                y2 = midPoint.y + radius + (radius / 5f);

                contact2Image.setImageId(imageId);
                contact2Image.set(x1, y1, x2, y2);

                values.put(Contact2Image.COLUMN_CONTACT_ID, fakeContact--);
                values.put(Contact2Image.COLUMN_X1, x1);
                values.put(Contact2Image.COLUMN_X2, x2);
                values.put(Contact2Image.COLUMN_Y1, y1);
                values.put(Contact2Image.COLUMN_Y2, y2);
                values.put(Contact2Image.COLUMN_DATE, Calendar.getInstance().getTimeInMillis());

                ops.add(ContentProviderOperation.newInsert(SPOCContentProvider.CONTACTS_2_IMAGES_URI).withValues(values).build());

                contact2ImageList.add(contact2Image);
            }
        }

        try {
            context.getContentResolver().applyBatch(SPOCContentProvider.AUTHORITY, ops);
        } catch (RemoteException | OperationApplicationException e) {
            Log.w(getClass().getSimpleName(), e);
        }

        return contact2ImageList;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private Bitmap loadBitmap(String filename) {
        mInternalBitmap = true;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int desiredWidth;
        int desiredHeight;
        if (Build.VERSION.SDK_INT >= 13) {
            android.graphics.Point size = new android.graphics.Point();
            display.getSize(size);
            desiredWidth = size.x;
            desiredHeight = size.y;
        } else {
            //noinspection deprecation
            desiredWidth = display.getWidth();
            //noinspection deprecation
            desiredHeight = display.getHeight();
        }

        return decodeSampledBitmapFromFile(filename, desiredWidth, desiredHeight);
    }
}
