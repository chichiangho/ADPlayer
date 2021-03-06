package com.adplayer.utils

import com.chichiangho.common.extentions.appCtx

object PlayManager {
    const val TYPE_PIC = 10
    const val TYPE_VIDEO = 11
    const val TYPE_MAP = 12
    const val TYPE_BARCODE = 13
    const val TYPE_UNKNOW = 14

    fun getVideoDir(): String {
        return appCtx.getExternalFilesDir("video").absolutePath
    }

    fun getPicDir(): String {
        return appCtx.getExternalFilesDir("picture").absolutePath
    }

    fun getMapPath(): String {
        return appCtx.getExternalFilesDir("map").absolutePath + "/map.png"
    }

    fun getBarCodePath(): String {
        return appCtx.getExternalFilesDir("barcode").absolutePath + "/barcode.png"
    }

    @JvmStatic
    fun main(args: Array<String>) {
        System.out.print(PlayManager.getBarCodePath())
    }

    fun getPics(withHeader: Boolean = true, callback: (array: Array<String>) -> Unit) {
        val picDir = getPicDir()
        val list = appCtx.getExternalFilesDir("picture").list()?.map {
            if (withHeader)
                "$picDir/$it"
            else
                it
        }?.toTypedArray() ?: emptyArray()
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
        val list = appCtx.getExternalFilesDir("video").list()?.map {
            if (withHeader)
                "$videoDir/$it"
            else
                it
        }?.toTypedArray() ?: emptyArray()
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

    fun getType(name1: String): Int {
        val name = name1.toLowerCase()
        return if (name.startsWith("map")) {
            TYPE_MAP
        } else if (name.startsWith("barcode")) {
            TYPE_BARCODE
        } else if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp")) {
            TYPE_PIC
        } else if (name.endsWith(".mp4") || name.endsWith(".rm") || name.endsWith(".rmvb") || name.endsWith(".flv")
                || name.endsWith(".mpeg1") || name.endsWith(".mpeg2") || name.endsWith(".mpeg3") || name.endsWith(".mpeg4")
                || name.endsWith(".mov") || name.endsWith(".mtv") || name.endsWith(".dat") || name.endsWith(".wmv")
                || name.endsWith(".avi") || name.endsWith(".3gp") || name.endsWith(".amv") || name.endsWith(".dmv")) {
            TYPE_VIDEO
        } else {
            TYPE_UNKNOW
        }
    }

    fun getPath(name: String): String {
        return when (getType(name)) {
            TYPE_MAP -> getMapPath()
            TYPE_PIC -> getPicDir() + "/" + name
            TYPE_VIDEO -> getVideoDir() + "/" + name
            TYPE_BARCODE -> getBarCodePath()
            else -> ""
        }
    }
}