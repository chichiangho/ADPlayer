package com.adplayer.fragment

import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.adplayer.bean.ResultJSON
import com.adplayer.utils.PlayManager
import java.io.File

class CirclePLayer : FrameLayout {
    lateinit var fragmentManager: FragmentManager
    var sourceList = ArrayList<String>()
    var delayTime = 3000L
    val pic1 = PicFragment()
    val pic2 = PicFragment()
    val video = VideoFragment()
    lateinit var cur: Fragment

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    fun init(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
        fragmentManager.beginTransaction()
                .add(id, pic1).add(id, pic2).add(id, video)
                .hide(pic1).hide(pic2).hide(video).commit()
    }

    fun setData(data: ArrayList<String>): CirclePLayer {
        sourceList.clear()
        sourceList.addAll(data)
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (sourceList.size == 0)
            return
        val path = sourceList[0]
        cur = pic1
        play(path)
    }

    fun play(path: String, text: String = ""): ResultJSON {
        if (!File(path).exists())
            return ResultJSON(ResultJSON.NO_SUCH_FILE, "no such file")
        val last = cur
        if (last is PicFragment) {
            last.dispose()
        }

        val trans = fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        when (PlayManager.getType(path)) {
            PlayManager.TYPE_PIC -> {
                cur = if (cur != pic1) pic1 else pic2
                trans.show(cur).hide(last).commitAllowingStateLoss()
                (cur as PicFragment).setDelayTime(delayTime).playPic(path, text) {
                    playNext(path)
                }
                if (last is VideoFragment) {
                    last.stop()
                }
                return ResultJSON()
            }
            PlayManager.TYPE_VIDEO -> {
                if (cur != video) {
                    cur = video
                    trans.show(cur).hide(last).commitAllowingStateLoss()
                }
                (cur as VideoFragment).playVideo(path, text) {
                    playNext(path)
                }
                return ResultJSON()
            }
            else -> {
                return ResultJSON(ResultJSON.TYPE_NOT_SUPPORT, "type not support")
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
}