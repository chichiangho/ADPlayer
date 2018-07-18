package com.adplayer.utils

import com.chichiangho.common.extentions.appCtx

object PlayManager {

    fun getVideoDir(): String {
        return appCtx.getExternalFilesDir("video").absolutePath
    }

    fun getPicDir(): String {
        return appCtx.getExternalFilesDir("picture").absolutePath
    }

    fun getPics(withHeader: Boolean = true, callback: (array: Array<String>) -> Unit) {
        val picDir = getPicDir()
        val list = appCtx.getExternalFilesDir("picture").list().map {
            if (withHeader)
                "$picDir/$it"
            else
                it
        }.toTypedArray()
        if (list.isEmpty()) {
            appCtx.assets.list("pic").forEach {
                CopyUtil.copyAsserts(appCtx, "pic/$it", PlayManager.getPicDir() + "/" + it)
            }

            callback(appCtx.getExternalFilesDir("picture").list().map {
                if (withHeader)
                    "$picDir/$it"
                else
                    it
            }.toTypedArray())
        } else {
            callback(list)
        }
    }

    fun getVideos(withHeader: Boolean = true, callback: (array: Array<String>) -> Unit) {
        val videoDir = getVideoDir()
        val list = appCtx.getExternalFilesDir("video").list().map {
            if (withHeader)
                "$videoDir/$it"
            else
                it
        }.toTypedArray()
        if (list.isEmpty()) {
            appCtx.assets.list("video").forEach {
                CopyUtil.copyAsserts(appCtx, "video/$it", PlayManager.getVideoDir() + "/" + it)
            }

            callback(appCtx.getExternalFilesDir("video").list().map {
                if (withHeader)
                    "$videoDir/$it"
                else
                    it
            }.toTypedArray())
        } else {
            callback(list)
        }
    }
}