package hu.mrolcsi.android.spoc.gallery.home;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
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

    private String filename;
    private int iData;

    private Cursor cursor;

    public HomeScreenAdapter(Context context) {
        this.context = context;
        int preferredColumns = context.getResources().getInteger(R.integer.preferredColumns);
        columnSpan = (int) Math.round((preferredColumns + 0.5) / 2);
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
    public void onBindViewHolder(ImageViewHolder imageViewHolder, int i) {

        cursor.moveToPosition(i);

        filename = cursor.getString(iData);

        //load image
        Picasso.with(context).cancelRequest(imageViewHolder.img);
        Picasso.with(context).load("file://" + filename).centerCrop().resizeDimen(R.dimen.image_thumbnail_size, R.dimen.image_thumbnail_size).into(imageViewHolder.img);

        SpannableGridLayoutManager.LayoutParams lp = (SpannableGridLayoutManager.LayoutParams) imageViewHolder.itemView.getLayoutParams();

        if (i % 12 == 0) { //TODO: expand frequently used items
            lp.colSpan = columnSpan;
            lp.rowSpan = columnSpan;
            //imageViewHolder.itemView.setBackgroundColor(Color.BLUE);
        } else {
            lp.colSpan = 1;
            lp.rowSpan = 1;
            //imageViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        imageViewHolder.itemView.setLayoutParams(lp);
    }

    @Override
    public int getItemCount() {
        if (cursor != null)
            return cursor.getCount();
        else return 0;
    }

    public Cursor getCursor() {
        return cursor;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;

        public ImageViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.img);
        }
    }
}
