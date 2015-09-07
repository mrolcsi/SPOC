package hu.mrolcsi.android.spoc.gallery.search;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.TextView;
import hu.mrolcsi.android.spoc.common.loader.ImageTableLoader;
import hu.mrolcsi.android.spoc.common.loader.LabelTableLoader;
import hu.mrolcsi.android.spoc.database.model.Label;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.main.GalleryActivity;
import hu.mrolcsi.android.spoc.gallery.main.ThumbnailsFragment;

import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.12.
 * Time: 9:37
 */

public class SearchResultsFragment extends ThumbnailsFragment {

    private TextView tvMessage;
    private String mQuery;
    private SuggestionAdapter mSuggestionsAdapter;
    private Bundle mSuggestionArgs = new Bundle();
    private CursorLoader mSuggestionsLoader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mSuggestionArgs.putStringArray(LabelTableLoader.ARG_PROJECTION, new String[]{"_id", Label.COLUMN_NAME, Label.COLUMN_TYPE});
        mSuggestionArgs.putString(LabelTableLoader.ARG_SELECTION, Label.COLUMN_TYPE + " LIKE '%_TEXT' AND " + Label.COLUMN_NAME + " LIKE ?");
        mSuggestionArgs.putStringArray(LabelTableLoader.ARG_SELECTION_ARGS, new String[]{"%"});
        mSuggestionArgs.putString(LabelTableLoader.ARG_SORT_ORDER, Label.COLUMN_NAME + " ASC");
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
        }

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mImagesLoader.reset();

        mSuggestionsLoader = (CursorLoader) getLoaderManager().initLoader(LabelTableLoader.ID, mSuggestionArgs, new LabelTableLoader(getActivity(), this));
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

        mSuggestionsAdapter = new SuggestionAdapter(getActivity());
        searchView.setSuggestionsAdapter(mSuggestionsAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //do query

                mSuggestionsLoader.reset();
                mSuggestionsLoader.setSelectionArgs(new String[]{newText.toLowerCase(Locale.getDefault()) + "%"});
                mSuggestionsLoader.startLoading();

                mImagesLoader.reset();
                performSearch(newText);

                return true;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                final Cursor cursorWithSuggestion = (Cursor) mSuggestionsAdapter.getItem(position);
                final String name = cursorWithSuggestion.getString(1);
                searchView.setQuery(name, true);
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

        mImagesLoader.setUri(Uri.withAppendedPath(SPOCContentProvider.SEARCH_URI, searchText));
        mImagesLoader.setProjection(null);
        mImagesLoader.setSelection(null);
        mImagesLoader.setSelectionArgs(null);

        mImagesLoader.startLoading();
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        super.onLoadComplete(loader, data);

        if (loader.getId() == LabelTableLoader.ID) {
            //load suggestions to adapter
            mSuggestionsAdapter.changeCursor(data);
        }

        if (loader.getId() == ImageTableLoader.ID) {
            mAdapter.setUseColumnSpan(false);
            final String quantityString = getResources().getQuantityString(R.plurals.message_numberOfResults, data.getCount(), data.getCount());
            tvMessage.setText(quantityString);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        super.onLoaderReset(loader);

        if (loader.getId() == LabelTableLoader.ID) {
            mSuggestionsAdapter.changeCursor(null);
        }

    }
}
