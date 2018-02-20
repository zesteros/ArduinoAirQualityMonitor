package angelo.itl.arduinoairqualitymonitor.activity.airmonitor;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.*;
import android.app.AlertDialog;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import angelo.itl.arduinoairqualitymonitor.R;
import angelo.itl.arduinoairqualitymonitor.adapter.main.CustomListAdapter;
import angelo.itl.arduinoairqualitymonitor.adapter.main.CustomListAdapterSimple;
import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;
import angelo.itl.arduinoairqualitymonitor.database.DataBaseController;
import angelo.itl.arduinoairqualitymonitor.util.graphic.LRUCache;
import angelo.itl.arduinoairqualitymonitor.util.sensor.GasComputer;
import angelo.itl.arduinoairqualitymonitor.util.sensor.RatioGasComputer;
/*x>200 interior
* x<200 exterior*/

public class AirMonitorActivity extends AppCompatActivity implements View.OnClickListener, AirMonitorValues {

    private ListView generalValuesList, gasesList;
    private String[] subtitlesGeneralValues, gasName;
    private String[] generalValuesDescription;
    private String[] gasValue, gasLimitNormal, gasLimitHazard;
    private double[] limitNormal;
    private double[] limitHazardous;
    private CustomListAdapterSimple generalValuesAdapter;
    private CustomListAdapter gasesAdapter;
    private boolean isTimer;
    private Timer timer;
    private TimerTask timerTask;
    private SimpleDateFormat dateFormat;
    private String currentDateandTime;
    private DataBaseController dbController;

    //private final String
    private FloatingActionButton fabSave;
    private ScrollView mainScrollView;
    private ImageView airQualityGraph;
    private TextView airQuality;


    private boolean saveAutomatically;
    private String gasUnity, voltUnity, updateInterval;
    private int ratioValue, ratioHazard;
    private RatioGasComputer ratio;

