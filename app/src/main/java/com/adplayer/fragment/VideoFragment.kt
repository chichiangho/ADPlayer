package com.adplayer.fragment

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import com.adplayer.R
import com.chichiangho.common.extentions.delayThenRunOnUiThread
import java.lang.Exception


class VideoFragment : Fragment() {
    private lateinit var video: VideoView
    private lateinit var tV: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_video, null, false)!!
        video = view.findViewById(R.id.video)
        tV = view.findViewById(R.id.text)
        return view
    }

    fun playVideo(path: String, text: String = "", callback: () -> Unit) {
        try {
            tV.text = text

            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(path)
            val bitmap = mmr.frameAtTime//获取第一帧图片
            video.background = BitmapDrawable(resources, bitmap)
            mmr.release()//释放资源

            video.setVideoPath(path)
            video.setOnPreparedListener {
//                it.setOnInfoListener { _, what, _ ->
//                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START)
                        video.setBackgroundColor(Color.TRANSPARENT)
//                    true
//                }
                video.start()
            }
            video.setOnCompletionListener {
                callback.invoke()
            }
        } catch (e: Exception) {
            throw RuntimeException(e.message);
        }

    }

    fun stop() {
        if (video.isPlaying)
            video.pause()
        delayThenRunOnUiThread(300) {
            if (video.isPlaying)
                video.pause()
        }
    }
}