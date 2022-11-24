/*
 * Created by Stoyanoff on 14/06/2017.
 */

package co.centroida.notifications.notifications

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

/** Key for image url extra, received from notification. */
internal const val IMAGE_URL_EXTRA = "imageUrl"

/**
 * Helper service for when we need to do some work when the user clicks the like button of a
 * received notification during app foreground.
 */
class LikeService : Service() {

    override fun onCreate() {
        super.onCreate()
        cancelNotification()
        // Store the image from the notification in gallery and admire it, or something.
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun cancelNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
