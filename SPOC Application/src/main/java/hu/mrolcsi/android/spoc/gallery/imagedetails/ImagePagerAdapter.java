package hu.mrolcsi.android.spoc.gallery.imagedetails;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import hu.mrolcsi.android.spoc.database.model.Image;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.13.
 * Time: 20:10
 */

public class ImagePagerAdapter extends FragmentStatePagerAdapter {

    private int iID;
    private int iFilename;
    private int iLocation;
    private int mCount;
    private Cursor mCursor;
    private boolean mDataValid;
    private DataSetObserver mDataSetObserver;

    //private SparseArray<Fragment> mFragmentCache = new SparseArray<>();

    public ImagePagerAdapter(FragmentManager fm, Cursor cursor) {
        super(fm);
        mCursor = cursor;
        mDataValid = cursor != null;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
            iID = mCursor.getColumnIndex("_id");
            iFilename = mCursor.getColumnIndex(Image.COLUMN_FILENAME);
            iLocation = mCursor.getColumnIndex(Image.COLUMN_LOCATION);
        }
    }

    @Override
    public Fragment getItem(int position) {
//        if (mFragmentCache.get(position) != null) {
//            return mFragmentCache.get(position);
//        } else {
        if (mCursor != null && mDataValid) {
            mCursor.moveToPosition(position);
            final String imagePath = mCursor.getString(iFilename);
            final int imageId = mCursor.getInt(iID);
            final String location = mCursor.getString(iLocation);

            final SingleImageFragment fragment = SingleImageFragment.newInstance(imageId, imagePath, location);
            //mFragmentCache.put(position, fragment);
            return fragment;
        }
//        }
        return null;
    }

    @Override
    public int getCount() {
        if (mCursor != null && !mCursor.isClosed()) {
            mCount = mCursor.getCount();
        }
        return mCount;
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     */
    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     */
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
        return oldCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
}
