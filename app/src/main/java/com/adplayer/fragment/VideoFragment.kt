package com.adplayer.fragment

import android.app.Fragment
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import com.adplayer.R
import android.media.MediaPlayer
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever


class VideoFragment : Fragment() {
    private lateinit var video: VideoView
    private lateinit var tV: TextView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.fragment_video, null, false)!!
        video = view.findViewById(R.id.video)
        tV = view.findViewById(R.id.text)
        return view
    }

    fun playVideo(path: String, text: String = "", callback: () -> Unit) {
        tV.text = text

        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(path)
        val bitmap = mmr.frameAtTime//获取第一帧图片
        video.background = BitmapDrawable(resources, bitmap)
        mmr.release()//释放资源

        video.setVideoPath(path)
        video.setOnPreparedListener {
            it.setOnInfoListener { _, what, _ ->
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START)
                    video.setBackgroundColor(Color.TRANSPARENT)
                true
            }
            video.start()
        }
        video.setOnCompletionListener {
            callback.invoke()
        }
    }

    fun stop() {
        if (video.isPlaying)
            video.pause()
    }
}