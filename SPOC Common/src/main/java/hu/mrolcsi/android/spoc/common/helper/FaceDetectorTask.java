package hu.mrolcsi.android.spoc.common.helper;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;
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

    private final Context context;
    private final int imageId;

    public FaceDetectorTask(Context context, int imageId) {
        this.context = context;
        this.imageId = imageId;
    }

    @Override
    protected List<Contact2Image> doInBackground(Bitmap... bitmaps) {

        //detect faces
        FaceDetector detector = new FaceDetector(bitmaps[0].getWidth(), bitmaps[0].getHeight(), 5);
        FaceDetector.Face[] faces = new FaceDetector.Face[5];
        detector.findFaces(bitmaps[0], faces);

        List<Contact2Image> contact2ImageList = new ArrayList<>();

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ContentValues values = new ContentValues();
        values.put(Contact2Image.COLUMN_IMAGE_ID, imageId);
        int fakeContact = -1;

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
}
