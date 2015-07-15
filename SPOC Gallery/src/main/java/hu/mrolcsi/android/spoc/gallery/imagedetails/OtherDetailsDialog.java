package hu.mrolcsi.android.spoc.gallery.imagedetails;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.15.
 * Time: 9:34
 */

public class OtherDetailsDialog extends DialogFragment {

    public static final String TAG = "SPOC.Gallery.ImageDetails.Dialog";
    private final SimpleDateFormat sdf;

    private TextView tvDateTaken;
    private TextView tvLocation;
    private TextView tvCoordinates;
    private TextView tvFileSize;
    private TextView tvSavedTo;
    private TextView tvFilePath;
    private TextView tvResolution;
    private TextView tvModel;
    private TextView tvAperture;
    private TextView tvExposureTime;
    private TextView tvFlash;
    private TextView tvFocalLength;
    private TextView tvDigitalZoom;
    private TextView tvIsoValue;

    private LocationFinderTask mLocationFinderTask;


    public OtherDetailsDialog() {
        sdf = new SimpleDateFormat("yyyy:MM:DD HH:mm:ss", Locale.getDefault());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.details_title)
                .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        final View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_details, null);
        builder.setView(contentView);

        onViewCreated(contentView, savedInstanceState);

        return builder.create();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //TODO: find views

        tvDateTaken = (TextView) view.findViewById(R.id.tvDateTaken);
        tvLocation = (TextView) view.findViewById(R.id.tvLocation);
        tvCoordinates = (TextView) view.findViewById(R.id.tvCoordinates);
        tvFileSize = (TextView) view.findViewById(R.id.tvFileSize);
        tvSavedTo = (TextView) view.findViewById(R.id.tvSavedTo);
        tvFilePath = (TextView) view.findViewById(R.id.tvFilePath);
        tvResolution = (TextView) view.findViewById(R.id.tvResolution);
        tvModel = (TextView) view.findViewById(R.id.tvCameraModel);
        tvAperture = (TextView) view.findViewById(R.id.tvAperture);
        tvExposureTime = (TextView) view.findViewById(R.id.tvExposureTime);
        tvFlash = (TextView) view.findViewById(R.id.tvFlash);
        tvFocalLength = (TextView) view.findViewById(R.id.tvFocalLength);
        tvDigitalZoom = (TextView) view.findViewById(R.id.tvDigitalZoom);
        tvIsoValue = (TextView) view.findViewById(R.id.tvIsoValue);

        loadInfo();
    }

    private void loadInfo() {
        if (getArguments() != null && getArguments().containsKey(ImageDetailsFragment.ARG_IMAGE_PATH)) {
            final String path = getArguments().getString(ImageDetailsFragment.ARG_IMAGE_PATH);

            try {
                ExifInterface exif = new ExifInterface(path);

                final String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);
                final Date date = sdf.parse(dateString);
                tvDateTaken.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date));

                float latLong[] = new float[2];
                if (exif.getLatLong(latLong)) {
                    final String latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                    final String longRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                    tvCoordinates.setText(String.format("Latitude:\t%s %f\nLongitude:\t%s %f", latRef, Math.abs(latLong[0]), longRef, Math.abs(latLong[1])));

                    mLocationFinderTask = new LocationFinderTask();
                    mLocationFinderTask.execute(latLong[0], latLong[1]);
                }

                final File imageFile = new File(path);
                tvFileSize.setText(FileUtils.getHumanReadableSize(imageFile.length()));
                tvFilePath.setText(imageFile.getAbsolutePath());

//                final boolean isEmulated = Environment.isExternalStorageEmulated(imageFile);
//                tvSavedTo.setText(isEmulated ? "Internal memory" : "SD Card");

                final int width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
                final int height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
                tvResolution.setText(width + " x " + height);

                final String brand = exif.getAttribute(ExifInterface.TAG_MAKE);
                final String model = exif.getAttribute(ExifInterface.TAG_MODEL);
                tvModel.setText(brand + " " + model);

                String aperture;
                float exposureTime;
                String isoValue;
                if (Build.VERSION.SDK_INT > 11) {
                    aperture = exif.getAttribute(ExifInterface.TAG_APERTURE);
                    exposureTime = Float.parseFloat(exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
                    isoValue = exif.getAttribute(ExifInterface.TAG_ISO);
                } else {
                    aperture = exif.getAttribute("FNumber");
                    exposureTime = Float.parseFloat(exif.getAttribute("ExposureTime"));
                    isoValue = exif.getAttribute("ISOSpeedRatings");
                }
                tvAperture.setText("f/" + aperture);
                tvExposureTime.setText(String.format("1/%.0f s", 1 / exposureTime));
                tvIsoValue.setText(isoValue);

                // http://stackoverflow.com/questions/7076958/read-exif-and-determine-if-the-flash-has-fired
                final int flash = exif.getAttributeInt(ExifInterface.TAG_FLASH, 0);
                tvFlash.setText(flash % 2 == 1 ? getString(R.string.details_message_flashOn) : getString(R.string.details_message_flashOff));

                final String focalLengthString = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
                final float focalLength = Float.parseFloat(focalLengthString.substring(0, focalLengthString.lastIndexOf('/'))) / 100f;
                tvFocalLength.setText(String.format("%.2f mm", focalLength));

            } catch (IOException | ParseException e) {
                Log.w(getClass().getName(), e);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mLocationFinderTask.cancel(true);
    }

    class LocationFinderTask extends AsyncTask<Float, Void, List<Address>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvLocation.setText(Html.fromHtml(getString(R.string.details_message_lookingUpLocation)));
        }

        @Override
        protected List<Address> doInBackground(Float... floats) {
            Geocoder geocoder = new Geocoder(getActivity());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(floats[0], floats[1], 1);
            } catch (IOException e) {
                Log.w(getClass().getName(), "Location lookup failed.");
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            super.onPostExecute(addresses);

            if (isCancelled()) return;

            if (addresses == null) {
                //TODO: use cached value from db
                tvLocation.setText(Html.fromHtml(getString(R.string.details_message_unknowLocation)));
            } else {
                final String locality = addresses.get(0).getLocality();
                final String countryName = addresses.get(0).getCountryName();
                tvLocation.setText(locality + ", " + countryName);
            }
        }
    }
}
