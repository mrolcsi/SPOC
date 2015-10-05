package hu.mrolcsi.android.spoc.gallery.main;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import hu.mrolcsi.android.spoc.common.helper.GlideHelper;
import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.CursorRecyclerViewAdapter;
import org.lucasr.twowayview.widget.SpannableGridLayoutManager;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:48
 */

public class ThumbnailsAdapter extends CursorRecyclerViewAdapter<ThumbnailsAdapter.ThumbnailHolder> {

    public static final int VIEW_TYPE_SMALL = 0x00;
    public static final int VIEW_TYPE_BIG = 0x01;

    private final int columnSpan;
    private final Context context;
    private final int mThumbnailSize;

    private int iData = -1;
    private int mCount = 0;
    private boolean mUseColumnSpan = true;

    public ThumbnailsAdapter(Context context) {
        super(context, null);
        this.context = context;

        int preferredColumns = context.getResources().getInteger(R.integer.preferredColumns);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnSpan = (int) Math.round((preferredColumns + 0.5) / 2);
        } else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columnSpan = (int) Math.round((preferredColumns + 0.5) / 3);
        } else columnSpan = 1;

        mThumbnailSize = context.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
    }

    @Override
    public ThumbnailHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        if (viewType == VIEW_TYPE_BIG) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_thumbnails_item, viewGroup, false);
            SpannableGridLayoutManager.LayoutParams lp = (SpannableGridLayoutManager.LayoutParams) v.getLayoutParams();
            lp.colSpan = columnSpan;
            lp.rowSpan = columnSpan;
            v.setLayoutParams(lp);
        } else {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_thumbnails_item, viewGroup, false);
        }
        return new ThumbnailHolder(v);
    }

    @Override
    public void onViewDetachedFromWindow(ThumbnailHolder holder) {
        holder.img.clearAnimation();
    }

    @Override
    public void onBindViewHolder(ThumbnailHolder viewHolder, int position) {
        if (getCursor() == null || getCursor().isClosed()) {
            return;
        }
        super.onBindViewHolder(viewHolder, position);
    }

    @Override
    public void onBindViewHolder(ThumbnailHolder holder, Cursor cursor) {
        if (cursor == null || !isDataValid()) {
            return;
        }

        if (iData < 0) {
            iData = cursor.getColumnIndex(Image.COLUMN_FILENAME);
        }

        String filename = cursor.getString(iData);

        GlideHelper.loadThumbnail(context.getApplicationContext(), filename, mThumbnailSize, holder.img);
//        if (BuildConfig.DEBUG) {
//            holder.text.setVisibility(View.VISIBLE);
//            holder.text.setText(String.valueOf(cursor.getPosition()));
//        }
    }

    @Override
    public int getItemViewType(int position) {
        if (!mUseColumnSpan) return VIEW_TYPE_SMALL;
        if (position % 11 == 0) {
            return VIEW_TYPE_BIG;
        } else {
            return VIEW_TYPE_SMALL;
        }
    }

    @Override
    public int getItemCount() {
        if (getCursor() != null && !getCursor().isClosed())
            mCount = getCursor().getCount();
        return mCount;
    }

    public boolean useColumnSpan() {
        return mUseColumnSpan;
    }

    public void setUseColumnSpan(boolean useColumnSpan) {
        this.mUseColumnSpan = useColumnSpan;
    }

    class ThumbnailHolder extends RecyclerView.ViewHolder {
        private ImageView img;
        private TextView text;

        public ThumbnailHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.img);
            text = (TextView) itemView.findViewById(R.id.text);
        }
    }
}
