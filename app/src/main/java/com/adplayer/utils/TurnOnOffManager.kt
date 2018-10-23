package com.adplayer.utils

import com.adplayer.bean.ResultJSON
import com.chichiangho.common.extentions.getPrivateSharedPreferences

object TurnOnOffManager {
    fun setOnOff(turnOn: String?, turnOff: String?): ResultJSON {
        getPrivateSharedPreferences().edit().putString("turnOn", turnOn ?: "")
                .putString("turnOff", turnOff ?: "").apply()
        return ResultJSON()
    }

    fun getTurnOff(): String {
        return getPrivateSharedPreferences().getString("turnOff", "")
    }
}