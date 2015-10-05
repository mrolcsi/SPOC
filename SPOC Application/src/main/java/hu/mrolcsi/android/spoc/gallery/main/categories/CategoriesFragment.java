package hu.mrolcsi.android.spoc.gallery.main.categories;

import android.widget.ImageView;
import android.widget.TextView;

import hu.mrolcsi.android.spoc.gallery.main.GalleryActivity;
import hu.mrolcsi.android.spoc.gallery.main.ThumbnailsFragment;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.21.
 * Time: 15:35
 */

public abstract class CategoriesFragment extends ThumbnailsFragment {

    public static final String ARG_SELECTED_CATEGORY = "category";

    @Override
    protected void setupImagesAdapter() {
        mAdapter = new SectionedThumbnailsAdapter(getActivity(), null, setupCategoryLoader());
        twList.setAdapter(mAdapter);
    }

    @Override
    protected int getClickedImagePosition(int adapterPosition) {
        return ((SectionedThumbnailsAdapter) mAdapter).getCursorPosition(adapterPosition);
    }

    @Override
    public boolean onBackPressed() {
        ((GalleryActivity) getActivity()).restoreFragmentFromStack();
        return true;
    }

    protected abstract CategoryHeaderLoader setupCategoryLoader();

    public interface CategoryHeaderLoader {
        void loadIcon(ImageView view, String headerText, String extra);

        void loadText(TextView view, String headerText, String extra);
    }
}
