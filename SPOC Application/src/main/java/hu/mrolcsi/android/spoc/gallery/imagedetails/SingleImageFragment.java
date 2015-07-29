package hu.mrolcsi.android.spoc.gallery.imagedetails;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.*;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import hu.mrolcsi.android.spoc.common.BuildConfig;
import hu.mrolcsi.android.spoc.common.fragment.SPOCFragment;
import hu.mrolcsi.android.spoc.common.utils.FileUtils;
import hu.mrolcsi.android.spoc.gallery.R;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.13.
 * Time: 20:19
 */

public class SingleImageFragment extends SPOCFragment {

    public static final String ARG_IMAGE_PATH = "SPOC.Gallery.Details.ImagePath";
    private PhotoView photoView;

    private int mDesiredWidth;
    private int mDesiredHeight;

    private String mImagePath;
    private boolean isLoaded = false;
    private ShareActionProvider mShareActionProvider;

    public static SingleImageFragment newInstance(String imagePath) {
        final SingleImageFragment f = new SingleImageFragment();

        final Bundle args = new Bundle();
        args.putString(ARG_IMAGE_PATH, imagePath);
        f.setArguments(args);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_singleimage, container, false);
        return mRootView;
    }

    @Override
    @TargetApi(16)
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*
        problem:
            java.lang.ArrayIndexOutOfBoundsException: length=1; index=1
            at android.support.v4.widget.ViewDragHelper.shouldInterceptTouchEvent(ViewDragHelper.java:1014)
            at android.support.v4.widget.DrawerLayout.onInterceptTouchEvent(DrawerLayout.java:1140)...
            when pinch-zooming

        solution:
            http://www.arthurwang.net/android/arrayindexoutofboundsexception-with-photoview-library-and-drawerlayout
         */
        photoView = (PhotoView) view.findViewById(R.id.image);
        photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float v, float v1) {
                toggleFullScreen();
            }
        });

        final ViewTreeObserver viewTreeObserver = photoView.getViewTreeObserver();
        if (viewTreeObserver != null && viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < 16) {
                        //noinspection deprecation
                        viewTreeObserver.removeGlobalOnLayoutListener(this);
                    } else
                        viewTreeObserver.removeOnGlobalLayoutListener(this);

                    mDesiredWidth = photoView.getWidth();
                    mDesiredHeight = photoView.getHeight();

                    if (getArguments() != null && getArguments().containsKey(ARG_IMAGE_PATH)) {
                        mImagePath = getArguments().getString(ARG_IMAGE_PATH);

                        if (isAdded() && !isLoaded) {
                            Glide.with(SingleImageFragment.this).load("file://" + mImagePath).override(mDesiredWidth, mDesiredHeight).fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL).into(photoView);
                            isLoaded = true;
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!isLoaded && mDesiredWidth > 0 && mDesiredHeight > 0) {
            Glide.with(this).load("file://" + mImagePath).override(mDesiredWidth, mDesiredHeight).fitCenter().diskCacheStrategy(DiskCacheStrategy.RESULT).into(photoView);
            isLoaded = true;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menuShare);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(createShareIntent());
    }

    // Create and return the Share Intent
    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg"); //TODO: get actual metadata
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + mImagePath));
        return shareIntent;
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
                final AlertDialog.Builder questionBuilder = new AlertDialog.Builder(getActivity());
                questionBuilder.setIcon(R.drawable.help)
                        .setTitle(getString(R.string.dialog_title_areYouSure))
                        .setMessage(getString(R.string.dialog_delete_message))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    final boolean success = FileUtils.deleteFile(mImagePath);
                                    if (success)
                                        Toast.makeText(getActivity(), "Picture deleted.", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(getActivity(), "Picture not deleted.", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    String message;
                                    final AlertDialog.Builder errorBuilder = new AlertDialog.Builder(getActivity());
                                    errorBuilder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });
                                    if (e instanceof FileNotFoundException) {
                                        message = "Picture could not be deleted. File does not exist anymore.";
                                        if (BuildConfig.DEBUG) message += "\n" + e.toString();
                                    } else {
                                        message = "Picture could not be deleted.";
                                        if (BuildConfig.DEBUG) message += "\n" + e.toString();
                                    }
                                    errorBuilder.setMessage(message);
                                    errorBuilder.show();
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Glide.clear(photoView);
    }
}
