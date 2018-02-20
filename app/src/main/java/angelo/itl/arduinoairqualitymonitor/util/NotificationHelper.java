package angelo.itl.arduinoairqualitymonitor.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import angelo.itl.arduinoairqualitymonitor.R;
import angelo.itl.arduinoairqualitymonitor.activity.main.MainActivity;

/**
 * Created by Angelo on 15/12/2016.
 */

public class NotificationHelper {
    public static final int RECEIVING_DATA = 1;
    public static final int GAS_ALARM = 2;
    private Context context;
    private NotificationManager notificationManager;
    private Notification.Builder builder;
    private Notification notification;

    public NotificationHelper(Context context) {
        this.context = context;
        setNotificationManager((NotificationManager) context
                .getSystemService(context.NOTIFICATION_SERVICE));
    }

    /**
     * @param contentTitle the title of the notification
     * @param contentText  the text of the notification
     */
    public Notification createNotification(
            int contentTitle,
            int contentText,
            int icon,
            Class<?> resultIntentClass,
            Class<?> parentStackClass,
            int flag) {
        Intent resultIntent = new Intent(context, resultIntentClass);//create intent to open with not
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);//create a stack builder to contain intent
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(parentStackClass);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        //build the not
        builder = new Notification.Builder(context);
        builder
                .setSmallIcon(icon)
                .setAutoCancel(false)
                .setContentTitle(context.getString(contentTitle))
                .setContentText(context.getString(contentText))
                .setContentIntent(resultPendingIntent);
        notification = builder.build();//assing the build to not
        notification.flags = flag;//indicates that is not a closable not
        return notification;
    }

   /* public Notification createNotification(){

    }*/

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }
}
