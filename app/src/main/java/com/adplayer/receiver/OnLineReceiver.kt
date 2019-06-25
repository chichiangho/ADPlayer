package com.adplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import java.lang.ref.WeakReference

/**
 * @author chichiangho
 * @date 2018/7/28
 * @desc
 */
class OnLineReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {//网络状态发送变化
            val info = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.activeNetworkInfo
            if (info?.isConnected == true) {
                listener.forEach {
                    it.get()?.onLine()
                }
            }
        }
    }

    interface ChangeListener {
        fun onLine()
    }

    companion object {
        private val listener: ArrayList<WeakReference<ChangeListener>> = ArrayList()

        fun addOnLineListener(changeListener: ChangeListener) {
            listener.add(WeakReference(changeListener))
        }
    }
}