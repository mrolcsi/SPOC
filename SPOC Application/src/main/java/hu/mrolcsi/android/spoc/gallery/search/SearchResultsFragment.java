package hu.mrolcsi.android.spoc.gallery.search;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.TextView;
import hu.mrolcsi.android.spoc.database.models.Image;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.main.GalleryActivity;
import hu.mrolcsi.android.spoc.gallery.main.ThumbnailsFragment;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.12.
 * Time: 9:37
 */

public class SearchResultsFragment extends ThumbnailsFragment {

    private TextView tvMessage;
    private String mQuery;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_search_result, container, false);

            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mRootView.getLayoutParams();
            lp.topMargin = getResources().getDimensionPixelOffset(R.dimen.abc_action_bar_default_height_material);
            mRootView.setLayoutParams(lp);

            fabSearch.setVisibility(View.GONE);

            tvMessage = (TextView) mRootView.findViewById(R.id.tvMessage);
        }

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mLoader.reset();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        final MenuItem searchItem = menu.findItem(R.id.menuSearch);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setQueryHint(getString(R.string.search_hint));
        MenuItemCompat.expandActionView(searchItem);

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                onBackPressed();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //do query

                mLoader.reset();
                mAdapter = null;
                twList.setAdapter(null);
                performSearch(newText);

                return true;
            }
        });

        searchView.setQuery(mQuery, true);
    }

    @Override
    public boolean onBackPressed() {
        ((GalleryActivity) getActivity()).restoreFragmentFromStack();
        return true;
    }

    private void performSearch(String searchText) {
        if (TextUtils.isEmpty(searchText)) {
            tvMessage.setText(R.string.error_noResults);
            return;
        }

        mQuery = searchText;

        String[] projection = new String[]{Image.COLUMN_FILENAME, Image.COLUMN_DATE_TAKEN};
        String selection = "lower(" + Image.COLUMN_FILENAME + ") LIKE lower(?)";
        String[] selectionArgs = new String[]{"%" + searchText + "%"};

        mLoader.setProjection(projection);
        mLoader.setSelection(selection);
        mLoader.setSelectionArgs(selectionArgs);

        mLoader.startLoading();
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        super.onLoadComplete(loader, data);

        mAdapter.setUseColumnSpan(false);
        final String quantityString = getResources().getQuantityString(R.plurals.message_numberOfResults, data.getCount(), data.getCount());
        tvMessage.setText(quantityString);
    }
}
