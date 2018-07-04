package com.adplayer.utils

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


object CopyUtil {
    @Throws(IOException::class)
    fun copyDir(oldPath: String, newPath: String) {
        val file1 = File(oldPath)
        val fs = file1.listFiles() ?: return

        val file2 = File(newPath)
        if (!file2.exists()) {
            file2.mkdirs()
        }

        for (f in fs) {
            if (f.isFile) { //文件
                copyFile(f.path, newPath + "/" + f.name)
            } else if (f.isDirectory) { //文件夹
                copyDir(f.path, newPath + "/" + f.name)
            }
        }
    }

    @Throws(IOException::class)
    fun copyFile(oldPath: String, newPath: String) {
        val oldFile = File(oldPath)
        val file = File(newPath)
        val `in` = FileInputStream(oldFile)
        val out = FileOutputStream(file)

        val buffer = ByteArray(2097152)

        while (`in`.read(buffer) != -1) {
            out.write(buffer)
        }
    }

    @Throws(IOException::class)
    fun copyAsserts(context: Context, oldPath: String, newPath: String) {
        val file = File(newPath)
        val `in` = context.assets.open(oldPath)
        val out = FileOutputStream(file)

        val buffer = ByteArray(2097152)

        while (`in`.read(buffer) != -1) {
            out.write(buffer)
        }
    }
}