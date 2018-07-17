package com.adplayer.utils

import android.util.Log
import com.chichiangho.common.extentions.appCtx
import com.koushikdutta.async.http.Multimap
import com.koushikdutta.async.http.body.AsyncHttpRequestBody
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import com.koushikdutta.async.http.server.HttpServerRequestCallback
import org.json.JSONObject
import java.net.URI

object ConnectManager : HttpServerRequestCallback {
    const val COMMAND_PLAY_BANNER = "playBanner"
    const val COMMAND_PLAY_VIDEO = "playVideo"
    const val COMMAND_TACK_PICTURE = "takePicture"
    const val COMMAND_SHOW_MAP = "showMap"
    const val COMMAND_UPDATE = "update"

    private const val PORT_LISTEN_DEFALT = 8000

    private var server = AsyncHttpServer()

    enum class Status(val requestStatus: Int, val description: String) {
        REQUEST_OK(200, "请求成功"),
        REQUEST_ERROR(500, "请求失败"),
        REQUEST_ERROR_API(501, "无效的请求接口"),
        REQUEST_ERROR_CMD(502, "无效命令"),
        REQUEST_ERROR_DEVICEID(503, "不匹配的设备ID"),
        REQUEST_ERROR_ENV(504, "不匹配的服务环境")
    }

    private var callback: ((command: String, extra: JSONObject?) -> Unit?)? = null

    fun registerCommandListener(callback: (command: String, extra: JSONObject?) -> Unit) {
        this.callback = callback
    }

    /**
     * 开启本地服务
     */
    fun init() { //如果有其他的请求方式，例如下面一行代码的写法
        server.addAction("OPTIONS", "[\\d\\D]*", this)
        server.get("[\\d\\D]*", this)
        server.post("[\\d\\D]*", this)
        server.listen(PORT_LISTEN_DEFALT)
    }

    //localhost:8000/command?params={command:"playVideo",extra:{path:"1.mp4"}}
    override fun onRequest(request: AsyncHttpServerRequest, response: AsyncHttpServerResponse) {
        val uri = request.path //这个是获取header参数的地方，一定要谨记哦
        val headers = request.headers.multiMap
        if (checkUri(uri)) {// 针对的是接口的处理
            val query = request.query
            when (uri) {
                "/command" -> {
                    val params = JSONObject(query.getString("params"))
                    val command = params.optString("command")
                    val extraObj = params.optJSONObject("extra")
                    when (command) {
                        COMMAND_PLAY_BANNER -> {
                            callback?.invoke(COMMAND_PLAY_BANNER, extraObj)
                            response.send("command $command received")
                        }
                        COMMAND_PLAY_VIDEO -> {
                            callback?.invoke(COMMAND_PLAY_VIDEO, extraObj)
                            response.send("command $command received")
                        }
                        COMMAND_TACK_PICTURE -> {
                            callback?.invoke(COMMAND_TACK_PICTURE, extraObj)
                            response.send("command $command received")
                        }
                        COMMAND_SHOW_MAP -> {
                            callback?.invoke(COMMAND_SHOW_MAP, extraObj)
                            response.send("command $command received")
                        }
                    }
                }
            }
        }
    }

    fun checkUri(uri: String): Boolean {
        if (uri.startsWith("/play"))
            return true
        return true
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
