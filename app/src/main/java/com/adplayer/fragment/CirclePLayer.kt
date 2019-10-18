package com.adplayer.fragment

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.widget.FrameLayout
import com.adplayer.R
import com.adplayer.bean.ResultJSON
import com.adplayer.utils.PlayManager
import com.chichiangho.common.extentions.delayThenRunOnUiThread
import com.chichiangho.common.extentions.logD
import java.io.File

class CirclePLayer : FrameLayout {
    private var signature = System.currentTimeMillis()
    lateinit var fragmentManager: FragmentManager
    lateinit var activity: AppCompatActivity
    var sourceList = ArrayList<String>()
    var delayTime = 5000L
    val pic1 = PicFragment()
    val pic2 = PicFragment()
    val video1 = VideoFragment()
    val video2 = VideoFragment()
    var cur: BaseFragment? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    fun init(activity: AppCompatActivity, fragmentManager: FragmentManager) {
        this.activity = activity
        this.fragmentManager = fragmentManager
        fragmentManager.beginTransaction()
                .add(id, pic1).add(id, pic2).add(id, video1).add(id, video2)
                .hide(pic1).hide(pic2).hide(video1).hide(video2).commitAllowingStateLoss()
    }

    fun setData(data: ArrayList<String>): CirclePLayer {
        sourceList.clear()
        sourceList.addAll(data)
        return this
    }

    fun setSignature(signature: Long): CirclePLayer {
        this.signature = signature
        return this
    }

    fun setDelay(time: Long): CirclePLayer {
        delayTime = time
        return this@CirclePLayer
    }

    fun start() {
        if (isAttachedToWindow) {
            if (sourceList.size == 0)
                return
            val path = sourceList[0]
            play(path)
        }
    }

    fun stopped(action: () -> Unit) {
        if (cur is VideoFragment)
            (cur as? VideoFragment)?.stopped(action)
        else if (cur is PicFragment)
            (cur as? PicFragment)?.stopped(action)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (sourceList.size == 0)
            return
        val path = sourceList[0]
        play(path)
    }

    fun play(path: String, text: String = ""): ResultJSON {
        if (!File(path).exists())
            return ResultJSON(ResultJSON.NO_SUCH_FILE)
        val last = cur ?: pic2
        when (PlayManager.getType(path)) {
            PlayManager.TYPE_PIC -> {
                cur = if (cur != pic1) pic1 else pic2
                (cur as PicFragment).setDelayTime(delayTime).playPic(path, text, signature,
                        onReady = {
                            if (!activity.isDestroyed)
                                fragmentManager.beginTransaction().animated().show(cur).hide(last).commitAllowingStateLoss()
                            prepareNextIfImage(path)
                        },
                        onFinish = {
                            playNext(path)
                        })
                return ResultJSON()
            }
            PlayManager.TYPE_VIDEO -> {
                cur = if (cur != video1) video1 else video2
                (cur as VideoFragment).playVideo(path, text,
                        onReady = {
                            if (!activity.isDestroyed)
                                fragmentManager.beginTransaction().animated().show(cur).hide(last).commitAllowingStateLoss()
                            prepareNextIfImage(path)
                        },
                        onFinish = {
                            playNext(path)
                        })
                return ResultJSON()
            }
            else -> {
                return ResultJSON(ResultJSON.TYPE_NOT_SUPPORT)
            }
        }
    }

    private fun prepareNextIfImage(path: String) {
        if (sourceList.size == 0)
            return

        var index = sourceList.indexOf(path)
        if (index < 0)
            index = -1
        index++
        if (index == sourceList.size)
            index = 0

        val next = sourceList[index]
        when (PlayManager.getType(next)) {
            PlayManager.TYPE_PIC -> {
                val nextF = if (cur != pic1) pic1 else pic2
                delayThenRunOnUiThread(500) {
                    //确保在切换动画后
                    nextF.prepare(next)
                }
            }
        }
    }

    private fun playNext(path: String) {
        if (sourceList.size == 0)
            return

        var index = sourceList.indexOf(path)
        if (index < 0)
            index = -1
        index++
        if (index == sourceList.size)
            index = 0
        play(sourceList[index])
    }

    fun tryRefresh() {
        if (cur?.isPlaying() != true)
            delayThenRunOnUiThread(2000) {
                if (cur?.isPlaying() != true) {
                    playNext(cur?.getCurPath() ?: "")
                    logD("reTry start play")
                }
            }
    }
}

fun FragmentTransaction.animated(): FragmentTransaction {
//    return setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
    return setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
}