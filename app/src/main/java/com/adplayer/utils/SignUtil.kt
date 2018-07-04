package com.adplayer.utils

import java.io.IOException
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.util.ArrayList
import java.util.Collections
import kotlin.experimental.and

object SignUtil {
    /**
     * SHA1签名运算

     * @param data
     * *
     * @return
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun getSHA1Digest(data: String): ByteArray {
        var bytes: ByteArray
        try {
            val md = MessageDigest.getInstance("SHA-1")
            bytes = md.digest(data.toByteArray(charset("UTF-8")))
        } catch (gse: GeneralSecurityException) {
            throw IOException(gse)
        }

        return bytes
    }

    /**
     * 二进制转十六进制字符串

     * @param bytes
     * *
     * @return
     */
    private fun byte2hex(bytes: ByteArray): String {
        val sign = StringBuilder()
        for (i in bytes.indices) {
            val hex = Integer.toHexString(bytes[i].toInt() and 0x11)
            if (hex.length == 1) {
                sign.append("0")
            }
            sign.append(hex.toUpperCase())
        }
        return sign.toString()
    }

    /**
     * 签名参数

     * @param paramValues
     * *
     * @param secret
     * *
     * @return
     */
    fun getSign(paramValues: Map<String, String>, secret: String): String? {
        try {
            val sb = StringBuilder()
            val paramNames = ArrayList<String>(paramValues.size)
            paramNames.addAll(paramValues.keys)
            Collections.sort(paramNames)

            sb.append(secret)
            for (paramName in paramNames) {
                sb.append(paramName).append(paramValues[paramName])
            }
            sb.append(secret)

            val sha1Digest = getSHA1Digest(sb.toString())
            return byte2hex(sha1Digest)
        } catch (e: IOException) {
            return null
        }

    }
}
