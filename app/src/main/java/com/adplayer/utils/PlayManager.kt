package com.adplayer.utils

import com.chichiangho.common.base.BaseApplication

object PlayManager {


    fun getVideoDir(): String? {
        return BaseApplication.instance.getExternalFilesDir("video").absolutePath
    }

    fun getPicDir(): String? {
        return BaseApplication.instance.getExternalFilesDir("picture").absolutePath
    }

    fun getPics(callback: (array: Array<String>) -> Unit) {
        val list = BaseApplication.instance.getExternalFilesDir("picture").list()
        if (list.isEmpty()) {
            ConnectManager.getPics {
                callback(it)
            }
        } else {
            callback(list)
        }
    }

    fun getVideos(callback: (array: Array<String>) -> Unit) {
        val list = BaseApplication.instance.getExternalFilesDir("video").list()
        if (list.isEmpty()) {
            ConnectManager.getVideos {
                callback(it)
            }
        } else {
            callback(list)
        }
    }
}