package hu.mrolcsi.android.spoc.gallery.home;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import hu.mrolcsi.android.spoc.common.SPOCFragment;
import hu.mrolcsi.android.spoc.common.loader.MediaStoreLoader;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.ImagePagerFragment;
import hu.mrolcsi.android.spoc.gallery.navigation.NavigationActivity;
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

    private View mRootView;
    private TwoWayView twList;
    private HomeScreenAdapter mAdapter;
    private Loader<Cursor> mLoader;
    private int mSavedPosition = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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

                mSavedPosition = i;

                fragment.setArguments(args);

                ((NavigationActivity) getActivity()).swapFragment(fragment);
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
    public void onDestroy() {
        super.onDestroy();

        getLoaderManager().destroyLoader(MediaStoreLoader.ID);
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        Log.v(getClass().getSimpleName(), "onLoadComplete");

        mAdapter = new HomeScreenAdapter(getActivity(), data);
        twList.setAdapter(mAdapter);

//        if (mSavedPosition > 0) {
//            twList.scrollToPosition(mSavedPosition);
//        }
//        mSavedPosition = -1;
    }
}
