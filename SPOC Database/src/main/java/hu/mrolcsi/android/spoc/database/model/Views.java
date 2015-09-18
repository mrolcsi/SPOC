package hu.mrolcsi.android.spoc.database.model;

import hu.mrolcsi.android.spoc.database.model.binder.Contact2Image;
import hu.mrolcsi.android.spoc.database.model.binder.Label2Image;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015. 09. 06.
 * Time: 19:03
 */

public abstract class Views {

//    public static final String IMAGES_WITH_LABELS_NAME = "images_with_labels";
//    public static final String IMAGES_WITH_LABELS_CREATE = "CREATE VIEW " + IMAGES_WITH_LABELS_NAME + " AS SELECT " +
//            Image.TABLE_NAME + "._id, " +
//            Image.TABLE_NAME + "." + Image.COLUMN_FILENAME + ", " +
//            Image.TABLE_NAME + "." + Image.COLUMN_DATE_TAKEN + ", " +
//            Image.TABLE_NAME + "." + Image.COLUMN_LOCATION + ", " +
//            Label.TABLE_NAME + "._id AS " + Label2Image.COLUMN_LABEL_ID + ", " +
//            Label.TABLE_NAME + "." + Label.COLUMN_NAME + ", " +
//            Label.TABLE_NAME + "." + Label.COLUMN_TYPE +
//            " FROM " + Image.TABLE_NAME + ", " + Label.TABLE_NAME +
//            " INNER JOIN " + Label2Image.TABLE_NAME +
//            " ON " + Label2Image.TABLE_NAME + "." + Label2Image.COLUMN_IMAGE_ID + "=" + Image.TABLE_NAME + "._id" +
//            " AND " + Label2Image.TABLE_NAME + "." + Label2Image.COLUMN_LABEL_ID + "=" + Label.TABLE_NAME + "._id" +
//            " ORDER BY " + Label2Image.COLUMN_IMAGE_ID + " ASC";

    public static final String LABELS_WITH_CONTACTS_NAME = "labels_with_contacts";
    public static final String LABELS_WITH_CONTACTS_CREATE = "CREATE VIEW " + LABELS_WITH_CONTACTS_NAME + " AS " +
            "SELECT " + Label2Image.TABLE_NAME + "." + Label2Image.COLUMN_LABEL_ID + " AS " + Label2Image.COLUMN_LABEL_ID + ", " + Label2Image.TABLE_NAME + "." + Label2Image.COLUMN_IMAGE_ID + " AS " + Label2Image.COLUMN_IMAGE_ID + ", " + Label2Image.TABLE_NAME + "." + Label2Image.COLUMN_DATE + " AS " + Label2Image.COLUMN_DATE + ", " +
            Label.TABLE_NAME + "." + Label.COLUMN_NAME + ", " + Label.TABLE_NAME + "." + Label.COLUMN_TYPE + " " +
            "FROM " + Label2Image.TABLE_NAME + " " +
            "INNER JOIN " + Label.TABLE_NAME + " " +
            "ON " + Label.TABLE_NAME + "._id = " + Label2Image.TABLE_NAME + "." + Label2Image.COLUMN_LABEL_ID + " " +
            "UNION " +
            "SELECT " +
            Contact2Image.TABLE_NAME + "." + Contact2Image.COLUMN_CONTACT_ID + " AS " + Label2Image.COLUMN_LABEL_ID + ", " + Contact2Image.TABLE_NAME + "." + Label2Image.COLUMN_IMAGE_ID + " AS " + Label2Image.COLUMN_IMAGE_ID + ", " + Contact2Image.TABLE_NAME + "." + Label2Image.COLUMN_DATE + " AS " + Label2Image.COLUMN_DATE + ", " +
            Contact.TABLE_NAME + "." + Label.COLUMN_NAME + ", " + Contact.TABLE_NAME + "." + Label.COLUMN_TYPE + " " +
            "FROM " + Contact2Image.TABLE_NAME + " " +
            "INNER JOIN " + Contact.TABLE_NAME + " " +
            "ON " + Contact.TABLE_NAME + "._id = " + Contact2Image.TABLE_NAME + "." + Contact2Image.COLUMN_CONTACT_ID;

    public static final String IMAGES_WITH_LABELS_NAME = "images_with_labels";
    public static final String IMAGES_WITH_LABELS_CREATE = "CREATE VIEW " + IMAGES_WITH_LABELS_NAME + " AS " +
            "SELECT " + Image.TABLE_NAME + "._id, " + Image.TABLE_NAME + "." + Image.COLUMN_FILENAME + ", " + Image.TABLE_NAME + "." + Image.COLUMN_DATE_TAKEN + ", " + Image.TABLE_NAME + "." + Image.COLUMN_LOCATION + ", " +
            Label2Image.COLUMN_LABEL_ID + ", " + LABELS_WITH_CONTACTS_NAME + "." + Label.COLUMN_NAME + ", " + LABELS_WITH_CONTACTS_NAME + "." + Label.COLUMN_TYPE + " " +
            "FROM " + Image.TABLE_NAME + " " +
            "INNER JOIN " + LABELS_WITH_CONTACTS_NAME + " " +
            "ON " + Image.TABLE_NAME + "._id = " + LABELS_WITH_CONTACTS_NAME + "." + Label2Image.COLUMN_IMAGE_ID + " " +
            "ORDER BY " + Label2Image.COLUMN_IMAGE_ID + " ASC";

    public static final String IMAGES_BY_DAY_NAME = "images_by_day";
    public static final String IMAGES_BY_DAY_DAY_TAKEN = "day_taken";
    public static final String IMAGES_BY_DAY_CREATE = "CREATE VIEW " + IMAGES_BY_DAY_NAME + " AS" +
            " SELECT _id, " + Image.COLUMN_DATE_TAKEN + " - (" + Image.COLUMN_DATE_TAKEN + " % 86400000) AS " + IMAGES_BY_DAY_DAY_TAKEN +
            " FROM " + Image.TABLE_NAME +
            " ORDER BY " + Image.COLUMN_DATE_TAKEN + " DESC";
}
