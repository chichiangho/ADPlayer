package com.adplayer.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import com.adplayer.R
import com.adplayer.utils.ConnectManager
import com.adplayer.utils.PlayManager
import com.chichiangho.common.base.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    private val bannerList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ConnectManager.init()
        ConnectManager.registerCommandListener { command, extra ->
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

        PlayManager.getPics {
            bannerList.addAll(it.sortedArray())
            banner.setImages(bannerList)
            banner.setDelayTime(3000)
            playBanner("")
        }
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
