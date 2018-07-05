package com.adplayer.utils

import android.util.Log
import com.chichiangho.common.extentions.appCtx
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket


object ConnectManager {
    const val COMMAND_PLAY_BANNER = "playBanner"
    const val COMMAND_PLAY_VIDEO = "playVideo"
    const val COMMAND_TACK_PICTURE = "takePicture"
    const val COMMAND_SHOW_MAP = "showMap"
    const val COMMAND_UPDATE = "update"

    private var callback: ((command: String, extra: JSONObject?) -> Unit?)? = null

    fun init() {
        Thread {
            /*指明服务器端的端口号*/
            var serverSocket: ServerSocket? = null
            try {
                serverSocket = ServerSocket(8000)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.getInetAddresses()
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    val mIP = inetAddress.getHostAddress().substring(0, 3)
                    if (mIP == "192") {
                        var IP = inetAddress.getHostAddress()    //获取本地IP
                        var PORT = serverSocket?.getLocalPort()    //获取本地的PORT
                        Log.e("IP", "" + IP)
                        Log.e("PORT", "" + PORT)
                    }
                }
            }

            var socket: Socket? = null
            while (true) {
                try {
                    socket = serverSocket?.accept()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                val byte = ByteArray(1024)
                socket?.getInputStream()?.read(byte)
                val commandObj = JSONObject(String(byte).trim())
                if (commandObj.has("command")) {
                    var extraObj: JSONObject? = null
                    if (commandObj.has("extra"))
                        extraObj = commandObj.optJSONObject("extra")
                    when (commandObj.opt("command")) {
                        COMMAND_PLAY_BANNER -> {
                            callback?.invoke(COMMAND_PLAY_BANNER, extraObj)
                        }
                        COMMAND_PLAY_VIDEO -> {
                            callback?.invoke(COMMAND_PLAY_VIDEO, extraObj)
                        }
                        COMMAND_TACK_PICTURE -> {
                            callback?.invoke(COMMAND_TACK_PICTURE, extraObj)
                        }
                        COMMAND_SHOW_MAP -> {
                            callback?.invoke(COMMAND_SHOW_MAP, extraObj)
                        }
                    }
                    val outStream = socket?.getOutputStream()
                    outStream?.write("received command ${commandObj.opt("command")}".toByteArray())
                    outStream?.flush()
                }
            }
        }.start()
    }

    fun registerCommandListener(callback: (command: String, extra: JSONObject?) -> Unit) {
        this.callback = callback
    }

    fun getPics(callback: (array: Array<String>) -> Unit) {
        //以下为网络请求失败后的备用
        appCtx.assets.list("pic").forEach {
            CopyUtil.copyAsserts(appCtx, "pic/$it", PlayManager.getPicDir() + "/" + it)
        }

        //网络请求后一定调用
        callback(appCtx.getExternalFilesDir("picture").list().map {
            PlayManager.getPicDir() + "/" + it
        }.toTypedArray())
    }

    fun getVideos(callback: (array: Array<String>) -> Unit) {
        //以下为网络请求失败后的备用
        appCtx.assets.list("video").forEach {
            CopyUtil.copyAsserts(appCtx, "video/$it", PlayManager.getVideoDir() + "/" + it)
        }
        //网络请求后一定调用
        callback(appCtx.getExternalFilesDir("video").list().map {
            PlayManager.getVideoDir() + "/" + it
        }.toTypedArray())
    }
}