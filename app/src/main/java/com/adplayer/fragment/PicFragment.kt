package com.adplayer.fragment

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.adplayer.R
import com.bumptech.glide.Glide
import com.chichiangho.common.extentions.delayThenRunOnUiThread
import io.reactivex.disposables.Disposable

class PicFragment : Fragment() {
    private lateinit var pic: ImageView
    private lateinit var tV: TextView
    private var delayTime = 1000L
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.fragment_pic, null, false)!!
        pic = view.findViewById(R.id.image)
        tV = view.findViewById(R.id.text)
        return view
    }

    private var disposeAble: Disposable? = null

    fun playPic(path: String, text: String = "", callback: () -> Unit) {
        Glide.with(this).load(path).into(pic)
        tV.text = text
        if (disposeAble?.isDisposed == false)
            disposeAble?.dispose()
        disposeAble = delayThenRunOnUiThread(delayTime) {
            callback.invoke()
        }
    }

    fun setDelayTime(delayTime: Long): PicFragment {
        this.delayTime = delayTime
        return this
    }

    fun dispose() {
        if (disposeAble?.isDisposed == false)
            disposeAble?.dispose()
        disposeAble = null
    }
}