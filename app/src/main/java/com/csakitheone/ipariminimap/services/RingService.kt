package com.csakitheone.ipariminimap.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.csakitheone.ipariminimap.MainActivity
import com.csakitheone.ipariminimap.R
import com.csakitheone.ipariminimap.Task
import com.csakitheone.ipariminimap.helper.Notifications
import com.csakitheone.ipariminimap.helper.Rings
import java.util.*
import kotlin.concurrent.timerTask

class RingService : Service() {
    val timer: Timer = Timer("ringService")

    lateinit var prefs: SharedPreferences
    lateinit var tasks: List<Task>
    lateinit var notifBuilder: NotificationCompat.Builder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        Notifications.createChannels(this)

        notifBuilder = NotificationCompat.Builder(this, "ring")
            .setSmallIcon(R.drawable.ic_alarm_bell)
            .setContentTitle(Rings.getCurrentLesson())
            .setContentText(Rings.getTimeUntilNext())
            .setOnlyAlertOnce(true)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))

        startForeground(1, notifBuilder.build())

        timer.schedule(timerTask { tick() }, 0, 1000L)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        val notifManager = ContextCompat.getSystemService(this@RingService, NotificationManager::class.java)
        notifManager?.cancel(1)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun tick() {
        val notifManager = ContextCompat.getSystemService(this@RingService, NotificationManager::class.java)
        notifBuilder = NotificationCompat.Builder(this@RingService, "ring")
            .setSmallIcon(R.drawable.ic_alarm_bell)
            .setContentTitle(Rings.getCurrentLesson())
            .setContentText(Rings.getTimeUntilNext())
            .setContentIntent(PendingIntent.getActivity(this@RingService, 1, Intent(this@RingService, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            .setOnlyAlertOnce(true)
            .setColor(ContextCompat.getColor(this@RingService, R.color.colorPrimary))
        notifManager?.notify(1, notifBuilder.build())

        // Weekend
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) return

        // Tasks
        tasks = prefs.getStringSet("tasks", setOf())
            ?.toList()
            ?.map { r -> Task(r) }
            ?.filter { r -> r.state }
            ?: listOf()

        for (t in tasks) {
            if (t.condition == "minden óra elején" && Rings.isLessonStart()) {
                t.execute(this@RingService)
            }
            else if (t.condition == "minden szünet elején" && Rings.isLessonEnd()) {
                t.execute(this@RingService)
            }
            else if (Rings.times[t.condition] == Rings.calToInt(Calendar.getInstance())) {
                t.execute(this@RingService)
            }
        }
    }
}
