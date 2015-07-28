package hu.mrolcsi.android.spoc.gallery.imagedetails;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.13.
 * Time: 20:10
 */

public class ImageDetailsAdapter extends FragmentStatePagerAdapter {

    private int iData = -1;
    private Cursor cursor;
    private int mCount;

    public ImageDetailsAdapter(FragmentManager fm, Cursor cursor) {
        super(fm);
        this.cursor = cursor;
        iData = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
    }

    public ImageDetailsAdapter(FragmentManager childFragmentManager) {
        super(childFragmentManager);
    }

    @Override
    public Fragment getItem(int position) {

        if (cursor != null && !cursor.isClosed()) {
            cursor.moveToPosition(position);
            final String imagePath = cursor.getString(iData);

            return SingleImageFragment.newInstance(imagePath);
        }
        return null;
    }

    @Override
    public int getCount() {
        if (cursor != null && !cursor.isClosed()) {
            mCount = cursor.getCount();
        }
        return mCount;
    }
}
