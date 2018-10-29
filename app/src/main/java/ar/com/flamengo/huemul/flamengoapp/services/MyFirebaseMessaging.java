package ar.com.flamengo.huemul.flamengoapp.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ar.com.flamengo.huemul.flamengoapp.MainActivity;
import ar.com.flamengo.huemul.flamengoapp.R;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "123456789";

    public static final String ANDROID_CHANNEL_ID = "ar.com.flamengo.huemul.flamengoapp.ANDROID";
    public static final CharSequence ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("PRIMER_PLANO", remoteMessage.getNotification().getBody() );

        showNotification(remoteMessage.getNotification());
    }

    private void showNotification(RemoteMessage.Notification notification) {
        try {
            PendingIntent pendingIntent = getPendingIntent();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.flamencolaunchapk)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle(notification.getTitle())
                    .setContentText(notification.getBody())
                    .setChannelId(CHANNEL_ID)
                    //.setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "flamengo";
                String description = "descripcion";

                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance r other notification behaviors after this

                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(0, builder.build());

        }catch (Exception e){
            Log.e("ERROR_NOTI", e.getMessage());
        }
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKE", s);
    }


}
