package angelo.itl.arduinoairqualitymonitor.database;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import angelo.itl.arduinoairqualitymonitor.activity.main.MainActivity;
import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;
import angelo.itl.arduinoairqualitymonitor.R;

public class DataBaseController {
    private GlobalVariable btData;
    private DataBaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;
    private ContentValues values;
    private Cursor cursor;

    public DataBaseController(Context context) {
        this.context = context;
    }

    public DataBaseController open() throws SQLException {
        dbHelper = new DataBaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public enum Average {
        ALL_GAS,
        CO2_AVG_AND_ALL_GAS,
        ONLY_MINOR_GASES,
    }

    public void insertData(String date, String co, String co2, String eth, String nh4, String tol
            , String ace, String hum, String temp, String press) {

        values = new ContentValues();

        values.put(dbHelper.DATE_COLUMN, date);
        values.put(dbHelper.CO_COLUMN, co);
        values.put(dbHelper.CO2_COLUMN, co2);
        values.put(dbHelper.ETHANOL_COLUMN, eth);
        values.put(dbHelper.NH4_COLUMN, nh4);
        values.put(dbHelper.TOLUENE_COLUMN, tol);
        values.put(dbHelper.ACETONE_COLUMN, ace);
        values.put(dbHelper.HUMIDITY_COLUMN, hum);
        values.put(dbHelper.TEMPERATURE_COLUMN, temp);
        values.put(dbHelper.PRESSURE_COLUMN, press);

        database.insert(dbHelper.TABLE_PPM, null, values);
    }

    public Cursor readEntry() {
        btData = btData.getInstance();


        cursor = database.query(dbHelper.TABLE_PPM, dbHelper.allColumns, null, null, null,
                null, null);

        if (cursor != null) cursor.moveToFirst();

        if (cursor.moveToFirst()) btData.isEmptyDatabase(false);
        else btData.isEmptyDatabase(true);

        return cursor;
    }

    public Cursor dateQuery(String date) {
        return database.rawQuery("SELECT * FROM " + dbHelper.TABLE_PPM + " WHERE "
                + dbHelper.DATE_COLUMN + " like '%" + date + "%'", null);
    }

    public Cursor searchQuery(String column, String value, String operator, String orderByColumn,
                              String orderByCriterion, boolean allValue) {

        value = operator.equals(context.getResources().getStringArray(R.array.operator)[0]) ?
                "'%" + value + "%'" : value;
        orderByColumn = orderByColumn == null ? "" : " order by " + orderByColumn;
        orderByCriterion = orderByCriterion == null ? "" : " " + orderByCriterion + " ";
        Log.d("TAGS", "column:" + column + ", value:" + value +
                ", operator:" + operator + ", orderby:" + orderByColumn + ", orderbycri:" + orderByCriterion);
        if (column.equals("*")) {
            String complement = " " + operator + " " + value + " or ";
            String end = " " + operator + " " + value;
            return database.rawQuery("select " + column + " from "
                    + DataBaseHelper.TABLE_PPM
                    + " where "
                    + DataBaseHelper.DATE_COLUMN + complement
                    + DataBaseHelper.CO_COLUMN + complement
                    + DataBaseHelper.CO2_COLUMN + complement
                    + DataBaseHelper.ETHANOL_COLUMN + complement
                    + DataBaseHelper.NH4_COLUMN + complement
                    + DataBaseHelper.TOLUENE_COLUMN + complement
                    + DataBaseHelper.ACETONE_COLUMN + complement
                    + DataBaseHelper.HUMIDITY_COLUMN + complement
                    + DataBaseHelper.TEMPERATURE_COLUMN + complement
                    + DataBaseHelper.PRESSURE_COLUMN + end + orderByColumn + orderByCriterion, null);
        } else {
            value = column.equals(DataBaseHelper.DATE_COLUMN) &&
                    !operator.equals(context.getResources().getStringArray(R.array.operator)[0]) ?
                    "'" + value + "'" : value;
            if (!allValue)
                return database.rawQuery(
                        "select distinct " + column + " from "
                                + DataBaseHelper.TABLE_PPM + " where "
                                + column + " " + operator + " " + value + orderByColumn
                                + orderByCriterion, null);
            else
                return database.rawQuery("select * from "
                        + DataBaseHelper.TABLE_PPM + " where "
                        + column + " " + operator + " " + value + orderByColumn
                        + orderByCriterion, null);
        }
    }

    public Cursor orderBy(String column, String orderForm) {
        return database.rawQuery("SELECT * FROM " + dbHelper.TABLE_PPM +
                " order by " + column + " " + orderForm, null);
    }

    public void deleteRow(String field) {
        database.delete(dbHelper.TABLE_PPM, dbHelper.DATE_COLUMN + " = '" + field + "'", null);
    }

    public boolean recordExists(String field) {
        String query = "select * from " + dbHelper.TABLE_PPM + " where " +
                dbHelper.DATE_COLUMN + " like '%" + field + "%'";
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }


    /**
     * @param cursor
     * @param column
     * @return
     */
    public String[][] buildTableAsMatrix(final Cursor cursor, String column) {
        final String[] names = context.getResources().getStringArray(R.array.columns);
        int rows = cursor.getCount();
        int cols = cursor.getColumnCount();
        if (rows == 0) return null;
        rows = rows == 1 ? rows + 1 : rows;
        String matrix[][] = new String[rows][cols];
        cursor.moveToFirst();
        if (column != null)
            matrix[0][0] = column;
        else for (int j = 0; j < cols; j++) matrix[0][j] = names[j];
        if (rows > 2) {
            // outer for loop
            for (int i = 1; i < rows; i++) {
                for (int j = 0; j < cols; j++) matrix[i][j] = cursor.getString(j);
                cursor.moveToNext();
            }
        } else for (int i = 0; i < cols; i++) {
            matrix[1][i] = cursor.getString(i);
        }
        return matrix;
    }

    public boolean importDb() {
        File exportDir = new File(Environment.getExternalStorageDirectory() + "/" + context.getString(R.string.app_name), "");
        if (!exportDir.exists()) exportDir.mkdirs();

        File file = new File(exportDir.getAbsolutePath(), "GAS_READING.csv");
        FileReader fileReader = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            fileReader = new FileReader(file);
            BufferedReader buffer = new BufferedReader(fileReader);
            String line = "";
            String tableName = dbHelper.TABLE_PPM;
            String columns = "";
            for (int i = 0; i < dbHelper.allColumns.length; i++) {
                if (i == dbHelper.allColumns.length - 1) columns += dbHelper.allColumns[i];
                else columns += dbHelper.allColumns[i] + ",";
            }
            Log.d("columns", columns);
            String str1 = "INSERT INTO " + tableName + " (" + columns + ") values(";
            String str2 = ");";

            db.beginTransaction();
            while ((line = buffer.readLine()) != null) {
                StringBuilder sb = new StringBuilder(str1);
                String[] str = line.split(",");
                sb.append("'" + str[0] + "',");
                for (int i = 1; i < 7; i++)
                    sb.append(str[i].replace(" PPM", "") + ",");
                sb.append(str[7].replace(" %", "") + ",");
                sb.append(str[8].replace(" °C", "") + ",");
                sb.append(str[9].replace(" hPA", ""));
                sb.append(str2);
                try {
                    db.execSQL(sb.toString());
                } catch (Exception e) {
                    db.endTransaction();
                    e.printStackTrace();
                    return false;
                }
                Log.d("TRANSACTION", sb.toString());
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public double[] getAverage(Average which) {
        String query = "select";
        switch (which) {
            case ALL_GAS:
                for (int i = 0; i < DataBaseHelper.gasColumns.length; i++)
                    query += " avg(" + DataBaseHelper.gasColumns[i] + "), ";
                query = query.substring(0, query.length() - 2);
                break;
            case CO2_AVG_AND_ALL_GAS:
                query += " avg(" + DataBaseHelper.CO2_COLUMN + "), (";
                for (int i = 0; i < DataBaseHelper.gasColumns.length; i++)
                    query += i != 1 ? "avg(" + DataBaseHelper.gasColumns[i] + ")+" : "";
                query = query.substring(0, query.length() - 1);
                query += ")/5";
                break;
            case ONLY_MINOR_GASES:
                for (int i = 0; i < DataBaseHelper.gasColumns.length; i++)
                    query += i != 1 ? " avg(" + DataBaseHelper.gasColumns[i] + "), " : "";
                query = query.substring(0, query.length() - 2);
                break;

        }
        query += " from " + DataBaseHelper.TABLE_PPM;
        double[] avg;

        Cursor cursor = database.rawQuery(query, null);
        avg = new double[cursor.getColumnCount()];
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            avg[i] = cursor.getDouble(i);
            Log.d(which+""+i,avg[i]+"");
        }
        return avg;
    }

    public double[] getAverageByDate(String date) {
        double[] avg;
        Cursor cursor = database.rawQuery("select " +
                "avg(" + DataBaseHelper.CO_COLUMN + "), " +
                "avg(" + DataBaseHelper.CO2_COLUMN + "), " +
                "avg(" + DataBaseHelper.ETHANOL_COLUMN + "), " +
                "avg(" + DataBaseHelper.NH4_COLUMN + "), " +
                "avg(" + DataBaseHelper.TOLUENE_COLUMN + "), " +
                "avg(" + DataBaseHelper.ACETONE_COLUMN + ") " +
                "from " + DataBaseHelper.TABLE_PPM + " " +
                "where date(" + DataBaseHelper.DATE_COLUMN + ") = date('" + date + "')", null);
        avg = new double[cursor.getColumnCount()];
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getColumnCount(); i++)
            avg[i] = cursor.getDouble(i);
        return avg;
    }

    public float[] getAllAverageByDate(String date) {
        float[] avg;
        Cursor cursor = database.rawQuery("select " +
                "avg(" + DataBaseHelper.CO_COLUMN + "), " +
                "avg(" + DataBaseHelper.CO2_COLUMN + "), " +
                "avg(" + DataBaseHelper.ETHANOL_COLUMN + "), " +
                "avg(" + DataBaseHelper.NH4_COLUMN + "), " +
                "avg(" + DataBaseHelper.TOLUENE_COLUMN + "), " +
                "avg(" + DataBaseHelper.ACETONE_COLUMN + "), " +
                "avg(" + DataBaseHelper.HUMIDITY_COLUMN + "), " +
                "avg(" + DataBaseHelper.TEMPERATURE_COLUMN + "), " +
                "avg(" + DataBaseHelper.PRESSURE_COLUMN + ") " +
                "from " + DataBaseHelper.TABLE_PPM + " " +
                "where date(" + DataBaseHelper.DATE_COLUMN + ") = date('" + date + "')", null);
        avg = new float[cursor.getColumnCount()];
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getColumnCount(); i++)
            avg[i] = cursor.getFloat(i);
        return avg;
    }

