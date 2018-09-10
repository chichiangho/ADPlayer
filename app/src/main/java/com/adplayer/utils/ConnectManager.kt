package com.adplayer.utils

import com.adplayer.bean.ResultJSON
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
    const val COMMAND_PLAY = "play"
    const val COMMAND_TACK_PICTURE = "takePicture"
    private const val COMMAND_GET_PICS = "getPics"
    private const val COMMAND_GET_VIDEOS = "getVideos"
    private const val COMMAND_IS_ONLINE = "isOnline"
    private const val COMMAND_UPLOAD = "upload"
    private const val COMMAND_REBOOT = "reboot"
    private const val COMMAND_DELETE = "delete"
    const val COMMAND_SET_LIGHT = "setLight"
    const val COMMAND_GET_LIGHT = "getLight"
    private const val COMMAND_SET_AUTO_TURN_ON_OFF = "setAutoTurnOnTurnOff"

    const val REFRESH = "refresh"

    const val PORT_LISTEN_DEFAULT = 1234

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

    fun stop() {
        server.stop()
    }

    //http://10.5.7.225:8000/playVideo?params={path:%221.mp4%22}
    override fun onRequest(request: AsyncHttpServerRequest, response: AsyncHttpServerResponse) {
        val uri = request.path.replace("/", "")
        var params = JSONObject()
        if (request.method == "GET") {
            params = request.query.getString("params")?.let { JSONObject(it) } ?: JSONObject()
        } else if (request.method == "POST") {
            (request.body as? JSONObjectBody)?.get()?.let {
                params = it
            }
        }
        when (uri) {
            COMMAND_GET_LIGHT -> {
                callback?.invoke(COMMAND_GET_LIGHT, params) {
                    response.send(it)
                }
            }
            COMMAND_SET_LIGHT -> {
                callback?.invoke(COMMAND_GET_LIGHT, params) {
                    response.send(it)
                }
            }
            COMMAND_PLAY -> {
                callback?.invoke(COMMAND_PLAY, params) {
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
                        response.send(ResultJSON(ResultJSON.TYPE_NOT_SUPPORT))
                    } else {
                        File(path).delete()
                        callback?.invoke(REFRESH, params) {}
                        response.send(ResultJSON())
                    }
                } else {
                    response.send(ResultJSON(ResultJSON.PARAMS_ERROR))
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
            COMMAND_TACK_PICTURE -> {
                callback?.invoke(COMMAND_TACK_PICTURE, params) {
                    if (it.get("code") == ResultJSON.TAKE_PIC) {
                        try {
                            val fullPath = it.optString("msg")
                            response.setContentType("application/x-png")
                            val bInputStream = BufferedInputStream(File(fullPath).inputStream())
                            response.sendStream(bInputStream, bInputStream.available().toLong())
                        } catch (e: IOException) {
                            e.printStackTrace()
                            response.send(ResultJSON(3000, e.message
                                    ?: "image file download failed"))
                        }
                    } else {
                        response.send(it)
                    }
                }
            }
            COMMAND_UPLOAD -> {
                if (request.body !is MultipartFormDataBody) {
                    response.send(ResultJSON(ResultJSON.PARAMS_ERROR))
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

                        if (savePath.isBlank()) {
                            response.send(ResultJSON(ResultJSON.TYPE_NOT_SUPPORT))
                            return@MultipartCallback
                        }
                        if (savePath == PlayManager.getMapPath()) {//地图删除旧的
                            File(PlayManager.getMapPath()).delete()
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
                        response.send(ResultJSON(ResultJSON.UPLOAD_FAILED))
                    fileUploadHolder.fileOutPutStream?.close()
                }
            }
            else -> {
                response.send(ResultJSON(ResultJSON.NO_SUCH_COMMAND))
            }
        }
    }

    internal class FileUploadHolder {
        var fileName: String? = null
        var recievedFile: File? = null
        var fileOutPutStream: BufferedOutputStream? = null
    }
}
