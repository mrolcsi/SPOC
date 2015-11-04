package hu.mrolcsi.android.spoc.common.utils;

import android.graphics.Point;
import android.location.Location;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.11.04.
 * Time: 9:25
 */

public abstract class CameraUtils {
    private static final int TWO_MINUTES = 1000 * 60 * 2;

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

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
