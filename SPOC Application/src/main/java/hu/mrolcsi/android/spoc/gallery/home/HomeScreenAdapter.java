package hu.mrolcsi.android.spoc.gallery.home;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import hu.mrolcsi.android.spoc.gallery.R;
import org.lucasr.twowayview.widget.SpannableGridLayoutManager;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:48
 */

public class HomeScreenAdapter extends RecyclerView.Adapter<HomeScreenAdapter.ImageViewHolder> {

    private final int columnSpan;
    private final Context context;
    private final int preferredColumns;
    private final int mThumbnailSize;

    private String filename;
    private int iData;

    private Cursor cursor;
    private int mCount = 0;

    public HomeScreenAdapter(Context context) {
        this.context = context;
        preferredColumns = context.getResources().getInteger(R.integer.preferredColumns);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnSpan = (int) Math.round((preferredColumns + 0.5) / 2);
        } else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columnSpan = (int) Math.round((preferredColumns + 0.5) / 3);
        } else columnSpan = 1;

        mThumbnailSize = context.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
    }

    public HomeScreenAdapter(Context context, Cursor cursor) {
        this(context);
        this.cursor = cursor;

        iData = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
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

        filename = cursor.getString(iData);
        SpannableGridLayoutManager.LayoutParams lp = (SpannableGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();

        if (i % 7 == 1) { //TODO: expand frequently used items (or just leave it static like this?)
            lp.colSpan = columnSpan;
            lp.rowSpan = columnSpan;
            //holder.itemView.setBackgroundColor(Color.BLUE);
        } else {
            lp.colSpan = 1;
            lp.rowSpan = 1;
            //holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setLayoutParams(lp);

        Glide.with(context)
                .fromMediaStore()
                .load(Uri.parse("file://" + filename))
                .override(mThumbnailSize, mThumbnailSize)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(holder.img);
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
