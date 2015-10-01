package hu.mrolcsi.android.spoc.gallery.main.categories;

import android.support.v4.content.CursorLoader;
import android.widget.ImageView;
import android.widget.TextView;

import hu.mrolcsi.android.spoc.common.loader.ImagesTableLoader;
import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.gallery.R;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.10.01.
 * Time: 9:55
 */

public final class PlacesFragment extends CategoriesFragment {
    public static final int PLACES_LOADER_ID = 21;

    @Override
    protected CategoryHeaderLoader setupCategoryLoader() {
        return new CategoryHeaderLoader() {
            @Override
            public void loadIcon(String s, ImageView view) {
                view.setImageResource(R.drawable.marker);
            }

            @Override
            public void loadText(String s, TextView view) {
                view.setText(s);
            }
        };
    }

    @Override
    protected CursorLoader setupLoader() {
        mQueryArgs.clear();
        //args.putString(ImagesTableLoader.ARG_URI_STRING, Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, "search").toString());
        mQueryArgs.putStringArray(ImagesTableLoader.ARG_PROJECTION,
                new String[]{"DISTINCT _id", Image.COLUMN_FILENAME, Image.COLUMN_LOCATION + " AS " + SectionedThumbnailsAdapter.HEADER_COLUMN_NAME});
        //args.putString(ImagesTableLoader.ARG_SELECTION, "type = ?");
        //args.putStringArray(ImagesTableLoader.ARG_SELECTION_ARGS, new String[]{LabelType.LOCATION_LOCALITY.name()});
        mQueryArgs.putString(ImagesTableLoader.ARG_SORT_ORDER, "(CASE WHEN " + Image.COLUMN_LOCATION + " IS NULL THEN 1 ELSE 0 END), " + Image.COLUMN_LOCATION + ", " + Image.COLUMN_DATE_TAKEN + " DESC");
        return (CursorLoader) getLoaderManager().initLoader(PLACES_LOADER_ID, mQueryArgs, new ImagesTableLoader(getActivity(), this));
    }

    @Override
    public int getLoaderId() {
        return PLACES_LOADER_ID;
    }
}
