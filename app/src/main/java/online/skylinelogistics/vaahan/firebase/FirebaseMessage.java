package online.skylinelogistics.vaahan.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import online.skylinelogistics.vaahan.R;

public class FirebaseMessage extends FirebaseMessagingService {

    public void onMessageReceived(RemoteMessage message) {
        String from = message.getFrom();
        Map data = message.getData();
        String msg = data.get("message").toString();

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendNotificationOreo(msg);
        }
        else {
            sendNotification(msg);
        }
            // [END_EXCLUDE]
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotificationOreo(String msg) {
        final String CHANNEL_ID = "online.skylinelogistics.skylineoffice.officeupdate";
        final String CHANNEL_NAME = "Office Update";

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notifManager.createNotificationChannel(notificationChannel);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Next Stop")
                .setContentText(msg)
                .setSmallIcon(R.drawable.cloud)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);

        notifManager.notify(0 , builder.build());
    }

    private void sendNotification(String message) {

        Intent intent = new Intent(this, online.skylinelogistics.vaahan.MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.cloud)
                .setContentTitle("Vehicle Status")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);
        
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
