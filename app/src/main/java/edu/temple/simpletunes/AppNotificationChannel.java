package edu.temple.simpletunes;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
 * The App class is used to create items that are global to the application lifecycle.
 */

//https://codinginflow.com/tutorials/android/foreground-service
public class AppNotificationChannel extends Application {
    public static final String CHANNEL_ID = "mediaPlayerServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    /**
     * The createNotificationChannel method creates the notification channel for the application.
     *
     *  * Starting in Android 8.0 (API level 26), all notifications must be assigned to a channel.
     *  * The notification channel is used to  set the visual and auditory behavior that is applied to
     *  * all notifications in that channel. The users can change these settings and decide
     *  * which notification channels should be intrusive or visible at all.
     *  * https://developer.android.com/training/notify-user/channels#:~:text=When%20you%20target%20Android%208.0,API%20level%2025)%20or%20lower.
     */
    private void createNotificationChannel() {
        NotificationChannel mediaServiceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Media Player Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(mediaServiceChannel);
    }

}
