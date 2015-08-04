package hu.mrolcsi.android.spoc.database.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.04.
 * Time: 9:26
 */
@SuppressWarnings("unused")
@DatabaseTable(tableName = "labels")
public class Label {
    public static final String TABLE_NAME = "labels";
    public static final String COLUMN_FOREIGN_ID = "label_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CREATION_DATE = "creation_date";

    @DatabaseField(generatedId = true)
    private int _id;
    @DatabaseField(columnName = COLUMN_NAME, canBeNull = false)
    private String name;
    @DatabaseField(columnName = COLUMN_CREATION_DATE, dataType = DataType.DATE_LONG)
    private Date creationDate;

    public Label() {
    }

    public Label(String name) {
        this.name = name;
    }

    public Label(String name, Date creationDate) {
        this.name = name;
        this.creationDate = creationDate;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
