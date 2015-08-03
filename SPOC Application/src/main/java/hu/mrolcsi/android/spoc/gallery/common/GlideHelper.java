package hu.mrolcsi.android.spoc.gallery.common;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;

import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.30.
 * Time: 11:14
 */

public abstract class GlideHelper {

    private static RequestListener<Uri, GlideDrawable> requestListener = new RequestListener<Uri, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
            Log.w(getClass().getSimpleName(), model.toString(), e);
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    };

    public static void cacheThumbnail(Context appContext, String filename, int thumbnailSize) throws ExecutionException, InterruptedException {

        Glide.with(appContext)
                .fromMediaStore()
                .centerCrop()
                .listener(requestListener)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .signature(new StringSignature(filename + "_thumb"))
                .load(Uri.parse("file://" + filename))
                .into(thumbnailSize, thumbnailSize)
                .get();
    }

    public static void loadThumbnail(Context context, String filename, int thumbnailSize, ImageView target) {
        Glide.with(context)
                .fromMediaStore()
                .centerCrop()
                .override(thumbnailSize, thumbnailSize)
                .listener(requestListener)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .signature(new StringSignature(filename + "_thumb"))
                .load(Uri.parse("file://" + filename))
                .into(target);
    }

    public static void cacheBigImage(Context appContext, String filename, int screenWidth, int screenHeight) throws ExecutionException, InterruptedException {
        Glide.with(appContext)
                .fromMediaStore()
                .fitCenter()
                .listener(requestListener)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .signature(new StringSignature(filename + "_big"))
                .load(Uri.parse("file://" + filename))
                .into(screenWidth, screenHeight)
                .get();
    }

    public static void loadBigImage(Fragment fragment, String filename, int width, int height, ImageView target) {
        Glide.with(fragment)
                .fromMediaStore()
                .fitCenter()
                .override(width, height)
                .listener(requestListener)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .signature(new StringSignature(filename + "_big"))
                .load(Uri.parse("file://" + filename))
                .into(target);
    }

}
