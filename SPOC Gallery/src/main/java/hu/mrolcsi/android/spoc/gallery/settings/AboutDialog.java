package hu.mrolcsi.android.spoc.gallery.settings;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.utils.FileUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.16.
 * Time: 16:07
 */

public class AboutDialog extends DialogFragment {

    public static final String TAG = "SPOC.Gallery.About";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.details_title)
                .setIcon(R.drawable.info)
                .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        final View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_about, null);
        builder.setView(contentView);

        onViewCreated(contentView, savedInstanceState);

        return builder.create();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        WebView webView = (WebView) view.findViewById(R.id.wvAbout);

        final String html = FileUtils.readRawResource(getActivity(), R.raw.about);

        webView.loadData(html, "text/html", null);
    }
}
