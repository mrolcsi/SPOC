package hu.mrolcsi.android.spoc.gallery.imagedetails;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import hu.mrolcsi.android.spoc.database.models.Image;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.13.
 * Time: 20:10
 */

public class ImagePagerAdapter extends FragmentStatePagerAdapter {

    private int iID = -1;
    private int iData = -1;
    private Cursor cursor;
    private int mCount;

    private SparseArray<Fragment> mFragmentCache = new SparseArray<>();

    public ImagePagerAdapter(FragmentManager fm, Cursor cursor) {
        super(fm);
        if (cursor != null) {
            this.cursor = cursor;
            iData = cursor.getColumnIndex(Image.COLUMN_FILENAME);
            iID = cursor.getColumnIndex("_id");
        }
    }

    @Override
    public Fragment getItem(int position) {

        if (mFragmentCache.get(position) != null) return mFragmentCache.get(position);
        else if (cursor != null && !cursor.isClosed()) {
            cursor.moveToPosition(position);
            final String imagePath = cursor.getString(iData);
            final long imageId = cursor.getLong(iID);

            final SingleImageFragment fragment = SingleImageFragment.newInstance(imageId, imagePath);
            mFragmentCache.put(position, fragment);
            return fragment;
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
