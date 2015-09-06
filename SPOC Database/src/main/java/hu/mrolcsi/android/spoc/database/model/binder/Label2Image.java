package hu.mrolcsi.android.spoc.database.model.binder;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import hu.mrolcsi.android.spoc.database.model.Image;
import hu.mrolcsi.android.spoc.database.model.Label;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.04.
 * Time: 9:40
 */
@SuppressWarnings("unused")
@DatabaseTable(tableName = "labels2images")
public class Label2Image {
    public static final String TABLE_NAME = "labels2images";
    public static final String COLUMN_LABEL_ID = "label_id";
    public static final String COLUMN_IMAGE_ID = "image_id";
    public static final String COLUMN_DATE = "date";

    @DatabaseField(generatedId = true)
    private int _id;
    @DatabaseField(columnName = COLUMN_LABEL_ID, foreign = true, canBeNull = false, uniqueCombo = true)
    private Label label;
    @DatabaseField(columnName = COLUMN_IMAGE_ID, foreign = true, canBeNull = false, uniqueCombo = true)
    private Image image;
    @DatabaseField(columnName = COLUMN_DATE, dataType = DataType.DATE_LONG)
    private Date date;

    public Label2Image() {
    }

    public Label2Image(Label label, Image image) {
        this.label = label;
        this.image = image;
    }

    public Label2Image(Label label, Image image, Date date) {
        this.label = label;
        this.image = image;
        this.date = date;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
