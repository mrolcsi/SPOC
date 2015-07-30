package hu.mrolcsi.android.spoc.gallery.home;

import android.app.AlertDialog;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import hu.mrolcsi.android.spoc.common.fragment.SPOCFragment;
import hu.mrolcsi.android.spoc.common.loader.MediaStoreLoader;
import hu.mrolcsi.android.spoc.gallery.GalleryActivity;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.imagedetails.ImagePagerFragment;
import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.SpannableGridLayoutManager;
import org.lucasr.twowayview.widget.TwoWayView;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:12
 */

public class HomeFragment extends SPOCFragment implements CursorLoader.OnLoadCompleteListener<Cursor> {

    private static final String SAVED_ORIENTATION = "SPOC.Gallery.Home.SavedOrientation";
    private TwoWayView twList;
    private HomeScreenAdapter mAdapter;
    private Loader<Cursor> mLoader;

    private Parcelable mListInstanceState;
    private int mSavedOrientation = Configuration.ORIENTATION_UNDEFINED;

    @Override
    public int getNavigationItemId() {
        return R.id.navigation_home;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (savedInstanceState != null)
            mSavedOrientation = savedInstanceState.getInt(SAVED_ORIENTATION);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_home, container, false);

        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        twList = (TwoWayView) view.findViewById(R.id.list);
        twList.setHasFixedSize(true);

        ((SpannableGridLayoutManager) twList.getLayoutManager()).setNumColumns(getResources().getInteger(R.integer.preferredColumns));

        ItemClickSupport itemClick = ItemClickSupport.addTo(twList);

        itemClick.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int i, long l) {
                ImagePagerFragment fragment = new ImagePagerFragment();

                Bundle args = new Bundle();
                args.putInt(ImagePagerFragment.ARG_LOADER_ID, mLoader.getId());
                args.putInt(ImagePagerFragment.ARG_SELECTED_POSITION, i);

                mListInstanceState = twList.getLayoutManager().onSaveInstanceState();
                mSavedOrientation = getResources().getConfiguration().orientation;
                fragment.setArguments(args);

                ((GalleryActivity) getActivity()).swapFragment(fragment);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getArguments() != null) {
            //TODO: process args
        }

        mLoader = getLoaderManager().initLoader(MediaStoreLoader.ID, null, new MediaStoreLoader(getActivity(), this));
    }

    @Override
    public void onResume() {
        super.onResume();

        mLoader.startLoading();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_ORIENTATION, mSavedOrientation);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getLoaderManager().destroyLoader(MediaStoreLoader.ID);
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, final Cursor data) {
        Log.v(getClass().getSimpleName(), "onLoadComplete");

        if (data == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("No images found.")
                    .setNeutralButton(android.R.string.ok, null)
                    .show();
            return;
        }

        mAdapter = new HomeScreenAdapter(getActivity(), data);
        twList.setAdapter(mAdapter);
        
        if (mListInstanceState != null
                && mSavedOrientation == getResources().getConfiguration().orientation) { //different orientation -> different layout params
            twList.getLayoutManager().onRestoreInstanceState(mListInstanceState);
        }
    }
}
