package com.chichiangho.widget.recyclerview.extension

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import android.view.ViewGroup

abstract class RecyclerViewFooterAdapter<T> : RecyclerViewWithEmptyAdapter<T>() {
    var footerView: View? = null
        set(footerView) {
            field = footerView
            notifyItemInserted(mDatas.size + 1)
        }

    override fun getItemViewType(position: Int): Int {
        if (footerView != null && position > 0 && position == itemCount - 1)
            return TYPE_FOOTER
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_FOOTER) {
            return object : RecyclerView.ViewHolder(footerView) {}
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (getItemViewType(position) == TYPE_FOOTER)
                        manager.spanCount
                    else
                        1
                }
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val lp = holder.itemView?.layoutParams
        if (lp != null && lp is StaggeredGridLayoutManager.LayoutParams
                && holder.layoutPosition == 0) {
            lp.isFullSpan = true
        }
    }

    override fun getItemCount(): Int = if (footerView == null) super.getItemCount() else super.getItemCount() + 1

    companion object {
        const val TYPE_FOOTER = 1
    }
}