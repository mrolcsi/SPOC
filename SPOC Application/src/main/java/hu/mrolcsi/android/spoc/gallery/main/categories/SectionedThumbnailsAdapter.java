package hu.mrolcsi.android.spoc.gallery.main.categories;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.CursorRecyclerViewAdapter;
import hu.mrolcsi.android.spoc.gallery.main.ThumbnailsAdapter;
import org.lucasr.twowayview.widget.SpannableGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class SectionedThumbnailsAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {

    public static final String HEADER_COLUMN_NAME = "header";
    public static final String EXTRA_COLUMN_NAME = "extra";
    public static final int VIEW_TYPE_CONTENT = 0x00;
    public static final int VIEW_TYPE_HEADER = 0x01;

    private final CategoriesFragment.CategoryHeaderLoader mHeaderLoader;
    private final int mPreferredColumns;

    private int mHeaderIndex = -1;
    private int mExtraIndex = -1;

    private List<ItemInfo> mItems = new ArrayList<>();
    private CursorRecyclerViewAdapter mCursorAdapter;

    public SectionedThumbnailsAdapter(Context context, Cursor cursor, CategoriesFragment.CategoryHeaderLoader iconLoader) {
        super(context, cursor);

        mHeaderLoader = iconLoader;

        if (cursor != null) {
            buildItemInfo(cursor);
        }

        mPreferredColumns = context.getResources().getInteger(R.integer.preferredColumns);

        mCursorAdapter = new ThumbnailsAdapter(context);
        ((ThumbnailsAdapter) mCursorAdapter).setUseColumnSpan(false);
    }

    protected void buildItemInfo(Cursor cursor) {
        mItems.clear();
        mHeaderIndex = cursor.getColumnIndex(HEADER_COLUMN_NAME);
        mExtraIndex = cursor.getColumnIndex(EXTRA_COLUMN_NAME);
        String lastHeader;

        if (cursor.moveToFirst()) {
            //first item always has a header
            lastHeader = cursor.getString(mHeaderIndex);
            mItems.add(new ItemInfo(cursor.getPosition(), true, lastHeader));   //header
            mItems.add(new ItemInfo(cursor.getPosition(), false));  //content

            String currentHeader;

            while (cursor.moveToNext()) {
                currentHeader = cursor.getString(mHeaderIndex);
                //if item in cursor requires new header?
                if (!TextUtils.equals(currentHeader, lastHeader)) {
                    // add header item
                    mItems.add(new ItemInfo(cursor.getPosition(), true, currentHeader));
                    lastHeader = currentHeader;
                }
                // add content item (for every item)
                mItems.add(new ItemInfo(cursor.getPosition(), false));
            }
        }
        Log.d("", "");
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            //create header view
            final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_thumbnails_header, viewGroup, false);
            return new HeaderViewHolder(view);
        } else {
            return mCursorAdapter.onCreateViewHolder(viewGroup, 0x00); //ensure the underlying adapter uses the default viewType
        }
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ItemInfo itemInfo = mItems.get(position);

        if (holder instanceof HeaderViewHolder) {
            final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

            getCursor().moveToPosition(itemInfo.cursorPosition);
            String headerText = getCursor().getString(mHeaderIndex);
            String extraText = null;
            if (mExtraIndex != -1) {
                extraText = getCursor().getString(mExtraIndex);
            }

            mHeaderLoader.loadIcon(headerViewHolder.icon, headerText, extraText);
            mHeaderLoader.loadText(headerViewHolder.text, headerText, extraText);
        } else {
            mCursorAdapter.onBindViewHolder(holder, itemInfo.cursorPosition);
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (holder instanceof HeaderViewHolder) {
            final View item = holder.itemView;
            final ViewGroup.LayoutParams layoutParams = item.getLayoutParams();
            if (item.getHeight() > 0) {
                layoutParams.height = item.getHeight();
            } else {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            item.setLayoutParams(layoutParams);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        return mCursorAdapter.getItemId(mItems.get(position).cursorPosition);
    }

    @Override
    public Cursor getCursor() {
        return mCursorAdapter.getCursor();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        mCursorAdapter.onBindViewHolder(viewHolder, cursor);
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).isHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
    }

    public void changeCursor(Cursor cursor) {
        if (cursor == null) {
            mItems.clear();
            mHeaderIndex = -1;
        } else if (cursor != getCursor()) {
            buildItemInfo(cursor);
        }
        mCursorAdapter.changeCursor(cursor);
        notifyDataSetChanged();
    }

    public int getCursorPosition(int adapterPosition) {
        return mItems.get(adapterPosition).cursorPosition;
    }

    public int getHeaderPosition(String headerText) {
        int i = 0;
        while (i < mItems.size() && (!mItems.get(i).isHeader || mItems.get(i).headerText == null || !(mItems.get(i).headerText.contains(headerText)))) {
            i++;
        }
        if (i == mItems.size()) {
            return -1;
        } else {
            return i;
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView icon;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            final SpannableGridLayoutManager.LayoutParams lp = (SpannableGridLayoutManager.LayoutParams) itemView.getLayoutParams();
            lp.colSpan = mPreferredColumns;
            //lp.height = ViewGroup.LayoutParams.WRAP_CONTENT; //set to fixed 64dp? ?attr/actionBarSize
            itemView.setLayoutParams(lp);

            text = (TextView) itemView.findViewById(R.id.tvHeader);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }
    }

    class ItemInfo {
        boolean isHeader;
        int cursorPosition;
        String headerText = "";

        public ItemInfo(int cursorPosition, boolean isHeader) {
            this.cursorPosition = cursorPosition;
            this.isHeader = isHeader;
        }

        public ItemInfo(int cursorPosition, boolean isHeader, String headerText) {
            this(cursorPosition, isHeader);
            this.headerText = headerText;
        }
    }
}