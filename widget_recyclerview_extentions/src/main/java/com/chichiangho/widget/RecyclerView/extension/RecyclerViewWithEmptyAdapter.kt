package com.chichiangho.widget.recyclerview.extension

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import java.util.*

/**
 * @author chichiangho
 * @date 2018/3/14
 * @desc
 */
abstract class RecyclerViewWithEmptyAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    protected var mDatas = ArrayList<T>()
    fun setData(datas: ArrayList<T>) {
        mDatas.clear()
        appendData(datas)
    }

    fun appendData(datas: ArrayList<T>) {
        mDatas.addAll(datas)
        notifyDataSetChanged()
    }

    open fun getEmptyView(parent: View): View = LayoutInflater.from(parent.context).inflate(R.layout.layout_default_empty, null)


    override fun getItemCount(): Int = if (mDatas.size > 0) mDatas.size else 1


    override fun getItemViewType(position: Int): Int {
        return if (mDatas.size == 0)
            TYPE_EMPTY
        else
            TYPE_NORMAL
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) != TYPE_NORMAL) return
        val data = mDatas[position]
        onBind(viewHolder, position, data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_EMPTY) {
            val x = getEmptyView(parent)
            x.layoutParams = RelativeLayout.LayoutParams(parent.measuredWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            return object : RecyclerView.ViewHolder(x) {}
        }
        return onCreate(parent, viewType)
    }

    /**
     * 由于会使用数据数组进行一些计算，请务必使用[&lt;][.setData]来设置数据源，然后通过参数data来获取数据

     * @param holder
     * *
     * @param position
     * *
     * @param data
     */
    abstract fun onBind(holder: RecyclerView.ViewHolder, position: Int, data: T)

    abstract fun onCreate(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

    companion object {
        const val TYPE_EMPTY = -1
        const val TYPE_NORMAL = 0
    }
}