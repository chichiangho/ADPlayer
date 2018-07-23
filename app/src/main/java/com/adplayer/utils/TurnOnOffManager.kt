package com.adplayer.utils

import android.content.Context
import android.os.PowerManager
import com.adplayer.bean.ResultJSON
import com.chichiangho.common.extentions.appCtx

object TurnOnOffManager {
    fun setOnOff(turnOn: String?, turnOff: String?): ResultJSON {

        return ResultJSON()
    }

    fun reboot(): ResultJSON {
        return try {
//                    Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot")).waitFor() //重启
            val pManager = appCtx.getSystemService(Context.POWER_SERVICE) as PowerManager
            pManager.reboot("重启")
//                    val intent2 = Intent(Intent.ACTION_REBOOT)
//                    intent2.putExtra("nowait", 1)
//                    intent2.putExtra("interval", 1)
//                    intent2.putExtra("window", 0)
//                    appCtx.sendBroadcast(intent2)
            ResultJSON()
        } catch (ex: Exception) {
            ResultJSON(ResultJSON.REBOOT_FAILED, "reboot failed: " + ex.message)
        }
    }
}