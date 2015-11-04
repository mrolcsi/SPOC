package hu.mrolcsi.android.spoc.common.utils;

import android.graphics.Point;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.11.04.
 * Time: 9:25
 */

public abstract class CameraUtils {
    public static Point getAspectRatio(double ratio) {
        double bestDelta = Double.MAX_VALUE;
        int i = 1;
        int j = 1;
        int bestI = 0;
        int bestJ = 0;

        for (int iterations = 0; iterations < 100; iterations++) {
            double delta = (double) i / (double) j - ratio;

            // Optionally, quit here if delta is "close enough" to zero
            if (delta < 0) i++;
            else j++;

            double newDelta = Math.abs((double) i / (double) j - ratio);
            if (newDelta < bestDelta) {
                bestDelta = newDelta;
                bestI = i;
                bestJ = j;
            }
        }
        return new Point(bestI, bestJ);
    }
}
