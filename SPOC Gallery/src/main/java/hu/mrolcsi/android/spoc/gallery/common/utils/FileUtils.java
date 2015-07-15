package hu.mrolcsi.android.spoc.gallery.common.utils;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.15.
 * Time: 11:46
 */

public abstract class FileUtils {

    public static String getHumanReadableSize(long rawSize) {
        if (rawSize > 1000000000) return String.format("%.2f GB", (float) rawSize / 1000000000);
        else if (rawSize > 1000000) return String.format("%.2f MB", (float) rawSize / 1000000);
        else if (rawSize > 1000) return String.format("%.2f kB", (float) rawSize / 1000);
        else return String.format("%d B", rawSize);
    }

}
