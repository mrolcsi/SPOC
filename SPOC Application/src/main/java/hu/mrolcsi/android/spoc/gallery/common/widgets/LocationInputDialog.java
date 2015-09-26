package hu.mrolcsi.android.spoc.gallery.common.widgets;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import hu.mrolcsi.android.spoc.gallery.R;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015. 09. 26.
 * Time: 14:36
 */

public class LocationInputDialog extends DialogFragment implements OnMapReadyCallback {

    public static final String TAG = "LocationInputDialog";
    private static final String MAP_TAG = "SupportMapFragment";

    private MapView mMapView;
    private AutoCompleteTextView etSearch;
    private CheckBox cbSaveToExif;
    private TextView tvCurrentLocation;

    private Geocoder mGeocoder;

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

    private void save() {
        Toast.makeText(getActivity(), "Something something saved.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        mGeocoder = new Geocoder(getActivity());

        //TODO: getArguments();

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                map.clear();
                map.addMarker(new MarkerOptions().position(latLng));

                try {
                    //TODO: async
                    final List<Address> addresses = mGeocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (!addresses.isEmpty()) {
                        tvCurrentLocation.setText(addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName());
                    } else {
                        tvCurrentLocation.setText("Unknown location.");
                    }
                } catch (IOException e) {
                    Log.w(getClass().getSimpleName(), e);
                }
            }
        });
    }
}
