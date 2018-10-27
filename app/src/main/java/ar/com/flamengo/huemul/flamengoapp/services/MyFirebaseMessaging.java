package ar.com.flamengo.huemul.flamengoapp.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ar.com.flamengo.huemul.flamengoapp.MainActivity;
import ar.com.flamengo.huemul.flamengoapp.R;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    public static final String ANDROID_CHANNEL_ID = "ar.com.flamengo.huemul.flamengoapp.ANDROID";
    public static final CharSequence ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d("PRIMER_PLANO", remoteMessage.getNotification().getBody() );

        super.onMessageReceived(remoteMessage);
        showNotification(remoteMessage.getNotification());
    }

    private void showNotification(RemoteMessage.Notification notification) {
        try {
            PendingIntent pendingIntent = getPendingIntent();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(notification.getTitle())
                    .setContentText(notification.getBody())
                    .setChannelId("default")
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(0, builder.build());


        }catch (Exception e){
            Log.e("ERROR_NOTI", e.getMessage());
        }
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKE", s);
    }


}
