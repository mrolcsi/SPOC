package hu.mrolcsi.android.spoc.gallery.main;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import hu.mrolcsi.android.spoc.common.fragment.ISPOCFragment;
import hu.mrolcsi.android.spoc.common.fragment.RetainedFragment;
import hu.mrolcsi.android.spoc.common.loader.MediaStoreLoader;
import hu.mrolcsi.android.spoc.common.service.CacheBuilderService;
import hu.mrolcsi.android.spoc.common.service.DatabaseBuilderService;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.search.SearchResultsFragment;
import hu.mrolcsi.android.spoc.gallery.service.CacheBuilderReceiver;
import hu.mrolcsi.android.spoc.gallery.settings.SettingsFragment;

import java.util.Stack;

public final class GalleryActivity extends AppCompatActivity {

    public static final String DATA_FRAGMENT_STACK = "SPOC.Gallery.Navigation.FragmentStack";
    public static final String DATA_CURRENT_FRAGMENT = "SPOC.Gallery.Navigation.CurrentFragment";
    private static final String DATA_IS_FIRST_START = "SPOC.Gallery.IsFirstStart";
    private static final String DATA_CACHE_BUILDER_SERVICE = "SPOC.Gallery.CacheBuilder";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;

    private ISPOCFragment mCurrentFragment;
    private Stack<ISPOCFragment> mFragmentStack = new Stack<>();
    private RetainedFragment mRetainedFragment;
    private boolean mIsFirstStart = true;
    private CacheBuilderReceiver mCacheBuilderReceiver;
    private DatabaseBuilderWatcher mDatabaseBuilderReceiver;
    private Intent mServiceIntent;
    private boolean mIsRefreshing = true;
    private MenuItem mRefreshMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        loadRetainedData();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation);

        mCacheBuilderReceiver = new CacheBuilderReceiver();
        mDatabaseBuilderReceiver = new DatabaseBuilderWatcher();

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

        if (mCurrentFragment == null)
            mCurrentFragment = new ThumbnailsFragment();
        swapFragment(mCurrentFragment);
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter mCacheBuilderIntentFilter = new IntentFilter();
        mCacheBuilderIntentFilter.addAction(CacheBuilderService.BROADCAST_ACTION_CACHING);
        mCacheBuilderIntentFilter.addAction(CacheBuilderService.BROADCAST_ACTION_INCREMENTAL);
        LocalBroadcastManager.getInstance(this).registerReceiver(mCacheBuilderReceiver, mCacheBuilderIntentFilter);

        IntentFilter mDatabaseBuilderIntentFilter = new IntentFilter();
        mDatabaseBuilderIntentFilter.addAction(DatabaseBuilderService.BROADCAST_ACTION_IMAGES_READY);
        mDatabaseBuilderIntentFilter.addAction(DatabaseBuilderService.BROADCAST_ACTION_FINISHED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mDatabaseBuilderReceiver, mDatabaseBuilderIntentFilter);
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
        if (mDatabaseBuilderReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mDatabaseBuilderReceiver);
        }

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(CacheBuilderReceiver.NOTIFICATION_ID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getSupportLoaderManager().destroyLoader(MediaStoreLoader.ID);

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
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //switch (menuItem.getId()) case
                //replace fragment

                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.navigation_settings:
                        swapFragment(new SettingsFragment());
                        break;
                    case R.id.navigation_home:
                    default:
                        swapFragment(new ThumbnailsFragment());
                        break;
                }

                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                return false;
            }
        });
    }

    @TargetApi(11)
    private void setUpDrawerToggle() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(0);
        }

        setSupportActionBar(toolbar);

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
        final MenuItem item = mNavigationView.getMenu().findItem(mCurrentFragment.getNavigationItemId());
        if (item != null)
            item.setChecked(true);
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
                        final SearchResultsFragment resultsFragment = new SearchResultsFragment();
                        swapFragment(resultsFragment);
                    }
                }
            }
        });

        mRefreshMenuItem = menu.findItem(R.id.menuRefresh);
        final View refreshActionView = MenuItemCompat.getActionView(mRefreshMenuItem);
        final Animation refreshAnimation = AnimationUtils.loadAnimation(this, R.anim.refresh);
        refreshAnimation.setRepeatCount(Animation.INFINITE);
        refreshActionView.startAnimation(refreshAnimation);
        mRefreshMenuItem.setVisible(mIsRefreshing);

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
        if (mCurrentFragment != null && mCurrentFragment != newFragment) //avoid storing the same fragment more than once
            mFragmentStack.push(mCurrentFragment);

        mCurrentFragment = newFragment;

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.container, (Fragment) newFragment, newFragment.getTagString());
        transaction.commit();

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

        Log.v(getClass().getSimpleName(), "Fragment restored from stack: " + mCurrentFragment.toString());
    }

    private class DatabaseBuilderWatcher extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DatabaseBuilderService.BROADCAST_ACTION_IMAGES_READY)) {
                mIsRefreshing = false;

                final ImageView imageView = (ImageView) MenuItemCompat.getActionView(mRefreshMenuItem);
                imageView.setImageDrawable(null);

                invalidateOptionsMenu();
            }
        }
    }
}
