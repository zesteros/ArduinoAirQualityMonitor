package angelo.itl.arduinoairqualitymonitor.activity.main;


import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.util.*;
//local packages
import angelo.itl.arduinoairqualitymonitor.activity.plot.CarbonDioxideRealTimePlotActivity;
import angelo.itl.arduinoairqualitymonitor.activity.plot.ComparisonGasPlotActivity;
import angelo.itl.arduinoairqualitymonitor.activity.plot.HistoricalAllGasPlotActivity;
import angelo.itl.arduinoairqualitymonitor.activity.plot.HistoricalCo2PlotActivity;
import angelo.itl.arduinoairqualitymonitor.activity.plot.RemainingGasesRealTimePlotActivity;
import angelo.itl.arduinoairqualitymonitor.fragment.NavigationDrawerFragment;
import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;
import angelo.itl.arduinoairqualitymonitor.R;
import angelo.itl.arduinoairqualitymonitor.bluetooth.*;
import angelo.itl.arduinoairqualitymonitor.database.*;
import angelo.itl.arduinoairqualitymonitor.activity.airmonitor.*;
import angelo.itl.arduinoairqualitymonitor.activity.environment.*;
//import angelo.itl.arduinoairqualitymonitor.activity.plot.*;
import angelo.itl.arduinoairqualitymonitor.activity.setting.GeneralPreferenceActivity;
import angelo.itl.arduinoairqualitymonitor.activity.table.*;
import angelo.itl.arduinoairqualitymonitor.util.NotificationHelper;
import angelo.itl.arduinoairqualitymonitor.web.InternetConnectionThread;
/*USE ONLY ACTIONBARACTIVITY*/

