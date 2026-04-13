package com.mercury.messengerportal.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.mercury.messengerportal.MainActivity
import com.mercury.messengerportal.R

private const val TAG = "MercuryFCM"
private const val CHANNEL_ID = "mercury_jobs"
private const val CHANNEL_NAME = "Job Assignments"

/**
 * FCM push notification handler.
 *
 * To enable:
 * 1. Create a Firebase project at console.firebase.google.com
 * 2. Add google-services.json to /app
 * 3. Uncomment the firebase dependencies in app/build.gradle.kts
 * 4. Uncomment the <service> block in AndroidManifest.xml
 * 5. Uncomment the extends FirebaseMessagingService line below
 *
 * Expected FCM payload:
 * {
 *   "data": {
 *     "type": "JOB_ASSIGNED",
 *     "jobId": "JOB-006",
 *     "jobTitle": "Document Delivery – Revenue Dept"
 *   }
 * }
 */
// TODO: Uncomment when Firebase is configured
// class MercuryMessagingService : FirebaseMessagingService() {
class MercuryMessagingService {

    // override fun onNewToken(token: String) {
    //     Log.d(TAG, "New FCM token: $token")
    //     // TODO: POST token to backend so admin can target this device
    // }

    // override fun onMessageReceived(remoteMessage: RemoteMessage) {
    //     Log.d(TAG, "FCM received from: ${remoteMessage.from}")
    //     val data = remoteMessage.data
    //     when (data["type"]) {
    //         "JOB_ASSIGNED" -> showJobAssignedNotification(
    //             jobId = data["jobId"] ?: return,
    //             title = data["jobTitle"] ?: "New Job Assigned"
    //         )
    //     }
    // }

    fun showJobAssignedNotification(context: Context, jobId: String, title: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Notifications for new job assignments" }
        manager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("jobId", jobId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with app icon
            .setContentTitle("New Job Assigned")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(jobId.hashCode(), notification)
    }
}
