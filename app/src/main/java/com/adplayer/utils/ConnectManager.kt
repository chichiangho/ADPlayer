package com.adplayer.utils

import android.net.Proxy.getPort
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket


object ConnectManager {
    const val COMMAND_PLAY_BANNER = "playBanner"
    const val COMMAND_PLAY_VIDEO = "playVideo"
    const val COMMAND_TACK_PICTURE = "takePicture"
    const val COMMAND_SHOW_MAP = "showMap"
    const val COMMAND_UPDATE = "update"

    private var callback: ((command: String, extra: String) -> Unit?)? = null

    fun init() {
        Thread {
            /*指明服务器端的端口号*/
            var serverSocket: ServerSocket? = null
            try {
                serverSocket = ServerSocket(8000)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            var socket: Socket? = null
            var inputStream: InputStream? = null
            while (true) {
                try {
                    socket = serverSocket?.accept()
                    inputStream = socket?.getInputStream()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                val byte = ByteArray(1024)
                inputStream?.read(byte)
                val commandObj = JSONObject(String(byte))
                if (commandObj.has("command")) {
                    when (commandObj.opt("command")) {
                        COMMAND_PLAY_BANNER -> {
                            callback?.invoke(COMMAND_PLAY_BANNER, "")
                        }
                        COMMAND_PLAY_VIDEO -> {
                            callback?.invoke(COMMAND_PLAY_VIDEO, "")
                        }
                        COMMAND_TACK_PICTURE -> {
                            callback?.invoke(COMMAND_TACK_PICTURE, "")
                        }
                        COMMAND_SHOW_MAP -> {
                            callback?.invoke(COMMAND_SHOW_MAP, "")
                        }
                    }
                    val outStream = socket?.getOutputStream()
                    outStream?.write("received command ${commandObj.opt("command")}".toByteArray())
                    outStream?.flush()
                }
            }
        }.start()
    }

    fun registerCommandListener(callback: (command: String, name: String) -> Unit) {
        this.callback = callback
    }

    fun getPics(callback: (array: Array<String>) -> Unit) {

    }

    fun getVideos(callback: (array: Array<String>) -> Unit) {

    }
}