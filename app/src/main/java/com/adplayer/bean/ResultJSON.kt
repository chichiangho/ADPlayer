package com.adplayer.bean

import org.json.JSONObject

class ResultJSON(code: Int = 1000, msg: String = "") : JSONObject() {
    init {
        put("code", code)
        if (msg.isBlank()) {
            when (code) {
                1000 -> put("msg", "success")
                NO_SUCH_FILE -> put("msg", "no such file")
                PARAMS_ERROR -> put("msg", "params error")
                NO_SUCH_COMMAND -> put("msg", "no such command")
                REBOOT_FAILED -> put("msg", "reboot failed")
                TYPE_NOT_SUPPORT -> put("msg", "file type not support")
                UPLOAD_FAILED -> put("msg", "upload file failed")
                CAMERA_NOT_READY -> put("msg", "camera not ready yet")
                else -> put("msg", msg)
            }
        } else {
            put("msg", msg)
        }
    }

    fun add(key: String, value: Any): ResultJSON {
        put(key, value)
        return this
    }

    companion object {
        const val NO_SUCH_FILE = 2001
        const val PARAMS_ERROR = 2002
        const val NO_SUCH_COMMAND = 2003
        const val REBOOT_FAILED = 2004
        const val TYPE_NOT_SUPPORT = 2005
        const val UPLOAD_FAILED = 2006
        const val ADT_ERROR = 2007
        const val TAKE_PIC = 1283//专为拍照后传递照片地址使用
        const val CAMERA_NOT_READY = 1284
    }
}