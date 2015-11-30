package hu.mrolcsi.android.spoc.gallery.main;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.bumptech.glide.Glide;
import hu.mrolcsi.android.spoc.common.fragment.ISPOCFragment;
import hu.mrolcsi.android.spoc.common.fragment.RetainedFragment;
import hu.mrolcsi.android.spoc.common.fragment.SPOCFragment;
import hu.mrolcsi.android.spoc.common.service.CacheBuilderService;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.widgets.AnimatedExpandableListView;
import hu.mrolcsi.android.spoc.gallery.main.categories.CategoriesFragment;
import hu.mrolcsi.android.spoc.gallery.main.categories.DatesFragment;
import hu.mrolcsi.android.spoc.gallery.main.categories.FoldersFragment;
import hu.mrolcsi.android.spoc.gallery.main.categories.PeopleFragment;
import hu.mrolcsi.android.spoc.gallery.main.categories.PlacesFragment;
import hu.mrolcsi.android.spoc.gallery.main.categories.TagsFragment;
import hu.mrolcsi.android.spoc.gallery.search.SearchResultsFragment;
import hu.mrolcsi.android.spoc.gallery.service.CacheBuilderReceiver;
import hu.mrolcsi.android.spoc.gallery.settings.SettingsFragment;

import java.util.Stack;

public final class GalleryActivity extends AppCompatActivity {

    public static final String DATA_FRAGMENT_STACK = "SPOC.Gallery.Navigation.FragmentStack";
    public static final String DATA_CURRENT_FRAGMENT = "SPOC.Gallery.Navigation.CurrentFragment";
    private static final String DATA_IS_FIRST_START = "SPOC.Gallery.IsFirstStart";
    private static final String DATA_CACHE_BUILDER_SERVICE = "SPOC.Gallery.CacheBuilder";
    private static final String DATA_LAST_NAVIGATION_POSITION = "SPOC:Gallery.Navigation.LastView";

    private DrawerLayout mDrawerLayout;
    private ExpandableListView mNavigation;
    private ActionBarDrawerToggle mDrawerToggle;

    private ISPOCFragment mCurrentFragment;
    private Stack<ISPOCFragment> mFragmentStack = new Stack<>();
    private RetainedFragment mRetainedFragment;
    private boolean mIsFirstStart = true;
    private CacheBuilderReceiver mCacheBuilderReceiver;
    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigation = (AnimatedExpandableListView) findViewById(R.id.navigation);

        mCacheBuilderReceiver = new CacheBuilderReceiver();

        loadRetainedData();

