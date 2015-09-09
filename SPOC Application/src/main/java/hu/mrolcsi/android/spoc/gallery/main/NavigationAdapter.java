package hu.mrolcsi.android.spoc.gallery.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.widgets.AnimatedExpandableListView;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.08.
 * Time: 15:39
 */

public class NavigationAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

    private final Context mContext;
    private final LayoutInflater mInflater;

    private NavigationItem[] mGroups;

    public NavigationAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);

        mGroups = new NavigationItem[getGroupCount()];
        for (int i = 0; i < getGroupCount(); i++) {
            mGroups[i] = createGroupItems(i);
        }
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
    public Object getChild(int i, int i1) {
        // TODO
        return null;
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
        // TODO
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.navigation_child_item, parent, false);
        }
        return convertView;
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        if (groupPosition == 0 || groupPosition == getGroupCount() - 1) return 0;
        return 3;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    class NavigationItemHolder {
        TextView tvTitle;
        ImageView imgIcon;
        ImageView imgGroupIndicator;
    }

    class NavigationItem {
        CharSequence title;
        Drawable icon;
        boolean isExpandable;
        boolean isExpanded = false;
    }
}
