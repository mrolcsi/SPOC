package hu.mrolcsi.android.spoc.common.helper;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.24.
 * Time: 16:38
 */

public class LocationFinderTask extends AsyncTask<Float, Void, List<Address>> {

    private final Context context;

    public LocationFinderTask(Context context) {
        this.context = context;
    }

//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//        tvLocation.setText(Html.fromHtml(getString(R.string.details_message_lookingUpLocation)));
//    }

    @Override
    protected List<Address> doInBackground(Float... floats) {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(floats[0], floats[1], 1);
        } catch (IOException e) {
            Log.w(getClass().getName(), "Location lookup failed. Cause:\n" + e.toString());
        }
        return addresses;
    }

    @Override
    protected void onPostExecute(List<Address> addresses) {
        super.onPostExecute(addresses);

        if (isCancelled()) return;

//        if (addresses == null) {
//            //TODO: use cached value from db
//            tvLocation.setText(Html.fromHtml(getString(R.string.details_message_unknownLocation)));
//        } else {
//            final String locality = addresses.get(0).getLocality();
//            final String countryName = addresses.get(0).getCountryName();
//            tvLocation.setText(locality + ", " + countryName);
//        }
    }
}
