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
    public static final int LABELS_LOADER_ID = 23;
    public static final int FOLDERS_LOADER_ID = 24;

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
        void loadIcon(String s, ImageView view);

        void loadText(String s, TextView view);
    }
}
