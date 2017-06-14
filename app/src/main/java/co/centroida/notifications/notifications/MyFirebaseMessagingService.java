package co.centroida.notifications.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import co.centroida.notifications.R;
import co.centroida.notifications.StartActivity;

/**
 * Created by L on 09/05/2017.
 * Copyright (c) 2017 Centroida. All rights reserved.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String NOTIFICATION_ID_EXTRA = "notificationId";
    private static final String IMAGE_URL_EXTRA = "imageUrl";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Intent notificationIntent;
        if(StartActivity.isAppRunning){
            notificationIntent = new Intent(this, StartActivity.class);
        }else{
            notificationIntent = new Intent(this, StartActivity.class);
        }

        //  notificationIntent.putExtra(Activity_ItemDetail.PRODUCT_ID, remoteMessage.getData().get("product_id"));
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0 /* Request code */, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT);

        int notificationId = new Random().nextInt(60000);

        //For further use
        String storeId = remoteMessage.getData().get("store_id");
        String offerId = remoteMessage.getData().get("offer_id");

        Bitmap bitmap = getBitmapfromUrl(remoteMessage.getData().get("image-url"));

        Intent likeIntent = new Intent(this,LikeService.class);
        likeIntent.putExtra(NOTIFICATION_ID_EXTRA,notificationId);
        likeIntent.putExtra(IMAGE_URL_EXTRA,remoteMessage.getData().get("image-url"));
        PendingIntent likePendingIntent = PendingIntent.getService(this,
                notificationId+1,likeIntent,PendingIntent.FLAG_ONE_SHOT);

        String channelId = "channeloneid2";

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(this,channelId)
                    .setLargeIcon(bitmap)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(remoteMessage.getData().get("title"))
                    .setStyle(new Notification.BigPictureStyle()
                            .setSummaryText(remoteMessage.getData().get("message"))
                            .bigPicture(bitmap))/*Notification with Image*/
                    .setContentText(remoteMessage.getData().get("message"))
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .addAction(R.drawable.ic_favorite_true,getString(R.string.notification_add_to_cart_button),likePendingIntent)
                    .setContentIntent(pendingIntent)

                    .build();
            notificationManager.notify(notificationId /* ID of notification */, notification);
        }else{

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setLargeIcon(bitmap)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(remoteMessage.getData().get("title"))
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .setSummaryText(remoteMessage.getData().get("message"))
                            .bigPicture(bitmap))/*Notification with Image*/
                    .setContentText(remoteMessage.getData().get("message"))
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .addAction(R.drawable.ic_favorite_true,getString(R.string.notification_add_to_cart_button),likePendingIntent)
                    .setContentIntent(pendingIntent);
            notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build());

        }



    }

    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}