public class MainActivity extends AppCompatActivity implements NavigationDrawerFragment
        .NavigationDrawerCallbacks, View.OnClickListener {
    private static final String HISTORICAL_CO2_PLOT = "history_co2_plot";
    //main activity components
    //protected Switch connectionSwitch;
    protected Button mConnectButton;
    protected static ProgressBar connectionProgressBar;
    protected TextView connectionTextView;

    protected static TextView connectionProperties;
    private static String[] nameProp;

    protected NavigationDrawerFragment navDrawerFragment;
    private String mainTitle;

    //fragments variables
    private final String AIR_MONITOR_FRAG = "airmonitor";
    private final String ALTIMETER_FRAG = "altimeter";
    private final String DATA_LIST_FRAG = "list";
    private final String PLOT_CO2_FRAG = "plotco2";
    private final String ALL_PLOT_FRAG = "allgasplot";
    private final String HISTORICAL_PLOT = "history_plot";

    private final String SETIINGS_FRAG = "prefs";
    protected FragmentManager fragmentManager;
    protected FragmentTransaction fragmentTransaction;
    protected static Fragment mFragment;
    //bt variables
    protected BluetoothAdapter btAdapter = null;
    protected GlobalVariable global;
    protected BroadcastReceiver btDevicesReceiver;
    protected BroadcastReceiver pairedDevicesReceiver;
    protected AlertDialog btDevicesDialog = null;
    protected ArrayAdapter<String> devicesArrayAdapter;
    protected ArrayAdapter<String> discoverArrayAdapter;
    protected ListView pairedDevices;
    protected ListView availableDevices;
    protected ProgressBar discoveringProgressBar = null;
    protected AlertDialog.Builder dialogBuilder;
    protected LayoutInflater inflater;
    protected View dialogView;
    protected Set<BluetoothDevice> setBtPairedDevices;
    protected IntentFilter availableDevicesFilter, pairedDevicesFilter;
    protected BluetoothDevice btDevice;
    protected String deviceInfo;
    private ConnectThread connectThread;
    private boolean isChecked;

    private Vibrator vibrator;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean resumed, pause;
    public NotificationHelper notification;

    private static SharedPreferences prefs;
    private final String ROTATION_ANIMATION = "rotation";
    private final String ALPHA_ANIMATION = "alpha";

    /**
     * @param savedInstanceState the instance of the activity
     *                           instantiate all basic items
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMain();
        setUpNavDrawer(R.id.drawer_layout);
        instantiateItems();//CREATE ITEMS OF MAIN ACTIVITY
        setUpItems();//CONFIGURE ITEMS
        getPreferences();
    }

    public void setUpMain() {
        setTheme(android.R.style.Theme_Material_Light);
        global = global.getInstance();//get global variable and data
        global.setContext(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public void setUpNavDrawer(int drawerId) {
        navDrawerFragment = (NavigationDrawerFragment)//SHOW NAV DRAWER
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        navDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(drawerId));
    }

    /**
     * Get user preferences for this activity
     */
    public void getPreferences() {
        global = global.getInstance();//get global variable and data
        global.setTimeout(Integer.parseInt(prefs.getString("timeout", "20000")));
    }

    /**
     * Instantiate all items
     */
    protected void instantiateItems() {
        //connectionSwitch = (Switch) findViewById(R.id.bluetooth_switch);
        global.setConnectButton((Button) findViewById(R.id.connect_button));
        connectionProgressBar = (ProgressBar) findViewById(R.id.bluetooth_progress_bar);
        connectionTextView = (TextView) findViewById(R.id.bluetooth_tv);
        connectionProperties = (TextView) findViewById(R.id.connection_properties);
        notification = new NotificationHelper(this);
    }

    /**
     * Show device list connect dialog
     */
    private void showDialog() {
        try {
            new DeviceDialog(this).showDeviceListDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Instantiate the SwitchButton and set up the listener
     */
    private void setUpItems() {
        global.getConnectButton().setOnClickListener(this);
        connectionProgressBar.setVisibility(ProgressBar.INVISIBLE);
        connectionTextView.setVisibility(TextView.VISIBLE);
        connectionProperties.setVisibility(TextView.INVISIBLE);
        connectionTextView.setText(R.string.disconnect);
        nameProp = getResources().getStringArray(R.array.name_connection_properties);
    }


    /**
     * @param v view of listener
     */
    @Override
    public void onClick(View v) {
        animView(v, ROTATION_ANIMATION, 500, 0f, 720f);
        isChecked = global.isConnected() ? true : false;
        if (!isChecked) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.connection_mode_message);
            //builder.setMessage(R.string.connection_mode_message);
            builder.setItems(new CharSequence[]{
                            getString(R.string.connection_mode_bluetooth),
                            getString(R.string.internet_connection)
                    },
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int position) {
                            switch (position) {
                                case 0:
                                    global.isBluetooth(true);
                                    if (isBluetoothEnabled()) {
                                        try {
                                            if (!global.isConnected()) showDialog();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else { //IF BT ISNT ENABLED HIDE UI AND TOAST TO THE USER
                                        showToast(R.string.request_active_bt);
                                        hideBluetoothConnectionUI();
                                    }
                                    break;
                                case 1:
                                    global.isBluetooth(false);
                                    new InternetConnectionThread(MainActivity.this,
                                            prefs.getString(
                                                    getString(R.string.ip_preference_key),
                                                    getString(R.string.ip_preference_default)),
                                            Integer.parseInt(
                                                    prefs.getString(
                                                            getString(R.string.port_preference_key),
                                                            getString(R.string.port_preference_default))

                                            )).start();
                                    break;
                            }
                        }
                    });
            builder.create().show();
        } else closeConnection();

    }

    /**
     * @param address   the mac address of the device to connect
     * @param btAdapter the bt adapter object
     */
    public void connectToDevice(String address, BluetoothAdapter btAdapter) {
        getPreferences();
        global = global.getInstance();//get global bt data
        global.setAddress(address);//get the mac address
        showBluetoothConnectingUI();//show bt connecting in ui
        connectThread = new ConnectThread(btAdapter, this);//start connect attempt thread
        connectThread.start();
        final long start = System.currentTimeMillis();//get the start of the thread
        final boolean[] tryToConnect = {true};
        new Thread(new Runnable() {
            public void run() {
                while (tryToConnect[0]) {
                    if (System.currentTimeMillis() - start == global.getTimeout()) {//timeout 20 seg default
                        tryToConnect[0] = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideBluetoothConnectionUI();
                                showToast(R.string.timeout_elapsed);
                            }
                        });
                        connectThread.closeConnection();
                        break;
                    }
                    if (global.isConnected()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showBluetoothConnectedUI();
                            }
                        });
                        connectThread.startConnectedThread();
                        tryToConnect[0] = false;
                    }
                }
            }
        }).start();
    }

    /**
     * Show the Bluetooth connecting UI
     */
    public void showBluetoothConnectingUI() {
        global.getConnectButton().setClickable(false);
        changeButtonUI(R.drawable.round_button, R.string.connecting, R.color.white);
        connectionTextView.setText(R.string.bluetooth_connecting);
        if (global.isBluetooth()) connectionProgressBar.setVisibility(ProgressBar.VISIBLE);
        connectionTextView.setVisibility(TextView.VISIBLE);
    }

    /**
     * Show the Bluetooth connected UI
     */
    public void showBluetoothConnectedUI() {
        global.getConnectButton().setClickable(true);
        showDeviceInfo();
        changeButtonUI(R.drawable.round_button_connected, R.string.disconnect, R.color.black);
        animView(global.getConnectButton(), ROTATION_ANIMATION, 500, 0f, 720f);
        //connectionSwitch.setChecked(true);
        connectionTextView.setText(R.string.bluetooth_recieve);
        connectionProgressBar.setVisibility(ProgressBar.VISIBLE);
        if (!resumed && !pause) showToast(R.string.bluetooth_connection_successful);
    }

    /**
     * @param drawable the background to change
     * @param text     the text to change
     * @param color    the color to change
     */
    public void changeButtonUI(int drawable, int text, int color) {
        global = global.getInstance();
        Button button = global.getConnectButton();
        Context context = global.getContext();
        button.setBackground(context.getResources().getDrawable(drawable));
        button.setText(context.getString(text));
        button.setTextColor(context.getResources().getColor(color));
    }

    /**
     * @param view     view to apply anim
     * @param type     type of anim
     * @param duration duration
     * @param from     form where
     * @param to       to where
     */
    public void animView(View view, String type, int duration, float from, float to) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, type, from, to);
        anim.setDuration(duration);
        anim.start();
    }

    /**
     * Hide the bluetooth connection UI
     */
    public void hideBluetoothConnectionUI() {
        try {
            global.getConnectButton().setClickable(true);
            changeButtonUI(R.drawable.round_button, R.string.connect, R.color.white);
            connectionProperties.setVisibility(View.INVISIBLE);
            connectionProgressBar.setVisibility(ProgressBar.INVISIBLE);
            connectionTextView.setVisibility(TextView.INVISIBLE);
            notification.getNotificationManager().cancel(NotificationHelper.RECEIVING_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Show the info of the device when it is connected
     */
    public void showDeviceInfo() {
        if (global.isBluetooth()) {
            connectionProperties.setVisibility(TextView.VISIBLE);
            animView(connectionProperties, ALPHA_ANIMATION, 3000, 0f, 1f);
            setBluetoothAdapter();
            BluetoothDevice device = btAdapter.getRemoteDevice(global.getAddress());
            ConnectThread connectThread = new ConnectThread(null, null);
            connectionProperties.setText(
                    nameProp[0] + ": \n" + global.getAddress() + "\n\n\n" +
                            nameProp[1] + ": \n" + String.valueOf(connectThread.BTMODULEUUID) + "\n\n\n" +
                            nameProp[2] + ": \n" + device.getName());
        }

    }

    /**
     * Close the socket (connection)
     */
    public void closeConnection() {
        hideBluetoothConnectionUI();
        global.isConnected(false);
        if (global.isBluetooth()) {
            if (global.getBtSocket() != null) {
                try {
                    global.getBtSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else try {
            if (global.getSocket() != null) global.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return true if the bluetooth is enabled, also if is not, request bluetooth activation to the user
     */
    public boolean isBluetoothEnabled() {
        setBluetoothAdapter();
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                //INSTANTIATE A NEW ACTIVITY FROM SYSTEM
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
                if (btAdapter.isEnabled()) return true;
                else return false;
            } else return true;
        } else return false;
    }

    /**
     * Instantiate Bluetooth adapter
     */
    public void setBluetoothAdapter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * @param position switch the selected item of the navigation drawer in position
     */
    public void onNavigationDrawerItemSelected(int position) {
        fragmentManager = getFragmentManager();//get mFragment manager
        fragmentTransaction = fragmentManager.beginTransaction();
        Intent intent;
        switch (position) {//switch position when is 0, the first element(list) of drawer is selected
            case 2://position 2 1st list element, 1 is the title and 0 is header list
                openActivity(AirMonitorActivity.class, true, false);
                break;
            case 3:
                openActivity(CarbonDioxideRealTimePlotActivity.class, true, false);
                break;
            case 4:
                openActivity(RemainingGasesRealTimePlotActivity.class, true, false);
                break;
            case 6://position 4 is 2nd list element, 3 is title
                openActivity(EnvironmentActivity.class, true, false);
                break;
            case 8://position 4 is 2nd list element, 3 is title
                openActivity(TableActivity.class, false, true);
                break;
            case 9:
                openActivity(HistoricalCo2PlotActivity.class, false, true);
                break;
            case 10:
                openActivity(HistoricalAllGasPlotActivity.class, false, true);
                break;
            case 11:
                openActivity(ComparisonGasPlotActivity.class, false, true);
                break;
        }
    }

    /**
     * Open the Database fragments
     *
     * @param global               the global variables (variable)
     * @param mFragmentTransaction the main fragment transaction
     * @param fragmentInstance     the instance of the table (new FragmentClassName())
     * @param tag                  the tag of the fragment for id purposes
     * @param resTitle             the title of the action bar
     */
    public void openDatabaseFragment(GlobalVariable global, FragmentTransaction mFragmentTransaction,
                                     Fragment fragmentInstance, String tag, int resTitle) {
        DataBaseController dbc = new DataBaseController(this);
        dbc.open();
        dbc.readEntry();
        if (!global.isEmptyDatabase()) {
            mFragmentTransaction.replace(R.id.container, fragmentInstance, tag).commit();//add to backstack
            restoreActionBar(getString(resTitle));
        } else showToast(R.string.empty_database);
        dbc.close();
    }

    /**
     * @param cl                 the class of which activity start
     * @param requireConnected   if require connection
     * @param isDatabaseActivity if a database activity
     */
    public void openActivity(Class<?> cl, boolean requireConnected, boolean isDatabaseActivity) {
        DataBaseController dbc = new DataBaseController(this);
        if (!requireConnected) {
            if (isDatabaseActivity) {
                try {
                    dbc.open();
                    dbc.readEntry();
                    if (!global.isEmptyDatabase()) {
                        startActivity(cl);
                    } else showToast(R.string.empty_database);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dbc.close();
            } else startActivity(cl);
        } else {
            if (global.isConnected()) startActivity(cl);
            else showToast(R.string.request_bluetooth_connection);
        }
    }

    /**
     * @param cl a class to be started
     */
    public void startActivity(Class<?> cl) {
        Intent intent = new Intent(this, cl);
        startActivity(intent);
    }

    /**
     * Open single fragments
     *
     * @param global               the global variables (variable)
     * @param mFragmentTransaction the main fragment transaction
     * @param fragmentInstance     the instance of the table (new FragmentClassName())
     * @param tag                  the tag of the fragment for id purposes
     * @param resTitle             the title of the action bar
     */
    public void openFragment(GlobalVariable global, final FragmentTransaction mFragmentTransaction,
                             final Fragment fragmentInstance, final String tag, int resTitle, boolean requestBtConnection) {
        if (global.isConnected() || !requestBtConnection) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mFragmentTransaction.replace(R.id.container, fragmentInstance, tag).commit();
                }
            }).start();
            restoreActionBar(getString(resTitle));
        } else showToast(R.string.request_bluetooth_connection);
    }

    /**
     * @param mFragmentManager the fragment manager to evaluate
     * @return a array with all the fragments available
     */
    public Fragment[] getFragmentsAsArray(FragmentManager mFragmentManager) {
        Fragment fragment[] = {
                mFragmentManager.findFragmentByTag(AIR_MONITOR_FRAG),
                mFragmentManager.findFragmentByTag(PLOT_CO2_FRAG),
                mFragmentManager.findFragmentByTag(ALL_PLOT_FRAG),
                mFragmentManager.findFragmentByTag(ALTIMETER_FRAG),
                mFragmentManager.findFragmentByTag(DATA_LIST_FRAG),
                mFragmentManager.findFragmentByTag(SETIINGS_FRAG),
                mFragmentManager.findFragmentByTag(HISTORICAL_PLOT),
                mFragmentManager.findFragmentByTag(HISTORICAL_CO2_PLOT)
        };
        return fragment;
    }

    /**
     * @param frags the array of fragments
     * @return the actual fragment on UI
     */
    public Fragment returnActualFragment(Fragment[] frags) {
        for (int i = 0; i < frags.length; i++)
            if (frags[i] != null) if (frags[i].isVisible()) return frags[i];
        return null;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * @param item the item of the action bar menu
     * @return if item is selected
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this,
                R.style.AlertDialogLight);
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        switch (item.getItemId()) {
            case R.id.action_settings:
                vibrator.vibrate(35);
            case R.id.settings:
                openActivity(GeneralPreferenceActivity.class, false, false);
                break;
            case R.id.menu_info:
                dialog.setTitle(getResources().getString(R.string.menu_info_title));
                try {
                    dialog.setMessage(String.format(getString(R.string.menu_info_content),
                            getPackageManager().getPackageInfo(getPackageName(), 0)
                                    .versionName));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                dialog.setNeutralButton(R.string.alert_dialog_accept, null);
                dialog.create();
                dialog.show();
                break;
            case R.id.menu_get_out:
                closeConnection();
                notification.getNotificationManager().cancel(NotificationHelper.RECEIVING_DATA);
                BluetoothConnectionThread.stopThread();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * The adapter method implemented for manage back button pressed
     */
    public void onBackPressed() {
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        if (navDrawerFragment.isDrawerOpen())
            navDrawerFragment.mDrawerLayout.closeDrawer(navDrawerFragment
                    .mFragmentContainerView);
        else if (!navDrawerFragment.isDrawerOpen()) {
            Fragment actualFragment = returnActualFragment(getFragmentsAsArray(fragmentManager));
            if (hideActualFragment(fragmentManager, fragmentTransaction,
                    R.string.title_activity_main)) {
                ColorDrawable colorDraw = new ColorDrawable(getResources().getColor(R.color.cyan_800));
                getSupportActionBar().setBackgroundDrawable(colorDraw);
                return;
            } else exitWhenBackButtonIsPressedTwice();
        }
    }

    /**
     * @param fragmentManager     the actual fragment manager
     * @param fragmentTransaction
     * @param resTitleToRestore   title to put in action bar
     * @return true if the fragment has been removed
     */
    public boolean hideActualFragment(FragmentManager fragmentManager,
                                      FragmentTransaction fragmentTransaction, int resTitleToRestore) {
        Fragment actualFragment = returnActualFragment(getFragmentsAsArray(fragmentManager));
        if (actualFragment != null) {
            fragmentTransaction.hide(actualFragment).remove(actualFragment).commit();
            restoreActionBar(getString(resTitleToRestore));
            return true;
        }
        return false;
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
     * @param mTitle string to show in action bar title
     */
    public void restoreActionBar(String mTitle) {
        this.setMainTitle(mTitle);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    /**
     * @param mainTitle set main activity title
     */
    protected void setMainTitle(CharSequence mainTitle) {
        this.mainTitle = (String) mainTitle;
    }

    /**
     * @param id the id of string resource for show message
     */
    public void showToast(int id) {
        /*Find the root view*/
        View view = findViewById(android.R.id.content);
        /*Show the snackbar*/
        Snackbar.make(view, getString(id), Snackbar.LENGTH_LONG).show();
        //Toast.makeText(getApplicationContext(), getResources().getString(id),
        //      Toast.LENGTH_SHORT).show();

    }

    /**
     * When app is resumed
     */
    @Override
    protected void onResume() {
        resumed = true;
        getPreferences();
        if (global.isConnected()) {
            showBluetoothConnectedUI();
            notification.getNotificationManager().cancel(NotificationHelper.RECEIVING_DATA);
        } else
            closeConnection();

        super.onResume();
    }

    /**
     * When app is paused
     */
    @Override
    protected void onPause() {
        pause = true;
        getPreferences();
        if (global.isConnected()) {
            showBluetoothConnectedUI();
            if (!global.isAnotherActivityVisible())
                notification.getNotificationManager().notify(NotificationHelper.RECEIVING_DATA,
                        notification.createNotification(
                                R.string.app_name,
                                R.string.bluetooth_recieve,
                                R.drawable.ic_archive_white_36dp,
                                MainActivity.class,
                                MainActivity.class,
                                Notification.FLAG_ONGOING_EVENT
                        )
                );
        } else
            closeConnection();

        super.onPause();
    }

    /**
     * When app is destroyed
     */
    @Override
    public void onDestroy() {
        getPreferences();
        if (!global.isConnected()) closeConnection();
        super.onDestroy();
    }
}