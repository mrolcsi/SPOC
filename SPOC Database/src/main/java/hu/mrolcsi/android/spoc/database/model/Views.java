package hu.mrolcsi.android.spoc.database.model;

import hu.mrolcsi.android.spoc.database.model.binder.Label2Image;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015. 09. 06.
 * Time: 19:03
 */

public abstract class Views {

    public static final String IMAGES_WITH_LABELS_NAME = "images_with_labels";
    public static final String IMAGES_WITH_LABELS_CREATE = "CREATE VIEW " + IMAGES_WITH_LABELS_NAME + " AS SELECT " +
            Image.TABLE_NAME + "._id, " +
            Image.TABLE_NAME + "." + Image.COLUMN_FILENAME + ", " +
            Image.TABLE_NAME + "." + Image.COLUMN_DATE_TAKEN + ", " +
            Image.TABLE_NAME + "." + Image.COLUMN_LOCATION + ", " +
            Label.TABLE_NAME + "._id AS " + Label2Image.COLUMN_LABEL_ID + ", " +
            Label.TABLE_NAME + "." + Label.COLUMN_NAME + ", " +
            Label.TABLE_NAME + "." + Label.COLUMN_TYPE +
            " FROM " + Image.TABLE_NAME + ", " + Label.TABLE_NAME +
            " INNER JOIN " + Label2Image.TABLE_NAME +
            " ON " + Label2Image.TABLE_NAME + "." + Label2Image.COLUMN_IMAGE_ID + "=" + Image.TABLE_NAME + "._id" +
            " AND " + Label2Image.TABLE_NAME + "." + Label2Image.COLUMN_LABEL_ID + "=" + Label.TABLE_NAME + "._id" +
            " ORDER BY " + Label2Image.COLUMN_IMAGE_ID + " ASC";


    public static final String IMAGES_BY_DAY_NAME = "images_by_day";
    public static final String IMAGES_BY_DAY_DAY_TAKEN = "day_taken";
    public static final String IMAGES_BY_DAY_CREATE = "CREATE VIEW " + IMAGES_BY_DAY_NAME + " AS" +
            " SELECT _id, " + Image.COLUMN_DATE_TAKEN + " - (" + Image.COLUMN_DATE_TAKEN + " % 86400000) AS " + IMAGES_BY_DAY_DAY_TAKEN +
            " FROM " + Image.TABLE_NAME +
            " ORDER BY " + Image.COLUMN_DATE_TAKEN + " DESC";
}
