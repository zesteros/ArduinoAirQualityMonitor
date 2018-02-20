package angelo.itl.arduinoairqualitymonitor.activity.airmonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import angelo.itl.arduinoairqualitymonitor.R;
import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;
import angelo.itl.arduinoairqualitymonitor.util.sensor.GasAlarm;

/**
 * Created by Angelo on 16/12/2016.
 */

public class AlarmActivity extends Activity implements DialogInterface.OnClickListener{

    private GlobalVariable global;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_dialog);
        global = GlobalVariable.getInstance();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AlertDialogRed);
        dialog
                .setTitle(R.string.alarm_notification_title)
                .setMessage(R.string.alarm_notification_message)
                .setPositiveButton(R.string.alert_dialog_accept, null)
                .setNegativeButton(R.string.turn_off_alarm, this)
                .show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        modifySettingsAlarm();
        GasAlarm.stopAlarm();
    }

    public void modifySettingsAlarm(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(getString(R.string.alarm_setting_key), false);
        editor.commit();
    }
}
