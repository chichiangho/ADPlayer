package com.adplayer.utils

import com.chichiangho.common.extentions.appCtx

object PlayManager {

    fun getVideoDir(): String {
        return appCtx.getExternalFilesDir("video").absolutePath
    }

    fun getPicDir(): String {
        return appCtx.getExternalFilesDir("picture").absolutePath
    }

    fun getPics(callback: (array: Array<String>) -> Unit) {
        val list = appCtx.getExternalFilesDir("picture").list().map {
            getPicDir() + "/" + it
        }.toTypedArray()
        if (list.isEmpty()) {
            ConnectManager.getPics {
                callback(it)
            }
        } else {
            callback(list)
        }
    }

    fun getVideos(callback: (array: Array<String>) -> Unit) {
        val list = appCtx.getExternalFilesDir("video").list().map {
            getVideoDir() + "/" + it
        }.toTypedArray()
        if (list.isEmpty()) {
            ConnectManager.getVideos {
                callback(it)
            }
        } else {
            callback(list)
        }
    }
}