package hu.mrolcsi.android.spoc.database.model;

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
    public static final String COLUMN_CONTACT_KEY = "key";
    //public static final String COLUMN_FAMILY_NAME = "family_name";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TYPE = "type";

    @DatabaseField(generatedId = true)
    private int _id;
    @DatabaseField(columnName = COLUMN_CONTACT_KEY, unique = true, index = true)
    private String contactKey;
    @DatabaseField(columnName = COLUMN_NAME)
    private String name;
    @DatabaseField(columnName = COLUMN_TYPE, defaultValue = "CONTACT")
    private String type;

    public Contact() {
    }

//    public Contact(String contactKey, String familyName, byte[] photo) {
//        this.contactKey = contactKey;
//        this.familyName = familyName;
//        this.photo = photo;
//    }

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

//    public String getFamilyName() {
//        return familyName;
//    }

//    public void setFamilyName(String familyName) {
//        this.familyName = familyName;
//    }
}
