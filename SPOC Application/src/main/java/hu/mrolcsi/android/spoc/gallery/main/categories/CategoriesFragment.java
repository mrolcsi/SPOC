package hu.mrolcsi.android.spoc.gallery.main.categories;

import android.database.Cursor;
import android.support.v4.content.Loader;
import android.text.TextUtils;
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

    private boolean scrollHappened = false;

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

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        super.onLoadComplete(loader, data);
        if (loader.getId() == getLoaderId()) {
            final String headerText = getArguments().getString(ARG_SELECTED_CATEGORY);
            if (!TextUtils.isEmpty(headerText)) {
                final int headerPosition = ((SectionedThumbnailsAdapter) mAdapter).getHeaderPosition(headerText);
                if (!scrollHappened && headerPosition > 0) {
                    twList.smoothScrollToPosition(headerPosition);
                    scrollHappened = true;
                }
            }
        }
    }

    protected abstract CategoryHeaderLoader setupCategoryLoader();

    public interface CategoryHeaderLoader {
        void loadIcon(ImageView view, String headerText, String extra);

        void loadText(TextView view, String headerText, String extra);
    }
}
