package hu.mrolcsi.android.spoc.gallery.main;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import hu.mrolcsi.android.spoc.common.fragment.SPOCFragment;
import hu.mrolcsi.android.spoc.common.loader.ImagesTableLoader;
import hu.mrolcsi.android.spoc.common.service.DatabaseBuilderService;
import hu.mrolcsi.android.spoc.common.utils.FileUtils;
import hu.mrolcsi.android.spoc.common.utils.GeneralUtils;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.CursorRecyclerViewAdapter;
import hu.mrolcsi.android.spoc.gallery.common.HideOnScrollListener;
import hu.mrolcsi.android.spoc.gallery.common.utils.DialogUtils;
import hu.mrolcsi.android.spoc.gallery.imagedetails.ImagePagerActivity;
import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.ItemSelectionSupport;
import org.lucasr.twowayview.widget.SpannableGridLayoutManager;
import org.lucasr.twowayview.widget.TwoWayView;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:12
 */

public abstract class ThumbnailsFragment extends SPOCFragment implements ImagesTableLoader.LoaderCallbacks, SwipeRefreshLayout.OnRefreshListener {

    public static final int IMAGES_LOADER_ID = 1;
    public static final String ARG_QUERY_BUNDLE = "SPOC.Gallery.Thumbnails.ARGUMENT_BUNDLE";
    public static final String ARG_LOADER_ID = "SPOC.Gallery.Thumbnails.LOADER_ID";

    protected CursorRecyclerViewAdapter mAdapter;
    protected FloatingActionButton fabSearch;
    protected TwoWayView twList;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected Bundle mQueryArgs = new Bundle();

