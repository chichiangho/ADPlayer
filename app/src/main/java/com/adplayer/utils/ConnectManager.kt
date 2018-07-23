package com.adplayer.utils

import android.content.Context
import android.os.PowerManager
import com.adplayer.bean.ResultJSON
import com.chichiangho.common.extentions.appCtx
import com.chichiangho.common.extentions.toJson
import com.koushikdutta.async.callback.CompletedCallback
import com.koushikdutta.async.callback.DataCallback
import com.koushikdutta.async.http.body.JSONObjectBody
import com.koushikdutta.async.http.body.MultipartFormDataBody
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import com.koushikdutta.async.http.server.HttpServerRequestCallback
import org.json.JSONArray
import org.json.JSONObject
import java.io.*


object ConnectManager : HttpServerRequestCallback {
    const val COMMAND_PLAY_BANNER = "playBanner"
    const val COMMAND_PLAY_VIDEO = "playVideo"
    const val COMMAND_TACK_PICTURE = "takePicture"
    const val COMMAND_SHOW_MAP = "showMap"
    private const val COMMAND_GET_PICS = "getPics"
    private const val COMMAND_GET_VIDEOS = "getVideos"
    private const val COMMAND_UPLOAD = "upload"
    private const val COMMAND_REBOOT = "reboot"
    private const val COMMAND_DELETE = "delete"
    private const val COMMAND_SET_AUTO_TURN_ON_OFF = "setAutoTurnOnTurnOff"

    const val REFRESH = "refresh"

    private const val PORT_LISTEN_DEFAULT = 8000

    private var server = AsyncHttpServer()

    private var callback: ((command: String, params: JSONObject, (result: ResultJSON) -> Unit) -> Unit)? = null

    fun registerCommandListener(callback: (command: String, params: JSONObject, (result: ResultJSON) -> Unit) -> Unit) {
        this.callback = callback
    }

    /**
     * 开启本地服务
     */
    fun init() { //如果有其他的请求方式，例如下面一行代码的写法
        server.addAction("OPTIONS", "[\\d\\D]*", this)
        server.get("[\\d\\D]*", this)
        server.post("[\\d\\D]*", this)
        server.listen(PORT_LISTEN_DEFAULT)
    }

    //http://10.5.7.225:8000/playVideo?params={path:%221.mp4%22}
    override fun onRequest(request: AsyncHttpServerRequest, response: AsyncHttpServerResponse) {
        val uri = request.path.replace("/", "")
        var params = JSONObject()
        if (request.method == "GET") {
            params = request.query.getString("params")?.let { JSONObject(it) } ?: JSONObject()
        } else if (request.method == "POST") {
            if (request.body is JSONObjectBody)
                params = (request.body as JSONObjectBody).get()
        }
        when (uri) {
            COMMAND_PLAY_BANNER -> {
                callback?.invoke(COMMAND_PLAY_BANNER, params) {
                    response.send(it)
                }
            }
            COMMAND_PLAY_VIDEO -> {
                callback?.invoke(COMMAND_PLAY_VIDEO, params) {
                    response.send(it)
                }
            }
            COMMAND_TACK_PICTURE -> {
                callback?.invoke(COMMAND_TACK_PICTURE, params) {
                    response.send(it)
                }
            }
            COMMAND_SHOW_MAP -> {
                callback?.invoke(COMMAND_SHOW_MAP, params) {
                    response.send(it)
                }
            }
            COMMAND_GET_PICS -> {
                PlayManager.getPics(false) {
                    response.send(ResultJSON().put("data", JSONArray(it.toJson())))
                }
            }
            COMMAND_GET_VIDEOS -> {
                PlayManager.getVideos(false) {
                    response.send(ResultJSON().put("data", JSONArray(it.toJson())))
                }
            }
            COMMAND_DELETE -> {
                if (params.has("path")) {
                    val path = PlayManager.getPath(params.optString("path"))
                    if (path == "") {
                        response.send(ResultJSON(ResultJSON.TYPE_NOT_SUPPORT, "file type not support"))
                    } else {
                        File(path).delete()
                        callback?.invoke(REFRESH, params) {

                        }
                        response.send(ResultJSON())
                    }
                } else {
                    response.send(ResultJSON(ResultJSON.PARAMS_ERROR, "params error"))
                }
            }
            COMMAND_REBOOT -> {
               response.send(TurnOnOffManager.reboot())
            }
            COMMAND_SET_AUTO_TURN_ON_OFF -> {
                val turnOn = params.optString("turnOn")
                val turnOff = params.optString("turnOff")
                response.send(TurnOnOffManager.setOnOff(turnOn, turnOff))
            }
            COMMAND_UPLOAD -> {
                if (request.body !is MultipartFormDataBody) {
                    response.send(ResultJSON(ResultJSON.PARAMS_ERROR, "params error"))
                    return
                }
                val body = request.body as MultipartFormDataBody
                val fileUploadHolder = FileUploadHolder()
                var savePath = ""
                var name = ""
                body.multipartCallback = MultipartFormDataBody.MultipartCallback { part ->
                    if (savePath.isBlank()) {
                        name = part.filename.toLowerCase()
                        savePath = PlayManager.getPath(name)

                        if (savePath == "") {
                            response.send(ResultJSON(ResultJSON.TYPE_NOT_SUPPORT, "file type not support"))
                            return@MultipartCallback
                        }
                    }

                    if (part.isFile) {
                        body.dataCallback = DataCallback { _, bb ->
                            if (fileUploadHolder.fileName?.isBlank() != false) {
                                fileUploadHolder.fileName = name
                                fileUploadHolder.recievedFile = File(savePath)
                                if (fileUploadHolder.recievedFile?.exists() == true)
                                    fileUploadHolder.recievedFile?.delete()
                                fileUploadHolder.recievedFile?.createNewFile()

                                var fs: BufferedOutputStream? = null
                                try {
                                    fs = BufferedOutputStream(FileOutputStream(fileUploadHolder.recievedFile))
                                } catch (e: FileNotFoundException) {
                                    e.printStackTrace()
                                }

                                fileUploadHolder.fileOutPutStream = fs
                            }
                            if (fileUploadHolder.fileOutPutStream != null) {//已经开始传输文件
                                try {
                                    fileUploadHolder.fileOutPutStream?.write(bb.allByteArray)
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }

                                bb.recycle()
                            }
                        }
                    }
                }
                request.endCallback = CompletedCallback {
                    if (File(savePath).exists()) {
                        response.send(ResultJSON())
                        callback?.invoke(REFRESH, params) {

                        }
                    } else
                        response.send(ResultJSON(10000, "not exist"))
                    fileUploadHolder.fileOutPutStream?.close()
                }
            }
            else -> {
                response.send(ResultJSON(ResultJSON.NO_SUCH_COMMAND, "no such command"))
            }
        }
    }

    internal class FileUploadHolder {
        var fileName: String? = null
        var recievedFile: File? = null
        var fileOutPutStream: BufferedOutputStream? = null

        fun reset() {
            fileName = null
            fileOutPutStream = null
        }
    }
}
