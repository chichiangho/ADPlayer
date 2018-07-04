package com.adplayer.activity

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import com.adplayer.R
import com.adplayer.utils.ConnectManager
import com.adplayer.utils.CopyUtil
import com.adplayer.utils.PlayManager
import com.bumptech.glide.Glide
import com.chichiangho.common.base.BaseActivity
import com.yanzhenjie.permission.AndPermission
import com.youth.banner.Transformer
import com.youth.banner.loader.ImageLoaderInterface
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {
    private val bannerList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndPermission.with(this)
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onGranted {
                    PlayManager.getPics {
                        if (it.isEmpty()) {
                            PlayManager.getPics {
                                initBanner(it)
                            }
                        } else {
                           initBanner(it)
                        }
                    }
                }
                .onDenied { finish() }
                .start()

        ConnectManager.init()
        ConnectManager.registerCommandListener { command, extra ->
            runOnUiThread {
                when (command) {
                    ConnectManager.COMMAND_PLAY_BANNER -> {
                        playBanner(extra)
                    }
                    ConnectManager.COMMAND_PLAY_VIDEO -> {
                        playVideo(extra)
                    }
                    ConnectManager.COMMAND_TACK_PICTURE -> {
                        takePic()
                    }
                    ConnectManager.COMMAND_SHOW_MAP -> {

                    }
                }
            }
        }
    }

    private fun initBanner(it: Array<String>) {
        bannerList.clear()
        bannerList.addAll(it.sortedArray())

        banner.setImages(bannerList)
        banner.setDelayTime(5000)
        banner.setImageLoader(object : ImageLoaderInterface<ImageView> {
            override fun displayImage(context: Context?, path: Any?, imageView: ImageView?) {
                imageView?.scaleType = ImageView.ScaleType.FIT_CENTER
                Glide.with(this@MainActivity).load(path).into(imageView!!)
            }

            override fun createImageView(context: Context?): ImageView {
                return ImageView(context)
            }
        })
        banner.setBannerAnimation(Transformer.Default)
        banner.isAutoPlay(true)
        playBanner("")
    }

    private fun takePic() {

    }

    private fun playVideo(name: String) {
        banner.visibility = View.GONE
        video.visibility = View.VISIBLE
        val uri = Uri.parse(PlayManager.getVideoDir() + "/" + name)
        video.setVideoURI(uri)
        video.setMediaController(MediaController(this@MainActivity))//显示控制栏
        video.setOnPreparedListener {
            video.start()
        }
    }

    private fun playBanner(name: String) {
        video.visibility = View.GONE
        banner.visibility = View.VISIBLE
        video.pause()
        banner.start()
    }
}
