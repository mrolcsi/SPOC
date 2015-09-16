package hu.mrolcsi.android.spoc.common.helper;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015. 09. 15.
 * Time: 16:39
 */

public class FaceDetectorTask extends AsyncTask<Bitmap, Void, List<RectF>> {

    public FaceDetectorTask() {
    }

    @Override
    protected List<RectF> doInBackground(Bitmap... bitmaps) {

        //detect faces
        FaceDetector detector = new FaceDetector(bitmaps[0].getWidth(), bitmaps[0].getHeight(), 5);
        FaceDetector.Face[] faces = new FaceDetector.Face[5];
        detector.findFaces(bitmaps[0], faces);

        List<RectF> rectangles = new ArrayList<>();

        for (FaceDetector.Face face : faces) {
            //check for valid face
            if (face != null && face.confidence() > 0.5f) {

                if (isCancelled()) {
                    detector = null;
                    faces = null;
                    return null;
                }

                float x1, y1, x2, y2;

                PointF midPoint = new PointF();
                face.getMidPoint(midPoint);
                final float radius = face.eyesDistance() * 1.25f;

                x1 = midPoint.x - radius;
                y1 = midPoint.y - radius + (radius / 5f);
                x2 = midPoint.x + radius;
                y2 = midPoint.y + radius + (radius / 5f);

                rectangles.add(new RectF(x1, y1, x2, y2));
            }
        }

        return rectangles;
    }
}
