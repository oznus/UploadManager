package nusem.oz.uploadmanager.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

import nusem.oz.uploadmanager.Model.NotificationSettings;

/**
 * Created by oznusem on 1/22/16.
 */
public class UploadNotificationManager {

    private static final long MAX_TIME_TO_LIVE = 90 * 1000;

    public static void createNotification(final Service service, NotificationCompat.Builder notificationBuilder,
                                          final NotificationSettings notificationSettings) {


        notificationBuilder.setContentTitle(notificationSettings.getTitle())
                .setContentText(notificationSettings.getMessage())
                .setContentIntent(PendingIntent.getBroadcast(service, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(notificationSettings.getIconResourceID())
                .setProgress(100, 0, true)
                .setOngoing(true);

        service.startForeground(notificationSettings.getNotificationId(), notificationBuilder.build());

        NotificationManager notificationManager = (NotificationManager)
                service.getSystemService(Context.NOTIFICATION_SERVICE);
        // hide the notificationBuilder after its selected
        Notification noti = notificationBuilder.build();
        noti.flags |= NotificationCompat.FLAG_AUTO_CANCEL;
        notificationManager.notify(notificationSettings.getNotificationId(), noti);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                cancelNotification(notificationSettings,service);
            }
        }, MAX_TIME_TO_LIVE);
    }

    public static void updateNotificationProgress(int progress,
                                                   NotificationSettings notificationSettings,
                                                   NotificationCompat.Builder notificationBuilder,
                                                   Service service) {

        notificationBuilder.setContentTitle(notificationSettings.getTitle()).setContentText(notificationSettings.getMessage())
                .setSmallIcon(notificationSettings.getIconResourceID()).setProgress(100, progress, false)
                .setOngoing(true);

        NotificationManager notificationManager = (NotificationManager)
                service.getSystemService(Context.NOTIFICATION_SERVICE);
        // hide the notificationBuilder after its selected
        Notification noti = notificationBuilder.build();
        noti.flags |= NotificationCompat.FLAG_AUTO_CANCEL;
        notificationManager.notify(notificationSettings.getNotificationId(), noti);

        service.startForeground(notificationSettings.getNotificationId(), notificationBuilder.build());
    }

    public static void updateNotificationCompleted(NotificationSettings notificationSettings,
                                                   NotificationCompat.Builder notificationBuilder,
                                                   Service service) {

        service.stopForeground(notificationSettings.isAutoClearOnSuccess());

        if (!notificationSettings.isAutoClearOnSuccess()) {
            notificationBuilder.setContentTitle(notificationSettings.getTitle())
                    .setContentText(notificationSettings.getCompleted())
                    .setSmallIcon(notificationSettings.getIconResourceID()).setProgress(0, 0, false).setOngoing(false);

            getNotificationManager(service).notify(notificationSettings.getNotificationId(), notificationBuilder.build());
        }
    }

    public static void updateNotificationError(NotificationSettings notificationSettings,
                                               NotificationCompat.Builder notificationBuilder,
                                               Service service) {
        service.stopForeground(false);

        notificationBuilder.setContentTitle(notificationSettings.getTitle()).setContentText(notificationSettings.getError())
                .setSmallIcon(notificationSettings.getIconResourceID()).setProgress(0, 0, false).setOngoing(false);

        getNotificationManager(service).notify(notificationSettings.getNotificationId(), notificationBuilder.build());
    }

    public static void cancelNotification(NotificationSettings notificationSettings,Service service) {
        service.stopForeground(false);
        getNotificationManager(service).cancel(notificationSettings.getNotificationId());
    }

    public static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
