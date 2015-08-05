package hu.mrolcsi.android.spoc.gallery.main;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.bumptech.glide.Glide;
import hu.mrolcsi.android.spoc.common.fragment.ISPOCFragment;
import hu.mrolcsi.android.spoc.common.fragment.RetainedFragment;
import hu.mrolcsi.android.spoc.common.loader.MediaStoreLoader;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.service.CacheBuilderReceiver;
import hu.mrolcsi.android.spoc.gallery.service.CacheBuilderService;
import hu.mrolcsi.android.spoc.gallery.settings.SettingsFragment;

import java.util.Stack;

public final class GalleryActivity extends AppCompatActivity {

    public static final String DATA_FRAGMENT_STACK = "SPOC.Gallery.Navigation.FragmentStack";
    public static final String DATA_CURRENT_FRAGMENT = "SPOC.Gallery.Navigation.CurrentFragment";
    private static final String DATA_IS_FIRST_START = "SPOC.Gallery.IsFirstStart";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;

    private ISPOCFragment mCurrentFragment;
    private Stack<ISPOCFragment> mFragmentStack = new Stack<>();
    private RetainedFragment mRetainedFragment;
    private boolean isFirstStart = true;
    private CacheBuilderReceiver mCacheBuilderReceiver;
    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        loadRetainedData();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation);
        mCacheBuilderReceiver = new CacheBuilderReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CacheBuilderService.BROADCAST_ACTION_FIRST);
        intentFilter.addAction(CacheBuilderService.BROADCAST_ACTION_INCREMENTAL);
        LocalBroadcastManager.getInstance(this).registerReceiver(mCacheBuilderReceiver, intentFilter);

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
                isFirstStart = (boolean) mRetainedFragment.getRetainedData(DATA_IS_FIRST_START);
            } catch (Exception e) {
                Log.w(getClass().getName(), e);
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();

        mServiceIntent = new Intent(this, CacheBuilderService.class);

        if (mCurrentFragment == null)
            mCurrentFragment = new ThumbnailsFragment();
        swapFragment(mCurrentFragment);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        isFirstStart = false;
        retainData();

        Glide.get(this).clearMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getSupportLoaderManager().destroyLoader(MediaStoreLoader.ID);

        if (mCacheBuilderReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mCacheBuilderReceiver);
        }

        // TODO: should only stop service and notification when app crashes
        if (mServiceIntent != null)
            stopService(mServiceIntent);

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(CacheBuilderReceiver.NOTIFICATION_ID);
    }

    private void retainData() {
        mRetainedFragment.putRetainedData(DATA_FRAGMENT_STACK, mFragmentStack);
        mRetainedFragment.putRetainedData(DATA_CURRENT_FRAGMENT, mCurrentFragment);
        mRetainedFragment.putRetainedData(DATA_IS_FIRST_START, isFirstStart);
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

//    @Override
//    public ActionMode startSupportActionMode(final ActionMode.Callback callback) {
//        if (getSupportActionBar() != null)
//            return getSupportActionBar().startActionMode(callback);
//        else return null;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
        restoreActionBar();
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view

        //noinspection SimplifiableIfStatement
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.START);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);

        return super.onPrepareOptionsMenu(menu);
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
}
