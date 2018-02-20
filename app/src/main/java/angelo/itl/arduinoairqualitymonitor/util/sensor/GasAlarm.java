package angelo.itl.arduinoairqualitymonitor.util.sensor;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import angelo.itl.arduinoairqualitymonitor.R;
import angelo.itl.arduinoairqualitymonitor.activity.airmonitor.AirMonitorActivity;
import angelo.itl.arduinoairqualitymonitor.activity.airmonitor.AirMonitorValues;
import angelo.itl.arduinoairqualitymonitor.activity.airmonitor.AlarmActivity;
import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;
import angelo.itl.arduinoairqualitymonitor.util.NotificationHelper;

/**
 * Created by Angelo on 15/12/2016.
 */

public class GasAlarm implements AirMonitorValues {
    private static Context context;
    private static NotificationHelper notificationHelper;
    private static Uri notification;
    private static Ringtone ring;
    private static Vibrator vibrator;
    private GlobalVariable global;
    private static SharedPreferences prefs;
    private static int vibration;

    public GasAlarm(Context context) {
        this.context = context;
        global = global.getInstance();
        notificationHelper = new NotificationHelper(context);
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ring = RingtoneManager.getRingtone(context, notification);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void startAlarm() {
        showAlarmNotification();
        playAlarmSound();
        vibrate();
    }

    public static void stopAlarm() {
        hideAlarmNotification();
        stopSound();
        stopVibrate();
    }

    public void checkAlarm() {
        if (global.getIsSmoke() != null) {
            if (!global.getIsSmoke().equals(NO_SMOKE_INDICATOR)) {
                if (prefs.getBoolean(context.getString(R.string.alarm_setting_key), true))
                    startAlarm();
                else stopAlarm();
            } else stopAlarm();
        }

    }

    public void showAlarmNotification() {
        notificationHelper
                .getNotificationManager()
                .notify(
                        NotificationHelper.GAS_ALARM,
                        notificationHelper
                                .createNotification(
                                        R.string.alarm_notification_title,
                                        R.string.alarm_notification_message,
                                        R.drawable.ic_warning_white_24dp,
                                        AlarmActivity.class,
                                        AirMonitorActivity.class,
                                        Notification.FLAG_ONGOING_EVENT
                                )
                );
    }

    public static void hideAlarmNotification() {
        notificationHelper.getNotificationManager().cancel(NotificationHelper.GAS_ALARM);
    }

    public static void playAlarmSound() {
        if (!ring.isPlaying()) ring.play();
    }

    public static void vibrate() {
        vibrator.vibrate(prefs.getInt(context.getString(R.string.vibration_setting_key), 400));
    }

    public static void stopSound() {
        if (ring.isPlaying()) ring.stop();
    }

    public static void stopVibrate() {
        vibrator.cancel();
    }
}
