package hu.mrolcsi.android.spoc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import hu.mrolcsi.android.spoc.database.model.*;
import hu.mrolcsi.android.spoc.database.model.binder.Contact2Image;
import hu.mrolcsi.android.spoc.database.model.binder.Label2Image;

import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.04.
 * Time: 9:46
 */

@SuppressWarnings("unused")
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    public static final int DATABASE_VERSION = 12;
    private static final String DATABASE_NAME = "spoc.db";
    private static Context context;
    private static DatabaseHelper ourInstance;

    private RuntimeExceptionDao<Image, Integer> imagesDao = null;
    private RuntimeExceptionDao<Contact, Integer> contactsDao = null;
    private RuntimeExceptionDao<Label, Integer> labelsDao = null;
    private RuntimeExceptionDao<Contact2Image, Integer> contacts2ImagesDao = null;
    private RuntimeExceptionDao<Label2Image, Integer> labels2ImagesDao = null;

    private DatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
    }

    public static void init(Context appContext) {
        context = appContext;
        ourInstance = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance() {
        if (ourInstance == null) {
            ourInstance = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        return ourInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        Log.i(getClass().getSimpleName(), "onCreate");
        try {
            TableUtils.createTable(connectionSource, Image.class);
            TableUtils.createTable(connectionSource, Label.class);
            TableUtils.createTable(connectionSource, Label2Image.class);
            TableUtils.createTable(connectionSource, Contact.class);
            TableUtils.createTable(connectionSource, Contact2Image.class);
            database.execSQL(Views.LABELS_WITH_CONTACTS_CREATE);
            database.execSQL(Views.IMAGES_WITH_LABELS_CREATE);
            database.execSQL(Views.IMAGES_BY_DAY_CREATE);
        } catch (SQLException e) {
            Log.w(getClass().getName(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Log.i(getClass().getSimpleName(), "onUpgrade | oldVersion=" + oldVersion + " newVersion=" + newVersion);
        if (oldVersion < 2) {
            //Add location text to Images table.
            database.execSQL("ALTER TABLE " + Image.TABLE_NAME + " ADD COLUMN " + Image.COLUMN_LOCATION + " VARCHAR");
        }
        if (oldVersion < 3) {
            //Create Labels table and Label2Image table.
            try {
                TableUtils.createTableIfNotExists(connectionSource, Label.class);
                TableUtils.createTableIfNotExists(connectionSource, Label2Image.class);
            } catch (SQLException e) {
                Log.w(getClass().getSimpleName(), e);
            }
        }
        if (oldVersion < 4) {
            //update labels with new types
            try {
                TableUtils.clearTable(connectionSource, Label2Image.class);
                TableUtils.clearTable(connectionSource, Label.class);
            } catch (SQLException e) {
                Log.w(getClass().getSimpleName(), e);
            }
        }
        if (oldVersion < 6) {
            //5: add LabelSearch view
            //6: add type column to search-view
            database.execSQL("DROP VIEW IF EXISTS " + Views.IMAGES_WITH_LABELS_NAME);
            database.execSQL(Views.IMAGES_WITH_LABELS_CREATE);
        }
        if (oldVersion < 7) {
            //rename some label types

            ContentValues values = new ContentValues();
            values.put(Label.COLUMN_TYPE, LabelType.LOCATION_LOCALITY.name());
            database.update(Label.TABLE_NAME, values, "type = ?", new String[]{"LOCATION_LOCALITY"});

            values.clear();
            values.put(Label.COLUMN_TYPE, LabelType.LOCATION_COUNTRY.name());
            database.update(Label.TABLE_NAME, values, "type = ?", new String[]{"LOCATION_COUNTRY"});
        }
        if (oldVersion < 8) {
            //add Date Taken to view
            database.execSQL("DROP VIEW IF EXISTS " + Views.IMAGES_WITH_LABELS_NAME);
            database.execSQL(Views.IMAGES_WITH_LABELS_CREATE);
        }
        if (oldVersion < 9) {
            //rename labelSearchView to images_with_labels
            //add label_id column
            database.execSQL("DROP VIEW IF EXISTS labelSearchView");
            database.execSQL("DROP VIEW IF EXISTS " + Views.IMAGES_WITH_LABELS_NAME);
            database.execSQL(Views.IMAGES_WITH_LABELS_CREATE);
            //create ImagesByDay view
            database.execSQL("DROP VIEW IF EXISTS " + Views.IMAGES_BY_DAY_NAME);
            database.execSQL(Views.IMAGES_BY_DAY_CREATE);
        }
        if (oldVersion < 10) {
            try {
                //create Contacts and Contacts2Images tables
                TableUtils.createTable(connectionSource, Contact.class);
                TableUtils.createTable(connectionSource, Contact2Image.class);
            } catch (SQLException e) {
                Log.w(getClass().getSimpleName(), e);
            }

            //remove unused labels

            database.delete(Label.TABLE_NAME, "type IN (?,?,?,?,?)",
                    new String[]{"DATE_YEAR_NUMERIC", "DATE_MONTH_NUMERIC", "DATE_DAY_NUMERIC", "PEOPLE_FIRSTNAME_TEXT", "PEOPLE_LASTNAME_TEXT"});

            //rename label types

            ContentValues values = new ContentValues();
            values.put(Label.COLUMN_TYPE, LabelType.LOCATION_LOCALITY.name());
            database.update(Label.TABLE_NAME, values, "type = ?", new String[]{"LOCATION_LOCALITY_TEXT"});

            values.clear();
            values.put(Label.COLUMN_TYPE, LabelType.LOCATION_COUNTRY.name());
            database.update(Label.TABLE_NAME, values, "type = ?", new String[]{"LOCATION_COUNTRY_TEXT"});

            values.clear();
            values.put(Label.COLUMN_TYPE, LabelType.DATE_DAY.name());
            database.update(Label.TABLE_NAME, values, "type = ?", new String[]{"DATE_DAY_TEXT"});

            values.clear();
            values.put(Label.COLUMN_TYPE, LabelType.DATE_MONTH.name());
            database.update(Label.TABLE_NAME, values, "type = ?", new String[]{"DATE_MONTH_TEXT"});

            values.clear();
            values.put(Label.COLUMN_TYPE, LabelType.FOLDER.name());
            database.update(Label.TABLE_NAME, values, "type = ?", new String[]{"DIRECTORY_TEXT"});
        }
        if (oldVersion < 11) {
            //make contactId and imageId in contacts2images unique
            try {
                TableUtils.dropTable(connectionSource, Contact2Image.class, true);
                TableUtils.createTable(connectionSource, Contact2Image.class);
            } catch (SQLException e) {
                Log.w(getClass().getSimpleName(), e);
            }
        }
        if (oldVersion < 12) {
            //add images_with_contacts helper view and add contacts to images_with_labels view
            database.execSQL("DROP VIEW IF EXISTS " + Views.LABELS_WITH_CONTACTS_NAME);
            database.execSQL(Views.LABELS_WITH_CONTACTS_CREATE);
            database.execSQL("DROP VIEW IF EXISTS " + Views.IMAGES_WITH_LABELS_NAME);
            database.execSQL(Views.IMAGES_WITH_LABELS_CREATE);
        }
    }

    @Override
    public void close() {
        super.close();
        imagesDao = null;
        contactsDao = null;
        labelsDao = null;
        contacts2ImagesDao = null;
        labels2ImagesDao = null;
    }

    @Deprecated
    public RuntimeExceptionDao<Image, Integer> getImagesDao() {
        if (imagesDao == null) imagesDao = getRuntimeExceptionDao(Image.class);
        return imagesDao;
    }

    @Deprecated
    public RuntimeExceptionDao<Contact, Integer> getContactsDao() {
        if (contactsDao == null) contactsDao = getRuntimeExceptionDao(Contact.class);
        return contactsDao;
    }

    @Deprecated
    public RuntimeExceptionDao<Label, Integer> getLabelsDao() {
        if (labelsDao == null) labelsDao = getRuntimeExceptionDao(Label.class);
        return labelsDao;
    }

    @Deprecated
    public RuntimeExceptionDao<Contact2Image, Integer> getContacts2ImagesDao() {
        if (contacts2ImagesDao == null) contacts2ImagesDao = getRuntimeExceptionDao(Contact2Image.class);
        return contacts2ImagesDao;
    }

    @Deprecated
    public RuntimeExceptionDao<Label2Image, Integer> getLabels2ImagesDao() {
        if (labels2ImagesDao == null) labels2ImagesDao = getRuntimeExceptionDao(Label2Image.class);
        return labels2ImagesDao;
    }
}
