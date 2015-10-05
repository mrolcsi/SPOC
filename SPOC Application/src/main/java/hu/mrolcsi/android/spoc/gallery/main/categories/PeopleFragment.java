package hu.mrolcsi.android.spoc.gallery.main.categories;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.text.Html;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import hu.mrolcsi.android.spoc.common.loader.ImagesTableLoader;
import hu.mrolcsi.android.spoc.database.model.Contact;
import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.ContactPhotoLoader;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.10.01.
 * Time: 16:25
 */

public final class PeopleFragment extends CategoriesFragment {

    public static final int PEOPLE_LOADER_ID = 22;

    @Override
    protected CategoryHeaderLoader setupCategoryLoader() {
        return new CategoryHeaderLoader() {
            @Override
            public void loadIcon(final ImageView view, String headerText, String extra) {
                if (!TextUtils.isEmpty(extra)) {
                    final int contactId = Integer.parseInt(extra);
                    new ContactPhotoLoader(getActivity(), contactId) {
                        @Override
                        protected void onPreExecute() {
                            view.setImageResource(R.drawable.user);
                        }

                        @Override
                        protected void onPostExecute(Drawable drawable) {
                            view.setImageDrawable(drawable);
                        }
                    }.execute();
                } else {
                    view.setImageResource(R.drawable.user);
                }
            }

            @Override
            public void loadText(TextView view, String headerText, String extra) {
                if (TextUtils.isEmpty(headerText)) {
                    view.setText(Html.fromHtml(getString(R.string.details_message_unknownLocation)));
                } else {
                    view.setText(headerText);
                }
            }
        };
    }

    @Override
    protected CursorLoader setupLoader() {
        mQueryArgs.clear();
        /*
        SELECT DISTINCT images._id, filename, name
        FROM images
        INNER JOIN contacts2images
	        ON images._id=contacts2images.image_id
        LEFT JOIN contacts
	        ON contacts._id=contacts2images.contact_id
        ORDER BY (CASE WHEN name IS NULL THEN 1 ELSE 0 END), header, date_taken DESC;
         */
        mQueryArgs.putString(ImagesTableLoader.ARG_URI_STRING, Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, Contact.TABLE_NAME).toString());
        mQueryArgs.putStringArray(ImagesTableLoader.ARG_PROJECTION,
                new String[]{"DISTINCT " + Image.TABLE_NAME + "._id",
                        Image.COLUMN_FILENAME,
                        Image.COLUMN_LOCATION,
                        Image.COLUMN_DATE_TAKEN,
                        Contact.COLUMN_NAME + " AS " + SectionedThumbnailsAdapter.HEADER_COLUMN_NAME,
                        Contact.TABLE_NAME + "._id AS " + SectionedThumbnailsAdapter.EXTRA_COLUMN_NAME});
        mQueryArgs.putString(ImagesTableLoader.ARG_SORT_ORDER, "(CASE WHEN " + SectionedThumbnailsAdapter.HEADER_COLUMN_NAME + " IS NULL THEN 1 ELSE 0 END), " + SectionedThumbnailsAdapter.HEADER_COLUMN_NAME + ", " + Image.COLUMN_DATE_TAKEN + " DESC");
        return (CursorLoader) getLoaderManager().initLoader(PEOPLE_LOADER_ID, mQueryArgs, new ImagesTableLoader(getActivity(), this));
    }

    @Override
    public int getLoaderId() {
        return PEOPLE_LOADER_ID;
    }
}
