package com.adplayer.bean

import org.json.JSONObject

class ResultJSON(code: Int = 1000, msg: String = "success") : JSONObject() {
    init {
        put("code", code).put("msg", msg)
    }

    companion object {
        const val NO_SUCH_FILE = 2001
        const val PARAMS_ERROR = 2002
        const val NO_SUCH_COMMAND = 2003
        const val REBOOT_FAILED = 2004
        const val TYPE_NOT_SUPPORT = 2005
    }
}