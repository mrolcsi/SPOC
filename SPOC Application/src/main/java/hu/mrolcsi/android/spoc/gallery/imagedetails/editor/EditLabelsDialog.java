package hu.mrolcsi.android.spoc.gallery.imagedetails.editor;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import hu.mrolcsi.android.spoc.common.loader.LabelsTableLoader;
import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.database.model.Label;
import hu.mrolcsi.android.spoc.database.model.LabelType;
import hu.mrolcsi.android.spoc.database.model.binder.Label2Image;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.utils.DialogUtils;
import hu.mrolcsi.android.spoc.gallery.imagedetails.SingleImageFragment;
import hu.mrolcsi.android.spoc.gallery.search.SuggestionAdapter;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.29.
 * Time: 12:59
 */

public class EditLabelsDialog extends DialogFragment implements LabelsTableLoader.LoaderCallbacks {
    public static final String TAG = "EditLabelsDialog";
    public static final int SUGGESTIONS_LOADER_ID = 64;
    public static final int LABELS_LOADER_ID = 65;

    private Bundle mSuggestionArgs = new Bundle();
    private SuggestionAdapter mSuggestionsAdapter;
    private CursorLoader mSuggestionsLoader;
    private CursorLoader mLabelsLoader;
    private CustomLabelsAdapter mLabelsAdapter;

    private ListView lvLabels;
    private AutoCompleteTextView etNewLabel;
    private ImageButton btnSaveTag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSuggestionArgs.putStringArray(LabelsTableLoader.ARG_PROJECTION, new String[]{"DISTINCT " + Label.COLUMN_FOREIGN_ID + " AS _id", Label.COLUMN_NAME, Label.COLUMN_TYPE});
        mSuggestionArgs.putString(LabelsTableLoader.ARG_SELECTION, Label.COLUMN_TYPE + "='" + LabelType.CUSTOM.name() + "' AND (" + "upper(" + Label.COLUMN_NAME + ") LIKE ?" + " OR " + "lower(" + Label.COLUMN_NAME + ") LIKE ?)");
        mSuggestionArgs.putStringArray(LabelsTableLoader.ARG_SELECTION_ARGS, new String[]{"%"});
        mSuggestionArgs.putString(LabelsTableLoader.ARG_SORT_ORDER, Label.COLUMN_NAME + " ASC");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        @SuppressLint("InflateParams") final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_editlabels, null);
        builder.setView(view);

        onViewCreated(view, savedInstanceState);

        builder.setTitle(R.string.navigation_tags);
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        lvLabels = (ListView) view.findViewById(R.id.lvLabels);
        etNewLabel = (AutoCompleteTextView) view.findViewById(R.id.etNewLabel);
        btnSaveTag = (ImageButton) view.findViewById(R.id.btnSave);

        lvLabels.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, final long l) {
                final AlertDialog.Builder builder = DialogUtils.buildConfirmDialog(getActivity());
                builder.setMessage(R.string.editLabels_confirm_removeTag)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final int deletedCount = getActivity().getContentResolver().delete(SPOCContentProvider.LABELS_2_IMAGES_URI,
                                        Image.COLUMN_FOREIGN_ID + " = ? AND " + Label.COLUMN_FOREIGN_ID + " = ?",
                                        new String[]{String.valueOf(getArguments().getInt(SingleImageFragment.ARG_IMAGE_ID)), String.valueOf(l)});
                                if (deletedCount > 0) {
                                    Toast.makeText(getActivity(), R.string.editLabels_message_tagRemoved, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), R.string.editLabels_message_tagNotRemoved, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
            }
        });

        etNewLabel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                etNewLabel.setError(null);
                mSuggestionsLoader.setSelectionArgs(new String[]{"%" + editable.toString().toUpperCase(Locale.getDefault()) + "%", "%" + editable.toString().toLowerCase(Locale.getDefault()) + "%"});
                mSuggestionsLoader.forceLoad();
            }
        });

        etNewLabel.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    saveTag(textView.getText().toString().trim());

                    mLabelsLoader.forceLoad();
                }
                return false;
            }
        });

        btnSaveTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(etNewLabel.getText())) {
                    etNewLabel.setError(getString(R.string.editLabels_error_emptyTagName));
                } else {
                    etNewLabel.setError(null);
                    saveTag(etNewLabel.getText().toString().trim());
                }
            }
        });
    }

    private void saveTag(String tagName) {
        ContentValues values = new ContentValues();
        values.put(Label.COLUMN_NAME, tagName);
        values.put(Label.COLUMN_TYPE, LabelType.CUSTOM.name());
        final Uri insertedUri = getActivity().getContentResolver().insert(SPOCContentProvider.LABELS_URI, values);

        int labelId = -1;
        try {
            labelId = Integer.parseInt(insertedUri.getLastPathSegment());
        } catch (NumberFormatException e) {
            //no insertion occurred
            final Cursor cursorWithLabel = getActivity().getContentResolver().query(
                    SPOCContentProvider.LABELS_URI,
                    new String[]{"_id"},
                    Label.COLUMN_NAME + " = ?",
                    new String[]{tagName},
                    null);
            if (cursorWithLabel.moveToFirst()) {
                labelId = cursorWithLabel.getInt(0);
            }
            cursorWithLabel.close();
        }
        values.clear();
        values.put(Label2Image.COLUMN_DATE, Calendar.getInstance().getTimeInMillis());
        values.put(Label2Image.COLUMN_LABEL_ID, labelId);
        values.put(Label2Image.COLUMN_IMAGE_ID, getArguments().getInt(SingleImageFragment.ARG_IMAGE_ID));
        getActivity().getContentResolver().insert(SPOCContentProvider.LABELS_2_IMAGES_URI, values);

        etNewLabel.setText(null);
    }

    @Override
    public void onStart() {
        super.onStart();

        mSuggestionsLoader = (CursorLoader) getLoaderManager().initLoader(SUGGESTIONS_LOADER_ID, mSuggestionArgs, new LabelsTableLoader(getActivity(), this));

        Bundle args = new Bundle();
        args.putStringArray(LabelsTableLoader.ARG_PROJECTION, new String[]{"DISTINCT " + Label.COLUMN_FOREIGN_ID + " AS _id", Label.COLUMN_NAME});
        args.putString(LabelsTableLoader.ARG_SELECTION, Label.COLUMN_TYPE + " = ? AND " + Image.COLUMN_FOREIGN_ID + " = ?");
        args.putStringArray(LabelsTableLoader.ARG_SELECTION_ARGS, new String[]{LabelType.CUSTOM.name(), String.valueOf(getArguments().getInt(SingleImageFragment.ARG_IMAGE_ID))});
        mLabelsLoader = (CursorLoader) getLoaderManager().initLoader(LABELS_LOADER_ID, args, new LabelsTableLoader(getActivity(), this));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LABELS_LOADER_ID) {
            mLabelsAdapter.changeCursor(null);
        }
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LABELS_LOADER_ID) {
            if (mLabelsAdapter == null) {
                mLabelsAdapter = new CustomLabelsAdapter(getActivity());
                lvLabels.setAdapter(mLabelsAdapter);
            }
            mLabelsAdapter.changeCursor(data);
        } else if (loader.getId() == SUGGESTIONS_LOADER_ID) {
            if (mSuggestionsAdapter == null) {
                mSuggestionsAdapter = new SuggestionAdapter(getActivity());
                etNewLabel.setAdapter(mSuggestionsAdapter);
            } else {
                mSuggestionsAdapter.changeCursor(data);
            }
        }
    }

}
