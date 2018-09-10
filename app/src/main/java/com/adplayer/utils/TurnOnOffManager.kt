package com.adplayer.utils

import android.content.Intent
import com.adplayer.bean.ResultJSON
import com.chichiangho.common.extentions.appCtx
import java.io.DataOutputStream
import java.io.File
import java.io.IOException


object TurnOnOffManager {
    fun setOnOff(turnOn: String?, turnOff: String?): ResultJSON {



        return ResultJSON()
    }

    fun reboot(): ResultJSON {
        return try {
//            createSuProcess("reboot -p").waitFor() //重启
////            val pManager = appCtx.getSystemService(Context.POWER_SERVICE) as PowerManager
////            pManager.reboot("重启")
                    val intent2 = Intent(Intent.ACTION_REBOOT)
                    intent2.putExtra("nowait", 1)
                    intent2.putExtra("interval", 1)
                    intent2.putExtra("window", 0)
                    appCtx.sendBroadcast(intent2)
            ResultJSON()
        } catch (ex: Exception) {
            ResultJSON(ResultJSON.REBOOT_FAILED, "reboot failed: " + ex.message)
        }
    }

    @Throws(IOException::class)
    private fun createSuProcess(): Process {
        val rootUser = File("/system/xbin/ru")
        return if (rootUser.exists()) {
            Runtime.getRuntime().exec(rootUser.absolutePath)
        } else {
            Runtime.getRuntime().exec("su")
        }
    }

    @Throws(IOException::class)
    private fun createSuProcess(cmd: String): Process {
        var os: DataOutputStream? = null
        val process = createSuProcess()

        try {
            os = DataOutputStream(process.outputStream)
            os.writeBytes(cmd + "\n")
            os.writeBytes("exit $?\n")
        } finally {
            try {
                os?.close()
            } catch (e: IOException) {
            }
        }

        return process
    }
}