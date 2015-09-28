package hu.mrolcsi.android.spoc.gallery.common.widgets;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import hu.mrolcsi.android.spoc.common.helper.LocationFinderTask;
import hu.mrolcsi.android.spoc.common.loader.LabelsTableLoader;
import hu.mrolcsi.android.spoc.database.model.Label;
import hu.mrolcsi.android.spoc.database.model.LabelType;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.search.SuggestionAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015. 09. 26.
 * Time: 14:36
 */

public class LocationInputDialog extends DialogFragment implements OnMapReadyCallback, LabelsTableLoader.LoaderCallbacks {

    public static final String TAG = "LocationInputDialog";
    private static final String MAP_TAG = "SupportMapFragment";
    private static final int SUGGESTIONS_LOADER_ID = 63;

    private MapView mMapView;
    private AutoCompleteTextView etSearch;
    private CheckBox cbSaveToExif;
    private TextView tvCurrentLocation;

    private Bundle mSuggestionArgs = new Bundle();
    private SuggestionAdapter mSuggestionsAdapter;
    private CursorLoader mSuggestionsLoader;
    private GoogleMap mMap;

    private List<String> mLabelsCache = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSuggestionArgs.putStringArray(LabelsTableLoader.ARG_PROJECTION, new String[]{"DISTINCT " + Label.COLUMN_FOREIGN_ID + " AS _id", Label.COLUMN_NAME, Label.COLUMN_TYPE});
        mSuggestionArgs.putString(LabelsTableLoader.ARG_SELECTION, Label.COLUMN_TYPE + "='" + LabelType.LOCATION_LOCALITY.name() + "' AND (" + "upper(" + Label.COLUMN_NAME + ") LIKE ?" + " OR " + "lower(" + Label.COLUMN_NAME + ") LIKE ?)");
        mSuggestionArgs.putStringArray(LabelsTableLoader.ARG_SELECTION_ARGS, new String[]{"%"});
        mSuggestionArgs.putString(LabelsTableLoader.ARG_SORT_ORDER, Label.COLUMN_NAME + " ASC");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        @SuppressLint("InflateParams") final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_locationinput, null);
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
        mMapView = (MapView) view.findViewById(R.id.map);
        etSearch = (AutoCompleteTextView) view.findViewById(R.id.etSearch);
        cbSaveToExif = (CheckBox) view.findViewById(R.id.cbSaveToExif);
        tvCurrentLocation = (TextView) view.findViewById(R.id.tvCurrentLocation);

        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //mSuggestionsLoader.reset();
                mSuggestionsLoader.setSelectionArgs(new String[]{"%" + editable.toString().toUpperCase(Locale.getDefault()) + "%", "%" + editable.toString().toLowerCase(Locale.getDefault()) + "%"});
                mSuggestionsLoader.forceLoad();
            }
        });
        etSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                etSearch.clearFocus();
                searchLocation(mLabelsCache.get(i));
            }
        });
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    searchLocation(textView.getText().toString());
                }
                return false;
            }
        });
    }

    //region Lifecycle
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    //endregion

    private void searchLocation(String locationText) {
        new AsyncTask<String, Void, List<Address>>() {

            @Override
            protected List<Address> doInBackground(String... strings) {
                Geocoder geocoder = new Geocoder(getActivity());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocationName(strings[0], 5);
                } catch (IOException e) {
                    Log.w(getClass().getName(), "Location lookup failed. Cause:\n" + e.toString());
                }
                return addresses;
            }

            @Override
            protected void onPostExecute(final List<Address> addresses) {
                if (addresses == null) {
                    Toast.makeText(getActivity(), "Location search does not work without internet.", Toast.LENGTH_SHORT).show();
                } else if (addresses.isEmpty()) {
                    Toast.makeText(getActivity(), "Nothing found.", Toast.LENGTH_SHORT).show();
                } else {
                    if (addresses.size() == 1) {
                        //jump to latLong
                        moveMap(addresses.get(0));
                    } else {
                        //show list in dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setAdapter(new AddressAdapter(getActivity(), addresses),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        moveMap(addresses.get(i));
                                    }
                                });
                        builder.show();
                    }
                }
            }
        }.execute(locationText);
    }

    private void moveMap(Address address) {
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 13);
        mMap.animateCamera(cameraUpdate);
    }

    private void save() {
        Toast.makeText(getActivity(), "Nothing saved.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        mMap = map;

        //TODO: getArguments();

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                map.clear();
                map.addMarker(new MarkerOptions().position(latLng));

                new LocationFinderTask(getActivity()) {
                    @Override
                    protected void onPreExecute() {
                        tvCurrentLocation.setText(Html.fromHtml(getString(R.string.details_message_lookingUpLocation)));
                    }

                    @Override
                    protected void onPostExecute(List<Address> addresses) {
                        if (addresses == null) {
                            tvCurrentLocation.setText(Html.fromHtml(getString(R.string.details_message_unknownLocation_noInternet)));
                        } else if (addresses.isEmpty()) {
                            tvCurrentLocation.setText(Html.fromHtml(getString(R.string.details_message_unknownLocation)));
                        } else {
                            final Address address = addresses.get(0);
                            String locality = address.getLocality();
                            if (locality == null) {
                                locality = address.getFeatureName();
                            }
                            if (locality == null) {
                                locality = address.getAdminArea();
                            }
                            tvCurrentLocation.setText(locality + ", " + address.getCountryName());
                        }
                    }
                }.execute((float) latLng.latitude, (float) latLng.longitude);
            }
        });

        mSuggestionsLoader = (CursorLoader) getLoaderManager().initLoader(SUGGESTIONS_LOADER_ID, mSuggestionArgs, new LabelsTableLoader(getActivity(), this));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == SUGGESTIONS_LOADER_ID) {
            mSuggestionsAdapter.changeCursor(null);
        }
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == SUGGESTIONS_LOADER_ID) {
            if (mSuggestionsAdapter == null) {
                mSuggestionsAdapter = new SuggestionAdapter(getActivity());
                etSearch.setAdapter(mSuggestionsAdapter);
            }
            mSuggestionsAdapter.changeCursor(data);

            //cache data for item click?
            if (data.getCount() > 0 && data.moveToFirst()) {
                mLabelsCache.clear();
                do {
                    mLabelsCache.add(data.getString(1));
                } while (data.moveToNext());
            }
        }
    }

    class AddressAdapter extends ArrayAdapter<Address> {

        public AddressAdapter(Context context, List<Address> objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);

            final Address address = getItem(position);
            String locality = address.getLocality();
            if (locality == null) {
                locality = address.getFeatureName();
            }
            if (locality == null) {
                locality = address.getAdminArea();
            }
            ((TextView) view).setText(locality + ", " + address.getCountryName());
            return view;
        }
    }
}
