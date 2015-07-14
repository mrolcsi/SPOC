package hu.mrolcsi.android.spoc.gallery.common;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import hu.mrolcsi.android.spoc.common.SPOCFragment;
import hu.mrolcsi.android.spoc.gallery.R;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.13.
 * Time: 20:19
 */

public class ImageDetailsFragment extends SPOCFragment {

    public static final String ARG_IMAGE_PATH = "SPOC.Gallery.Details.ImagePath";
    private ImageView photoView;

    private int mDesiredWidth;
    private int mDesiredHeight;

    private PhotoViewAttacher mAttacher;

    public ImageDetailsFragment() {
    }

    public static ImageDetailsFragment newInstance(String imagePath) {
        final ImageDetailsFragment f = new ImageDetailsFragment();

        final Bundle args = new Bundle();
        args.putString(ARG_IMAGE_PATH, imagePath);
        f.setArguments(args);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_imagedetails, container, false);
    }

    @Override
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
                        final String imagePath = getArguments().getString(ARG_IMAGE_PATH);

                        Picasso.with(getActivity()).load("file://" + imagePath).resize(mDesiredWidth, mDesiredHeight).centerInside().onlyScaleDown().into(photoView);
                    }
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Picasso.with(getActivity()).cancelRequest(photoView);
    }
}
