package hu.mrolcsi.android.spoc.database.model.binder;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import hu.mrolcsi.android.spoc.database.model.Contact;
import hu.mrolcsi.android.spoc.database.model.Image;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.04.
 * Time: 9:32
 */
@SuppressWarnings("unused")
@DatabaseTable(tableName = "contacts2images")
public class Contact2Image {
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
    @DatabaseField(columnName = COLUMN_CONTACT_ID, foreign = true, canBeNull = false)
    private Contact contact;
    @DatabaseField(columnName = COLUMN_IMAGE_ID, foreign = true, canBeNull = false)
    private Image image;
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

    public Contact2Image(Contact contact, Image image) {
        this.contact = contact;
        this.image = image;
    }

    public Contact2Image(Contact contact, Image image, int x1, int y1, int x2, int y2, Date date) {
        this.contact = contact;
        this.image = image;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.date = date;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
