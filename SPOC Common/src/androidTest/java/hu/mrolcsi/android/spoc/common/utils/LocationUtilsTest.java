package hu.mrolcsi.android.spoc.common.utils;

import android.location.Address;
import android.location.Geocoder;
import android.test.AndroidTestCase;
import android.text.TextUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.11.30.
 * Time: 9:35
 */

public class LocationUtilsTest extends AndroidTestCase {

    public void testGetLocationText() throws Exception {

        Geocoder geocoder = new Geocoder(getContext());

        //known location: Óbudai Egyetem, Budapest, Hungary
        Address testAddress = geocoder.getFromLocation(47.533953, 19.034624, 1).get(0);

        String locationText = LocationUtils.getLocationText(testAddress);
        String[] splitLocationText = locationText.split(", ");

        assertEquals("Location text is has 2 parts", 2, splitLocationText.length);
        assertEquals("City is not empty.", false, TextUtils.isEmpty(splitLocationText[0]));
        assertEquals("Country is not empty", false, TextUtils.isEmpty(splitLocationText[1]));

        //unknown location: middle of the North Atlantic Ocean
        testAddress.setLatitude(35.097375);
        testAddress.setLongitude(-40.771348);
        final List<Address> addresses = geocoder.getFromLocation(35.097375, -40.771348, 1);
        assertEquals("No address found", 0, addresses.size());
    }

    public void testLatitudeRef() throws Exception {
        //Tunis
        final String latRefN = LocationUtils.latitudeRef(35.813294);
        assertEquals("N", latRefN);

        //Madagascar
        final String latRefS = LocationUtils.latitudeRef(-18.234644);
        assertEquals("S", latRefS);
    }

    public void testLongitudeRef() throws Exception {
        //New York
        final String longRefW = LocationUtils.longitudeRef(-73.874158);
        assertEquals("W", longRefW);

        //Moscow
        final String longRefE = LocationUtils.longitudeRef(37.648859);
        assertEquals("E", longRefE);
    }

    public void testConvert() throws Exception {
        final String dms = LocationUtils.convert(-79.948862);
        assertEquals("79/1,56/1,55903/1000,", dms);
    }
}
