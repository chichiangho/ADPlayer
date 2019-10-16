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


class VideoFragment : Fragment() {
    private lateinit var video: VideoView
    private lateinit var tV: TextView
    val isPlaying
        get() = video.isPlaying

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_video, null, false)!!
        video = view.findViewById(R.id.video)
        tV = view.findViewById(R.id.text)
        return view
    }

    fun playVideo(path: String, text: String = "", onReady: () -> Unit, onFinish: () -> Unit) {
        logD("play video $path")
        try {
            tV.text = text

            video.visibility = VISIBLE
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(File(path).absolutePath)
            val bitmap = mmr.frameAtTime//获取第一帧图片
            video.background = BitmapDrawable(resources, bitmap)
            mmr.release()//释放资源

            video.setOnPreparedListener {
                try {
                    video.start()
                } catch (e: Exception) {
                    stopped { onFinish.invoke() }
                }
                delayThenRunOnUiThread(300) {
                    video.background = null
                }
            }
            video.setOnCompletionListener {
                stopped { onFinish.invoke() }
            }
            video.setOnErrorListener { _: MediaPlayer, _: Int, _: Int ->
                stopped { onFinish.invoke() }
                true
            }

            onReady.invoke()
            video.setVideoURI(Uri.fromFile(File(path)))
        } catch (e: Exception) {
            stopped { onFinish.invoke() }
        }
    }

    fun stopped(action: () -> Unit) {
        if (video.isPlaying) {
            video.pause()
            video.suspend()
        }
        video.visibility = GONE
        delayThenRunOnUiThread(300) {
            if (video.isPlaying) {
                video.pause()
                video.suspend()
            }
            video.visibility = GONE
            action.invoke()
        }
    }
}