package hu.mrolcsi.android.spoc.gallery.main.categories;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.CursorRecyclerViewAdapter;
import hu.mrolcsi.android.spoc.gallery.main.ThumbnailsAdapter;
import org.lucasr.twowayview.widget.SpannableGridLayoutManager;

public class SectionedThumbnailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_HEADER = 0x00;
    public static final int VIEW_TYPE_CONTENT = 0x01;
    private final Context context;
    private final int mPreferredColumns;

    private SparseArray<String> mHeaders = new SparseArray<>();
    private CursorRecyclerViewAdapter mCursorAdapter;

    private int mHeaderIndex = -1;

    public SectionedThumbnailsAdapter(Context context, Cursor cursor) {
        this.context = context;
        if (cursor != null) {
            buildHeaders(cursor);
        }

        mPreferredColumns = context.getResources().getInteger(R.integer.preferredColumns);

        mCursorAdapter = new ThumbnailsAdapter(context);
        ((ThumbnailsAdapter) mCursorAdapter).setUseColumnSpan(false);
    }

    protected void buildHeaders(Cursor cursor) {
        mHeaders.clear();

        if (cursor.moveToFirst()) {
            String lastHeader = cursor.getString(2);
            mHeaders.put(0, lastHeader);
            while (cursor.moveToNext()) {
                final String headerString = cursor.getString(2);
                if (!TextUtils.equals(headerString, lastHeader)) {
                    lastHeader = headerString;
                    mHeaders.put(cursor.getPosition() + mHeaders.size(), headerString);
                }
            }
        }
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            //create header view
            final View view = LayoutInflater.from(context).inflate(R.layout.fragment_thumbnails_header, viewGroup, false);
            return new HeaderViewHolder(view);
        } else {
            //create contentView
            return mCursorAdapter.onCreateViewHolder(viewGroup, viewType);
        }
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            if (TextUtils.equals(mHeaders.get(position), Character.toString((char) 126))) {
                headerViewHolder.text.setText(Html.fromHtml(context.getString(R.string.details_message_unknownLocation)));
            } else {
                headerViewHolder.text.setText(mHeaders.get(position));
            }
            mHeaderIndex = mHeaders.indexOfKey(position);
        } else {
            final int cursorPosition = getCursorPosition(position);
            if (cursorPosition >= 0) {
                //noinspection unchecked
                mCursorAdapter.onBindViewHolder(holder, cursorPosition);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getItemCount() + mHeaders.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) { //first item is always a header;
            return VIEW_TYPE_HEADER;
        } else {
            if (mHeaders.get(position) != null) {
                return VIEW_TYPE_HEADER;
            }
        }
        return VIEW_TYPE_CONTENT;
    }

    private int getCursorPosition(int adapterPosition) {
        return adapterPosition - (mHeaderIndex + 1);
    }

    public Context getContext() {
        return context;
    }

    public void changeCursor(Cursor cursor) {
        if (cursor == null) {
            mHeaders.clear();
        } else if (cursor != mCursorAdapter.getCursor()) {
            buildHeaders(cursor);
        }
        mCursorAdapter.changeCursor(cursor);
        notifyDataSetChanged();
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            final SpannableGridLayoutManager.LayoutParams lp = (SpannableGridLayoutManager.LayoutParams) itemView.getLayoutParams();
            lp.colSpan = mPreferredColumns;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            itemView.setLayoutParams(lp);

            text = (TextView) itemView.findViewById(R.id.tvHeader);
        }
    }
}