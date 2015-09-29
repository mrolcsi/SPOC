package hu.mrolcsi.android.spoc.common.utils;

import android.location.Address;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.09.29.
 * Time: 9:36
 */

public abstract class LocationUtils {

    private static StringBuilder sb = new StringBuilder(20);

    public static String getLocationText(Address address) {
        String locality = address.getLocality();
        if (locality == null) {
            locality = address.getFeatureName();
        }
        if (locality == null) {
            locality = address.getAdminArea();
        }
        String countryName = address.getCountryName();
        if (countryName == null) {
            countryName = address.getAddressLine(address.getMaxAddressLineIndex());
        }
        return locality + ", " + countryName;
    }

    /**
     * returns ref for latitude which is S or N.
     *
     * @param latitude
     * @return S or N
     */
    public static String latitudeRef(double latitude) {
        return latitude < 0.0d ? "S" : "N";
    }

    /**
     * returns ref for latitude which is S or N.
     *
     * @param longitude
     * @return S or N
     */
    public static String longitudeRef(double longitude) {
        return longitude < 0.0d ? "W" : "E";
    }

    /**
     * convert latitude into DMS (degree minute second) format. For instance<br/>
     * -79.948862 becomes<br/>
     * 79/1,56/1,55903/1000<br/>
     * It works for latitude and longitude<br/>
     *
     * @param latitude could be longitude.
     * @return
     */
    synchronized public static final String convert(double latitude) {
        latitude = Math.abs(latitude);
        int degree = (int) latitude;
        latitude *= 60;
        latitude -= (degree * 60.0d);
        int minute = (int) latitude;
        latitude *= 60;
        latitude -= (minute * 60.0d);
        int second = (int) (latitude * 1000.0d);

        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000,");
        return sb.toString();
    }
}
