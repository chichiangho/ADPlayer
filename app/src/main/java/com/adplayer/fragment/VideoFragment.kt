package com.adplayer.fragment

import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import com.adplayer.R
import com.chichiangho.common.extentions.delayThenRunOnUiThread
import com.chichiangho.common.extentions.logD
import java.io.File


class VideoFragment : BaseFragment() {
    private lateinit var video: VideoView
    private lateinit var tV: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_video, null, false)!!
        video = view.findViewById(R.id.video)
        tV = view.findViewById(R.id.text)
        return view
    }

    override fun isPlaying(): Boolean {
        return video.isPlaying
    }

    override fun getCurPath(): String {
        return curPath
    }

    private var curPath = ""
    fun playVideo(path: String, text: String = "", onReady: () -> Unit, onFinish: () -> Unit) {
        logD("play video $path")
        curPath = path
        try {
            tV.text = text

            video.visibility = VISIBLE
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(File(path).absolutePath)
            val bitmap = mmr.frameAtTime//获取第一帧图片
            video.background = BitmapDrawable(resources, bitmap)
            mmr.release()//释放资源

            video.setOnPreparedListener {
                video.setOnPreparedListener(null)
                try {
                    video.start()
                } catch (e: Exception) {
                    stopped(onFinish)
                }
                delayThenRunOnUiThread(400) {
                    if (video.visibility == VISIBLE)
                        video.background = null
                }
            }
            video.setOnCompletionListener {
                video.setOnCompletionListener(null)
                stopped(onFinish)
            }
            video.setOnErrorListener { _: MediaPlayer, _: Int, _: Int ->
                video.setOnErrorListener(null)
                stopped(onFinish)
                true
            }

            onReady.invoke()
            video.setVideoPath(path)

            delayThenRunOnUiThread(500) {
                if (isVisible && !video.isPlaying)
                    stopped(onFinish)
            }
        } catch (e: Exception) {
            stopped(onFinish)
        }
    }

    fun stopped(action: () -> Unit) {
        if (video.isPlaying) {
            video.pause()
            video.suspend()
        }
        action.invoke()
        video.visibility = GONE
    }
}