package hu.mrolcsi.android.spoc.gallery.main;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import hu.mrolcsi.android.spoc.common.GlideHelper;
import hu.mrolcsi.android.spoc.database.models.Image;
import hu.mrolcsi.android.spoc.gallery.R;
import org.lucasr.twowayview.widget.SpannableGridLayoutManager;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:48
 */

public class ThumbnailsAdapter extends RecyclerView.Adapter<ThumbnailsAdapter.ImageViewHolder> {

    private final int columnSpan;
    private final Context context;
    private final int mThumbnailSize;

    private int iData;

    private Cursor cursor;
    private int mCount = 0;

    public ThumbnailsAdapter(Context context) {
        this.context = context;
        int preferredColumns = context.getResources().getInteger(R.integer.preferredColumns);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnSpan = (int) Math.round((preferredColumns + 0.5) / 2);
        } else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columnSpan = (int) Math.round((preferredColumns + 0.5) / 3);
        } else columnSpan = 1;

        mThumbnailSize = context.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
    }

    public ThumbnailsAdapter(Context context, Cursor cursor) {
        this(context);
        this.cursor = cursor;

        iData = cursor.getColumnIndex(Image.COLUMN_FILENAME);
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
    public void onBindViewHolder(ImageViewHolder holder, int i) {
        if (cursor == null || cursor.isClosed()) return;

        cursor.moveToPosition(i);

        String filename = cursor.getString(iData);
        SpannableGridLayoutManager.LayoutParams lp = (SpannableGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();

        if (i % 12 == 0) { //TODO: expand frequently used items (or just leave it static like this?)
            lp.colSpan = columnSpan;
            lp.rowSpan = columnSpan;
            //holder.itemView.setBackgroundColor(Color.BLUE);
        } else {
            lp.colSpan = 1;
            lp.rowSpan = 1;
            //holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setLayoutParams(lp);

        GlideHelper.loadThumbnail(context, filename, mThumbnailSize, holder.img);
    }

    @Override
    public int getItemCount() {
        if (cursor != null && !cursor.isClosed())
            mCount = cursor.getCount();
        return mCount;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;

        public ImageViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.img);
        }
    }
}
