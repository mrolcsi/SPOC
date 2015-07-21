package hu.mrolcsi.android.spoc.gallery.common.utils;

import android.content.Context;

import java.io.*;

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

    public static String readRawResource(Context context, int resourceId) {
        InputStream inputStream = context.getResources().openRawResource(resourceId);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException ignored) {
        }

        return byteArrayOutputStream.toString();
    }

    public static boolean deleteFile(String mImagePath) throws IOException {
        File file = new File(mImagePath);

        if (!file.exists()) throw new FileNotFoundException();
        if (!file.canRead() || !file.canWrite())
            throw new IOException("File cannot be read or written. No permission to write on disk?");

        return file.delete();
    }
}