    private ProgressBar voltIndicator;
    private GlobalVariable global;
    private SharedPreferences prefs;
    private LRUCache cache;
    private boolean dialogShowing;
    private String[] gasAvg;
    private TextView location;
    private boolean establishAutomaticallyLimits;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_monitor);
        /**
         *  We will not use setContentView in this activty
         *  Rather than we will use layout inflater to add view in FrameLayout of our base activity layout*/

        /**
         * Adding our layout to parent class frame layout.
         */
        //getLayoutInflater().inflate(R.layout.activity_air_monitor, frameLayout);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        getSupportActionBar().setTitle(R.string.title_air_monitor);
        global = global.getInstance();
        global.isAnotherActivityVisible(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ratio = new RatioGasComputer(this);

        dateFormat = new SimpleDateFormat(format);

        dbController = new DataBaseController(this);
        /*Instantiate the items*/
        generalValuesList = (ListView) findViewById(android.R.id.list);
        gasesList = (ListView) findViewById(R.id.ppm_gases_list);
        fabSave = (FloatingActionButton) findViewById(R.id.fab_save);
        airQualityGraph = (ImageView) findViewById(R.id.air_quality_indicator);
        airQuality = (TextView) findViewById(R.id.air_quality);
        mainScrollView = (ScrollView) findViewById(R.id.air_monitor_scroll_view);
        voltIndicator = (ProgressBar) findViewById(R.id.volt_progress_bar);
        location = (TextView) findViewById(R.id.location);

        fabSave.setOnClickListener(this);

        subtitlesGeneralValues = getResources().getStringArray(R.array.subtitle_general_values);
        gasName = getResources().getStringArray(R.array.subtitle_gases);
        /*Get the default data (from user preference)*/
        getPreferenceData();
        /*Get the actual data from sensor*/
        getAirData();
        /*Update air indicator graph*/
        updateAirQualityGraph(getAirQuality());


        global.isAnotherActivityVisible(true);
        //mDrawerList.setItemChecked(position, true);
        //setTitle(listArray[position]);
    }

    public void addImageViewsToCache() {
        cache = new LRUCache(this);
        cache.createLRUCache();
        cache.setImageToProcess(airQualityGraph);
        cache.setImageSize(500, 500);
        for (int i = 0; i < getImageViewsRes().length; i++)
            cache.loadBitmap(getImageViewsRes()[i], airQualityGraph);
    }

    public int[] getImageViewsRes() {
        return new int[]{
                R.drawable.good_air_graph,
                R.drawable.middle_air_graph,
                R.drawable.bad_air_graph
        };
    }

    /**
     * Get the saved preferences
     */
    private void getPreferenceData() {
        saveAutomatically = prefs.getBoolean(SAVE_DATA_AUTOMATICALLY, false);
        gasUnity = prefs.getString(GAS_UNITY, GAS_UNITY_DEFAULT);
        voltUnity = prefs.getString(MEASURE_VOLT_UNITY, MEASURE_VOLT_UNITY_DEFAULT);
        updateInterval = prefs.getString(UPDATE_INTERVAL, UPDATE_INTERVAL_DEFAULT);
        establishAutomaticallyLimits = prefs.getBoolean(AUTOMATIC_LIMITS, true);
        getGasLimits(establishAutomaticallyLimits, false);
    }

    /**
     * @param automaticLevels the flag to indicate automatic limits
     * @param interior flags to indicate if the sensor is in the outside
     */
    public void getGasLimits(boolean automaticLevels, boolean interior) {
        /*If is not automatic levels*/
        if (!automaticLevels) {
            //if is not interior assign the ratio established else multiply by 2.5
            ratioValue =
                    interior ?
                            (int) (prefs.getInt(getString(R.string.limit_key), 100) * 2.5)
                            : (prefs.getInt(getString(R.string.limit_key), 100));
            limitNormal = ratio.getGasRatioAsFloatArray(ratioValue, normalValues)[0];
            limitHazardous = ratio.getGasRatioAsFloatArray(ratioValue, normalValues)[1];
        } else {
            //if is automatic assing default values and if is interior multiply by 2.5
            ratioValue = interior ? 250 : 100;
            limitNormal = ratio.getGasRatioAsFloatArray(ratioValue, normalValues)[0];
            limitHazardous = ratio.getGasRatioAsFloatArray(ratioValue, normalValues)[1];
        }
    }


    /**
     * Get the location of the sensor
     */
    public void getLocation() {
        location.post(new Runnable() {
            @Override
            public void run() {
                //if voltage is superior to 200 is interior else is exterior
                if (global.getMQ135AnalogRead() > 200) {
                    location.setText(R.string.interior_indicator);
                    getGasLimits(establishAutomaticallyLimits, true);
                } else {
                    location.setText(R.string.exterior_indicator);
                    getGasLimits(establishAutomaticallyLimits, false);
                }
            }
        });
    }

    /*Get the data from sensors*/
    private void getAirData() {
        try {
            /*Get the current date*/
            currentDateandTime = dateFormat.format(new Date());
            /*
            * Get the sensor values as string
            * according type: gas(gases) or weather(general)
            */
            generalValuesDescription = getDataAsString(TYPE_GENERAL);
            /*If the gas unity is mgm3*/
            if (gasUnity.equals(MGM3)) {
                /*Convert ppm to mgm3 (according molecular weights)*/
                gasValue = convertGasesToMgM3(GasComputer.getGasesPPM());
                gasLimitNormal = convertGasesToMgM3(limitNormal);
                gasLimitHazard = convertGasesToMgM3(limitHazardous);
                gasAvg = convertGasesToMgM3(getAverage());
                for (int i = 0; i < gasValue.length; i++) {
                    gasValue[i] += " " + MGM3;
                    gasLimitNormal[i] += " " + MGM3;
                    gasLimitHazard[i] += " " + MGM3;
                    gasAvg[i] += " " + MGM3;
                }
            } else if (gasUnity.equals(GAS_UNITY_DEFAULT)) {
                /*Else simply use ppm*/
                gasValue = getDataAsString(TYPE_GASES);
                gasLimitNormal = new String[gasValue.length];
                gasLimitHazard = new String[gasValue.length];
                gasAvg = new String[getAverage().length];
                for (int i = 0; i < gasValue.length; i++) {
                    gasValue[i] += " " + GAS_UNITY_DEFAULT;
                    gasLimitNormal[i] = (float) limitNormal[i] + " " + GAS_UNITY_DEFAULT;
                    gasLimitHazard[i] = (float) limitHazardous[i] + " " + GAS_UNITY_DEFAULT;
                    gasAvg[i] = (float) getAverage()[i] + " " + GAS_UNITY_DEFAULT;
                }
            }
            /*Convert volt according units from user*/
            generalValuesDescription[0] = convertVolt(voltUnity, generalValuesDescription[0]);
            /*If user wants to save automatically*/
            if (saveAutomatically) saveToDataBase();
            /*Redraw the lists with new values*/
            updateList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Execute the timer to update according interval established by user
     */
    public void executeUpdater() {
        try {
            /*Instantiate the timer*/
            timer = new Timer();
            /*Create a timer task*/
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (isTimer()) {
                            getAirData();
                            updateList();
                            getLocation();
                            updateAirQualityGraph(getAirQuality());
                        } else {
                            timer.cancel();
                            timer.purge();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            timer.schedule(timerTask, Integer.parseInt(updateInterval),
                    Integer.parseInt(updateInterval));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update list view
     */
    public void updateList() {
        generalValuesAdapter = new CustomListAdapterSimple(this, subtitlesGeneralValues,
                generalValuesDescription);
        gasesAdapter = new CustomListAdapter(this, gasName, gasValue, gasLimitNormal,
                gasLimitHazard, gasAvg);
        gasesList.post(new Runnable() {
            @Override
            public void run() {
                gasesList.setAdapter(gasesAdapter);
            }
        });
        generalValuesList.post(new Runnable() {
            @Override
            public void run() {
                generalValuesList.setAdapter(generalValuesAdapter);
            }
        });
        setUpProgressBar();
    }

    /**
     * @param type the type of values (gas or weather)
     * @return a string array values from type
     */
    public String[] getDataAsString(String type) {
        String[] data = null;
        if (type.equals(TYPE_GENERAL))
            data = new String[]{
                    global.getMQ135AnalogRead() + "",
                    getIsSmoke(global)};
        else if (type.equals(TYPE_GASES)) {
            data = new String[GasComputer.getGasesPPM().length];
            for (int i = 0; i < data.length; i++)
                data[i] = (float) GasComputer.getGasesPPM()[i] + "";
        }
        return data;
    }

    /**
     * This method changes all UI for warning to the user if the
     * environment is not safe
     *
     * @param quality the quality to evaluate
     */
    public void updateAirQualityGraph(Quality quality) {
        try {
            switch (quality) {
                case BAD:
                    airQuality.post(new Runnable() {
                        @Override
                        public void run() {
                            airQuality.setText(getString(R.string.bad));
                        }
                    });
                    airQualityGraph.post(new Runnable() {
                        @Override
                        public void run() {
                            airQualityGraph.setBackground(getResources().getDrawable(getImageViewsRes()[2]));
                            //cache.loadBitmap(getImageViewsRes()[2], airQualityGraph);
                        }
                    });
                    changeUI(getResources().getColor(R.color.red),
                            R.color.red,
                            R.drawable.between_red,
                            R.drawable.custom_progress_bar_red,
                            R.style.AppThemeRed);
                    break;
                case GOOD:
                    airQuality.post(new Runnable() {
                        @Override
                        public void run() {
                            airQuality.setText(getString(R.string.good));
                        }
                    });
                    airQualityGraph.post(new Runnable() {
                        @Override
                        public void run() {
                            airQualityGraph.setBackground(getResources().getDrawable(getImageViewsRes()[0]));
                            //cache.loadBitmap(getImageViewsRes()[0], airQualityGraph);
                        }
                    });
                    changeUI(getResources().getColor(R.color.green_500),
                            R.color.green_700,
                            R.drawable.between_green,
                            R.drawable.custom_progress_bar_green,
                            R.style.AppThemeGreen);
                    break;
                case REGULAR:
                    airQuality.post(new Runnable() {
                        @Override
                        public void run() {
                            airQuality.setText(getString(R.string.moreorless));
                        }
                    });
                    airQualityGraph.post(new Runnable() {
                        @Override
                        public void run() {
                            airQualityGraph.setBackground(getResources().getDrawable(getImageViewsRes()[1]));
                            //cache.loadBitmap(getImageViewsRes()[1], airQualityGraph);
                        }
                    });
                    changeUI(getResources().getColor(R.color.yellow_dark),
                            R.color.yellow_fb,
                            R.drawable.between_yellow,
                            R.drawable.custom_progress_bar_yellow,
                            R.style.AppThemeYellow);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the air Quality enum
     * determine the air Quality according the
     * atmosphere normal values from each gas
     */
    public Quality getAirQuality() {
        /*Get the values from Arduino*/
        double[] gases = GasComputer.getGasesPPM();
        int good = 0;//count gases on good threshold
        int regular = 0;//count gases on regular threshold
        int bad = 0;//count gases on bad threshold
        for (int i = 0; i < gases.length; i++) {
            //if is under normal values is good
            if (gases[i] <= limitNormal[i]) good++;
            else if (gases[i] > limitNormal[i] && gases[i] < limitHazardous[i]) regular++;
            else if (gases[i] >= limitHazardous[i]) bad++;
        }
        return compareAmountOfGasesFromAirQualityRule(good, regular, bad);
    }

    /**
     * @param good    counter of gasses with good air Quality
     * @param regular counter of gasses with moreorless air Quality
     * @param bad     counter of gasses with bad air Quality
     * @return the enum with air Quality
     */
    private Quality compareAmountOfGasesFromAirQualityRule(int good, int regular, int bad) {
        if (good >= regular && good >= bad) return Quality.GOOD;
        else if (bad >= regular && bad >= good) return Quality.BAD;
        else if (regular >= good && regular >= bad) return Quality.REGULAR;
        return null;
    }

    /**
     * @param color            the color of action bar
     * @param colorRes         the color of fab button
     * @param scrollBackground the background of scrollview
     * @param styleProgresbar  the style of progressbar
     */
    private void changeUI(int color, final int colorRes, final int scrollBackground,
                          final int styleProgresbar, final int style) {
        try {
            final ColorDrawable colorDraw = new ColorDrawable(color);
            fabSave.post(new Runnable() {
                @Override
                public void run() {
                    fabSave.setBackgroundTintList(
                            ColorStateList.valueOf(getResources().getColor(colorRes))
                    );
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getSupportActionBar().setBackgroundDrawable(colorDraw);
                    setTheme(style);
                }
            });
            mainScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mainScrollView.setBackground(getResources().getDrawable(scrollBackground));
                }
            });
            voltIndicator.post(new Runnable() {
                @Override
                public void run() {
                    voltIndicator.setProgressDrawable(getResources().getDrawable(styleProgresbar));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param global the instance of global variables
     * @return a string flag according if is smoke present
     */
    public String getIsSmoke(GlobalVariable global) {
        if (global.getIsSmoke() != null) {
            if (global.getIsSmoke().equals(NO_SMOKE_INDICATOR))
                return getString(R.string.isnotsmoke);
            else return getString(R.string.issmoke);
        }
        return null;
    }

    /**
     * @param unityVolt the unity of volts (mV or V)
     * @param value     the value to convert
     * @return a string result from the operation below
     */
    private String convertVolt(String unityVolt, String value) {
        String voltage = null;
        if (unityVolt.equals("V")) voltage = Float.parseFloat(value) / 1000 + "V";
        else if (unityVolt.equals("mV")) voltage = value + " " + getString(R.string.mv);
        return voltage;
    }

    /**
     * @param ppmGases the array of gases to convert to mgm3
     * @return a string array with the result
     */
    public String[] convertGasesToMgM3(double[] ppmGases) {
        String[] convertedValues = new String[ppmGases.length];
        for (int i = 0; i < ppmGases.length; i++)
            convertedValues[i] = ((float) ((ppmGases[i] * molecularWeights[i]) / 24.45)) + "";
        return convertedValues;
    }

    /**
     * Save data to database
     */
    public void saveToDataBase() {
        dbController.open();
        currentDateandTime = dateFormat.format(new Date());
        dbController.insertData(
                currentDateandTime,
                GasComputer.getGasesPPM()[0] + "",
                GasComputer.getGasesPPM()[1] + "",
                GasComputer.getGasesPPM()[2] + "",
                GasComputer.getGasesPPM()[3] + "",
                GasComputer.getGasesPPM()[4] + "",
                GasComputer.getGasesPPM()[5] + "",
                global.getHumidity() + "",
                global.getTemperature() + "",
                global.getPressure() + "");
        dbController.close();
    }


    public double[] getAverage() {
        double[] avg;
        dbController.open();
        currentDateandTime = dateFormat.format(new Date());
        avg = dbController.getAverageByDate(currentDateandTime);
        return avg;
    }

    /**
     * Compute and set up progress bar
     */
    public void setUpProgressBar() {
        float scaleVoltage = 0;
        try {
            scaleVoltage = (float) (global.getMQ135AnalogRead() * 100) / 600;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        final float finalScaleVoltage = scaleVoltage;
        voltIndicator.post(new Runnable() {
            @Override
            public void run() {
                voltIndicator.setProgress((int) finalScaleVoltage);
            }
        });
    }

    /**
     * Listener for save data floating button
     *
     * @param view a view for listen
     */
    @Override
    public void onClick(View view) {
        saveToDataBase();
        Snackbar.make(mainScrollView, getString(R.string.data_saved), Snackbar.LENGTH_LONG).show();
    }

    public void showAlarmDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isDialogShowing(true);
                AlertDialog.Builder builder = new AlertDialog.Builder(AirMonitorActivity.this,
                        R.style.AlertDialogRed);
                builder
                        .setTitle(R.string.dialog_alert_title)
                        .setMessage(getString(R.string.dialog_alert_subtitle))
                        .setPositiveButton(R.string.alert_dialog_accept, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isDialogShowing(false);
                            }
                        })
                        .setNegativeButton(R.string.dialog_stop_show_alert_msg,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        isDialogShowing(false);
                                    }
                                })
                        .create().show();
            }
        });
    }

    public void isDialogShowing(boolean dialogShow) {
        this.dialogShowing = dialogShow;
    }

    public boolean isDialogShowing() {
        return this.dialogShowing;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        global.isAnotherActivityVisible(false);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                getAirData();
                Log.d("AIRQUALITY", getAirQuality() + "");
                updateAirQualityGraph(getAirQuality());
                break;
            case R.id.update_automatically:
                //notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                //ring = RingtoneManager.getRingtone(getActivity(), notification);
                executeUpdater();
                isTimer(true);
                break;
            case R.id.stop_update_automatically:
                isTimer(false);
                break;
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        isTimer(false);
        global.isAnotherActivityVisible(false);
    }

    private void isTimer(boolean timer) {
        this.isTimer = timer;
    }

    private boolean isTimer() {
        return isTimer;
    }
}
