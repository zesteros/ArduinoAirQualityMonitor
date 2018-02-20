package angelo.itl.arduinoairqualitymonitor.activity.setting;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import angelo.itl.arduinoairqualitymonitor.R;
import angelo.itl.arduinoairqualitymonitor.activity.airmonitor.AirMonitorValues;
import angelo.itl.arduinoairqualitymonitor.activity.main.MainActivity;
import angelo.itl.arduinoairqualitymonitor.database.DataBaseController;
import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;
import angelo.itl.arduinoairqualitymonitor.util.sensor.RatioGasComputer;

/**
 * Created by Angelo on 23/06/2016.
 */
public class GeneralPreferenceActivity extends AppCompatPreferenceActivity
        implements AirMonitorValues, Preference.OnPreferenceClickListener {
    private RatioGasComputer ratio;
    private Preference importButton, alarmButton, advancedButton;
    //private PreferenceScreen settingAdvanced;
    private GlobalVariable global;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.action_settings);
        global = global.getInstance();
        global.isAnotherActivityVisible(true);

        //settingAdvanced = (PreferenceScreen) findPreference(getString(R.string.preference_settings_advanced_key));
        addPreferencesFromResource(R.xml.preferences);
        // Get widgets :
        // Set listener :
        //updateMax();
        // Set seekbar summary :
        importButton = findPreference(getString(R.string.import_button));
        alarmButton = findPreference("alarm_setting_item");
        advancedButton = findPreference("setting_advanced_item");
        importButton.setOnPreferenceClickListener(this);
        alarmButton.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        /*if (preference.getKey().equals(importButton.getKey())) {*/

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog
                .setTitle(R.string.import_dialog_title)
                .setMessage(R.string.import_dialog_message)
                .setPositiveButton(R.string.alert_dialog_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        importDatabase();
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, null)
                .show();
        /*} else if (preference.getKey().equals(alarmButton.getKey()))
            getFragmentManager().beginTransaction().show(new AlarmPreference()).commit();
        else if(preference.getKey().equals(advancedButton))
            getFragmentManager().beginTransaction().show(new AdvancedSettings()).commit();*/
        return true;
    }

    public void importDatabase() {
        verifyStoragePermissions(this);
        DataBaseController dataBaseController = new DataBaseController(this);
        try {
            dataBaseController.open();
            if (dataBaseController.importDb())
                Toast.makeText(global.getContext(), this.getString(R.string.import_success),
                        Toast.LENGTH_LONG)
                        .show();
            else Toast.makeText(global.getContext(), this.getString(R.string.import_failed),
                    Toast.LENGTH_LONG)
                    .show();
        } catch (Exception e) {
            Toast.makeText(global.getContext(), this.getString(R.string.import_failed),
                    Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
        }
        dataBaseController.close();
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        global.isAnotherActivityVisible(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        global.isAnotherActivityVisible(false);
    }

    public static class AlarmPreference extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        private SeekBarPreference alarmSetting;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.alarm_preference);
            setHasOptionsMenu(true);
            alarmSetting = (SeekBarPreference) findPreference(getString(R.string.alarm_setting_key));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home)
                startActivity(new Intent(getActivity(), GeneralPreferenceActivity.class));
            return true;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            alarmSetting.setSummary("Valor establecido: "
                    + sharedPreferences.getInt(getString(R.string.vibration_setting_key), 0));
        }
    }

    public static class AdvancedSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private CustomSeekBarPreference alarmLimitSeekbar;
        private RatioGasComputer ratio;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.advanced_preferences);
            setHasOptionsMenu(true);
            ratio = new RatioGasComputer(getActivity());
            SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
            prefs.registerOnSharedPreferenceChangeListener(this);
            changeSeekbarState(prefs);
            updateSummary(false, getActivity(), getPreferenceScreen().getSharedPreferences(), normalValues,
                    alarmLimitSeekbar, getString(R.string.limit_key));

        }

        public void updateSummary(boolean withMax, Context context, SharedPreferences sharedPreferences,
                                  float[] values, CustomSeekBarPreference seekBarPreference, String key) {
            try {
                int radius;
                radius = sharedPreferences.getInt(key, 100);
                seekBarPreference.setSummary(context.getString(R.string.preference_air_monitor_gas_limit_summary)
                        .replace("$1", ratio.getGasRatioAsString(radius, values)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void changeSeekbarState(SharedPreferences prefs) {
            if (prefs.getBoolean(getString(R.string.automatic_limits_key), true) == true)
                alarmLimitSeekbar.setEnabled(false);
            else alarmLimitSeekbar.setEnabled(true);
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.automatic_limits_key)))
                changeSeekbarState(sharedPreferences);
            updateSummary(false, getActivity(), sharedPreferences, normalValues, alarmLimitSeekbar, key);

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home)
                startActivity(new Intent(getActivity(), GeneralPreferenceActivity.class));
            return true;
        }
    }
}
