package hu.mrolcsi.android.spoc.gallery.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.database.model.Label;
import hu.mrolcsi.android.spoc.database.model.LabelType;
import hu.mrolcsi.android.spoc.database.model.Views;
import hu.mrolcsi.android.spoc.database.model.binder.Label2Image;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.widgets.AnimatedExpandableListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.08.
 * Time: 15:39
 */

public class NavigationAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DATE_LOADER_ID = 40;
    private static final int PLACES_LOADER_ID = 41;

    private final Context mContext;
    private final LayoutInflater mInflater;
    //private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private final DateFormat dateFormat = new SimpleDateFormat("MMMM d.", Locale.getDefault());

    private NavigationItem[] mGroups;
    private NavigationItem[][] mChildren;

    public NavigationAdapter(Context context, LoaderManager loaderManager) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);

        mGroups = new NavigationItem[getGroupCount()];
        for (int i = 0; i < getGroupCount(); i++) {
            mGroups[i] = createGroupItems(i);
        }

        mChildren = new NavigationItem[getGroupCount()][];

        loaderManager.initLoader(DATE_LOADER_ID, null, this);
        loaderManager.initLoader(PLACES_LOADER_ID, null, this);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(22)
    private NavigationItem createGroupItems(int i) {
        NavigationItem item = new NavigationItem();

        switch (i) {
            case 0:
                item.title = mContext.getString(R.string.navigation_home);
                if (Build.VERSION.SDK_INT < 22) {
                    item.icon = mContext.getResources().getDrawable(R.drawable.ic_dashboard);
                } else {
                    item.icon = mContext.getResources().getDrawable(R.drawable.ic_dashboard, mContext.getTheme());
                }
                item.isExpandable = false;
                break;
            case 1:
                item.title = mContext.getString(R.string.navigation_date);
                if (Build.VERSION.SDK_INT < 22) {
                    item.icon = mContext.getResources().getDrawable(R.drawable.calendar);
                } else {
                    item.icon = mContext.getResources().getDrawable(R.drawable.calendar, mContext.getTheme());
                }
                item.isExpandable = true;
                break;
            case 2:
                item.title = mContext.getString(R.string.navigation_places);
                if (Build.VERSION.SDK_INT < 22) {
                    item.icon = mContext.getResources().getDrawable(R.drawable.map_marker);
                } else {
                    item.icon = mContext.getResources().getDrawable(R.drawable.map_marker, mContext.getTheme());
                }
                item.isExpandable = true;
                break;
            case 3:
                item.title = mContext.getString(R.string.navigation_people);
                if (Build.VERSION.SDK_INT < 22) {
                    item.icon = mContext.getResources().getDrawable(R.drawable.group);
                } else {
                    item.icon = mContext.getResources().getDrawable(R.drawable.group, mContext.getTheme());
                }
                item.isExpandable = true;
                break;
            case 4:
                item.title = mContext.getString(R.string.navigation_tags);
                if (Build.VERSION.SDK_INT < 22) {
                    item.icon = mContext.getResources().getDrawable(R.drawable.tag);
                } else {
                    item.icon = mContext.getResources().getDrawable(R.drawable.tag, mContext.getTheme());
                }
                item.isExpandable = true;
                break;
            case 5:
                item.title = mContext.getString(R.string.navigation_folders);
                if (Build.VERSION.SDK_INT < 22) {
                    item.icon = mContext.getResources().getDrawable(R.drawable.folder);
                } else {
                    item.icon = mContext.getResources().getDrawable(R.drawable.folder, mContext.getTheme());
                }
                item.isExpandable = true;
                break;
            case 6:
                item.title = mContext.getString(R.string.navigation_settings);
                if (Build.VERSION.SDK_INT < 22) {
                    item.icon = mContext.getResources().getDrawable(R.drawable.settings);
                } else {
                    item.icon = mContext.getResources().getDrawable(R.drawable.settings, mContext.getTheme());
                }
                item.isExpandable = false;
                break;
            default:
                return null;
        }

        return item;
    }

    @Override
    public int getGroupCount() {
        return 7;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object getGroup(int i) {
        return mGroups[i];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildren[groupPosition][childPosition];
    }

    @Override
    public long getGroupId(int i) {
        switch (i) {
            case 0:
                return R.id.navigation_home;
            case 1:
                return R.id.navigation_date;
            case 2:
                return R.id.navigation_places;
            case 3:
                return R.id.navigation_people;
            case 4:
                return R.id.navigation_tags;
            case 5:
                return R.id.navigation_folders;
            case 6:
                return R.id.navigation_settings;
            default:
                return -1;
        }
    }

    @Override
    public long getChildId(int i, int i1) {
        // TODO
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        NavigationItemHolder holder;
        if (view == null) {
            holder = new NavigationItemHolder();

            view = mInflater.inflate(R.layout.navigation_group_item, viewGroup, false);

            holder.tvTitle = (TextView) view.findViewById(android.R.id.title);
            holder.imgIcon = (ImageView) view.findViewById(android.R.id.icon);
            holder.imgGroupIndicator = (ImageView) view.findViewById(R.id.groupIndicator);

            view.setTag(holder);
        } else {
            holder = (NavigationItemHolder) view.getTag();
        }

        final NavigationItem groupItem = (NavigationItem) getGroup(i);

        if (groupItem.isExpandable) {
            holder.imgGroupIndicator.setVisibility(View.VISIBLE);
            if (groupItem.isExpanded) {
                holder.imgGroupIndicator.setImageResource(R.drawable.triangular_arrow_up);
            } else {
                holder.imgGroupIndicator.setImageResource(R.drawable.triangular_arrow_down);
            }
        } else {
            holder.imgGroupIndicator.setVisibility(View.INVISIBLE);
        }
        holder.tvTitle.setText(groupItem.title);
        holder.imgIcon.setImageDrawable(groupItem.icon);

        return view;
    }

    @Override
    public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        NavigationItemHolder holder;
        if (convertView == null) {
            holder = new NavigationItemHolder();

            convertView = mInflater.inflate(R.layout.navigation_child_item, parent, false);

            holder.tvTitle = (TextView) convertView.findViewById(android.R.id.title);
            holder.tvCount = (TextView) convertView.findViewById(R.id.count);

            convertView.setTag(holder);
        } else {
            holder = (NavigationItemHolder) convertView.getTag();
        }

        NavigationItem childItem = (NavigationItem) getChild(groupPosition, childPosition);

        if (childItem == null) {
            childItem = new NavigationItem();
        }

        holder.tvTitle.setText(childItem.title);
        if (childItem.count > 0) {
            holder.tvCount.setVisibility(View.VISIBLE);
            holder.tvCount.setText(String.valueOf(childItem.count));
        } else {
            holder.tvCount.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        if (groupPosition == 0 || groupPosition == getGroupCount() - 1) return 0;
        if (mChildren[groupPosition] == null) return 0;
        return mChildren[groupPosition].length;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(mContext);
        if (id == DATE_LOADER_ID) {
            loader.setUri(SPOCContentProvider.IMAGES_URI.buildUpon().appendPath(Views.IMAGES_BY_DAY_DAY_TAKEN).appendPath("count").build());
            return loader;
        } else if (id == PLACES_LOADER_ID) {
            /*
            SELECT count(image_id), label_id, name
            FROM images_with_labels
            WHERE type = 'LOCATION_LOCALITY'
            GROUP BY name
            ORDER BY date_taken DESC
             */
            // content://authority/images/location/count
            loader.setUri(SPOCContentProvider.IMAGES_URI.buildUpon().appendPath(Image.COLUMN_LOCATION).appendPath("count").build());
            loader.setProjection(new String[]{"count(_id)", Label.COLUMN_NAME, Label2Image.COLUMN_LABEL_ID});
            loader.setSelection(Label.COLUMN_TYPE + " = ?");
            loader.setSelectionArgs(new String[]{LabelType.LOCATION_LOCALITY.name()});

            return loader;
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == DATE_LOADER_ID) {
            int size = Math.min(3, data.getCount() - 1);

            if (size < 0) return;

            mChildren[1] = new NavigationItem[size + 1];

            for (int i = 0; i < size; i++) {
                data.moveToPosition(i);
                mChildren[1][i] = new NavigationItem();
                mChildren[1][i].count = data.getInt(0);
                final long dayLong = data.getLong(1);
                mChildren[1][i].title = dateFormat.format(new Date(dayLong));
            }
            mChildren[1][size] = new NavigationItem(mContext.getString(R.string.navigation_date_older));
        }

        if (loader.getId() == PLACES_LOADER_ID) {
            int size = Math.min(3, data.getCount() - 1);

            if (size < 0) return;

            mChildren[2] = new NavigationItem[size + 1];

            for (int i = 0; i < size; i++) {
                data.moveToPosition(i);
                mChildren[2][i] = new NavigationItem();
                mChildren[2][i].count = data.getInt(0);
                mChildren[2][i].title = data.getString(1);
            }
            mChildren[2][size] = new NavigationItem(mContext.getString(R.string.navigation_places_other));
        }

        notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO
    }

    class NavigationItemHolder {
        TextView tvTitle;
        ImageView imgIcon;
        ImageView imgGroupIndicator;
        TextView tvCount;
    }

    class NavigationItem {
        CharSequence title = "Navigation Item";
        Drawable icon = null;
        int count = 0;
        boolean isExpandable = false;
        boolean isExpanded = false;

        public NavigationItem() {
        }

        public NavigationItem(String title) {
            this.title = title;
        }
    }
}
