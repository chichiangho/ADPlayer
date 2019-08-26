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
import io.reactivex.disposables.Disposable

class PicFragment : Fragment() {
    private lateinit var pic: FrameLayout
    private lateinit var tV: TextView
    private var delayTime = 1000L
    private var showedCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pic, null, false)!!
        pic = view.findViewById(R.id.image)
        tV = view.findViewById(R.id.text)
        return view
    }

    private var disposeAble: Disposable? = null

    fun playPic(path: String, text: String = "", signature: Long, onReady: () -> Unit, onFinish: () -> Unit) {
        showedCount++
        if (showedCount > 10) {
            pic.removeAllViews()
            showedCount = 0
        }
        if (pic.childCount == 0)
            pic.addView(ImageView(context))
        Glide.with(this).load(path).apply(RequestOptions.signatureOf(ObjectKey(signature))).into(object : SimpleTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                onReady.invoke()
                (pic.getChildAt(0) as ImageView).setImageDrawable(resource)
            }
        })
        tV.text = text
        if (disposeAble?.isDisposed == false)
            disposeAble?.dispose()
        disposeAble = delayThenRunOnUiThread(delayTime) {
            onFinish.invoke()
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