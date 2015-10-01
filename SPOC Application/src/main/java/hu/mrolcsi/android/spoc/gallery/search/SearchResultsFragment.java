package hu.mrolcsi.android.spoc.gallery.search;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import hu.mrolcsi.android.spoc.common.loader.ImagesTableLoader;
import hu.mrolcsi.android.spoc.common.loader.LabelsTableLoader;
import hu.mrolcsi.android.spoc.database.model.Label;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.main.GalleryActivity;
import hu.mrolcsi.android.spoc.gallery.main.ThumbnailsAdapter;
import hu.mrolcsi.android.spoc.gallery.main.ThumbnailsFragment;

import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.12.
 * Time: 9:37
 */

public final class SearchResultsFragment extends ThumbnailsFragment {

    public static final int SEARCHRESULTS_LOADER_ID = 30;
    public static final int SUGGESTIONS_LOADER_ID = 31;

    private TextView tvMessage;
    private String mQuery;
    private SuggestionAdapter mSuggestionsAdapter;
    private Bundle mSuggestionArgs = new Bundle();
    private CursorLoader mSuggestionsLoader;
    private SearchView mSearchView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mSuggestionArgs.putStringArray(LabelsTableLoader.ARG_PROJECTION, new String[]{"DISTINCT " + Label.COLUMN_FOREIGN_ID + " AS _id", Label.COLUMN_NAME, Label.COLUMN_TYPE});
        mSuggestionArgs.putString(LabelsTableLoader.ARG_SELECTION, "upper(" + Label.COLUMN_NAME + ") LIKE ?" + " OR " + "lower(" + Label.COLUMN_NAME + ") LIKE ?");
        mSuggestionArgs.putStringArray(LabelsTableLoader.ARG_SELECTION_ARGS, new String[]{"%"});
        mSuggestionArgs.putString(LabelsTableLoader.ARG_SORT_ORDER, Label.COLUMN_NAME + " ASC");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_search_result, container, false);

            findViews();

            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mRootView.getLayoutParams();
            lp.topMargin = getResources().getDimensionPixelOffset(R.dimen.abc_action_bar_default_height_material);
            mRootView.setLayoutParams(lp);

            fabSearch.setVisibility(View.GONE);

            tvMessage = (TextView) mRootView.findViewById(R.id.tvMessage);
            tvMessage.setText(Html.fromHtml(getString(R.string.search_message_helpText)));
        }

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getImagesLoader().reset();

        mSuggestionsLoader = (CursorLoader) getLoaderManager().initLoader(SUGGESTIONS_LOADER_ID, mSuggestionArgs, new LabelsTableLoader(getActivity(), this));
    }

    @Override
    protected void setupImagesAdapter() {
        mAdapter = new ThumbnailsAdapter(getActivity());
        ((ThumbnailsAdapter) mAdapter).setUseColumnSpan(false);
        twList.setAdapter(mAdapter);
    }

    @Override
    protected Loader<Cursor> setupLoader() {
        Bundle loaderArgs = getArguments().getBundle(ThumbnailsFragment.ARG_QUERY_BUNDLE);
        return getLoaderManager().restartLoader(SEARCHRESULTS_LOADER_ID, loaderArgs, new ImagesTableLoader(getActivity(), this));
    }

    @Override
    public int getLoaderId() {
        return SEARCHRESULTS_LOADER_ID;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mSearchView != null && !TextUtils.isEmpty(mSearchView.getQuery())) {
            performNameSearch(mSearchView.getQuery().toString());
        }
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
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                onBackPressed();
                return true;
            }
        });

        mSuggestionsAdapter = new SuggestionAdapter(getActivity());
        mSearchView.setSuggestionsAdapter(mSuggestionsAdapter);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();

                getImagesLoader().reset();
                performNameSearch(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSuggestionsLoader.setSelectionArgs(new String[]{newText.trim().toUpperCase(Locale.getDefault()) + "%", newText.trim().toLowerCase(Locale.getDefault()) + "%"});
                mSuggestionsLoader.forceLoad();

                return true;
            }
        });

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                final Cursor cursorWithSuggestion = (Cursor) mSuggestionsAdapter.getItem(position);
                final int id = cursorWithSuggestion.getInt(0);
                final String name = cursorWithSuggestion.getString(1);
                final String type = cursorWithSuggestion.getString(2);

                mSearchView.clearFocus();
                performIdSearch(id, type);

                mSearchView.setQuery(name, false);
                return true;
            }
        });

        mSearchView.setQuery(mQuery, true);

    }

    @Override
    public boolean onBackPressed() {
        ((GalleryActivity) getActivity()).restoreFragmentFromStack();
        return true;
    }

    private void performNameSearch(String searchText) {
        if (TextUtils.isEmpty(searchText)) {
            tvMessage.setText(R.string.error_noResults);
            if (mAdapter != null) {
                mAdapter.swapCursor(null);
            }
            twList.setAdapter(null);
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        mQuery = searchText;

        getImagesLoader().setUri(Uri.withAppendedPath(SPOCContentProvider.SEARCH_URI, searchText));
        getImagesLoader().setProjection(null);
        getImagesLoader().setSelection(null);
        getImagesLoader().setSelectionArgs(null);

        swipeRefreshLayout.setRefreshing(true);

        getImagesLoader().startLoading();
    }

    private void performIdSearch(int labelId, String labelType) {
        getImagesLoader().setUri(SPOCContentProvider.SEARCH_URI);
        getImagesLoader().setProjection(null);
        getImagesLoader().setSelection(Label.COLUMN_TYPE + " = ?" + " AND " + Label.COLUMN_FOREIGN_ID + " = ? ");
        getImagesLoader().setSelectionArgs(new String[]{labelType, String.valueOf(labelId)});

        swipeRefreshLayout.setRefreshing(true);

        getImagesLoader().startLoading();
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        super.onLoadComplete(loader, data);

        if (loader.getId() == SUGGESTIONS_LOADER_ID) {
            //load suggestions to adapter
            mSuggestionsAdapter.changeCursor(data);
        }

        if (loader.getId() == SEARCHRESULTS_LOADER_ID) {
            if (twList.getAdapter() == null) {
                twList.setAdapter(mAdapter);
            }
            ((ThumbnailsAdapter) mAdapter).setUseColumnSpan(false);
            final String quantityString = getResources().getQuantityString(R.plurals.message_numberOfResults, data.getCount(), data.getCount());
            tvMessage.setText(quantityString);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        super.onLoaderReset(loader);

        if (loader.getId() == SUGGESTIONS_LOADER_ID) {
            mSuggestionsAdapter.changeCursor(null);
        }
    }

    @Override
    public void onRefresh() {
        //do nothing
    }
}
