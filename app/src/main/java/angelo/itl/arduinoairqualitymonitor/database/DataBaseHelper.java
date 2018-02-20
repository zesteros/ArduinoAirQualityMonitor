package angelo.itl.arduinoairqualitymonitor.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {
    //FILE DB PROP
    public static final String DATA_BASE_PATH = "/data/data/angelo.itl.arduinoairqualitymonitor/databases/";
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "airmonitor.db";
    //TABLE AND COLUMNS VAR
    public static final String TABLE_PPM = "ppm";
    public static final String DATE_COLUMN = "dateppm";
    public static final String CO_COLUMN = "coppm";
    public static final String CO2_COLUMN = "co2ppm";
    public static final String ETHANOL_COLUMN = "ethanolppm";
    public static final String NH4_COLUMN = "nh4ppm";
    public static final String TOLUENE_COLUMN = "tolueneppm";
    public static final String ACETONE_COLUMN = "acetoneppm";
    public static final String HUMIDITY_COLUMN = "humidity";
    public static final String TEMPERATURE_COLUMN = "temperature";
    public static final String PRESSURE_COLUMN = "pressure";
    public static String[] allColumns = {
            DATE_COLUMN, CO_COLUMN,
            CO2_COLUMN, ETHANOL_COLUMN, NH4_COLUMN,
            TOLUENE_COLUMN, ACETONE_COLUMN, HUMIDITY_COLUMN,
            TEMPERATURE_COLUMN, PRESSURE_COLUMN
    };
    public static String[] gasColumns = {
            CO_COLUMN, CO2_COLUMN, ETHANOL_COLUMN, NH4_COLUMN,
            TOLUENE_COLUMN, ACETONE_COLUMN
    };
    //SCRIPT
    private static final String CREATE_TABLE =
            "create table " +
                    TABLE_PPM
                    + "(" +
                    DATE_COLUMN + " date primary key not null," +//must be date type
                    CO_COLUMN + " float," +//here
                    CO2_COLUMN + " float," +
                    ETHANOL_COLUMN + " float," +
                    NH4_COLUMN + " float," +
                    TOLUENE_COLUMN + " float," +
                    ACETONE_COLUMN + " float," +
                    HUMIDITY_COLUMN + " float," +
                    TEMPERATURE_COLUMN + " float," +
                    PRESSURE_COLUMN + " float" +//to here must be float type
                    ");";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    public String getTableAsString() {
        SQLiteDatabase db = this.getReadableDatabase();
        String tableString = String.format("Table %s:\n", TABLE_PPM);
        Cursor allRows = db.rawQuery("SELECT * FROM " + TABLE_PPM, null);
        if (allRows.moveToFirst()) {
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name : columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PPM);
        onCreate(db);
    }
}