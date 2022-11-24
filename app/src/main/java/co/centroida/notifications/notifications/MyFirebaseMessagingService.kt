/*
 * Created by Stoyanoff on 14/06/2017.
 */

package co.centroida.notifications.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log

import androidx.core.app.NotificationCompat

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import co.centroida.notifications.R
import co.centroida.notifications.StartActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** Displayed notification ID. */
internal const val NOTIFICATION_ID = 0

/** Timeout for downloading notification image. We assume this service won't live more than the set time. */
private const val NOTIFICATION_IMAGE_DOWNLOAD_TIMEOUT = 5_000L
private const val LOG_TAG = "FirebaseService"
private const val CLICK_INTENT_REQUEST_CODE = 1
private const val LIKE_ACTION_INTENT_REQUEST_CODE = 2

/**
 * Handles notifications received from Firebase when the app is in foreground.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val job = Job()
    /** Scope for fetching notification image. */
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(LOG_TAG, "Refreshed token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(LOG_TAG, "Received message with body: ${message.notification?.body}")
        message.notification?.body?.let { description ->
            sendNotification(
                message.notification?.title,
                description,
                message.notification?.imageUrl
            )
        }
    }

    /**
     * Shows the notification to the user. Tries to fetch image, shows without image if fetch times out.
     */
    private fun sendNotification(
        title: String?,
        description: String,
        imageUrl: Uri?
    ) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // If notifications are not enabled, our work here is done.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.areNotificationsEnabled()) {
            return
        }
        // Build notification.
        val notificationBuilder = buildNotification(title, description, imageUrl)
        scope.launch {
            // Try to add image.
            imageUrl?.let { uri ->
                fetchNotificationImage(uri.toString())?.let {
                    addNotificationImage(
                        notificationBuilder,
                        it
                    )
                }
            }
            // Show notification.
            notificationManager.notify(NOTIFICATION_ID /* ID of notification */, notificationBuilder.build())
        }
    }

    /**
     * Fills a notification builder with the received data.
     */
    private fun buildNotification(
        title: String?,
        description: String,
        imageUrl: Uri?
    ) = NotificationCompat.Builder(this, getString(R.string.notifications_promotions_channel_id))
        .apply {
            setSmallIcon(R.drawable.ic_notification)
            setAutoCancel(true)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            setContentTitle(title)
            setContentText(description)
            setContentIntent(createClickActionIntent())
            imageUrl?.let {
                addAction(
                    R.drawable.ic_favorite_true,
                    getString(R.string.notification_add_to_cart_button),
                    createLikeActionIntent(it.toString())
                )
            }
        }

    private fun createClickActionIntent() = PendingIntent.getActivity(
        this,
        CLICK_INTENT_REQUEST_CODE /* Request code */,
        Intent(this, StartActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    /**
     * Creates a [PendingIntent] which delegates to another service the work of updating content
     * when the user "likes" the notification.
     *
     * @param imageUrl the received data from the notification.
     * @return the [PendingIntent] to the helper service.
     */
    private fun createLikeActionIntent(imageUrl: String): PendingIntent? {
        val likeIntent = Intent(this, LikeService::class.java)
        likeIntent.putExtra(IMAGE_URL_EXTRA, imageUrl)
        return PendingIntent.getService(
            this,
            LIKE_ACTION_INTENT_REQUEST_CODE,
            likeIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Sets the style of the notification as [NotificationCompat.BigPictureStyle] and adds
     * an image to it.
     *
     * @param builder current notification builder.
     * @param bitmap downloaded notification image.
     * @return a builder with the image added.
     */
    private fun addNotificationImage(builder: NotificationCompat.Builder, bitmap: Bitmap) =
        builder
            .setLargeIcon(bitmap)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null)
            )

    /**
     * Downloads image asynchronously with [Glide]. Has timeout.
     *
     * @param url image url.
     * @return downloaded [Bitmap] if successful.
     */
    private suspend fun fetchNotificationImage(url: String): Bitmap? =
        withTimeoutOrNull(NOTIFICATION_IMAGE_DOWNLOAD_TIMEOUT) {
            withContext(Dispatchers.IO) {
                try {
                    Glide.with(applicationContext)
                        .asBitmap()
                        .load(url)
                        .submit().get()
                } catch (e: Exception) {
                    // Catch an exception if the downloading fails, e.g. when the url is invalid.
                    null
                }
            }
        }
}