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
    const val COMMAND_UPLOAD = "upload"
    private const val COMMAND_REBOOT = "reboot"

    private const val PORT_LISTEN_DEFAULT = 8000

    private var server = AsyncHttpServer()

    private var callback: ((command: String, params: JSONObject?, (result: ResultJSON) -> Unit) -> Unit)? = null

    fun registerCommandListener(callback: (command: String, params: JSONObject?, (result: ResultJSON) -> Unit) -> Unit) {
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
            COMMAND_REBOOT -> {
                try {
//                    Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot")).waitFor() //重启
                    val pManager = appCtx.getSystemService(Context.POWER_SERVICE) as PowerManager
                    pManager.reboot("重启")
//                    val intent2 = Intent(Intent.ACTION_REBOOT)
//                    intent2.putExtra("nowait", 1)
//                    intent2.putExtra("interval", 1)
//                    intent2.putExtra("window", 0)
//                    appCtx.sendBroadcast(intent2)
                    response.send(ResultJSON())
                } catch (ex: Exception) {
                    response.send(ResultJSON(ResultJSON.REBOOT_FAILED, "reboot failed: " + ex.message))
                }
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
                        savePath = if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp")) {
                            PlayManager.getPicDir() + "/" + name
                        } else if (name.endsWith(".mp4") || name.endsWith(".rm") || name.endsWith(".rmvb") || name.endsWith(".flv")
                                || name.endsWith(".mpeg1") || name.endsWith(".mpeg2") || name.endsWith(".mpeg3") || name.endsWith(".mpeg4")
                                || name.endsWith(".mov") || name.endsWith(".mtv") || name.endsWith(".dat") || name.endsWith(".wmv")
                                || name.endsWith(".avi") || name.endsWith(".3gp") || name.endsWith(".amv") || name.endsWith(".dmv")) {
                            PlayManager.getVideoDir() + "/" + name
                        } else {
                            ""
                        }

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
                        callback?.invoke(COMMAND_UPLOAD, params) {

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
