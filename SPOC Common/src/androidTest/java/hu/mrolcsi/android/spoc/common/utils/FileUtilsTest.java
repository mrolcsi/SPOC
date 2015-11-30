package hu.mrolcsi.android.spoc.common.utils;

import android.os.Environment;
import android.test.AndroidTestCase;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.11.30.
 * Time: 10:54
 */

public class FileUtilsTest extends AndroidTestCase {

    public void testGetHumanReadableSize() throws Exception {
        //need permission

        // 126 byte
        String actualSize = FileUtils.getHumanReadableSize(126);
        assertEquals("126 B", actualSize);

        // 2.19 kB
        actualSize = FileUtils.getHumanReadableSize(2242);
        assertEquals("2,19 kB", actualSize);

        // 13.3 MB
        actualSize = FileUtils.getHumanReadableSize(13946060);
        assertEquals("13,30 MB", actualSize);
    }

    public void testDeleteFile() throws Exception {
        //create test file
        File testFile = new File(Environment.getExternalStorageDirectory(), "testFile.tmp");
        assertEquals(true, testFile.createNewFile());

        //delete file
        assertEquals(true, FileUtils.deleteFile(testFile.getAbsolutePath()));
    }
}