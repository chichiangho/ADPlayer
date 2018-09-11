package com.adplayer.utils

import android.content.Intent
import com.adplayer.bean.ResultJSON
import com.chichiangho.common.extentions.appCtx
import com.chichiangho.common.extentions.getPrivateSharedPreferences
import java.io.DataOutputStream
import java.io.File
import java.io.IOException


object TurnOnOffManager {
    fun setOnOff(turnOn: String?, turnOff: String?): ResultJSON {
        getPrivateSharedPreferences().edit().putString("turnOn", turnOn ?: "")
                .putString("turnOff", turnOff ?: "").apply()
        return ResultJSON()
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