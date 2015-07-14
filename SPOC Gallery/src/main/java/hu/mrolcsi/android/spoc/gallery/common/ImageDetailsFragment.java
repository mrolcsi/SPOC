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

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.13.
 * Time: 20:19
 */

public class ImageDetailsFragment extends SPOCFragment {

    public static final String ARG_IMAGE_PATH = "SPOC.Gallery.Details.ImagePath";
    private ImageView imageView;

    private int mDesiredWidth;
    private int mDesiredHeight;

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

        imageView = (ImageView) view.findViewById(R.id.image);

        final ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
        if (viewTreeObserver != null && viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < 16) {
                        //noinspection deprecation
                        viewTreeObserver.removeGlobalOnLayoutListener(this);
                    } else
                        viewTreeObserver.removeOnGlobalLayoutListener(this);

                    mDesiredWidth = imageView.getWidth();
                    mDesiredHeight = imageView.getHeight();

                    if (getArguments() != null && getArguments().containsKey(ARG_IMAGE_PATH)) {
                        final String imagePath = getArguments().getString(ARG_IMAGE_PATH);

                        Picasso.with(getActivity()).load("file://" + imagePath).resize(mDesiredWidth, mDesiredHeight).centerInside().into(imageView);
                    }
                }
            });
        }
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        if (getArguments() != null && getArguments().containsKey(ARG_IMAGE_PATH)) {
//            final String imagePath = getArguments().getString(ARG_IMAGE_PATH);
//
//            Picasso.with(getActivity()).load("file://" + imagePath).resize(mDesiredWidth, mDesiredHeight).centerInside().into(imageView);
//        }
//    }


    @Override
    public void onDetach() {
        super.onDetach();

        Picasso.with(getActivity()).cancelRequest(imageView);
    }
}