    public float[] getAllMaxMinByDate(String date, boolean max) {
        float[] maxMin;
        String maxmin = max ? "max(" : "min(";
        Cursor cursor = database.rawQuery("select " +
                maxmin + DataBaseHelper.CO_COLUMN + "), " +
                maxmin + DataBaseHelper.CO2_COLUMN + "), " +
                maxmin + DataBaseHelper.ETHANOL_COLUMN + "), " +
                maxmin + DataBaseHelper.NH4_COLUMN + "), " +
                maxmin + DataBaseHelper.TOLUENE_COLUMN + "), " +
                maxmin + DataBaseHelper.ACETONE_COLUMN + "), " +
                maxmin + DataBaseHelper.HUMIDITY_COLUMN + "), " +
                maxmin + DataBaseHelper.TEMPERATURE_COLUMN + "), " +
                maxmin + DataBaseHelper.PRESSURE_COLUMN + ") " +
                "from " + DataBaseHelper.TABLE_PPM + " " +
                "where date(" + DataBaseHelper.DATE_COLUMN + ") = date('" + date + "')", null);
        maxMin = new float[cursor.getColumnCount()];
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getColumnCount(); i++)
            maxMin[i] = cursor.getFloat(i);
        return maxMin;
    }

    public Cursor getTimestampsByDate() {
        return database.rawQuery("select (dates*1000) from (select strftime('%s',date(" +
                DataBaseHelper.DATE_COLUMN + ")) as dates from " + DataBaseHelper.TABLE_PPM + ")" +
                " group by dates", null);
    }

    public Cursor getCo2AverageByDate() {
        return database.rawQuery("select avg(co2) as promedio_co2" +
                " from (select date(" + DataBaseHelper.DATE_COLUMN + ") as dates," +
                " " + DataBaseHelper.CO2_COLUMN + " as co2" +
                " from ppm) " + DataBaseHelper.TABLE_PPM + " group by dates", null);
    }

    public Cursor getGasesAverageByDate() {
        return database.rawQuery("select avg(co) as promedio_co, avg(ethanol) as promedio_ethanol," +
                " avg(nh4) as promedio_nh4, avg(toluene) as promedio_tolueno, avg(acetone)" +
                " as promedio_acetona from (select date(" + DataBaseHelper.DATE_COLUMN + ") as dates," +
                " " + DataBaseHelper.CO_COLUMN + " as co," +
                " " + DataBaseHelper.ETHANOL_COLUMN + " as ethanol," +
                " " + DataBaseHelper.NH4_COLUMN + " as nh4," +
                " " + DataBaseHelper.TOLUENE_COLUMN + " as toluene," +
                " " + DataBaseHelper.ACETONE_COLUMN + " as acetone" +
                " from ppm) " + DataBaseHelper.TABLE_PPM + " group by dates", null);
    }

    public void exportDB() {

        File exportDir = new File(Environment.getExternalStorageDirectory() + "/" + context.getString(R.string.app_name), "");
        if (!exportDir.exists()) exportDir.mkdirs();

        File file = new File(exportDir.getAbsolutePath(), "GAS_READING.csv");
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + dbHelper.TABLE_PPM, null);
            int rows = cursor.getCount();
            int cols = cursor.getColumnCount();
            cursor.moveToFirst();
            FileWriter writer = new FileWriter(file);
            for (int i = 1; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    writer.write(cursor.getString(j));
                    if (j + 1 < cols) writer.write(",");
                }
                writer.write("\n");
                cursor.moveToNext();
            }
            cursor.close();
            writer.close();
            Snackbar.make(((MainActivity) context).findViewById(android.R.id.content)
                    , context.getString(R.string.route_data_saved) +
                            " " + file, Snackbar.LENGTH_LONG)
                    .show();
        } catch (Exception sqlEx) {
            sqlEx.printStackTrace();
        }
    }

}