        setUpDrawerToggle();
        setUpNavigationView();
    }

    private void loadRetainedData() {
        FragmentManager fm = getSupportFragmentManager();
        mRetainedFragment = (RetainedFragment) fm.findFragmentByTag(RetainedFragment.TAG);

        if (mRetainedFragment == null) {
            mRetainedFragment = new RetainedFragment();
            fm.beginTransaction().add(mRetainedFragment, RetainedFragment.TAG).commit();

            retainData();
        } else {
            //noinspection unchecked
            mFragmentStack = (Stack<ISPOCFragment>) mRetainedFragment.getRetainedData(DATA_FRAGMENT_STACK);
            mCurrentFragment = (ISPOCFragment) mRetainedFragment.getRetainedData(DATA_CURRENT_FRAGMENT);
            try {
                mIsFirstStart = (boolean) mRetainedFragment.getRetainedData(DATA_IS_FIRST_START);
            } catch (NullPointerException e) {
                Log.w(getClass().getName(), "Why is this here?" + e.toString());
            }
            mServiceIntent = (Intent) mRetainedFragment.getRetainedData(DATA_CACHE_BUILDER_SERVICE);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();

        if (mCurrentFragment == null) {
            Bundle args = new Bundle();
            args.putInt(SPOCFragment.ARG_NAVIGATION_POSITION, 0);

            final ThumbnailsFragment newFragment = new HomeFragment();
            newFragment.setArguments(args);

            swapFragment(newFragment);
        } else {
            swapFragment(mCurrentFragment);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter mCacheBuilderIntentFilter = new IntentFilter();
        mCacheBuilderIntentFilter.addAction(CacheBuilderService.BROADCAST_ACTION_CACHING);
        LocalBroadcastManager.getInstance(this).registerReceiver(mCacheBuilderReceiver, mCacheBuilderIntentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mIsFirstStart = false;
        retainData();

        if (mCacheBuilderReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mCacheBuilderReceiver);
        }

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(CacheBuilderReceiver.NOTIFICATION_ID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mServiceIntent != null) {
            stopService(mServiceIntent);
        }

        Glide.get(this).clearMemory();
    }

    private void retainData() {
        mRetainedFragment.putRetainedData(DATA_FRAGMENT_STACK, mFragmentStack);
        mRetainedFragment.putRetainedData(DATA_CURRENT_FRAGMENT, mCurrentFragment);
        mRetainedFragment.putRetainedData(DATA_IS_FIRST_START, mIsFirstStart);
        mRetainedFragment.putRetainedData(DATA_CACHE_BUILDER_SERVICE, mServiceIntent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setUpNavigationView() {

        final NavigationAdapter navigationAdapter = new NavigationAdapter(this, getSupportLoaderManager());
        mNavigation.setAdapter(navigationAdapter);
        mNavigation.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                final NavigationAdapter.NavigationItem groupItem = (NavigationAdapter.NavigationItem) navigationAdapter.getGroup(i);
                if (groupItem.isExpandable) {
                    groupItem.isExpanded = true;
                }
            }
        });
        mNavigation.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                final NavigationAdapter.NavigationItem groupItem = (NavigationAdapter.NavigationItem) navigationAdapter.getGroup(i);
                if (groupItem.isExpandable) {
                    groupItem.isExpanded = false;
                }
            }
        });
        mNavigation.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView listView, View groupView, int groupPosition, long groupId) {

                int index = listView.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(groupPosition));

                Bundle args = new Bundle();
                args.putInt(SPOCFragment.ARG_NAVIGATION_POSITION, index);

                //replace fragment
                switch (groupPosition) {
                    case NavigationAdapter.HOME_SCREEN_POSITION:
                        if (listView.getCheckedItemPosition() != index) {
                            final ThumbnailsFragment newFragment = new HomeFragment();
                            newFragment.setArguments(args);
                            swapFragment(newFragment);
                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    case NavigationAdapter.SETTINGS_POSITION:
                        if (listView.getCheckedItemPosition() != index) {
                            final SettingsFragment newFragment = new SettingsFragment();
                            newFragment.setArguments(args);
                            swapFragment(newFragment);
                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    default:
                        if (listView.isGroupExpanded(groupPosition)) {
                            ((AnimatedExpandableListView) mNavigation).collapseGroupWithAnimation(groupPosition);
                        } else {
                            ((AnimatedExpandableListView) mNavigation).expandGroupWithAnimation(groupPosition);
                        }
                        return true;
                }
            }
        });
        mNavigation.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView listView, View childView, int groupPosition, int childPosition, long childId) {
                // http://stackoverflow.com/questions/10318642/highlight-for-selected-item-in-expandable-list

                int index = listView.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
                listView.setItemChecked(index, true);

                ISPOCFragment categoryFragment;
                Bundle args = new Bundle();
                args.putInt(SPOCFragment.ARG_NAVIGATION_POSITION, index);

                final NavigationAdapter.NavigationItem childItem = (NavigationAdapter.NavigationItem) listView.getItemAtPosition(index);
                args.putString(CategoriesFragment.ARG_SELECTED_CATEGORY, (String) childItem.title);

                switch (groupPosition) {
                    case NavigationAdapter.DATES_POSITION:
                        categoryFragment = new DatesFragment();
                        break;
                    case NavigationAdapter.PLACES_POSITION:
                        categoryFragment = new PlacesFragment();
                        break;
                    case NavigationAdapter.PEOPLE_POSITION:
                        categoryFragment = new PeopleFragment();
                        break;
                    case NavigationAdapter.TAGS_POSITION:
                        categoryFragment = new TagsFragment();
                        break;
                    case NavigationAdapter.FOLDERS_POSITION:
                        categoryFragment = new FoldersFragment();
                        break;
                    default:
                        categoryFragment = null;
                        break;
                }

                if (categoryFragment != null) {
                    ((Fragment) categoryFragment).setArguments(args);
                    swapFragment(categoryFragment);
                    mDrawerLayout.closeDrawers();
                }

                return true;
            }
        });
    }

    @TargetApi(21)
    private void setUpDrawerToggle() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);

        this.mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(mCurrentFragment.getTitle());
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(getString(R.string.app_name));
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.syncState();

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(this.mDrawerToggle);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @TargetApi(22)
    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mCurrentFragment.getTitle());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);

        final MenuItem searchItem = menu.findItem(R.id.menuSearch);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (!(mCurrentFragment instanceof SearchResultsFragment)) {
                        Bundle args = new Bundle();
                        args.putInt(SearchResultsFragment.ARG_LOADER_ID, 15);
                        final SearchResultsFragment resultsFragment = new SearchResultsFragment();
                        resultsFragment.setArguments(args);
                        swapFragment(resultsFragment);
                    }
                }
            }
        });

        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else if (mCurrentFragment != null && !mCurrentFragment.onBackPressed())
            super.onBackPressed();
    }

    public void swapFragment(ISPOCFragment newFragment) {
        if (mCurrentFragment != null && mCurrentFragment != newFragment) { //avoid storing the same fragment more than once
            //TODO:
            /*
            java.lang.NullPointerException
            at hu.mrolcsi.android.spoc.gallery.main.GalleryActivity.swapFragment(GalleryActivity.java:361)
             */
            mFragmentStack.push(mCurrentFragment);
        }

        mCurrentFragment = newFragment;

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.container, (Fragment) newFragment, newFragment.getTagString());
        transaction.commit();

        mNavigation.setItemChecked(mCurrentFragment.getNavigationItemPosition(), true);

        Log.v(getClass().getSimpleName(), "Swapped to fragment: " + newFragment.toString());
    }

    public void restoreFragmentFromStack() {
        if (mFragmentStack.isEmpty()) return;

        mCurrentFragment = mFragmentStack.pop();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        transaction.replace(R.id.container, (Fragment) mCurrentFragment, mCurrentFragment.getTagString());
        transaction.commit();

        mNavigation.setItemChecked(mCurrentFragment.getNavigationItemPosition(), true);

        Log.v(getClass().getSimpleName(), "Fragment restored from stack: " + mCurrentFragment.toString());
    }
}
