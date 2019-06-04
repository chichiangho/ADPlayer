package com.adplayer.fragment

import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.widget.FrameLayout
import com.adplayer.R
import com.adplayer.bean.ResultJSON
import com.adplayer.utils.PlayManager
import com.chichiangho.common.extentions.delayThenRunOnUiThread
import com.chichiangho.common.extentions.toast
import java.io.File

class CirclePLayer : FrameLayout {
    lateinit var fragmentManager: FragmentManager
    lateinit var activity: AppCompatActivity
    var sourceList = ArrayList<String>()
    var delayTime = 5000L
    val pic1 = PicFragment()
    val pic2 = PicFragment()
    val video = VideoFragment()
    var cur: Fragment? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    fun init(activity: AppCompatActivity, fragmentManager: FragmentManager) {
        this.activity = activity
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

    fun stop() {
        if (cur is VideoFragment)
            (cur as? VideoFragment)?.stop()
        else if (cur is PicFragment)
            (cur as? PicFragment)?.dispose()
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
        if (last is PicFragment) {
            last.dispose()
        }

        val trans = fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        when (PlayManager.getType(path)) {
            PlayManager.TYPE_PIC -> {
                if (cur == null) {
                    cur = if (cur != pic1) pic1 else pic2
                    if (!activity.isDestroyed)
                        trans.show(cur).hide(last).commit()
                } else {
                    cur = if (cur != pic1) pic1 else pic2
                    delayThenRunOnUiThread(delayTime / 2) {
                        toast("图片切换")
                        if (!activity.isDestroyed)
                            trans.show(cur).hide(last).setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left).commit()
                    }
                }
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
                    delayThenRunOnUiThread(500) {
                        if (!activity.isDestroyed)
                            trans.show(cur).hide(last).commit()
                    }
                }
                (cur as VideoFragment).playVideo(path, text) {
                    playNext(path)
                }
                return ResultJSON()
            }
            else -> {
                return ResultJSON(ResultJSON.TYPE_NOT_SUPPORT)
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