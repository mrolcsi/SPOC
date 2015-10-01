package hu.mrolcsi.android.spoc.gallery.main.categories;

import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import hu.mrolcsi.android.spoc.common.loader.ImagesTableLoader;
import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.database.model.Views;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.R;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.10.01.
 * Time: 10:12
 */

public final class DatesFragment extends CategoriesFragment {

    public static final int DATES_LOADER_ID = 20;

    @Override
    protected CategoryHeaderLoader setupCategoryLoader() {
        return new CategoryHeaderLoader() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. MMMM d.", Locale.getDefault());

            @Override
            public void loadIcon(String s, ImageView view) {
                if (TextUtils.isEmpty(s)) return;

                final long dateLong = Long.parseLong(s);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date(dateLong));
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                try {
                    Class res = R.drawable.class;
                    Field field = res.getField("calendar_" + day);
                    int drawableId = field.getInt(null);
                    view.setImageResource(drawableId);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    view.setImageResource(R.drawable.calendar);
                }
            }

            @Override
            public void loadText(String s, TextView view) {
                final long dateLong = Long.parseLong(s);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date(dateLong));
                view.setText(dateFormat.format(calendar.getTime()));
            }
        };
    }

    @Override
    protected CursorLoader setupLoader() {
        mQueryArgs.clear();
        mQueryArgs.putString(ImagesTableLoader.ARG_URI_STRING, Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, Views.IMAGES_BY_DAY_DAY_TAKEN).toString());
        mQueryArgs.putStringArray(ImagesTableLoader.ARG_PROJECTION,
                new String[]{"DISTINCT " + Image.TABLE_NAME + "._id", Image.COLUMN_FILENAME, Views.IMAGES_BY_DAY_DAY_TAKEN + " AS " + SectionedThumbnailsAdapter.HEADER_COLUMN_NAME});
        //mQueryArgs.putString(ImagesTableLoader.ARG_SELECTION, "type = ?");
        //args.putStringArray(ImagesTableLoader.ARG_SELECTION_ARGS, new String[]{LabelType.LOCATION_LOCALITY.name()});
        mQueryArgs.putString(ImagesTableLoader.ARG_SORT_ORDER, Image.COLUMN_DATE_TAKEN + " DESC");
        return (CursorLoader) getLoaderManager().initLoader(DATES_LOADER_ID, mQueryArgs, new ImagesTableLoader(getActivity(), this));
    }

    @Override
    public int getLoaderId() {
        return DATES_LOADER_ID;
    }
}
