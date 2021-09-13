package com.csakitheone.ipariminimap.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.csakitheone.ipariminimap.services.RingService

class RingNotifCancelReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context.stopService(Intent(context, RingService::class.java))
    }
}