    private CursorLoader mImagesLoader;
    private Parcelable mListInstanceState;
    private int mSavedOrientation = Configuration.ORIENTATION_UNDEFINED;
    private Integer mSavedPosition;
    private ActionMode mActionMode;
    private ItemSelectionSupport mItemSelectionSupport;
    private MenuItem mSearchMenuItem;
    private BroadcastReceiver mDatabaseWatcher = new DatabaseBuilderWatcher();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_thumbnails, container, false);
            findViews();
        }

        //showSystemUI();

        return mRootView;
    }

    protected void findViews() {
        twList = (TwoWayView) mRootView.findViewById(R.id.list);
        twList.setHasFixedSize(true);
        twList.setAdapter(null);

        ((SpannableGridLayoutManager) twList.getLayoutManager()).setNumColumns(getResources().getInteger(R.integer.preferredColumns));

        mItemSelectionSupport = ItemSelectionSupport.addTo(twList);
        mItemSelectionSupport.setChoiceMode(ItemSelectionSupport.ChoiceMode.MULTIPLE);

        final ItemClickSupport itemClick = ItemClickSupport.addTo(twList);
        itemClick.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int i, long l) {
                if (mActionMode != null) {
                    mActionMode.setTitle(String.format(getString(R.string.cab_itemsSelectedFormat), mItemSelectionSupport.getCheckedItemCount()));
                    return;
                }

                mListInstanceState = twList.getLayoutManager().onSaveInstanceState();
                mSavedOrientation = getResources().getConfiguration().orientation;
                mSavedPosition = i;

                Intent imagePagerIntent = new Intent(getActivity(), ImagePagerActivity.class);

                imagePagerIntent.putExtra(ImagePagerActivity.ARG_LOADER_ID, mImagesLoader.getId());
                imagePagerIntent.putExtra(ImagePagerActivity.ARG_SELECTED_POSITION, getClickedImagePosition(i));
                imagePagerIntent.putExtra(ARG_QUERY_BUNDLE, mQueryArgs);

                startActivity(imagePagerIntent);
            }
        });
        itemClick.setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View view, int position, long id) {
                if (mActionMode != null) {
                    return false;
                }
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionModeCallback());
                mItemSelectionSupport.setItemChecked(position, true);
                mActionMode.setTitle(String.format(getString(R.string.cab_itemsSelectedFormat), mItemSelectionSupport.getCheckedItemCount()));
                return true;
            }
        });

        fabSearch = (FloatingActionButton) mRootView.findViewById(R.id.fabSearch);
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSearchMenuItem != null) {
                    MenuItemCompat.expandActionView(mSearchMenuItem);
                }
            }
        });

        final HideOnScrollListener hideOnScrollListener = new HideOnScrollListener() {
            @Override
            public void hide() {
                int fabMargin = ((ViewGroup.MarginLayoutParams) fabSearch.getLayoutParams()).bottomMargin;
                ViewCompat.animate(fabSearch).translationY(fabSearch.getHeight() + fabMargin).setInterpolator(new AccelerateInterpolator(2)).start();
            }

            @Override
            public void show() {
                ViewCompat.animate(fabSearch).translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }
        };
        twList.setOnScrollListener(hideOnScrollListener);

        swipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.background_floating_material_dark);
        swipeRefreshLayout.setColorSchemeResources(R.color.background_floating_material_light);
        swipeRefreshLayout.setOnRefreshListener(this);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                    if (GeneralUtils.isServiceRunning(manager, DatabaseBuilderService.class)) {
                        swipeRefreshLayout.setRefreshing(true);
                        swipeRefreshLayout.setEnabled(false);
                    } else {
                        swipeRefreshLayout.setRefreshing(false);
                        swipeRefreshLayout.setEnabled(true);
                    }
                }
            }
        }, 500);
    }

    protected int getClickedImagePosition(int adapterPosition) {
        return adapterPosition;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        mSearchMenuItem = menu.findItem(R.id.menuSearch);
    }

    @Override
    public boolean onBackPressed() {
        if (mActionMode != null) {
            mActionMode.finish();
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();

        SpannableGridLayoutManager layoutManager = ((SpannableGridLayoutManager) twList.getLayoutManager());
        layoutManager.setNumColumns(getResources().getInteger(R.integer.preferredColumns));
        layoutManager.setNumRows(getResources().getInteger(R.integer.preferredColumns));
        setupImagesAdapter();

        mImagesLoader = (CursorLoader) setupLoader();

        IntentFilter mDatabaseBuilderIntentFilter = new IntentFilter();
        mDatabaseBuilderIntentFilter.addAction(DatabaseBuilderService.BROADCAST_ACTION_IMAGES_READY);
        mDatabaseBuilderIntentFilter.addAction(DatabaseBuilderService.BROADCAST_ACTION_FINISHED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDatabaseWatcher, mDatabaseBuilderIntentFilter);
    }

    protected abstract void setupImagesAdapter();

    protected abstract Loader<Cursor> setupLoader();

    @Override
    public void onResume() {
        super.onResume();

//        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
//        swipeRefreshLayout.setRefreshing(GeneralUtils.isServiceRunning(manager, DatabaseBuilderService.class));
    }

    @Override
    public void onStop() {
        super.onStop();

        mAdapter = null;
        swipeRefreshLayout.setRefreshing(false);
        twList.setAdapter(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mActionMode != null) mActionMode.finish();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mDatabaseWatcher);
    }

    public int getLoaderId() {
        return IMAGES_LOADER_ID;
    }

    public CursorLoader getImagesLoader() {
        return mImagesLoader;
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, final Cursor data) {
        if (loader.getId() != getLoaderId()) return;

        Log.d(getClass().getSimpleName(), "onLoadComplete");
        if (data == null || data.getCount() == 0) {
            mRootView.findViewById(R.id.llEmptyIndicator).setVisibility(View.VISIBLE);
            return;
        }
        mRootView.findViewById(R.id.llEmptyIndicator).setVisibility(View.GONE);

        mQueryArgs.putString(ImagesTableLoader.ARG_URI_STRING, ((CursorLoader) loader).getUri().toString());
        mQueryArgs.putStringArray(ImagesTableLoader.ARG_PROJECTION, ((CursorLoader) loader).getProjection());
        mQueryArgs.putString(ImagesTableLoader.ARG_SELECTION, ((CursorLoader) loader).getSelection());
        mQueryArgs.putStringArray(ImagesTableLoader.ARG_SELECTION_ARGS, ((CursorLoader) loader).getSelectionArgs());
        mQueryArgs.putString(ImagesTableLoader.ARG_SORT_ORDER, ((CursorLoader) loader).getSortOrder());

        if (mAdapter == null) {
            setupImagesAdapter();
        }
        mAdapter.changeCursor(data);


        if (mListInstanceState != null && mSavedOrientation == getResources().getConfiguration().orientation) { //different orientation -> different layout params
            twList.getLayoutManager().onRestoreInstanceState(mListInstanceState);
            mListInstanceState = null;
        } else if (mSavedPosition != null) {
            twList.invalidate();
            twList.scrollToPosition(mSavedPosition);

            mSavedPosition = null;
            mListInstanceState = null;
        } //else handle orientation change internally
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() != getLoaderId()) return;

        Log.d(getClass().getSimpleName(), "onLoaderReset");
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
        }
    }

    private void doBatchDelete() {
        final AlertDialog.Builder confirmDialog = DialogUtils.buildConfirmDialog(getActivity());
        confirmDialog.setMessage(getResources().getQuantityString(R.plurals.dialog_message_deleteMultiplePictures, mItemSelectionSupport.getCheckedItemCount(), mItemSelectionSupport.getCheckedItemCount()));
        confirmDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new FileUtils.MultiFileDeleterTask(getActivity(), mItemSelectionSupport.getCheckedItemPositions()) {

                    private ProgressDialog pd;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        pd = new ProgressDialog(getActivity());
                        pd.setMessage(getString(R.string.message_deletingPictures));
                        pd.setIndeterminate(false);
                        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        pd.show();
                    }

                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        super.onProgressUpdate(values);

                        pd.setProgress(values[0]);
                        pd.setMax(values[1]);
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);

                        pd.dismiss();
                        mActionMode.finish();

                        mImagesLoader.startLoading();

                        final CharSequence text = getResources().getQuantityString(R.plurals.dialog_message_numPicturesDeleted, pd.getProgress(), pd.getProgress());
                        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                    }
                }.execute(mImagesLoader);
            }
        }).show();
    }

    private void doBatchShare() {

        new FileUtils.MultiFileShareTask(getActivity(), mItemSelectionSupport.getCheckedItemPositions()) {
            public ProgressDialog pd;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pd = new ProgressDialog(getActivity());
                pd.setMessage(getString(R.string.message_preparingToShare));
                pd.setIndeterminate(false);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.show();
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);

                pd.setProgress(values[0]);
                pd.setMax(values[1]);
            }

            @Override
            protected void onPostExecute(Intent intent) {
                super.onPostExecute(intent);

                pd.dismiss();
                startActivity(Intent.createChooser(intent, getString(R.string.details_action_share)));
            }
        }.execute(mImagesLoader);

    }

    @Override
    public void onRefresh() {
        // restart database builder
        Intent serviceIntent = new Intent(getActivity(), DatabaseBuilderService.class);
        serviceIntent.putExtra(DatabaseBuilderService.ARG_FIRST_START, false);
        getActivity().startService(serviceIntent);
    }

    protected class ActionModeCallback implements android.support.v7.view.ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.details_contextual, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            final int id = menuItem.getItemId();
            switch (id) {
                case R.id.menuDelete:
                    doBatchDelete();
                    return true;
                case R.id.menuShare:
                    doBatchShare();
                    return true;
                case R.id.menuSelectAll:
                    for (int i = 0; i < mAdapter.getItemCount(); i++) {
                        mItemSelectionSupport.setItemChecked(i, true);
                    }
                    menuItem.setVisible(false);
                    actionMode.getMenu().findItem(R.id.menuDeselectAll).setVisible(true);
                    actionMode.setTitle(String.format(getString(R.string.cab_itemsSelectedFormat), mItemSelectionSupport.getCheckedItemCount()));
                    return false;
                case R.id.menuDeselectAll:
                    for (int i = 0; i < mAdapter.getItemCount(); i++) {
                        mItemSelectionSupport.setItemChecked(i, false);
                    }
                    menuItem.setVisible(false);
                    actionMode.getMenu().findItem(R.id.menuSelectAll).setVisible(true);
                    actionMode.setTitle(String.format(getString(R.string.cab_itemsSelectedFormat), mItemSelectionSupport.getCheckedItemCount()));
                    return false;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mActionMode = null;
            mItemSelectionSupport.clearChoices();
        }
    }

    protected class DatabaseBuilderWatcher extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DatabaseBuilderService.BROADCAST_ACTION_FINISHED)) {
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(true);
            }
        }
    }
}
