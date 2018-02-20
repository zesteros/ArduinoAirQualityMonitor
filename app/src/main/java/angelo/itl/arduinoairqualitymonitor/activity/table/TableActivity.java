package angelo.itl.arduinoairqualitymonitor.activity.table;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.*;
import android.app.*;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import angelo.itl.arduinoairqualitymonitor.R;
import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;
import angelo.itl.arduinoairqualitymonitor.database.DataBaseController;
import angelo.itl.arduinoairqualitymonitor.database.DataBaseHelper;
import angelo.itl.arduinoairqualitymonitor.util.graphic.TableFixHeaders;
import angelo.itl.arduinoairqualitymonitor.adapter.table.MatrixTableAdapter;


public class TableActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, TextWatcher {

    private DataBaseController dbController;
    private static DataBaseHelper dbHelper;
    private GlobalVariable btData;
    private FloatingActionButton floatTopButton;
    private static TableFixHeaders table;

    private final String formatQuery = "yyyy-MM-dd";
    private final String ASC = "asc";
    private final String DESC = "desc";
    private SimpleDateFormat queryDateFormat;
    private String currentDate;
    private MenuItem backMenuItem, searchMenuItem, deleteMenuItem, saveMenuItem, sortMenuItem;
    private MatrixTableAdapter<String> allDataAdapter, queryAdapter;
    private int year;
    private int month;
    private int day;
    private Calendar calendar;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private Spinner filterSpinner, operatorSpinner, orderBySpinner, orderByCriterionSpinner;
    private String filter;
    private EditText search;
    private CheckBox showAllData;
    private int pos;
    private String textToSearch;
    private String operator;
    private Button clearButton;
    private boolean doubleBackToExitPressedOnce;
    private String orderBy;
    private String orderByCriterion;
    private TextView orderByCriterionTextView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_activity);
        queryDateFormat = new SimpleDateFormat(formatQuery);
        calendar = Calendar.getInstance();
        //android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.subtitle_register);

        appBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbar.setTitle(getString(R.string.expand));
        appBarLayout.setExpanded(false);
        search = (EditText) findViewById(R.id.data_to_search);
        filterSpinner = (Spinner) findViewById(R.id.filter);
        showAllData = (CheckBox) findViewById(R.id.show_all_data);
        operatorSpinner = (Spinner) findViewById(R.id.filter_amount);
        clearButton = (Button) findViewById(R.id.clear_button);
        orderBySpinner = (Spinner) findViewById(R.id.filter_order);
        orderByCriterionSpinner = (Spinner) findViewById(R.id.filter_order_criterion);
        orderByCriterionTextView = (TextView) findViewById(R.id.order_by_criterion);
        clearButton.setOnClickListener(this);

        setAdaptersToSpinners();

        setOperatorListener();
        setOrderByListener();
        setOrderByCriterionListener();

        filterSpinner.setOnItemSelectedListener(this);
        search.addTextChangedListener(this);
        showAllData.setOnClickListener(this);
        btData = btData.getInstance();
        dbHelper = new DataBaseHelper(this);
        table = (TableFixHeaders) findViewById(R.id.table);
        floatTopButton = (FloatingActionButton) findViewById(R.id.fab_close_bar);
        floatTopButton.setOnClickListener(this);
        dbController = new DataBaseController(this);
        dbController.open();
        Cursor cursor = dbController.readEntry();
        allDataAdapter = new MatrixTableAdapter<String>(this, dbController.buildTableAsMatrix(cursor, null), true);
        restoreTable(allDataAdapter);
        dbController.close();
    }

    /**
     * @param adapter
     */
    private void restoreTable(MatrixTableAdapter adapter) {
        table.setAdapter(adapter);
    }

    public void setAdaptersToSpinners() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(this,
                R.array.columns_search, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> amountAdapter = ArrayAdapter.createFromResource(this,
                R.array.operator_name, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> orderByAdapter = ArrayAdapter.createFromResource(this,
                R.array.columns_order, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> orderByCriterionAdapter = ArrayAdapter.createFromResource(this,
                R.array.columns_order_by_criterion, android.R.layout.simple_spinner_item);
        amountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderByCriterionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        operatorSpinner.setAdapter(amountAdapter);
        filterSpinner.setAdapter(filterAdapter);
        orderBySpinner.setAdapter(orderByAdapter);
        orderByCriterionSpinner.setAdapter(orderByCriterionAdapter);
    }

    public void setOrderByCriterionListener() {
        orderByCriterionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        setOrderByCriterion(null);
                        break;
                    case 1:
                        setOrderByCriterion("desc");
                        break;
                    case 2:
                        setOrderByCriterion("asc");
                        break;
                }
                if (getTextToSearch() == null) setTextToSearch("");
                queryToDatabase();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void setOrderByListener() {
        orderBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    orderByCriterionTextView.setVisibility(View.INVISIBLE);
                    orderByCriterionSpinner.setVisibility(View.INVISIBLE);
                } else {
                    orderByCriterionTextView.setVisibility(View.VISIBLE);
                    orderByCriterionSpinner.setVisibility(View.VISIBLE);
                }
                switch (position) {
                    case 0:
                        setOrderByCriterion(null);
                        setOrderBy(null);
                        break;
                    case 1:
                        setOrderBy(DataBaseHelper.DATE_COLUMN);
                        break;
                    case 2:
                        setOrderBy(DataBaseHelper.CO_COLUMN);
                        break;
                    case 3:
                        setOrderBy(DataBaseHelper.CO2_COLUMN);
                        break;
                    case 4:
                        setOrderBy(DataBaseHelper.ETHANOL_COLUMN);
                        break;
                    case 5:
                        setOrderBy(DataBaseHelper.NH4_COLUMN);
                        break;
                    case 6:
                        setOrderBy(DataBaseHelper.TOLUENE_COLUMN);
                        break;
                    case 7:
                        setOrderBy(DataBaseHelper.ACETONE_COLUMN);
                        break;
                    case 8:
                        setOrderBy(DataBaseHelper.HUMIDITY_COLUMN);
                        break;
                    case 9:
                        setOrderBy(DataBaseHelper.TEMPERATURE_COLUMN);
                        break;
                    case 10:
                        setOrderBy(DataBaseHelper.PRESSURE_COLUMN);
                        break;
                }
                if (getTextToSearch() == null) setTextToSearch("");
                queryToDatabase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void setOperatorListener() {
        operatorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        setOperator(getResources().getStringArray(R.array.operator)[0]);
                        break;
                    case 1:
                        setOperator(getResources().getStringArray(R.array.operator)[1]);
                        break;
                    case 2:
                        setOperator(getResources().getStringArray(R.array.operator)[2]);
                        break;
                    case 3:
                        setOperator(getResources().getStringArray(R.array.operator)[3]);
                        break;
                    case 4:
                        setOperator(getResources().getStringArray(R.array.operator)[4]);
                        break;
                    case 5:
                        setOperator(getResources().getStringArray(R.array.operator)[5]);
                        break;
                }
                if (getTextToSearch() != null) queryToDatabase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.show_all_data) {
            if (getTextToSearch() != null) queryToDatabase();
        } else if (v.getId() == R.id.fab_close_bar) {
            appBarLayout.setExpanded(false);
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } else
            clearAllUIData();
    }

    public void clearAllUIData() {
        filterSpinner.setSelection(0);
        operatorSpinner.setSelection(0);
        orderBySpinner.setSelection(0);
        orderByCriterionSpinner.setSelection(0);
        showAllData.setChecked(false);
        restoreTable(allDataAdapter);
        search.setText("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        TextView dataToSearchLabel;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_bar_return:
                restoreTable(allDataAdapter);
                hideBackActionbar();
                break;
            case R.id.action_bar_discard:
                showDeleteDialog();
                break;
            case R.id.action_bar_search:
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(view, 0);
                }
                appBarLayout.setExpanded(true);
                break;
            case R.id.save_database:
                showSaveDialog();
                break;
            /*case R.id.sort:
                dataToSearchLabel = (TextView) findViewById(R.id.data_to_search_label);
                dataToSearchLabel.setVisibility(View.INVISIBLE);
                search.setVisibility(View.INVISIBLE);
                clearButton.setVisibility(View.INVISIBLE);
                //showSortDialog();
                break;*/

        }
        return super.onOptionsItemSelected(item);
    }

    public void showSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.AlertDialogLight);
        builder
                .setTitle(getString(R.string.export_database_dialog_title))
                .setMessage(getString(R.string.export_database_dialog_message))
                .setNegativeButton(getString(R.string.alert_dialog_cancel), null)
                .setPositiveButton(R.string.alert_dialog_accept,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dbController.exportDB();
                            }
                        });

        builder.show();
    }

    public void showDateTimeDialog() {
        TimePickerDialog td = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        search.setText(getDateTimeFormatted(getYear(), getMonth(), getDay(), hourOfDay, minute));
                    }
                },
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                true
        );
        td.show();
    }

    public void showDateDialog(final boolean dateTime) {
        DatePickerDialog dialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        setYear(year);
                        setMonth(monthOfYear);
                        setDay(dayOfMonth);
                        if (!dateTime) {
                            search.setText(getDateFormatted(year, monthOfYear, dayOfMonth));
                            queryToDatabase();
                        } else showDateTimeDialog();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }


    /*public void sortTable(int column, String sortForm) {
        dbController.open();
        Cursor cursor = dbController.orderBy(dbHelper.allColumns[column], sortForm);
        if (dbController.buildTableAsMatrix(cursor, null) != null) {
            queryAdapter =
                    new MatrixTableAdapter<String>(this, dbController.buildTableAsMatrix(cursor, null), true);
            restoreTable(queryAdapter);
            showBackActionbar();
        }
        dbController.close();
    }*/

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setColumnItemPos(position);
        if (position == 0) showAllData.setVisibility(View.INVISIBLE);
        else showAllData.setVisibility(View.VISIBLE);
        switch (position) {
            case 0:
                setFilter("*");
                break;
            case 1:
                new AlertDialog.Builder(this, R.style.AlertDialogLight)
                        .setTitle(getString(R.string.dialog_search_title))
                        .setMessage(getString(R.string.dialog_search_message))
                        .setPositiveButton(getString(R.string.dialog_search_date_button),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        showDateDialog(false);
                                    }
                                }).setNegativeButton(getString(R.string.dialog_search_date_time_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showDateDialog(true);
                            }
                        }).show();
                setFilter(DataBaseHelper.DATE_COLUMN);
                break;
            case 2:
                setFilter(DataBaseHelper.CO_COLUMN);
                break;
            case 3:
                setFilter(DataBaseHelper.CO2_COLUMN);
                break;
            case 4:
                setFilter(DataBaseHelper.ETHANOL_COLUMN);
                break;
            case 5:
                setFilter(DataBaseHelper.NH4_COLUMN);
                break;
            case 6:
                setFilter(DataBaseHelper.TOLUENE_COLUMN);
                break;
            case 7:
                setFilter(DataBaseHelper.ACETONE_COLUMN);
                break;
            case 8:
                setFilter(DataBaseHelper.HUMIDITY_COLUMN);
                break;
            case 9:
                setFilter(DataBaseHelper.TEMPERATURE_COLUMN);
                break;
            case 10:
                setFilter(DataBaseHelper.PRESSURE_COLUMN);
                break;
        }
        Log.d("FILTER ", getFilter());
        if (getTextToSearch() != null) queryToDatabase();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setTextToSearch(s + "");
        queryToDatabase();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBackPressed() {
        appBarLayout.setExpanded(false);
        clearAllUIData();
        exitWhenBackButtonIsPressedTwice();
    }

    /**
     * Method to wait 2 seconds until back button is pressed if is pressed get out of the app
     */
    public void exitWhenBackButtonIsPressedTwice() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        showToast(R.string.toast_press_again_to_exit);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    /**
     * @param id the id of string resource for show message
     */
    public void showToast(int id) {
        View view = findViewById(android.R.id.content);
        Snackbar.make(view, getString(id), Snackbar.LENGTH_LONG).show();

    }

    /**
     * Method to query to the database with all parameters established by user in
     * spinners and input fields
     */
    public void queryToDatabase() {
        dbController.open();
        Cursor cursor = null;
        /*Get the cursor from database controller with parameters*/
        try {
            Log.d("QUERY", getFilter() + "," + getTextToSearch() + "," + getOperator() + "," + getOrderBy() + "," + getOrderByCriterion());
            cursor = dbController.searchQuery(
                    getFilter(),
                    getTextToSearch(),
                    getOperator(),
                    getOrderBy(),
                    getOrderByCriterion(),
                    showAllData.isChecked()
            );
        } catch (Exception e) {
            /*If doesn't found nothing restore table*/
            e.printStackTrace();
            dbController.close();
            restoreTable(allDataAdapter);
            return;
        }
        //else get column and build adapter to table
        String column =
                /*if show all data checkbox is checked and the column is "all" or
                * "*" send null to column in build table as matrix method because
                * if column is null the method will assign default table headers
                * (date-co-co2...etc) and if is not null only assign the required field*/
                showAllData.isChecked() || getFilter().equals("*") ?
                        null :
                        /*else send column (from array string resources)
                        * with the actual position of field filter spinner less 1*/
                        getResources().getStringArray(R.array.columns)[getPos() - 1];
        /*If the obtained matrix is not null*/
        if (dbController.buildTableAsMatrix(cursor, column) != null) {
            /*Long press in register to access to details or delete the register is
            * disabled if the filter of show all data is not enabled and the filter of column
            * is "all" or "*" from sqlite*/
            boolean longPressEnabled = !showAllData.isChecked() && getPos() != 0 ? false : true;
            /*Build the adapter from matrix with cursor defined bellow, the long press
             * flag and the column*/
            queryAdapter =
                    new MatrixTableAdapter<String>(this,
                            dbController.buildTableAsMatrix(cursor, column), longPressEnabled);
            /*Restore adapter with the new values*/
            restoreTable(queryAdapter);
        }
        dbController.close();
    }

    /**
     * Method for show snackbar on top of layout
     * @param parentLayout the view parent of snackbar
     * @param str the message to show
     */
    private void showSnackbarOnTop(View parentLayout, String str) {
        Snackbar snack = Snackbar.make(parentLayout, str, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.TOP;
        view.setLayoutParams(params);
        snack.show();
    }

    public void showBackActionbar() {
        //this.restoreActionBar(getString(R.string.subtitle_register_search_result));
        backMenuItem.setVisible(true);
        searchMenuItem.setVisible(false);
        deleteMenuItem.setVisible(false);
    }

    public void hideBackActionbar() {
        //((MainActivity) this).restoreActionBar(getString(R.string.subtitle_register));
        backMenuItem.setVisible(false);
        searchMenuItem.setVisible(true);
        deleteMenuItem.setVisible(true);
        saveMenuItem.setVisible(true);
        //sortMenuItem.setVisible(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.table_menu, menu); //add custom menu
        backMenuItem = menu.findItem(R.id.action_bar_return);
        searchMenuItem = menu.findItem(R.id.action_bar_search);
        deleteMenuItem = menu.findItem(R.id.action_bar_discard);
        saveMenuItem = menu.findItem(R.id.save_database);
        //sortMenuItem = menu.findItem(R.id.sort);
        return true;
    }

    /*public void showSortDialog() {
        String items[] = getResources().getStringArray(R.array.columns);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(R.string.dialog_sort_registers)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showSortDialogItems(i);
                    }
                });
        builder.create().show();
    }*/

   /* public void showSortDialogItems(final int element) {
        String items[] = getResources().getStringArray(R.array.gases);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(R.string.dialog_sort_registers)
                .setMessage(R.string.dialog_sort_message)
                .setPositiveButton(R.string.dialog_sort_asc, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sortTable(element, ASC);
                    }
                })
                .setNegativeButton(R.string.dialog_sort_desc, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sortTable(element, DESC);
                    }
                });
        builder.create().show();
    }*/

    /**
     * @param date the primary key of table to delete register
     */
    public void deleteRegister(String date) {
        dbController.open();
        dbController.deleteRow(date);//delete row
        Cursor cursor = dbController.readEntry();//restore table with new values
        allDataAdapter = new MatrixTableAdapter<String>(this, dbController.buildTableAsMatrix(cursor, null), true);
        restoreTable(allDataAdapter);
        dbController.close();
    }

    public void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogLight);
        builder
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message))
                .setPositiveButton(R.string.dialog_delete_database_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                showDeleteDatabaseDialog();
                            }
                        })
                /*.setNegativeButton(R.string.dialog_delete_register_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showDateDialog(true, true);
                    }
                })*/;
        builder.show();
    }

    public void showDeleteDatabaseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.AlertDialogLight)
                .setTitle(R.string.dialog_delete_database_button)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.alert_dialog_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TableActivity.this.deleteDatabase(dbHelper.DATABASE_NAME);
                        Toast.makeText(
                                TableActivity.this,
                                getString(R.string.menu_discard_database),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).setNegativeButton(R.string.alert_dialog_cancel, null);
        builder.show();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    public int getPos() {
        return pos;
    }

    public void setColumnItemPos(int pos) {
        this.pos = pos;
    }

    public String getTextToSearch() {
        return textToSearch;
    }

    public void setTextToSearch(String textToSearch) {
        this.textToSearch = textToSearch;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getDateFormatted(int year, int month, int day) {
        String dayCorrected = null;
        String monthCorrected = null;
        month += 1;

        if (day >= 10) dayCorrected = day + "";
        else if (day < 10) dayCorrected = "0" + day;
        if (month >= 10) monthCorrected = month + "";
        else if (month < 10) monthCorrected = "0" + month;

        return year + "-" + monthCorrected + "-" + dayCorrected;
    }

    public String getDateTimeFormatted(int year, int month, int day, int hour, int minute) {
        month += 1;
        String dayCorrected = (day >= 10) ? day + "" : "0" + day;
        String monthCorrected = (month >= 10) ? month + "" : "0" + month;
        String hourCorrected = (hour >= 10) ? hour + "" : "0" + hour;
        String minuteCorrected = (minute >= 10) ? minute + "" : "0" + minute;

        return year + "-" + monthCorrected + "-" + dayCorrected + " " + hourCorrected + ":" + minuteCorrected;
    }


    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderByCriterion() {
        return orderByCriterion;
    }

    public void setOrderByCriterion(String orderByCriterion) {
        this.orderByCriterion = orderByCriterion;
    }
}
