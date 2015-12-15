package hu.mrolcsi.android.spoc.gallery.imagedetails.editor;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import hu.mrolcsi.android.spoc.gallery.R;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.29.
 * Time: 13:18
 */
public class CustomLabelsAdapter extends CursorAdapter {

    public CustomLabelsAdapter(Context context) {
        super(context, null, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final View view = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, viewGroup, false);
        ((TextView) view).setCompoundDrawablePadding(context.getResources().getDimensionPixelOffset(R.dimen.margin_small));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view).setText(convertToString(cursor));
        ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.label_white, 0, 0, 0);
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        return cursor.getString(1);
    }
}
