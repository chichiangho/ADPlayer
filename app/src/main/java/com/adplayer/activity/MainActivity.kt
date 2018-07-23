package com.adplayer.activity

import android.Manifest
import android.os.Bundle
import com.adplayer.R
import com.adplayer.bean.ResultJSON
import com.adplayer.bean.ResultJSON.Companion.PARAMS_ERROR
import com.adplayer.fragment.CirclePLayer
import com.adplayer.utils.ConnectManager
import com.adplayer.utils.PlayManager
import com.chichiangho.common.base.BaseActivity
import com.yanzhenjie.permission.AndPermission
import java.io.File


class MainActivity : BaseActivity() {
    private val sourceList = ArrayList<String>()

    private lateinit var circlePLayer: CirclePLayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        circlePLayer = findViewById(R.id.circle_player)
        circlePLayer.setDelay(5000).init(fragmentManager)

        AndPermission.with(this)
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onGranted {
                    initDatas()
                }
                .onDenied { finish() }
                .start()

        ConnectManager.init()
        ConnectManager.registerCommandListener { command, params, result ->
            runOnUiThread {
                when (command) {
                    ConnectManager.COMMAND_PLAY_BANNER -> {
                        if (params.has("path"))
                            result(playBanner(params.optString("path"), params.optString("text")))
                        else
                            result(ResultJSON(PARAMS_ERROR, "params error"))
                    }
                    ConnectManager.COMMAND_PLAY_VIDEO -> {
                        if (params.has("path"))
                            result(playVideo(params.optString("path"), params.optString("text")))
                        else
                            result(ResultJSON(PARAMS_ERROR, "params error"))
                    }
                    ConnectManager.COMMAND_TACK_PICTURE -> {
                        takePic(result)
                    }
                    ConnectManager.COMMAND_SHOW_MAP -> {
                        showMap(result)
                    }
                    ConnectManager.REFRESH -> {
                        initDatas()
                    }
                    else -> result(ResultJSON(ResultJSON.NO_SUCH_COMMAND, "no such command"))
                }
            }
        }
    }

    private fun showMap(result: (result: ResultJSON) -> Unit) {

    }

    private fun initDatas() {
        PlayManager.getPics {
            sourceList.clear()
            sourceList.addAll(it.sortedArray())

            PlayManager.getVideos {
                sourceList.addAll(it)
                circlePLayer.setData(sourceList).start()
            }
        }
    }

    private fun takePic(result: (result: ResultJSON) -> Unit) {

    }

    private fun playVideo(name: String, text: String): ResultJSON {
        val path = PlayManager.getVideoDir() + "/" + name
        return circlePLayer.play(path, text)
    }

    private fun playBanner(name: String, text: String): ResultJSON {
        val path = PlayManager.getPicDir() + "/" + name
        return circlePLayer.play(path, text)
    }
}
