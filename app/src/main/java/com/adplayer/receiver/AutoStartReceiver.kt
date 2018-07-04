package com.adplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.adplayer.activity.MainActivity


class AutoStartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val i = Intent(context, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context?.startActivity(i)
    }
}