package hu.mrolcsi.android.spoc.gallery.search;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import hu.mrolcsi.android.spoc.database.model.LabelType;
import hu.mrolcsi.android.spoc.gallery.R;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.07.
 * Time: 13:47
 */

public class SuggestionAdapter extends CursorAdapter {

    public SuggestionAdapter(Context context) {
        super(context, null, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final TextView view = (TextView) LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, viewGroup, false);
        view.setCompoundDrawablePadding(context.getResources().getDimensionPixelOffset(R.dimen.margin_small));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (cursor == null || cursor.isClosed()) return;

        final String name = cursor.getString(1);
        final LabelType type = LabelType.valueOf(cursor.getString(2));

        ((TextView) view).setText(name);

        switch (type) {
            case DATE_MONTH:
            case DATE_DAY:
                ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.calendar, 0, 0, 0);
                break;
            case LOCATION_COUNTRY:
            case LOCATION_LOCALITY:
                ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.map_marker, 0, 0, 0);
                break;
            case CONTACT:
                ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.user, 0, 0, 0);
                //TODO: load contact photo
                break;
            case FOLDER:
                ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder, 0, 0, 0);
                break;
            case CUSTOM:
                ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.tag, 0, 0, 0);
                break;
            default:
                break;
        }
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        return cursor.getString(1);
    }
}
