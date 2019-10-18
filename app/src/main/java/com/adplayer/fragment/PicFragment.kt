package com.adplayer.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.adplayer.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.chichiangho.common.extentions.delayThenRunOnUiThread
import com.chichiangho.common.extentions.intervalUntilSuccessOnMain
import com.chichiangho.common.extentions.logD
import io.reactivex.disposables.Disposable

class PicFragment : BaseFragment() {
    private lateinit var pic: FrameLayout
    private lateinit var tV: TextView
    private var delayTime = 1000L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pic, null, false)!!
        pic = view.findViewById(R.id.image)
        tV = view.findViewById(R.id.text)
        return view
    }

    private var disposeAble: Disposable? = null

    override fun isPlaying(): Boolean {
        return disposeAble?.let {
            return !it.isDisposed
        } ?: false
    }

    override fun getCurPath(): String {
        return curPath
    }

    private var curPath = ""
    private var nextPath = ""

    fun playPic(path: String, text: String = "", signature: Long, onReady: () -> Unit, onFinish: () -> Unit) {
        logD("play pic $path")
        curPath = path

        if (curPath != nextPath) {
            val drawable = Drawable.createFromPath(path)
            if (pic.childCount == 0)
                pic.addView(ImageView(context))
            (pic.getChildAt(0) as? ImageView)?.setImageDrawable(drawable)
        }
        tV.text = text
        onReady.invoke()

//        if (pic.childCount == 0)
//            pic.addView(ImageView(context))
//        Glide.with(this).load(path).apply(RequestOptions.signatureOf(ObjectKey(signature))).into(object : SimpleTarget<Drawable>() {
//            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
//                (pic.getChildAt(0) as ImageView).setImageDrawable(resource)
//                tV.text = text
//                ready = true
//                onReady.invoke()
//            }
//        })
        if (disposeAble?.isDisposed == false)
            disposeAble?.dispose()
        disposeAble = delayThenRunOnUiThread(delayTime) {
            stopped(onFinish)
        }
    }

    fun setDelayTime(delayTime: Long): PicFragment {
        this.delayTime = delayTime
        return this
    }

    fun stopped(action: () -> Unit) {
        if (disposeAble?.isDisposed == false)
            disposeAble?.dispose()
        action.invoke()
        intervalUntilSuccessOnMain(1000) {
            if (!isVisible && nextPath == curPath) {
                nextPath = "" //确保即使被置空，正常播放时也能播放
                (pic.getChildAt(0) as? ImageView)?.setImageDrawable(null)
            }
            !isVisible
        }
    }

    fun prepare(next: String) {
        this.nextPath = next
        val drawable = Drawable.createFromPath(next)
        if (pic.childCount == 0)
            pic.addView(ImageView(context))
        (pic.getChildAt(0) as? ImageView)?.setImageDrawable(drawable)
    }
}