package hu.mrolcsi.android.spoc.gallery.common.widgets;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.R;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.24.
 * Time: 15:13
 */

public class DateTimePickerDialog extends DialogFragment {

    public static final String ARG_IMAGE_ID = "argImageId";
    public static final String TAG = "DateTimePickerDialog";

    private DatePicker datePicker;
    private TimePicker timePicker;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        @SuppressLint("InflateParams") final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_datetimepicker, null);
        builder.setView(view);

        onViewCreated(view, savedInstanceState);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                save();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        datePicker = (DatePicker) view.findViewById(R.id.datePicker);
        timePicker = (TimePicker) view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(DateFormat.is24HourFormat(getActivity()));
    }

    @SuppressWarnings("deprecation")
    @TargetApi(23)
    private void save() {
        final Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, datePicker.getYear());
        calendar.set(Calendar.MONTH, datePicker.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());

        if (Build.VERSION.SDK_INT >= 23) {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
        }
        if (Build.VERSION.SDK_INT >= 23) {
            calendar.set(Calendar.MINUTE, timePicker.getMinute());
        } else {
            calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        }

        ContentValues values = new ContentValues();
        values.put(Image.COLUMN_DATE_TAKEN, calendar.getTimeInMillis());

        final int updateCount = getActivity().getContentResolver().update(Uri.withAppendedPath(SPOCContentProvider.IMAGES_URI, String.valueOf(getArguments().getInt(ARG_IMAGE_ID))), values, null, null);
        if (updateCount > 0) {
            Toast.makeText(getActivity(), R.string.singleImage_message_dateChanged, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), R.string.singleImage_message_dateNotChanged, Toast.LENGTH_SHORT).show();
        }
    }
}
