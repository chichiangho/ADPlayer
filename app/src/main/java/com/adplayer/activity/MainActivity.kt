package com.adplayer.activity

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import com.adplayer.R
import com.adplayer.bean.ResultJSON
import com.adplayer.bean.ResultJSON.Companion.NO_SUCH_FILE
import com.adplayer.bean.ResultJSON.Companion.PARAMS_ERROR
import com.adplayer.utils.ConnectManager
import com.adplayer.utils.PlayManager
import com.bumptech.glide.Glide
import com.chichiangho.common.base.BaseActivity
import com.yanzhenjie.permission.AndPermission
import com.youth.banner.Transformer
import com.youth.banner.loader.ImageLoaderInterface
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : BaseActivity() {
    private val bannerList = ArrayList<String>()
    private val videoList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                            result(playBanner(params.optString("path")))
                        else
                            result(ResultJSON(PARAMS_ERROR, "params error"))
                    }
                    ConnectManager.COMMAND_PLAY_VIDEO -> {
                        if (params.has("path"))
                            result(playVideo(params.optString("path")))
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
            bannerList.clear()
            bannerList.addAll(it.sortedArray())

            banner.setImages(bannerList)
                    .setDelayTime(5000)
                    .setImageLoader(object : ImageLoaderInterface<ImageView> {
                        override fun displayImage(context: Context?, path: Any?, imageView: ImageView?) {
                            imageView?.scaleType = ImageView.ScaleType.FIT_CENTER
                            Glide.with(this@MainActivity).load(path).into(imageView!!)
                        }

                        override fun createImageView(context: Context?): ImageView {
                            return ImageView(context)
                        }
                    })
                    .setBannerAnimation(Transformer.Default)
                    .isAutoPlay(true)
            playBanner("")
        }
        PlayManager.getVideos {
            videoList.clear()
            videoList.addAll(it)
        }
    }

    private fun takePic(result: (result: ResultJSON) -> Unit) {

    }

    private fun playVideo(name: String): ResultJSON {
        val path = PlayManager.getVideoDir() + "/" + name
        if (!videoList.contains(path)) {
            return ResultJSON(NO_SUCH_FILE, "no such file")
        }
        if (!File(path).exists())
            return ResultJSON(NO_SUCH_FILE, "no such file")

        banner.visibility = View.GONE
        video.visibility = View.VISIBLE
        val uri = Uri.parse(path)
        video.setVideoURI(uri)
//        video.setMediaController(MediaController(this@MainActivity))//显示控制栏
        video.setOnPreparedListener {
            video.start()
        }
        video.setOnCompletionListener {
            playBanner("")
        }
        return ResultJSON()
    }

    private fun playBanner(name: String): ResultJSON {
        val path = PlayManager.getPicDir() + "/" + name
        if (!name.isEmpty() && !videoList.contains(path)) {
            return ResultJSON(NO_SUCH_FILE, "no such file")
        }
        if (!File(path).exists())
            return ResultJSON(NO_SUCH_FILE, "no such file")

        video.visibility = View.GONE
        banner.visibility = View.VISIBLE
        video.pause()
        banner.start()
        return ResultJSON()
    }
}
