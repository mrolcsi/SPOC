package hu.mrolcsi.android.spoc.database.model.binder;

import android.graphics.RectF;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.04.
 * Time: 9:32
 */
@SuppressWarnings("unused")
@DatabaseTable(tableName = "contacts2images")
public class Contact2Image extends RectF {
    public static final String TABLE_NAME = "contacts2images";
    public static final String COLUMN_CONTACT_ID = "contact_id";
    public static final String COLUMN_IMAGE_ID = "image_id";
    public static final String COLUMN_X1 = "x1";
    public static final String COLUMN_Y1 = "y1";
    public static final String COLUMN_X2 = "x2";
    public static final String COLUMN_Y2 = "y2";
    public static final String COLUMN_DATE = "date";

    @DatabaseField(generatedId = true)
    private int _id;
    @DatabaseField(uniqueCombo = true, columnName = COLUMN_CONTACT_ID)
    private int contactId;
    @DatabaseField(uniqueCombo = true, columnName = COLUMN_IMAGE_ID, canBeNull = false)
    private int imageId;
    @DatabaseField(columnName = COLUMN_X1)
    private int x1;
    @DatabaseField(columnName = COLUMN_Y1)
    private int y1;
    @DatabaseField(columnName = COLUMN_X2)
    private int x2;
    @DatabaseField(columnName = COLUMN_Y2)
    private int y2;
    @DatabaseField(columnName = COLUMN_DATE, dataType = DataType.DATE_LONG)
    private Date date;

    public Contact2Image() {
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
