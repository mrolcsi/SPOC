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

    public static final String ARG_SEARCH_QUERY = "SPOC.Gallery.Search.QUERY";

    private TextView tvMessage;
    private SearchView mSearchView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_search_result, container, false);

        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        lp.topMargin = getResources().getDimensionPixelOffset(R.dimen.abc_action_bar_default_height_material);
        view.setLayoutParams(lp);

        //fabCamera.setVisibility(View.GONE);
        fabSearch.setVisibility(View.GONE);

        tvMessage = (TextView) view.findViewById(R.id.tvMessage);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        final MenuItem searchItem = menu.findItem(R.id.menuSearch);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        mSearchView.setQueryHint(getString(R.string.search_hint));

        MenuItemCompat.expandActionView(searchItem);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                onBackPressed();
                return true;
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //do nothing?
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //do query

                mLoader.reset();
                mAdapter = null;
                performSearch(newText);

                return true;
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        ((GalleryActivity) getActivity()).restoreFragmentFromStack();
        return true;
    }

    private void performSearch(String searchText) {

        if (TextUtils.isEmpty(searchText)) return;

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
        tvMessage.setText("Result count: " + data.getCount());
    }
}
