package com.csakitheone.ipariminimap.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.csakitheone.ipariminimap.MainOldActivity
import com.csakitheone.ipariminimap.R

class Notifications {
    companion object {
        fun createChannels(context: Context) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val notifManager = context.getSystemService(NotificationManager::class.java)
                notifManager.createNotificationChannel(NotificationChannel("ring", "Csengetési rend", NotificationManager.IMPORTANCE_DEFAULT))
                notifManager.createNotificationChannel(NotificationChannel("task", "Feladat értesítés", NotificationManager.IMPORTANCE_DEFAULT))
            }
        }

        fun sendTaskNotification(context: Context, text: String) {
            createChannels(context)
            val notifManager = context.getSystemService(NotificationManager::class.java)
            notifManager.notify(2,
                NotificationCompat.Builder(context, "task")
                    .setSmallIcon(R.drawable.ic_my_location)
                    .setContentTitle("Feladat értesítés")
                    .setContentText(text)
                    .setContentIntent(PendingIntent.getActivity(context, 1, Intent(context, MainOldActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .build()
            )
        }
    }
}