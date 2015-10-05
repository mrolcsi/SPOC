package hu.mrolcsi.android.spoc.gallery.main.categories;

import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.Loader;
import android.widget.ImageView;
import android.widget.TextView;

import hu.mrolcsi.android.spoc.common.loader.ImagesTableLoader;
import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.database.model.Label;
import hu.mrolcsi.android.spoc.database.model.LabelType;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.R;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.10.05.
 * Time: 11:34
 */

public class FoldersFragment extends CategoriesFragment {

    public static final int FOLDERS_LOADER_ID = 24;

    @Override
    protected CategoryHeaderLoader setupCategoryLoader() {
        return new CategoryHeaderLoader() {
            @Override
            public void loadIcon(ImageView view, String headerText, String extra) {
                view.setImageResource(R.drawable.open_folder);
            }

            @Override
            public void loadText(TextView view, String headerText, String extra) {
                view.setText(headerText);
            }
        };
    }

    @Override
    protected Loader<Cursor> setupLoader() {
        /*
        SELECT DISTINCT _id,filename,name
        FROM images_with_labels
        WHERE type='CUSTOM'
        ORDER BY name, date_taken DESC
         */
        mQueryArgs.clear();
        mQueryArgs.putString(ImagesTableLoader.ARG_URI_STRING, Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, Label.TABLE_NAME).toString());
        mQueryArgs.putStringArray(ImagesTableLoader.ARG_PROJECTION,
                new String[]{"DISTINCT _id",
                        Image.COLUMN_FILENAME,
                        Image.COLUMN_LOCATION,
                        Image.COLUMN_DATE_TAKEN,
                        Label.COLUMN_NAME + " AS " + SectionedThumbnailsAdapter.HEADER_COLUMN_NAME});
        mQueryArgs.putString(ImagesTableLoader.ARG_SELECTION, "type = ?");
        mQueryArgs.putStringArray(ImagesTableLoader.ARG_SELECTION_ARGS, new String[]{LabelType.FOLDER.name()});
        mQueryArgs.putString(ImagesTableLoader.ARG_SORT_ORDER, "(CASE WHEN " + SectionedThumbnailsAdapter.HEADER_COLUMN_NAME + " IS NULL THEN 1 ELSE 0 END), " + SectionedThumbnailsAdapter.HEADER_COLUMN_NAME + ", " + Image.COLUMN_DATE_TAKEN + " DESC");
        return getLoaderManager().initLoader(FOLDERS_LOADER_ID, mQueryArgs, new ImagesTableLoader(getActivity(), this));
    }

    @Override
    public int getLoaderId() {
        return FOLDERS_LOADER_ID;
    }
}
