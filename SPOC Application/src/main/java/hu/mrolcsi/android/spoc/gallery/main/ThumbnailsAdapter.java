package hu.mrolcsi.android.spoc.gallery.main;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class ThumbnailsAdapter extends CursorRecyclerViewAdapter<ThumbnailsAdapter.ImageViewHolder> {

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
    public ImageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.gallery_thumbnail, viewGroup, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onViewDetachedFromWindow(ImageViewHolder holder) {
        holder.img.clearAnimation();
    }

    @Override
    public void onBindViewHolder(ImageViewHolder viewHolder, int position) {
        if (getCursor() == null || getCursor().isClosed()) {
            return;
        }
        super.onBindViewHolder(viewHolder, position);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, Cursor cursor) {
        if (cursor == null || !isDataValid()) {
            return;
        }

        iData = cursor.getColumnIndex(Image.COLUMN_FILENAME);

        String filename = cursor.getString(iData);

        SpannableGridLayoutManager.LayoutParams lp = (SpannableGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        if (mUseColumnSpan && cursor.getPosition() % 11 == 0) { //expand frequently used items (or just leave it static like this?)
            lp.colSpan = columnSpan;
            lp.rowSpan = columnSpan;
        } else {
            lp.colSpan = 1;
            lp.rowSpan = 1;
        }
        holder.itemView.setLayoutParams(lp);

        GlideHelper.loadThumbnail(context.getApplicationContext(), filename, mThumbnailSize, holder.img);
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

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;

        public ImageViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.img);
        }
    }
}
