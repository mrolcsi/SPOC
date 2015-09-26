package hu.mrolcsi.android.spoc.gallery.common.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import hu.mrolcsi.android.spoc.gallery.R;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.21.
 * Time: 16:32
 */

public abstract class DialogUtils {
    //TODO: standard error dialog, warning dialog, question/confirm dialog

    public static AlertDialog.Builder buildConfirmDialog(final Context context) {
        return new AlertDialog.Builder(context)
                .setIcon(R.drawable.help)
                .setTitle(context.getString(R.string.dialog_title_areYouSure))
                .setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
    }

    public static AlertDialog.Builder buildErrorDialog(final Context context) {
        return new AlertDialog.Builder(context)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setIcon(R.drawable.error)
                .setTitle(R.string.dialog_title_somethingWentWrong);
    }
}
