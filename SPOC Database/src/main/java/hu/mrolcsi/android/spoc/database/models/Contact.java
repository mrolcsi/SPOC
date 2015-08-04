package hu.mrolcsi.android.spoc.database.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.04.
 * Time: 9:12
 */
@SuppressWarnings("unused")
@DatabaseTable(tableName = "contacts")
public class Contact {
    public static final String TABLE_NAME = "contacts";
    public static final String COLUMN_CONTACT_KEY = "contact_key";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHOTO = "photo";

    @DatabaseField(generatedId = true)
    private int _id;
    @DatabaseField(columnName = COLUMN_CONTACT_KEY, unique = true, index = true)
    private String contactKey;
    @DatabaseField(columnName = COLUMN_NAME, canBeNull = false)
    private String name;
    @DatabaseField(columnName = COLUMN_PHOTO, dataType = DataType.BYTE_ARRAY)
    private byte[] photo;

    public Contact() {
    }

    public Contact(String name) {
        this.name = name;
    }

    public Contact(String contactKey, String name, byte[] photo) {
        this.contactKey = contactKey;
        this.name = name;
        this.photo = photo;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getContactKey() {
        return contactKey;
    }

    public void setContactKey(String contactKey) {
        this.contactKey = contactKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }
}
