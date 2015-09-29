package hu.mrolcsi.android.spoc.database.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.04.
 * Time: 8:53
 */

@SuppressWarnings("unused")
@DatabaseTable(tableName = "images")
public class Image {
    public static final String TABLE_NAME = "images";
    public static final String COLUMN_FOREIGN_ID = "image_id";
    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_MEDIASTORE_ID = "MediaStore_id";
    public static final String COLUMN_DATE_TAKEN = "date_taken";
    public static final String COLUMN_LOCATION = "location";

    @DatabaseField(generatedId = true)
    private int _id;
    @DatabaseField(columnName = COLUMN_FILENAME, canBeNull = false, unique = true, index = true)
    private String filename;
    @DatabaseField(columnName = COLUMN_MEDIASTORE_ID, unique = true, index = true)
    private long mediaStoreId;
    @DatabaseField(columnName = COLUMN_DATE_TAKEN, dataType = DataType.DATE_LONG)
    private Date dateTaken;
    @DatabaseField(columnName = COLUMN_LOCATION)
    private String location;

    public Image() {
    }

    public Image(String filename) {
        this.filename = filename;
    }

    public Image(String filename, int mediaStoreId, Date dateTaken) {
        this.filename = filename;
        this.mediaStoreId = mediaStoreId;
        this.dateTaken = dateTaken;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getMediaStoreId() {
        return mediaStoreId;
    }

    public void setMediaStoreId(long mediaStoreId) {
        this.mediaStoreId = mediaStoreId;
    }

    public Date getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(Date dateTaken) {
        this.dateTaken = dateTaken;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
