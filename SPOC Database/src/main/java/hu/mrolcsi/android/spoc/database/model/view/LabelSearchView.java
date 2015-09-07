package hu.mrolcsi.android.spoc.database.model.view;

import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.database.model.Label;
import hu.mrolcsi.android.spoc.database.model.binder.Label2Image;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015. 09. 06.
 * Time: 19:03
 */

public class LabelSearchView {

    public static final String VIEW_NAME = "labelSearchView";
    public static final String CREATE_SQL = "CREATE VIEW " + VIEW_NAME + " " +
            "AS " +
            "SELECT " + Image.TABLE_NAME + "._id, " + Image.TABLE_NAME + "." + Image.COLUMN_FILENAME + ", " + Image.TABLE_NAME + "." + Image.COLUMN_DATE_TAKEN + ", " + Image.TABLE_NAME + "." + Image.COLUMN_LOCATION + ", " +
            Label.TABLE_NAME + "." + Label.COLUMN_NAME + ", " + Label.TABLE_NAME + "." + Label.COLUMN_TYPE + " " +
            "FROM " + Image.TABLE_NAME + ", " + Label.TABLE_NAME + " " +
            "INNER JOIN " + Label2Image.TABLE_NAME + " " +
            "ON " + Label2Image.TABLE_NAME + "." + Label2Image.COLUMN_IMAGE_ID + "=" + Image.TABLE_NAME + "._id " +
            "AND " + Label2Image.TABLE_NAME + "." + Label2Image.COLUMN_LABEL_ID + "=" + Label.TABLE_NAME + "._id " +
            "ORDER BY " + Image.TABLE_NAME + "._id ASC;";
}
