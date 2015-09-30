package hu.mrolcsi.android.spoc.gallery.main.categories;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.mrolcsi.android.spoc.common.fragment.SPOCFragment;
import hu.mrolcsi.android.spoc.common.loader.ImagesTableLoader;
import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.gallery.R;
import org.lucasr.twowayview.TwoWayLayoutManager;
import org.lucasr.twowayview.widget.SpannableGridLayoutManager;
import org.lucasr.twowayview.widget.TwoWayView;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.21.
 * Time: 15:35
 */

public class CategoriesFragment extends SPOCFragment implements ImagesTableLoader.LoaderCallbacks {
    public static final int DATES_LOADER_ID = 20;
    public static final int PLACES_LOADER_ID = 21;
    public static final int PEOPLE_LOADER_ID = 22;
    public static final int LABELS_LOADER_ID = 23;
    public static final int FOLDERS_LOADER_ID = 24;

    private TwoWayView list;
    private SectionedThumbnailsAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_categories, container, false);

            list = (TwoWayView) mRootView.findViewById(R.id.recycler_view);
        }
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final SpannableGridLayoutManager lm = new SpannableGridLayoutManager(TwoWayLayoutManager.Orientation.VERTICAL, 3, 3);
        list.setLayoutManager(lm);

        mAdapter = new SectionedThumbnailsAdapter(getActivity(), null);
        list.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = new Bundle();
        args.putStringArray(ImagesTableLoader.ARG_PROJECTION,
                new String[]{"DISTINCT _id", Image.COLUMN_FILENAME, "ifnull(" + Image.COLUMN_LOCATION + ", '" + Character.toString((char) 126) + "')"});
        args.putString(ImagesTableLoader.ARG_SORT_ORDER, Image.COLUMN_LOCATION + ", " + Image.COLUMN_DATE_TAKEN + " DESC");
        getLoaderManager().initLoader(0, args, new ImagesTableLoader(getActivity(), this));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == 0) {
            mAdapter.changeCursor(null);
        }
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == 0) {
            mAdapter.changeCursor(data);
        }
    }
}
