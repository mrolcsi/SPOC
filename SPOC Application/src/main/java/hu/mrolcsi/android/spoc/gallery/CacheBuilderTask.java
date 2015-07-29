package hu.mrolcsi.android.spoc.gallery;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import hu.mrolcsi.android.spoc.gallery.common.widgets.ProgressDialogFragment;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.29.
 * Time: 11:48
 */

public class CacheBuilderTask extends AsyncTask<Void, Integer, Void> {

    //TODO: a background service might be better

    private final Context context;
    private final FragmentManager fragmentManager;
    private final Cursor cursor;
    private final int iData;
    private final int mThumbnailSize;

    private ProgressDialogFragment pd;

    public CacheBuilderTask(Context context, FragmentManager supportFragmentManager, Cursor cursor) {
        this.context = context;
        fragmentManager = supportFragmentManager;
        this.cursor = cursor;

        iData = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

        mThumbnailSize = context.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialogFragment();
        pd.setMax(cursor.getCount());
        pd.setIndeterminate(true);
        pd.setCancelable(false);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        if (cursor.isClosed()) return null;

        // TODO: check if an image is already cached
        // > compare with db - if it exists in db, it's in the cache
        String filename;
        while (!cursor.isClosed() && cursor.moveToNext()) {
            filename = cursor.getString(iData);

            //preload images to disk cache
            try {
                FutureTarget<File> future = Glide.with(context)
                        .load(Uri.parse("file://" + filename))
                        .downloadOnly(mThumbnailSize, mThumbnailSize);
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                Log.w(getClass().getName(), e);
            }

            publishProgress(cursor.getPosition());
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        switch (values[0]) {
            case 10:
                pd.show(fragmentManager, ProgressDialogFragment.TAG);
                pd.setMessage("Just a moment...");
                break;
            case 50:
                pd.showHorizontalBar(true);
                pd.setMessage("Preparing your photos.\nThis shouldn't take long...");
                break;
            case 100:
                pd.setMessage("Almost done, hang on...");
                break;
            case 500:
                pd.setMessage("This won't take this long all the time, trust me...");
                break;
            case 1000:
                pd.setMessage("Wow, that is a lot of photos...");
                break;
            default:
                break;
        }
        pd.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        pd.dismiss();
    }
}
