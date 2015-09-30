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

import hu.mrolcsi.android.spoc.database.model.Label;
import hu.mrolcsi.android.spoc.database.model.LabelType;
import hu.mrolcsi.android.spoc.database.model.Views;
import hu.mrolcsi.android.spoc.database.model.binder.Label2Image;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.ContactPhotoLoader;
import hu.mrolcsi.android.spoc.gallery.common.widgets.AnimatedExpandableListView;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    private static final int PEOPLE_LOADER_ID = 42;
    private static final int FOLDERS_LOADER_ID = 43;
    private static final int TAGS_LOADER_ID = 44;

    private static final int HOME_SCREEN_POSITION = 0;
    private static final int DATES_POSITION = 1;
    private static final int PLACES_POSITION = 2;
    private static final int PEOPLE_POSITION = 3;
    private static final int TAGS_POSITION = 4;
    private static final int FOLDERS_POSITION = 5;
    private static final int SETTINGS_POSITION = 6;

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
        loaderManager.initLoader(PEOPLE_LOADER_ID, null, this);
        loaderManager.initLoader(FOLDERS_LOADER_ID, null, this);
        loaderManager.initLoader(TAGS_LOADER_ID, null, this);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(22)
    private NavigationItem createGroupItems(int i) {
        NavigationItem item = new NavigationItem();

        switch (i) {
            case HOME_SCREEN_POSITION:
                item.title = mContext.getString(R.string.navigation_home);
                if (Build.VERSION.SDK_INT < 22) {
                    item.icon = mContext.getResources().getDrawable(R.drawable.ic_dashboard);
                } else {
                    item.icon = mContext.getResources().getDrawable(R.drawable.ic_dashboard, mContext.getTheme());
                }
                item.isExpandable = false;
                break;
            case DATES_POSITION:
                item.title = mContext.getString(R.string.navigation_date);
                if (Build.VERSION.SDK_INT < 22) {
                    item.icon = mContext.getResources().getDrawable(R.drawable.calendar);
                } else {
                    item.icon = mContext.getResources().getDrawable(R.drawable.calendar, mContext.getTheme());
                }
                item.isExpandable = true;
                break;
            case PLACES_POSITION:
                item.title = mContext.getString(R.string.navigation_places);
                if (Build.VERSION.SDK_INT < 22) {
                    item.icon = mContext.getResources().getDrawable(R.drawable.map_marker);
                } else {
                    item.icon = mContext.getResources().getDrawable(R.drawable.map_marker, mContext.getTheme());
                }
                item.isExpandable = true;
                break;
            case PEOPLE_POSITION:
                item.title = mContext.getString(R.string.navigation_people);
                if (Build.VERSION.SDK_INT < 22) {
                    item.icon = mContext.getResources().getDrawable(R.drawable.group);
                } else {
                    item.icon = mContext.getResources().getDrawable(R.drawable.group, mContext.getTheme());
                }
                item.isExpandable = true;
                break;
            case TAGS_POSITION:
                item.title = mContext.getString(R.string.navigation_tags);
                if (Build.VERSION.SDK_INT < 22) {
                    item.icon = mContext.getResources().getDrawable(R.drawable.tag);
                } else {
                    item.icon = mContext.getResources().getDrawable(R.drawable.tag, mContext.getTheme());
                }
                item.isExpandable = true;
                break;
            case FOLDERS_POSITION:
                item.title = mContext.getString(R.string.navigation_folders);
                if (Build.VERSION.SDK_INT < 22) {
                    item.icon = mContext.getResources().getDrawable(R.drawable.folder);
                } else {
                    item.icon = mContext.getResources().getDrawable(R.drawable.folder, mContext.getTheme());
                }
                item.isExpandable = true;
                break;
            case SETTINGS_POSITION:
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
            case HOME_SCREEN_POSITION:
                return R.id.navigation_home;
            case DATES_POSITION:
                return R.id.navigation_date;
            case PLACES_POSITION:
                return R.id.navigation_places;
            case PEOPLE_POSITION:
                return R.id.navigation_people;
            case TAGS_POSITION:
                return R.id.navigation_tags;
            case FOLDERS_POSITION:
                return R.id.navigation_folders;
            case SETTINGS_POSITION:
                return R.id.navigation_settings;
            default:
                return -1;
        }
    }

    @Override
    public long getChildId(int group, int child) {
        if (mChildren[group] != null) {
            if (mChildren[group][child] != null) {
                return mChildren[group][child].id;
            }
        }
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
        final NavigationItemHolder holder;
        if (convertView == null) {
            holder = new NavigationItemHolder();

            convertView = mInflater.inflate(R.layout.navigation_child_item, parent, false);

            holder.tvTitle = (TextView) convertView.findViewById(android.R.id.title);
            holder.tvCount = (TextView) convertView.findViewById(R.id.count);
            holder.imgIcon = (ImageView) convertView.findViewById(android.R.id.icon);

            convertView.setTag(holder);
        } else {
            holder = (NavigationItemHolder) convertView.getTag();
        }

        NavigationItem childItem = (NavigationItem) getChild(groupPosition, childPosition);

        holder.tvTitle.setText(childItem.title);
        if (childItem.count > 0) {
            holder.tvCount.setVisibility(View.VISIBLE);
            holder.tvCount.setText(String.valueOf(childItem.count));
        } else {
            holder.tvCount.setVisibility(View.INVISIBLE);
        }

        if (childPosition != mChildren[groupPosition].length - 1) { //last child
            switch (groupPosition) {
                case DATES_POSITION:
                    try {
                        Class res = R.drawable.class;
                        Field field = res.getField("calendar_" + childItem.day);
                        int drawableId = field.getInt(null);
                        holder.imgIcon.setImageResource(drawableId);
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        holder.imgIcon.setImageResource(R.drawable.calendar);
                    }
                    break;
                case PLACES_POSITION:
                    holder.imgIcon.setImageResource(R.drawable.marker);
                    break;
                case PEOPLE_POSITION:
                    holder.imgIcon.setImageDrawable(childItem.contactPhoto);
                    break;
                case FOLDERS_POSITION:
                    holder.imgIcon.setImageResource(R.drawable.open_folder);
                    break;
                case TAGS_POSITION:
                    holder.imgIcon.setImageResource(R.drawable.label_white);
                    break;
                default:
                    holder.imgIcon.setImageDrawable(null);
                    break;
            }
        } else {
            holder.imgIcon.setImageDrawable(null);
        }

        return convertView;
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        if (groupPosition == HOME_SCREEN_POSITION || groupPosition == SETTINGS_POSITION) return 0;
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
        } else {
            loader.setUri(SPOCContentProvider.IMAGES_URI.buildUpon().appendPath(Label.TABLE_NAME).appendPath("count").build());
            loader.setProjection(new String[]{"count(_id)", Label.COLUMN_NAME, Label2Image.COLUMN_LABEL_ID});
            loader.setSelection(Label.COLUMN_TYPE + " = ?");

            if (id == PLACES_LOADER_ID) {
                /*
                SELECT count(image_id), label_id, name
                FROM images_with_labels
                WHERE type = 'LOCATION_LOCALITY'
                GROUP BY label_id
                ORDER BY date_taken DESC
                 */
                // content://authority/images/location/count

                loader.setSelectionArgs(new String[]{LabelType.LOCATION_LOCALITY.name()});
                return loader;
            } else if (id == PEOPLE_LOADER_ID) {
                loader.setSelectionArgs(new String[]{LabelType.CONTACT.name()});
                return loader;
            } else if (id == FOLDERS_LOADER_ID) {
                loader.setSelectionArgs(new String[]{LabelType.FOLDER.name()});
                return loader;
            } else if (id == TAGS_LOADER_ID) {
                loader.setSelectionArgs(new String[]{LabelType.CUSTOM.name()});
                return loader;
            }
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == DATE_LOADER_ID) {
            int size = Math.min(3, data.getCount());

            mChildren[DATES_POSITION] = new NavigationItem[size + 1];

            for (int i = 0; i < size; i++) {
                if (data.moveToPosition(i)) {
                    mChildren[DATES_POSITION][i] = new NavigationItem();
                    mChildren[DATES_POSITION][i].count = data.getInt(0);

                    final long dayLong = data.getLong(1);
                    final Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date(dayLong));
                    mChildren[DATES_POSITION][i].title = dateFormat.format(calendar.getTime());
                    mChildren[DATES_POSITION][i].day = calendar.get(Calendar.DAY_OF_MONTH);
                }
            }
            mChildren[DATES_POSITION][size] = new NavigationItem(mContext.getString(R.string.navigation_date_older));
        }

        if (loader.getId() == PLACES_LOADER_ID) {
            int size = Math.min(3, data.getCount());

            mChildren[PLACES_POSITION] = new NavigationItem[size + 1];

            for (int i = 0; i < size; i++) {
                if (data.moveToPosition(i)) {
                    mChildren[PLACES_POSITION][i] = new NavigationItem();
                    mChildren[PLACES_POSITION][i].count = data.getInt(0);
                    mChildren[PLACES_POSITION][i].title = data.getString(1);
                    mChildren[PLACES_POSITION][i].id = data.getInt(2);
                }
            }
            mChildren[PLACES_POSITION][size] = new NavigationItem(mContext.getString(R.string.navigation_places_other));
        }

        if (loader.getId() == PEOPLE_LOADER_ID) {
            int size = Math.min(3, data.getCount());

            mChildren[PEOPLE_POSITION] = new NavigationItem[size + 1];

            for (int i = 0; i < size; i++) {
                if (data.moveToPosition(i)) {
                    mChildren[PEOPLE_POSITION][i] = new NavigationItem();
                    mChildren[PEOPLE_POSITION][i].count = data.getInt(0);
                    mChildren[PEOPLE_POSITION][i].title = data.getString(1);
                    mChildren[PEOPLE_POSITION][i].id = data.getInt(2);

                    final int finalI = i;
                    new ContactPhotoLoader(mContext, mChildren[PEOPLE_POSITION][i].id) {
                        @Override
                        protected void onPostExecute(Drawable drawable) {
                            mChildren[PEOPLE_POSITION][finalI].contactPhoto = drawable;
                        }
                    }.execute();
                }
            }
            mChildren[PEOPLE_POSITION][size] = new NavigationItem(mContext.getString(R.string.navigation_people_other));
        }

        if (loader.getId() == FOLDERS_LOADER_ID) {
            int size = Math.min(3, data.getCount());

            mChildren[FOLDERS_POSITION] = new NavigationItem[size + 1];

            for (int i = 0; i < size; i++) {
                if (data.moveToPosition(i)) {
                    mChildren[FOLDERS_POSITION][i] = new NavigationItem();
                    mChildren[FOLDERS_POSITION][i].count = data.getInt(0);
                    mChildren[FOLDERS_POSITION][i].title = data.getString(1);
                    mChildren[FOLDERS_POSITION][i].id = data.getInt(2);
                }
            }
            mChildren[FOLDERS_POSITION][size] = new NavigationItem(mContext.getString(R.string.navigation_folder_other));
        }

        if (loader.getId() == TAGS_LOADER_ID) {
            int size = Math.min(3, data.getCount());

            mChildren[TAGS_POSITION] = new NavigationItem[size + 1];

            for (int i = 0; i < size; i++) {
                if (data.moveToPosition(i)) {
                    mChildren[TAGS_POSITION][i] = new NavigationItem();
                    mChildren[TAGS_POSITION][i].count = data.getInt(0);
                    mChildren[TAGS_POSITION][i].title = data.getString(1);
                    mChildren[TAGS_POSITION][i].id = data.getInt(2);
                }
            }
            mChildren[TAGS_POSITION][size] = new NavigationItem(mContext.getString(R.string.navigation_tags_other));
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
        int id = 0;
        CharSequence title = "Navigation Item";
        Drawable icon = null;
        int count = 0;
        int day = 0;
        boolean isExpandable = false;
        boolean isExpanded = false;
        Drawable contactPhoto;

        public NavigationItem() {
        }

        public NavigationItem(String title) {
            this.title = title;
        }
    }
}
