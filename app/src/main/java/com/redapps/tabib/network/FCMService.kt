package com.redapps.tabib.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.clovertech.autolib.utils.PrefUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.redapps.tabib.R
import com.redapps.tabib.ui.LoginActivity

class FCMService: FirebaseMessagingService() {

    val TAG = "LOG TAG"


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        PrefUtils.with(applicationContext).save(PrefUtils.Keys.FCM_TOKEN, p0)
        Log.d(TAG, "onNewToken: token updated")
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        Log.d(TAG, "onMessageReceived: success")

        createNotificationChannel()

        // Create push notification
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        var builder = NotificationCompat.Builder(this, getString(R.string.default_channel_id))
            .setSmallIcon(R.drawable.doctor1)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notif_msg_default))
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(1000))
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            val notificationId = PrefUtils.with(applicationContext).getInt(PrefUtils.Keys.NOTIFICATION_COUNT, 0)
            notify(notificationId, builder.build())
            PrefUtils.with(applicationContext).save(PrefUtils.Keys.NOTIFICATION_COUNT, notificationId + 1)
        }
    }


    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(getString(R.string.default_channel_id), name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


}