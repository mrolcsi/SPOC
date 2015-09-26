package hu.mrolcsi.android.spoc.gallery.common.widgets;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import hu.mrolcsi.android.spoc.gallery.R;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.29.
 * Time: 14:09
 */

public class ProgressDialogFragment extends DialogFragment {

    public static final String TAG = "SPOC.Common.ProgressDialog";
    private int max;
    private String message;
    private boolean indeterminate;
    private int progress;

    private ProgressBar pbSpinner;
    private TextView tvMessage;
    private ProgressBar pbHorizontal;
    private TextView tvProgress;
    private TextView tvPercent;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        @SuppressLint("InflateParams") final View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_progress, null);
        builder.setView(contentView);

        onViewCreated(contentView, savedInstanceState);

        return builder.create();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        pbSpinner = (ProgressBar) view.findViewById(R.id.pbSpinner);
        tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        pbHorizontal = (ProgressBar) view.findViewById(R.id.pbHorizontal);
        tvProgress = (TextView) view.findViewById(R.id.tvProgress);
        tvPercent = (TextView) view.findViewById(R.id.tvPercent);

        setMax(max);
        setMessage(message);
        setIndeterminate(indeterminate);
        setProgress(progress);
    }

    public void setMax(int max) {
        this.max = max;
        if (pbHorizontal != null)
            pbHorizontal.setMax(max);
    }

    public void setMessage(String message) {
        this.message = message;
        if (tvMessage != null)
            tvMessage.setText(message);
    }

    public void setIndeterminate(boolean indeterminate) {
        this.indeterminate = indeterminate;
        if (indeterminate) {
            if (pbSpinner != null) pbSpinner.setVisibility(View.VISIBLE);
            if (tvMessage != null) tvMessage.setVisibility(View.VISIBLE);

            if (pbHorizontal != null) pbHorizontal.setVisibility(View.GONE);
            if (tvProgress != null) tvProgress.setVisibility(View.GONE);
            if (tvPercent != null) tvPercent.setVisibility(View.GONE);
        } else {
            if (pbSpinner != null) pbSpinner.setVisibility(View.GONE);
            if (tvMessage != null) tvMessage.setVisibility(View.GONE);

            if (pbHorizontal != null) pbHorizontal.setVisibility(View.VISIBLE);
            if (tvProgress != null) tvProgress.setVisibility(View.VISIBLE);
            if (tvPercent != null) tvPercent.setVisibility(View.VISIBLE);
        }
    }

    public void showHorizontalBar(boolean show) {
        if (show) {
            if (pbHorizontal != null) pbHorizontal.setVisibility(View.VISIBLE);
            if (tvProgress != null) tvProgress.setVisibility(View.VISIBLE);
            if (tvPercent != null) tvPercent.setVisibility(View.VISIBLE);
        } else {
            if (pbHorizontal != null) pbHorizontal.setVisibility(View.GONE);
            if (tvProgress != null) tvProgress.setVisibility(View.GONE);
            if (tvPercent != null) tvPercent.setVisibility(View.GONE);
        }
    }


    public void setProgress(int progress) {
        this.progress = progress;

        if (pbHorizontal != null)
            pbHorizontal.setProgress(progress);
        if (tvProgress != null)
            tvProgress.setText(progress + "/" + max);
        if (tvPercent != null)
            tvPercent.setText(String.format("%.0f%%", (float) progress / max * 100f));
    }
}
