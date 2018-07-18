package com.adplayer.utils

import com.adplayer.bean.ResultJSON
import com.chichiangho.common.extentions.appCtx
import com.chichiangho.common.extentions.toJson
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import com.koushikdutta.async.http.server.HttpServerRequestCallback
import org.json.JSONArray
import org.json.JSONObject

object ConnectManager : HttpServerRequestCallback {
    const val COMMAND_PLAY_BANNER = "playBanner"
    const val COMMAND_PLAY_VIDEO = "playVideo"
    const val COMMAND_TACK_PICTURE = "takePicture"
    const val COMMAND_SHOW_MAP = "showMap"
    const val COMMAND_UPDATE = "update"
    private const val COMMAND_GET_PICS = "getPics"
    private const val COMMAND_GET_VIDEOS = "getVideos"
    private const val COMMAND_PUSH_PIC = "pushPic"
    private const val COMMAND_PUSH_VIDEO = "pushVideo"
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

    //http://10.5.7.225:8000/getPics?params={path:%221.mp4%22}
    override fun onRequest(request: AsyncHttpServerRequest, response: AsyncHttpServerResponse) {
        val uri = request.path.replace("/", "")
        val query = request.query
        val params = JSONObject(query.getString("params"))
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
                getPics(false) {
                    response.send(ResultJSON().put("data", JSONArray(it.toJson())))
                }
            }
            COMMAND_GET_VIDEOS -> {
                getVideos(false) {
                    response.send(ResultJSON().put("data", JSONArray(it.toJson())))
                }
            }
            COMMAND_REBOOT -> {
                try {
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot ")).waitFor() //重启
                    response.send(ResultJSON())
                } catch (ex: Exception) {
                    response.send(ResultJSON(ResultJSON.REBOOT_FAILED, "reboot failed"))
                }
            }
            else -> {
                response.send(ResultJSON(ResultJSON.NO_SUCH_COMMAND, "no such command"))
            }
        }
    }

    fun getPics(withHeader: Boolean = true, callback: (array: Array<String>) -> Unit) {
        //以下为网络请求失败后的备用
        appCtx.assets.list("pic").forEach {
            CopyUtil.copyAsserts(appCtx, "pic/$it", PlayManager.getPicDir() + "/" + it)
        }

        //网络请求后一定调用
        callback(appCtx.getExternalFilesDir("picture").list().map {
            if (withHeader)
                PlayManager.getPicDir() + "/" + it
            else
                it
        }.toTypedArray())
    }

    fun getVideos(withHeader: Boolean = true, callback: (array: Array<String>) -> Unit) {
        //以下为网络请求失败后的备用
        appCtx.assets.list("video").forEach {
            CopyUtil.copyAsserts(appCtx, "video/$it", PlayManager.getVideoDir() + "/" + it)
        }
        //网络请求后一定调用
        callback(appCtx.getExternalFilesDir("video").list().map {
            if (withHeader)
                PlayManager.getVideoDir() + "/" + it
            else
                it
        }.toTypedArray())
    }
}
