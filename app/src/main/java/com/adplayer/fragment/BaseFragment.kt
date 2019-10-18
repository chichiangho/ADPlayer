package com.adplayer.fragment

import android.support.v4.app.Fragment

abstract class BaseFragment : Fragment() {
    abstract fun isPlaying(): Boolean
    abstract fun getCurPath(): String
}
