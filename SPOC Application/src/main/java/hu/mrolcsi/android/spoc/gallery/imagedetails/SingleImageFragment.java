package hu.mrolcsi.android.spoc.gallery.imagedetails;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.*;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.signature.StringSignature;
import hu.mrolcsi.android.spoc.common.fragment.SPOCFragment;
import hu.mrolcsi.android.spoc.common.helper.FaceDetectorTask;
import hu.mrolcsi.android.spoc.common.utils.FileUtils;
import hu.mrolcsi.android.spoc.gallery.BuildConfig;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.utils.DialogUtils;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.13.
 * Time: 20:19
 */

public class SingleImageFragment extends SPOCFragment {

    public static final String ARG_IMAGE_ID = "SPOC.Gallery.Details.ImageId";
    public static final String ARG_IMAGE_PATH = "SPOC.Gallery.Details.ImagePath";
    public static final String ARG_IMAGE_LOCATION = "SPOC.Gallery.Details.Location";

    private PhotoView photoView;

    private int mDesiredWidth;
    private int mDesiredHeight;

    private String mImagePath;

    private BitmapDrawable mOverlayDrawable;
    private BitmapDrawable mBitmapDrawable;
    private FaceDetectorTask mDetector;
    private List<RectF> mFacePositions;
    private BitmapImageViewTarget mBitmapTarget;
    private Bitmap mOverlayBitmap;

    public static SingleImageFragment newInstance(long imageId, String imagePath, String location) {
        final SingleImageFragment f = new SingleImageFragment();

        final Bundle args = new Bundle();
        args.putLong(ARG_IMAGE_ID, imageId);
        args.putString(ARG_IMAGE_PATH, imagePath);
        args.putString(ARG_IMAGE_LOCATION, location);
        f.setArguments(args);

        return f;
    }

    @TargetApi(13)
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            display.getSize(size);
            mDesiredWidth = size.x;
            mDesiredHeight = size.y;
        } else {
            //noinspection deprecation
            mDesiredWidth = display.getWidth();
            //noinspection deprecation
            mDesiredHeight = display.getHeight();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_singleimage, container, false);

            /*
            problem:
                java.lang.ArrayIndexOutOfBoundsException: length=1; index=1
                at android.support.v4.widget.ViewDragHelper.shouldInterceptTouchEvent(ViewDragHelper.java:1014)
                at android.support.v4.widget.DrawerLayout.onInterceptTouchEvent(DrawerLayout.java:1140)...
                when pinch-zooming

            solution:
                http://www.arthurwang.net/android/arrayindexoutofboundsexception-with-photoview-library-and-drawerlayout
             */
            photoView = (PhotoView) mRootView.findViewById(R.id.image);
            photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float v, float v1) {
                    toggleFullScreen();
                    if (isFullscreen()) {
                        mOverlayDrawable.setAlpha(0);
                        photoView.setImageDrawable(new LayerDrawable(new Drawable[]{mBitmapDrawable, mOverlayDrawable}));
                    } else {
                        mOverlayDrawable.setAlpha(200);
                        photoView.setImageDrawable(new LayerDrawable(new Drawable[]{mBitmapDrawable, mOverlayDrawable}));
                    }
                }
            });
        }
        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mImagePath = getArguments().getString(ARG_IMAGE_PATH);
        //GlideHelper.loadBigImage(this, mImagePath, mDesiredWidth, mDesiredHeight, photoView);

        loadImage();
    }

    private void loadImage() {

        if (mBitmapTarget != null) {
            Glide.clear(mBitmapTarget);
        }

        mBitmapTarget = new BitmapImageViewTarget(photoView) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                if (mFacePositions == null) {
                    mDetector = new FaceDetectorTask() {
                        @Override
                        protected void onPostExecute(List<RectF> rectFs) {
                            mFacePositions = rectFs;
                            drawFaces();
                        }
                    };
                    mDetector.execute(resource);
                }

                super.onResourceReady(resource, glideAnimation);
            }

            @Override
            protected void setResource(Bitmap resource) {
                mBitmapDrawable = new BitmapDrawable(getResources(), resource);
                mOverlayBitmap = Bitmap.createBitmap(resource.getWidth(), resource.getHeight(), Bitmap.Config.ARGB_8888);
                mOverlayDrawable = new BitmapDrawable(getResources(), mOverlayBitmap);

                LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{mBitmapDrawable, mOverlayDrawable});
                photoView.setImageDrawable(layerDrawable);
            }
        };

        Glide.with(this)
                .fromString()
                .asBitmap()
                .fitCenter()
                .override(mDesiredWidth, mDesiredHeight)
                .error(hu.mrolcsi.android.spoc.common.R.drawable.error)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .signature(new StringSignature(mImagePath + "_big"))
                .load(mImagePath)
                .into(mBitmapTarget);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menuShare);

        // Fetch and store ShareActionProvider
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        shareActionProvider.setShareIntent(FileUtils.createShareIntent(mImagePath));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.menuDetails:

                final Bundle args = new Bundle();
                args.putString(ARG_IMAGE_PATH, mImagePath);

                final ImageDetailsDialog dialog = new ImageDetailsDialog();
                dialog.setArguments(args);
                dialog.show(getChildFragmentManager(), ImageDetailsDialog.TAG);

                return true;
            case R.id.menuDelete:
                final AlertDialog.Builder builder = DialogUtils.buildConfirmDialog(getActivity());
                builder.setMessage(R.string.dialog_message_deletePicture)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    final boolean success = FileUtils.deleteFile(mImagePath);
                                    if (success)
                                        Toast.makeText(getActivity(), R.string.message_pictureDeleted, Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(getActivity(), R.string.message_pictureNotDeleted, Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    String message;
                                    final AlertDialog.Builder errorBuilder = DialogUtils.buildErrorDialog(getActivity());
                                    if (e instanceof FileNotFoundException) {
                                        message = getString(R.string.message_pictureNotDeleted_fileNotExist);
                                        if (BuildConfig.DEBUG) message += "\n" + e.toString();
                                    } else {
                                        message = getString(R.string.message_pictureNotDeleted);
                                        if (BuildConfig.DEBUG) message += "\n" + e.toString();
                                    }
                                    errorBuilder.setMessage(message);
                                    errorBuilder.show();
                                }
                            }
                        }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Glide.clear(photoView);
        Glide.clear(mBitmapTarget);
        Glide.get(getActivity()).clearMemory();

        if (mDetector != null) {
            mDetector.cancel(true);
        }

        if (mOverlayBitmap != null) {
            mOverlayBitmap.recycle();
            mOverlayBitmap = null;

            mOverlayDrawable = null;
        }
    }

    private void drawFaces() {

        if (mFacePositions == null || mFacePositions.isEmpty()) {
            return;
        }

        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.background_material_light));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.margin_xsmall));
        paint.setStyle(Paint.Style.STROKE);

        final int cornerRadius = getResources().getDimensionPixelSize(R.dimen.margin_small);

        final Canvas canvas = new Canvas(mOverlayDrawable.getBitmap());

        for (RectF rect : mFacePositions) {
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
        }

        photoView.invalidate();
    }
}
