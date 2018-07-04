package com.chichiangho.widget_date_time_picker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Typeface
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.support.v4.widget.ScrollerCompat
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import com.chichaingho.widget_date_time_picker.R

/**
 * changed from
 * github : https://github.com/Carbs0126/OptimizedNumberPicker
 */
class OptimizedNumberPicker : View {

    private var mTextColorNormal = DEFAULT_TEXT_COLOR_NORMAL
    private var mTextColorSelected = DEFAULT_TEXT_COLOR_SELECTED
    private var mTextColorHint = DEFAULT_TEXT_COLOR_SELECTED
    private var mTextSizeNormal = 0
    private var mTextSizeSelected = 0
    private var mTextSizeHint = 0
    private var mWidthOfHintText = 0
    private var mWidthOfAlterHint = 0
    private var mMarginStartOfHint = 0
    private var mMarginEndOfHint = 0
    private var mItemPaddingVertical = 0
    private var mItemPaddingHorizontal = 0
    private var mDividerColor = DEFAULT_DIVIDER_COLOR
    private var mDividerHeight = DEFAULT_DIVIDER_HEIGHT
    private var mDividerMarginL = DEFAULT_DIVIDER_MARGIN_HORIZONTAL
    private var mDividerMarginR = DEFAULT_DIVIDER_MARGIN_HORIZONTAL
    private var mShowCount = DEFAULT_SHOW_COUNT
    private var mDividerIndex0 = 0
    private var mDividerIndex1 = 0
    private var mMinShowIndex = 0
    private var mMaxShowIndex = 0
    //compat for android.widget.NumberPicker
    var minValue = 0
        set(minValue) {
            field = minValue
            mMinShowIndex = 0
        }
    //compat for android.widget.NumberPicker
    //compatible for android.widget.NumberPicker
    var maxValue = 0
        set(maxValue) {
            if (maxValue - minValue + 1 > mDisplayedValues.size) {
                throw IllegalArgumentException("(maxValue - mMinValue + 1) should not be greater than mDisplayedValues.length now " +
                        " (maxValue - mMinValue + 1) is " + (maxValue - minValue + 1) + " and mDisplayedValues.length is " + mDisplayedValues.size)
            }
            field = maxValue
            mMaxShowIndex = this.maxValue - minValue + mMinShowIndex
            setMinAndMaxShowIndex(mMinShowIndex, mMaxShowIndex)
            updateNotWrapYLimit()
        }
    private var mMaxWidthOfDisplayedValues = 0
    private var mMaxHeightOfDisplayedValues = 0
    private var mMaxWidthOfAlterArrayWithMeasureHint = 0
    private var mMaxWidthOfAlterArrayWithoutMeasureHint = 0
    private var mPrevPickedIndex = 0
    private var mMiniVelocityFling = 150
    private var mScaledTouchSlop = 8
    private var mHintText: String? = null
    private var mTextEllipsize: String? = null
    private var mEmptyItemHint: String? = null
    private var mAlterHint: String = ""
    //friction used by scroller when fling
    private var mFriction = 1f
    private var mTextSizeNormalCenterYOffset = 0f
    private var mTextSizeSelectedCenterYOffset = 0f
    private var mTextSizeHintCenterYOffset = 0f
    //true to show the two dividers
    private var mShowDivider = DEFAULT_SHOW_DIVIDER
    //true to wrap the displayed values
    private var mWrapSelectorWheel = DEFAULT_WRAP_SELECTOR_WHEEL
    //true to set to the current position, false set position to 0
    private var mCurrentItemIndexEffect = DEFAULT_CURRENT_ITEM_INDEX_EFFECT
    //true if OptimizedNumberPicker has initialized
    private var mHasInit = false
    // if displayed values' number is less than show count, then this value will be false.
    private var mWrapSelectorWheelCheck = true
    // if you want you set to linear mode from wrap mode when scrolling, then this value will be true.
    private var mPendingWrapToLinear = false

    // if this view is used in same dialog or PopupWindow more than once, and there are several
    // OptimizedNumberPickers linked, such as Gregorian Calendar with MonthPicker and DayPicker linked,
    // set mRespondChangeWhenDetach true to respond onValueChanged callbacks if this view is scrolling
    // when detach from window, but this solution is unlovely and may cause NullPointerException
    // (even i haven't found this NullPointerException),
    // so I highly recommend that every time setting up a reusable dialog with a OptimizedNumberPicker in it,
    // please initialize OptimizedNumberPicker's data, and in this way, you can set mRespondChangeWhenDetach false.
    private var mRespondChangeOnDetach = DEFAULT_RESPOND_CHANGE_ON_DETACH

    // this is to set which thread to respond onChange... listeners including
    // OnValueChangeListener, OnValueChangeListenerRelativeToRaw and OnScrollListener when view is
    // scrolling or starts to scroll or stops scrolling.
    private var mRespondChangeInMainThread = DEFAULT_RESPOND_CHANGE_IN_MAIN_THREAD

    private var mScroller: ScrollerCompat? = null
    private var mVelocityTracker: VelocityTracker? = null

    private val mPaintDivider = Paint()
    private val mPaintText = TextPaint()
    private val mPaintHint = Paint()

    private lateinit var mDisplayedValues: Array<String>
    private var mAlterTextArrayWithMeasureHint: Array<CharSequence>? = null
    private var mAlterTextArrayWithoutMeasureHint: Array<CharSequence>? = null

    private lateinit var mHandlerThread: HandlerThread
    private lateinit var mHandlerInNewThread: Handler
    private lateinit var mHandlerInMainThread: Handler

    // compatible for NumberPicker
    interface OnValueChangeListener {
        fun onValueChange(picker: OptimizedNumberPicker, oldVal: Int, newVal: Int)
    }

    interface OnValueChangeListenerRelativeToRaw {
        fun onValueChangeRelativeToRaw(picker: OptimizedNumberPicker, oldPickedIndex: Int, newPickedIndex: Int,
                                       displayedValues: Array<String>)
    }

    interface OnValueChangeListenerInScrolling {
        fun onValueChangeInScrolling(picker: OptimizedNumberPicker, oldVal: Int, newVal: Int)
    }

    // compatible for NumberPicker
    interface OnScrollListener {

        fun onScrollStateChange(view: OptimizedNumberPicker, scrollState: Int)

        companion object {
            val SCROLL_STATE_IDLE = 0
            val SCROLL_STATE_TOUCH_SCROLL = 1
            val SCROLL_STATE_FLING = 2
        }
    }

    private var mOnValueChangeListenerRaw: OnValueChangeListenerRelativeToRaw? = null
    private var mOnValueChangeListener: OnValueChangeListener? = null //compatible for NumberPicker
    private var mOnScrollListener: OnScrollListener? = null//compatible for NumberPicker
    private var mOnValueChangeListenerInScrolling: OnValueChangeListenerInScrolling? = null//response onValueChanged in scrolling

    // The current scroll state of the OptimizedNumberPicker.
    private var mScrollState = OnScrollListener.SCROLL_STATE_IDLE

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttr(context, attrs)
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttr(context, attrs)
        init(context)
    }

    private fun initAttr(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val a = context.obtainStyledAttributes(attrs, R.styleable.OptimizedNumberPicker)
        val n = a.indexCount
        for (i in 0..n - 1) {
            val attr = a.getIndex(i)
            if (attr == R.styleable.OptimizedNumberPicker_ShowCount) {
                mShowCount = a.getInt(attr, DEFAULT_SHOW_COUNT)
            } else if (attr == R.styleable.OptimizedNumberPicker_DividerColor) {
                mDividerColor = a.getColor(attr, DEFAULT_DIVIDER_COLOR)
            } else if (attr == R.styleable.OptimizedNumberPicker_DividerHeight) {
                mDividerHeight = a.getDimensionPixelSize(attr, DEFAULT_DIVIDER_HEIGHT)
            } else if (attr == R.styleable.OptimizedNumberPicker_DividerMarginLeft) {
                mDividerMarginL = a.getDimensionPixelSize(attr, DEFAULT_DIVIDER_MARGIN_HORIZONTAL)
            } else if (attr == R.styleable.OptimizedNumberPicker_DividerMarginRight) {
                mDividerMarginR = a.getDimensionPixelSize(attr, DEFAULT_DIVIDER_MARGIN_HORIZONTAL)
            } else if (attr == R.styleable.OptimizedNumberPicker_TextArray) {
                mDisplayedValues = convertCharSequenceArrayToStringArray(a.getTextArray(attr)) ?: Array(1, { "" })
            } else if (attr == R.styleable.OptimizedNumberPicker_TextColorNormal) {
                mTextColorNormal = a.getColor(attr, DEFAULT_TEXT_COLOR_NORMAL)
            } else if (attr == R.styleable.OptimizedNumberPicker_TextColorSelected) {
                mTextColorSelected = a.getColor(attr, DEFAULT_TEXT_COLOR_SELECTED)
            } else if (attr == R.styleable.OptimizedNumberPicker_TextColorHint) {
                mTextColorHint = a.getColor(attr, DEFAULT_TEXT_COLOR_SELECTED)
            } else if (attr == R.styleable.OptimizedNumberPicker_TextSizeNormal) {
                mTextSizeNormal = a.getDimensionPixelSize(attr, sp2px(context, DEFAULT_TEXT_SIZE_NORMAL_SP.toFloat()))
            } else if (attr == R.styleable.OptimizedNumberPicker_TextSizeSelected) {
                mTextSizeSelected = a.getDimensionPixelSize(attr, sp2px(context, DEFAULT_TEXT_SIZE_SELECTED_SP.toFloat()))
            } else if (attr == R.styleable.OptimizedNumberPicker_TextSizeHint) {
                mTextSizeHint = a.getDimensionPixelSize(attr, sp2px(context, DEFAULT_TEXT_SIZE_HINT_SP.toFloat()))
            } else if (attr == R.styleable.OptimizedNumberPicker_MinValue) {
                mMinShowIndex = a.getInteger(attr, 0)
            } else if (attr == R.styleable.OptimizedNumberPicker_MaxValue) {
                mMaxShowIndex = a.getInteger(attr, 0)
            } else if (attr == R.styleable.OptimizedNumberPicker_WrapSelectorWheel) {
                mWrapSelectorWheel = a.getBoolean(attr, DEFAULT_WRAP_SELECTOR_WHEEL)
            } else if (attr == R.styleable.OptimizedNumberPicker_ShowDivider) {
                mShowDivider = a.getBoolean(attr, DEFAULT_SHOW_DIVIDER)
            } else if (attr == R.styleable.OptimizedNumberPicker_HintText) {
                mHintText = a.getString(attr)
            } else if (attr == R.styleable.OptimizedNumberPicker_AlternativeHint) {
                mAlterHint = a.getString(attr)
            } else if (attr == R.styleable.OptimizedNumberPicker_EmptyItemHint) {
                mEmptyItemHint = a.getString(attr)
            } else if (attr == R.styleable.OptimizedNumberPicker_MarginStartOfHint) {
                mMarginStartOfHint = a.getDimensionPixelSize(attr, dp2px(context, DEFAULT_MARGIN_START_OF_HINT_DP.toFloat()))
            } else if (attr == R.styleable.OptimizedNumberPicker_MarginEndOfHint) {
                mMarginEndOfHint = a.getDimensionPixelSize(attr, dp2px(context, DEFAULT_MARGIN_END_OF_HINT_DP.toFloat()))
            } else if (attr == R.styleable.OptimizedNumberPicker_ItemPaddingVertical) {
                mItemPaddingVertical = a.getDimensionPixelSize(attr, dp2px(context, DEFAULT_ITEM_PADDING_DP_V.toFloat()))
            } else if (attr == R.styleable.OptimizedNumberPicker_ItemPaddingHorizontal) {
                mItemPaddingHorizontal = a.getDimensionPixelSize(attr, dp2px(context, DEFAULT_ITEM_PADDING_DP_H.toFloat()))
            } else if (attr == R.styleable.OptimizedNumberPicker_AlternativeTextArrayWithMeasureHint) {
                mAlterTextArrayWithMeasureHint = a.getTextArray(attr)
            } else if (attr == R.styleable.OptimizedNumberPicker_AlternativeTextArrayWithoutMeasureHint) {
                mAlterTextArrayWithoutMeasureHint = a.getTextArray(attr)
            } else if (attr == R.styleable.OptimizedNumberPicker_RespondChangeOnDetached) {
                mRespondChangeOnDetach = a.getBoolean(attr, DEFAULT_RESPOND_CHANGE_ON_DETACH)
            } else if (attr == R.styleable.OptimizedNumberPicker_RespondChangeInMainThread) {
                mRespondChangeInMainThread = a.getBoolean(attr, DEFAULT_RESPOND_CHANGE_IN_MAIN_THREAD)
            } else if (attr == R.styleable.OptimizedNumberPicker_TextEllipsize) {
                mTextEllipsize = a.getString(attr)
            }
        }
        a.recycle()
    }

    private fun init(context: Context) {
        mScroller = ScrollerCompat.create(context)
        mMiniVelocityFling = ViewConfiguration.get(getContext()).scaledMinimumFlingVelocity
        mScaledTouchSlop = ViewConfiguration.get(getContext()).scaledTouchSlop
        if (mTextSizeNormal == 0) mTextSizeNormal = sp2px(context, DEFAULT_TEXT_SIZE_NORMAL_SP.toFloat())
        if (mTextSizeSelected == 0)
            mTextSizeSelected = sp2px(context, DEFAULT_TEXT_SIZE_SELECTED_SP.toFloat())
        if (mTextSizeHint == 0) mTextSizeHint = sp2px(context, DEFAULT_TEXT_SIZE_HINT_SP.toFloat())
        if (mMarginStartOfHint == 0)
            mMarginStartOfHint = dp2px(context, DEFAULT_MARGIN_START_OF_HINT_DP.toFloat())
        if (mMarginEndOfHint == 0) mMarginEndOfHint = dp2px(context, DEFAULT_MARGIN_END_OF_HINT_DP.toFloat())

        mPaintDivider.color = mDividerColor
        mPaintDivider.isAntiAlias = true
        mPaintDivider.style = Paint.Style.STROKE
        mPaintDivider.strokeWidth = mDividerHeight.toFloat()

        mPaintText.color = mTextColorNormal
        mPaintText.isAntiAlias = true
        mPaintText.textAlign = Align.CENTER

        mPaintHint.color = mTextColorHint
        mPaintHint.isAntiAlias = true
        mPaintHint.textAlign = Align.CENTER
        mPaintHint.textSize = mTextSizeHint.toFloat()

        if (mShowCount % 2 == 0) {
            mShowCount++
        }
        if (mMinShowIndex == -1 || mMaxShowIndex == -1) {
            updateValueForInit()
        }
        initHandler()
    }

    private fun initHandler() {
        mHandlerThread = HandlerThread("HandlerThread-For-Refreshing")
        mHandlerThread.start()

        mHandlerInNewThread = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    HANDLER_WHAT_REFRESH -> if (!mScroller!!.isFinished) {
                        if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                        }
                        mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH, 0, 0, msg.obj), HANDLER_INTERVAL_REFRESH.toLong())
                    } else {
                        var duration = 0
                        val willPickIndex: Int
                        //if scroller finished(not scrolling), then adjust the position
                        if (mCurrDrawFirstItemY != 0) {//need to adjust
                            if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                                onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                            }
                            if (mCurrDrawFirstItemY < -mItemHeight / 2) {
                                //adjust to scroll upward
                                duration = (DEFAULT_INTERVAL_REVISE_DURATION.toFloat() * (mItemHeight + mCurrDrawFirstItemY) / mItemHeight).toInt()
                                mScroller!!.startScroll(0, mCurrDrawGlobalY, 0, mItemHeight + mCurrDrawFirstItemY, duration * 3)
                                willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY + mItemHeight + mCurrDrawFirstItemY)
                            } else {
                                //adjust to scroll downward
                                duration = (DEFAULT_INTERVAL_REVISE_DURATION.toFloat() * -mCurrDrawFirstItemY / mItemHeight).toInt()
                                mScroller!!.startScroll(0, mCurrDrawGlobalY, 0, mCurrDrawFirstItemY, duration * 3)
                                willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY + mCurrDrawFirstItemY)
                            }
                            postInvalidate()
                        } else {
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                            //get the index which will be selected
                            willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY)
                        }
                        val changeMsg = getMsg(HANDLER_WHAT_LISTENER_VALUE_CHANGED, mPrevPickedIndex, willPickIndex, msg.obj)
                        if (mRespondChangeInMainThread) {
                            mHandlerInMainThread.sendMessageDelayed(changeMsg, (duration * 2).toLong())
                        } else {
                            mHandlerInNewThread.sendMessageDelayed(changeMsg, (duration * 2).toLong())
                        }
                    }
                    HANDLER_WHAT_LISTENER_VALUE_CHANGED -> respondPickedValueChanged(msg.arg1, msg.arg2, msg.obj)
                }
            }
        }
        mHandlerInMainThread = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    HANDLER_WHAT_REQUEST_LAYOUT -> requestLayout()
                    HANDLER_WHAT_LISTENER_VALUE_CHANGED -> respondPickedValueChanged(msg.arg1, msg.arg2, msg.obj)
                }
            }
        }
    }

    private var mInScrollingPickedOldValue: Int = 0
    private var mInScrollingPickedNewValue: Int = 0

    private fun respondPickedValueChangedInScrolling(oldVal: Int, newVal: Int) {
        mOnValueChangeListenerInScrolling!!.onValueChangeInScrolling(this, oldVal, newVal)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        updateMaxWHOfDisplayedValues(false)
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w
        mViewHeight = h
        mItemHeight = mViewHeight / mShowCount
        mViewCenterX = (mViewWidth + paddingLeft - paddingRight).toFloat() / 2
        var defaultValue = 0
        if (oneRecycleSize > 1) {
            if (mHasInit) {
                defaultValue = value - minValue
            } else if (mCurrentItemIndexEffect) {
                defaultValue = mCurrDrawFirstItemIndex + (mShowCount - 1) / 2
            } else {
                defaultValue = 0
            }
        }
        correctPositionByDefaultValue(defaultValue, mWrapSelectorWheel && mWrapSelectorWheelCheck)
        updateFontAttr()
        updateNotWrapYLimit()
        updateDividerAttr()
        mHasInit = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!mHandlerThread.isAlive) {
            initHandler()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandlerThread.quit()
        //These codes are for dialog or PopupWindow which will be used for more than once.
        //Not an elegant solution, if you have any good idea, please let me know, thank you.
        if (mItemHeight == 0) return
        if (!mScroller!!.isFinished) {
            mScroller!!.abortAnimation()
            mCurrDrawGlobalY = mScroller!!.currY
            calculateFirstItemParameterByGlobalY()
            if (mCurrDrawFirstItemY != 0) {
                if (mCurrDrawFirstItemY < -mItemHeight / 2) {
                    mCurrDrawGlobalY += mItemHeight + mCurrDrawFirstItemY
                } else {
                    mCurrDrawGlobalY += mCurrDrawFirstItemY
                }
                calculateFirstItemParameterByGlobalY()
            }
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
        }
        // see the comments on mRespondChangeOnDetach, if mRespondChangeOnDetach is false,
        // please initialize OptimizedNumberPicker's data every time setting up OptimizedNumberPicker,
        // set the demo of GregorianLunarCalendar
        val currPickedIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY)
        if (currPickedIndex != mPrevPickedIndex && mRespondChangeOnDetach) {
            try {
                if (mOnValueChangeListener != null) {
                    mOnValueChangeListener!!.onValueChange(this@OptimizedNumberPicker, mPrevPickedIndex + minValue, currPickedIndex + minValue)
                }
                if (mOnValueChangeListenerRaw != null) {
                    mOnValueChangeListenerRaw!!.onValueChangeRelativeToRaw(this@OptimizedNumberPicker, mPrevPickedIndex, currPickedIndex, mDisplayedValues)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        mPrevPickedIndex = currPickedIndex
    }

    val oneRecycleSize: Int
        get() = mMaxShowIndex - mMinShowIndex + 1

    val rawContentSize: Int
        get() {
            return mDisplayedValues.size
        }

    fun setDisplayedValuesAndPickedIndex(newDisplayedValues: Array<String>?, pickedIndex: Int, needRefresh: Boolean) {
        stopScrolling()
        if (newDisplayedValues == null) {
            throw IllegalArgumentException("newDisplayedValues should not be null.")
        }
        if (pickedIndex < 0) {
            throw IllegalArgumentException("pickedIndex should not be negative, now pickedIndex is " + pickedIndex)
        }
        updateContent(newDisplayedValues)
        updateMaxWHOfDisplayedValues(true)
        updateNotWrapYLimit()
        updateValue()
        mPrevPickedIndex = pickedIndex + mMinShowIndex
        correctPositionByDefaultValue(pickedIndex, mWrapSelectorWheel && mWrapSelectorWheelCheck)
        if (needRefresh) {
            mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH), 0)
            postInvalidate()
        }
    }

    fun setDisplayedValues(newDisplayedValues: Array<String>, needRefresh: Boolean) {
        setDisplayedValuesAndPickedIndex(newDisplayedValues, 0, needRefresh)
    }

    /**
     * Gets the values to be displayed instead of string values.

     * @return The displayed values.
     */
    var displayedValues: Array<String>?
        get() = mDisplayedValues
        set(newDisplayedValues) {
            stopRefreshing()
            stopScrolling()
            if (newDisplayedValues == null) {
                throw IllegalArgumentException("newDisplayedValues should not be null.")
            }

            if (maxValue - minValue + 1 > newDisplayedValues.size) {
                throw IllegalArgumentException("mMaxValue - mMinValue + 1 should not be greater than mDisplayedValues.length, now "
                        + "((mMaxValue - mMinValue + 1) is " + (maxValue - minValue + 1)
                        + " newDisplayedValues.length is " + newDisplayedValues.size
                        + ", you need to set MaxValue and MinValue before setDisplayedValues(String[])")
            }
            updateContent(newDisplayedValues)
            updateMaxWHOfDisplayedValues(true)
            mPrevPickedIndex = 0 + mMinShowIndex
            correctPositionByDefaultValue(0, mWrapSelectorWheel && mWrapSelectorWheelCheck)
            postInvalidate()
            mHandlerInMainThread.sendEmptyMessage(HANDLER_WHAT_REQUEST_LAYOUT)
        }

    /**
     * get the "fromValue" by using getValue(), if your picker's minValue is not 0,
     * make sure you can get the accurate value by getValue(), or you can use
     * smoothScrollToValue(int fromValue, int toValue, boolean needRespond)

     * @param toValue the value you want picker to scroll to
     */
    fun smoothScrollToValue(toValue: Int) {
        smoothScrollToValue(value, toValue, true)
    }

    /**
     * get the "fromValue" by using getValue(), if your picker's minValue is not 0,
     * make sure you can get the accurate value by getValue(), or you can use
     * smoothScrollToValue(int fromValue, int toValue, boolean needRespond)

     * @param toValue     the value you want picker to scroll to
     * *
     * @param needRespond set if you want picker to respond onValueChange listener
     */
    fun smoothScrollToValue(toValue: Int, needRespond: Boolean) {
        smoothScrollToValue(value, toValue, needRespond)
    }

    /**
     * @param fromValue   need to set the fromValue, can be greater than mMaxValue or less than mMinValue
     * *
     * @param toValue     the value you want picker to scroll to
     * *
     * @param needRespond need Respond to the ValueChange callback When Scrolling, default is false
     */
    @JvmOverloads
    fun smoothScrollToValue(fromValue: Int, toValue: Int, needRespond: Boolean = true) {
        var fromValue = fromValue
        var toValue = toValue
        var deltaIndex: Int
        fromValue = refineValueByLimit(fromValue, minValue, maxValue,
                mWrapSelectorWheel && mWrapSelectorWheelCheck)
        toValue = refineValueByLimit(toValue, minValue, maxValue,
                mWrapSelectorWheel && mWrapSelectorWheelCheck)
        if (mWrapSelectorWheel && mWrapSelectorWheelCheck) {
            deltaIndex = toValue - fromValue
            val halfOneRecycleSize = oneRecycleSize / 2
            if (deltaIndex < -halfOneRecycleSize || halfOneRecycleSize < deltaIndex) {
                deltaIndex = if (deltaIndex > 0) deltaIndex - oneRecycleSize else deltaIndex + oneRecycleSize
            }
        } else {
            deltaIndex = toValue - fromValue
        }
        value = fromValue
        if (fromValue == toValue) return
        scrollByIndexSmoothly(deltaIndex, needRespond)
    }

    /**
     * simplify the "setDisplayedValue() + setMinValue() + setMaxValue()" process,
     * default minValue is 0, and make sure you do NOT change the minValue.

     * @param display new values to be displayed
     */
    fun refreshByNewDisplayedValues(display: Array<String>) {
        val oldMaxValue = maxValue
        val oldSpan = oldMaxValue - minValue + 1

        val newMaxValue = display.size - 1
        val newSpan = newMaxValue - minValue + 1

        if (newSpan > oldSpan) {
            displayedValues = display
            maxValue = newMaxValue
        } else {
            if (newMaxValue < minValue)
                minValue = newMaxValue
            maxValue = newMaxValue
            displayedValues = display
        }
    }

    /**
     * used by handlers to respond onchange callbacks

     * @param oldVal        prevPicked value
     * *
     * @param newVal        currPicked value
     * *
     * @param respondChange if want to respond onchange callbacks
     */
    private fun respondPickedValueChanged(oldVal: Int, newVal: Int, respondChange: Any?) {
        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
        if (oldVal != newVal) {
            if (respondChange == null || respondChange !is Boolean || (respondChange as Boolean?)!!) {
                if (mOnValueChangeListener != null) {
                    mOnValueChangeListener!!.onValueChange(this@OptimizedNumberPicker, oldVal + minValue, newVal + minValue)
                }
                if (mOnValueChangeListenerRaw != null) {
                    mOnValueChangeListenerRaw!!.onValueChangeRelativeToRaw(this@OptimizedNumberPicker, oldVal, newVal, mDisplayedValues)
                }
            }
        }
        mPrevPickedIndex = newVal
        if (mPendingWrapToLinear) {
            mPendingWrapToLinear = false
            internalSetWrapToLinear()
        }
    }

    /**
     * @param deltaIndex  the delta index it will scroll by
     * *
     * @param needRespond need Respond to the ValueChange callback When Scrolling, default is false
     */
    private fun scrollByIndexSmoothly(deltaIndex: Int, needRespond: Boolean = true) {
        var deltaIndex = deltaIndex
        if (!(mWrapSelectorWheel && mWrapSelectorWheelCheck)) {
            val willPickRawIndex = pickedIndexRelativeToRaw
            if (willPickRawIndex + deltaIndex > mMaxShowIndex) {
                deltaIndex = mMaxShowIndex - willPickRawIndex
            } else if (willPickRawIndex + deltaIndex < mMinShowIndex) {
                deltaIndex = mMinShowIndex - willPickRawIndex
            }
        }
        var duration: Int
        var dy: Int
        if (mCurrDrawFirstItemY < -mItemHeight / 2) {
            //scroll upwards for a distance of less than mItemHeight
            dy = mItemHeight + mCurrDrawFirstItemY
            duration = (DEFAULT_INTERVAL_REVISE_DURATION.toFloat() * (mItemHeight + mCurrDrawFirstItemY) / mItemHeight).toInt()
            if (deltaIndex < 0) {
                duration = -duration - deltaIndex * DEFAULT_INTERVAL_REVISE_DURATION
            } else {
                duration += deltaIndex * DEFAULT_INTERVAL_REVISE_DURATION
            }
        } else {
            //scroll downwards for a distance of less than mItemHeight
            dy = mCurrDrawFirstItemY
            duration = (DEFAULT_INTERVAL_REVISE_DURATION.toFloat() * -mCurrDrawFirstItemY / mItemHeight).toInt()
            if (deltaIndex < 0) {
                duration -= deltaIndex * DEFAULT_INTERVAL_REVISE_DURATION
            } else {
                duration += deltaIndex * DEFAULT_INTERVAL_REVISE_DURATION
            }
        }
        dy += deltaIndex * mItemHeight
        if (duration < DEFAULT_MIN_SCROLL_BY_INDEX_DURATION)
            duration = DEFAULT_MIN_SCROLL_BY_INDEX_DURATION
        if (duration > DEFAULT_MAX_SCROLL_BY_INDEX_DURATION)
            duration = DEFAULT_MAX_SCROLL_BY_INDEX_DURATION
        mScroller!!.startScroll(0, mCurrDrawGlobalY, 0, dy, duration)
        if (needRespond) {
            mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH), (duration / 4).toLong())
        } else {
            mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH, 0, 0, needRespond), (duration / 4).toLong())
        }
        postInvalidate()
    }

    //compatible for android.widget.NumberPicker
    //compatible for android.widget.NumberPicker
    var value: Int
        get() = pickedIndexRelativeToRaw + minValue
        set(value) {
            if (value < minValue) {
                return
            }
            if (value > maxValue) {
                return
            }
            pickedIndexRelativeToRaw = value - minValue
        }

    val contentByCurrValue: String
        get() = mDisplayedValues[value - minValue]

    var wrapSelectorWheel: Boolean
        get() = mWrapSelectorWheel
        set(wrapSelectorWheel) {
            if (mWrapSelectorWheel != wrapSelectorWheel) {
                if (!wrapSelectorWheel) {
                    if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        internalSetWrapToLinear()
                    } else {
                        mPendingWrapToLinear = true
                    }
                } else {
                    mWrapSelectorWheel = wrapSelectorWheel
                    updateWrapStateByContent()
                    postInvalidate()
                }
            }
        }

    val wrapSelectorWheelAbsolutely: Boolean
        get() = mWrapSelectorWheel && mWrapSelectorWheelCheck

    fun setHintText(hintText: String) {
        if (isStringEqual(mHintText, hintText)) return
        mHintText = hintText
        mTextSizeHintCenterYOffset = getTextCenterYOffset(mPaintHint.fontMetrics)
        mWidthOfHintText = getTextWidth(mHintText!!, mPaintHint)
        mHandlerInMainThread.sendEmptyMessage(HANDLER_WHAT_REQUEST_LAYOUT)
    }

    fun setPickedIndexRelativeToMin(pickedIndexToMin: Int) {
        if (pickedIndexToMin in 0..(oneRecycleSize - 1)) {
            mPrevPickedIndex = pickedIndexToMin + mMinShowIndex
            correctPositionByDefaultValue(pickedIndexToMin, mWrapSelectorWheel && mWrapSelectorWheelCheck)
            postInvalidate()
        }
    }

    fun setNormalTextColor(normalTextColor: Int) {
        if (mTextColorNormal == normalTextColor) return
        mTextColorNormal = normalTextColor
        postInvalidate()
    }

    fun setSelectedTextColor(selectedTextColor: Int) {
        if (mTextColorSelected == selectedTextColor) return
        mTextColorSelected = selectedTextColor
        postInvalidate()
    }

    fun setHintTextColor(hintTextColor: Int) {
        if (mTextColorHint == hintTextColor) return
        mTextColorHint = hintTextColor
        mPaintHint.color = mTextColorHint
        postInvalidate()
    }

    fun setDividerColor(dividerColor: Int) {
        if (mDividerColor == dividerColor) return
        mDividerColor = dividerColor
        mPaintDivider.color = mDividerColor
        postInvalidate()
    }

    var pickedIndexRelativeToRaw: Int
        get() {
            val willPickIndex: Int
            if (mCurrDrawFirstItemY != 0) {
                if (mCurrDrawFirstItemY < -mItemHeight / 2) {
                    willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY + mItemHeight + mCurrDrawFirstItemY)
                } else {
                    willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY + mCurrDrawFirstItemY)
                }
            } else {
                willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY)
            }
            return willPickIndex
        }
        set(pickedIndexToRaw) {
            if (mMinShowIndex > -1) {
                if (pickedIndexToRaw in mMinShowIndex..mMaxShowIndex) {
                    mPrevPickedIndex = pickedIndexToRaw
                    correctPositionByDefaultValue(pickedIndexToRaw - mMinShowIndex, mWrapSelectorWheel && mWrapSelectorWheelCheck)
                    postInvalidate()
                }
            }
        }

    @JvmOverloads
    fun setMinAndMaxShowIndex(minShowIndex: Int, maxShowIndex: Int, needRefresh: Boolean = true) {
        if (minShowIndex > maxShowIndex) {
            throw IllegalArgumentException("minShowIndex should be less than maxShowIndex, minShowIndex is "
                    + minShowIndex + ", maxShowIndex is " + maxShowIndex + "")
        }

        if (minShowIndex < 0) {
            throw IllegalArgumentException("minShowIndex should not be less than 0, now minShowIndex is " + minShowIndex)
        } else if (minShowIndex > mDisplayedValues.size - 1) {
            throw IllegalArgumentException("minShowIndex should not be greater than (mDisplayedValues.length - 1), now " +
                    "(mDisplayedValues.length - 1) is " + (mDisplayedValues.size - 1) + " minShowIndex is " + minShowIndex)
        }

        if (maxShowIndex < 0) {
            throw IllegalArgumentException("maxShowIndex should not be less than 0, now maxShowIndex is " + maxShowIndex)
        } else if (maxShowIndex > mDisplayedValues.size - 1) {
            throw IllegalArgumentException("maxShowIndex should not be greater than (mDisplayedValues.length - 1), now " +
                    "(mDisplayedValues.length - 1) is " + (mDisplayedValues.size - 1) + " maxShowIndex is " + maxShowIndex)
        }

        mMinShowIndex = minShowIndex
        mMaxShowIndex = maxShowIndex
        if (needRefresh) {
            mPrevPickedIndex = 0 + mMinShowIndex
            correctPositionByDefaultValue(0, mWrapSelectorWheel && mWrapSelectorWheelCheck)
            postInvalidate()
        }
    }

    /**
     * set the friction of scroller, it will effect the scroller's acceleration when fling

     * @param friction default is ViewConfiguration.get(mContext).getScrollFriction()
     * *                 if setFriction(2 * ViewConfiguration.get(mContext).getScrollFriction()),
     * *                 the friction will be twice as much as before
     */
    fun setFriction(friction: Float) {
        if (friction <= 0)
            throw IllegalArgumentException("you should set a a positive float friction, now friction is " + friction)
        mFriction = ViewConfiguration.getScrollFriction() / friction
    }

    //compatible for NumberPicker
    private fun onScrollStateChange(scrollState: Int) {
        if (mScrollState == scrollState) {
            return
        }
        mScrollState = scrollState
        if (mOnScrollListener != null) {
            mOnScrollListener!!.onScrollStateChange(this, scrollState)
        }
    }

    //compatible for NumberPicker
    fun setOnScrollListener(listener: OnScrollListener) {
        mOnScrollListener = listener
    }

    //compatible for NumberPicker
    fun setOnValueChangedListener(listener: OnValueChangeListener) {
        mOnValueChangeListener = listener
    }

    fun setOnValueChangedListenerRelativeToRaw(listener: OnValueChangeListenerRelativeToRaw) {
        mOnValueChangeListenerRaw = listener
    }

    fun setOnValueChangeListenerInScrolling(listener: OnValueChangeListenerInScrolling) {
        mOnValueChangeListenerInScrolling = listener
    }

    fun setContentTextTypeface(typeface: Typeface) {
        mPaintText.typeface = typeface
    }

    fun setHintTextTypeface(typeface: Typeface) {
        mPaintHint.typeface = typeface
    }

    //return index relative to mDisplayedValues from 0.
    private fun getWillPickIndexByGlobalY(globalY: Int): Int {
        if (mItemHeight == 0) return 0
        val willPickIndex = globalY / mItemHeight + mShowCount / 2
        val index = getIndexByRawIndex(willPickIndex, oneRecycleSize, mWrapSelectorWheel && mWrapSelectorWheelCheck)
        if (index in 0..(oneRecycleSize - 1)) {
            return index + mMinShowIndex
        } else {
            throw IllegalArgumentException("getWillPickIndexByGlobalY illegal index : " + index
                    + " getOneRecycleSize() : " + oneRecycleSize + " mWrapSelectorWheel : " + mWrapSelectorWheel)
        }
    }

    private fun getIndexByRawIndex(index: Int, size: Int, wrap: Boolean): Int {
        var index = index
        if (size <= 0) return 0
        if (wrap) {
            index %= size
            if (index < 0) index += size
        }
        return index
    }

    private fun internalSetWrapToLinear() {
        val rawIndex = pickedIndexRelativeToRaw
        correctPositionByDefaultValue(rawIndex - mMinShowIndex, false)
        mWrapSelectorWheel = false
        postInvalidate()
    }

    private fun updateDividerAttr() {
        mDividerIndex0 = mShowCount / 2
        mDividerIndex1 = mDividerIndex0 + 1
        dividerY0 = (mDividerIndex0 * mViewHeight / mShowCount).toFloat()
        dividerY1 = (mDividerIndex1 * mViewHeight / mShowCount).toFloat()
        if (mDividerMarginL < 0) mDividerMarginL = 0
        if (mDividerMarginR < 0) mDividerMarginR = 0

        if (mDividerMarginL + mDividerMarginR == 0) return
        if (paddingLeft + mDividerMarginL >= mViewWidth - paddingRight - mDividerMarginR) {
            val surplusMargin = paddingLeft + mDividerMarginL + paddingRight + mDividerMarginR - mViewWidth
            mDividerMarginL = (mDividerMarginL - surplusMargin.toFloat() * mDividerMarginL / (mDividerMarginL + mDividerMarginR)).toInt()
            mDividerMarginR = (mDividerMarginR - surplusMargin.toFloat() * mDividerMarginR / (mDividerMarginL + mDividerMarginR)).toInt()
        }
    }

    private var mNotWrapLimitYTop: Int = 0
    private var mNotWrapLimitYBottom: Int = 0

    private fun updateFontAttr() {
        if (mTextSizeNormal > mItemHeight) mTextSizeNormal = mItemHeight
        if (mTextSizeSelected > mItemHeight) mTextSizeSelected = mItemHeight

        mPaintHint.textSize = mTextSizeHint.toFloat()
        mTextSizeHintCenterYOffset = getTextCenterYOffset(mPaintHint.fontMetrics)
        mWidthOfHintText = getTextWidth(mHintText!!, mPaintHint)

        mPaintText.textSize = mTextSizeSelected.toFloat()
        mTextSizeSelectedCenterYOffset = getTextCenterYOffset(mPaintText.fontMetrics)
        mPaintText.textSize = mTextSizeNormal.toFloat()
        mTextSizeNormalCenterYOffset = getTextCenterYOffset(mPaintText.fontMetrics)
    }

    private fun updateNotWrapYLimit() {
        mNotWrapLimitYTop = 0
        mNotWrapLimitYBottom = -mShowCount * mItemHeight
        mNotWrapLimitYTop = (oneRecycleSize - mShowCount / 2 - 1) * mItemHeight
        mNotWrapLimitYBottom = -(mShowCount / 2) * mItemHeight
    }

    private var downYGlobal = 0f
    private var downY = 0f
    private var currY = 0f

    private fun limitY(currDrawGlobalYPreferred: Int): Int {
        if (mWrapSelectorWheel && mWrapSelectorWheelCheck) return currDrawGlobalYPreferred
        return when {
            currDrawGlobalYPreferred < mNotWrapLimitYBottom -> mNotWrapLimitYBottom
            currDrawGlobalYPreferred > mNotWrapLimitYTop -> mNotWrapLimitYTop
            else -> currDrawGlobalYPreferred
        }
    }

    private var mFlagMayPress = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mItemHeight == 0) return true

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
        currY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mFlagMayPress = true
                mHandlerInNewThread.removeMessages(HANDLER_WHAT_REFRESH)
                stopScrolling()
                downY = currY
                downYGlobal = mCurrDrawGlobalY.toFloat()
                onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val spanY = downY - currY

                if (mFlagMayPress && -mScaledTouchSlop < spanY && spanY < mScaledTouchSlop) {

                } else {
                    mFlagMayPress = false
                    mCurrDrawGlobalY = limitY((downYGlobal + spanY).toInt())
                    calculateFirstItemParameterByGlobalY()
                    invalidate()
                }
                onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
            }
            MotionEvent.ACTION_UP -> if (mFlagMayPress) {
                click(event)
            } else {
                val velocityTracker = mVelocityTracker
                velocityTracker!!.computeCurrentVelocity(1000)
                val velocityY = (velocityTracker.yVelocity * mFriction).toInt()
                if (Math.abs(velocityY) > mMiniVelocityFling) {
                    mScroller!!.fling(0, mCurrDrawGlobalY, 0, -velocityY,
                            Integer.MIN_VALUE, Integer.MAX_VALUE, limitY(Integer.MIN_VALUE), limitY(Integer.MAX_VALUE))
                    invalidate()
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                }
                mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH), 0)
                releaseVelocityTracker()
            }
            MotionEvent.ACTION_CANCEL -> {
                downYGlobal = mCurrDrawGlobalY.toFloat()
                stopScrolling()
                mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH), 0)
            }
        }
        return true
    }

    private fun click(event: MotionEvent) {
        val y = event.y
        for (i in 0 until mShowCount) {
            if (mItemHeight * i <= y && y < mItemHeight * (i + 1)) {
                clickItem(i)
                break
            }
        }
    }

    private fun clickItem(showCountIndex: Int) {
        if (showCountIndex in 0..(mShowCount - 1)) {
            //clicked the showCountIndex of the view
            scrollByIndexSmoothly(showCountIndex - mShowCount / 2)
        } else {
            //wrong
        }
    }

    private fun getTextCenterYOffset(fontMetrics: Paint.FontMetrics?): Float {
        if (fontMetrics == null) return 0f
        return Math.abs(fontMetrics.top + fontMetrics.bottom) / 2
    }

    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0
    private var mItemHeight: Int = 0
    private var dividerY0: Float = 0.toFloat()
    private var dividerY1: Float = 0.toFloat()
    private var mViewCenterX: Float = 0.toFloat()

    //defaultPickedIndex relative to the shown part
    private fun correctPositionByDefaultValue(defaultPickedIndex: Int, wrap: Boolean) {
        mCurrDrawFirstItemIndex = defaultPickedIndex - (mShowCount - 1) / 2
        mCurrDrawFirstItemIndex = getIndexByRawIndex(mCurrDrawFirstItemIndex, oneRecycleSize, wrap)
        if (mItemHeight == 0) {
            mCurrentItemIndexEffect = true
        } else {
            mCurrDrawGlobalY = mCurrDrawFirstItemIndex * mItemHeight

            mInScrollingPickedOldValue = mCurrDrawFirstItemIndex + mShowCount / 2
            mInScrollingPickedOldValue = mInScrollingPickedOldValue % oneRecycleSize
            if (mInScrollingPickedOldValue < 0) {
                mInScrollingPickedOldValue = mInScrollingPickedOldValue + oneRecycleSize
            }
            mInScrollingPickedNewValue = mInScrollingPickedOldValue
            calculateFirstItemParameterByGlobalY()
        }
    }

    //first shown item's content index, corresponding to the Index of mDisplayedValued
    private var mCurrDrawFirstItemIndex = 0
    //the first shown item's Y
    private var mCurrDrawFirstItemY = 0
    //global Y corresponding to scroller
    private var mCurrDrawGlobalY = 0

    override fun computeScroll() {
        if (mItemHeight == 0) return
        if (mScroller!!.computeScrollOffset()) {
            mCurrDrawGlobalY = mScroller!!.currY
            calculateFirstItemParameterByGlobalY()
            postInvalidate()
        }
    }

    private fun calculateFirstItemParameterByGlobalY() {
        mCurrDrawFirstItemIndex = Math.floor((mCurrDrawGlobalY.toFloat() / mItemHeight).toDouble()).toInt()
        mCurrDrawFirstItemY = -(mCurrDrawGlobalY - mCurrDrawFirstItemIndex * mItemHeight)
        if (mOnValueChangeListenerInScrolling != null) {
            if (-mCurrDrawFirstItemY > mItemHeight / 2) {
                mInScrollingPickedNewValue = mCurrDrawFirstItemIndex + 1 + mShowCount / 2
            } else {
                mInScrollingPickedNewValue = mCurrDrawFirstItemIndex + mShowCount / 2
            }
            mInScrollingPickedNewValue %= oneRecycleSize
            if (mInScrollingPickedNewValue < 0) {
                mInScrollingPickedNewValue += oneRecycleSize
            }
            if (mInScrollingPickedOldValue != mInScrollingPickedNewValue) {
                respondPickedValueChangedInScrolling(mInScrollingPickedNewValue, mInScrollingPickedOldValue)
            }
            mInScrollingPickedOldValue = mInScrollingPickedNewValue
        }
    }

    private fun releaseVelocityTracker() {
        mVelocityTracker?.clear()
        mVelocityTracker?.recycle()
        mVelocityTracker = null
    }

    private fun updateMaxWHOfDisplayedValues(needRequestLayout: Boolean) {
        updateMaxWidthOfDisplayedValues()
        updateMaxHeightOfDisplayedValues()
        if (needRequestLayout && (mSpecModeW == View.MeasureSpec.AT_MOST || mSpecModeH == View.MeasureSpec.AT_MOST)) {
            mHandlerInMainThread.sendEmptyMessage(HANDLER_WHAT_REQUEST_LAYOUT)
        }
    }

    private var mSpecModeW = View.MeasureSpec.UNSPECIFIED
    private var mSpecModeH = View.MeasureSpec.UNSPECIFIED

    private fun measureWidth(measureSpec: Int): Int {
        var result: Int
        mSpecModeW = View.MeasureSpec.getMode(measureSpec)
        val specMode = mSpecModeW
        val specSize = View.MeasureSpec.getSize(measureSpec)

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            val marginOfHint = if (Math.max(mWidthOfHintText, mWidthOfAlterHint) == 0) 0 else mMarginEndOfHint
            val gapOfHint = if (Math.max(mWidthOfHintText, mWidthOfAlterHint) == 0) 0 else mMarginStartOfHint

            val maxWidth = Math.max(mMaxWidthOfAlterArrayWithMeasureHint,
                    Math.max(mMaxWidthOfDisplayedValues, mMaxWidthOfAlterArrayWithoutMeasureHint) + 2 * (gapOfHint + Math.max(mWidthOfHintText, mWidthOfAlterHint) + marginOfHint + 2 * mItemPaddingHorizontal))
            result = this.paddingLeft + this.paddingRight + maxWidth//MeasureSpec.UNSPECIFIED
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }
        return result
    }

    private fun measureHeight(measureSpec: Int): Int {
        var result: Int
        mSpecModeH = View.MeasureSpec.getMode(measureSpec)
        val specMode = mSpecModeH
        val specSize = View.MeasureSpec.getSize(measureSpec)

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            val maxHeight = mShowCount * (mMaxHeightOfDisplayedValues + 2 * mItemPaddingVertical)
            result = this.paddingTop + this.paddingBottom + maxHeight//MeasureSpec.UNSPECIFIED
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawContent(canvas)
        drawLine(canvas)
        drawHint(canvas)
    }

    private fun drawContent(canvas: Canvas) {
        var index: Int
        var textColor: Int
        var textSize: Float
        var fraction = 0f// fraction of the item in state between normal and selected, in[0, 1]
        var textSizeCenterYOffset: Float

        for (i in 0..mShowCount + 1 - 1) {
            val y = (mCurrDrawFirstItemY + mItemHeight * i).toFloat()
            index = getIndexByRawIndex(mCurrDrawFirstItemIndex + i, oneRecycleSize, mWrapSelectorWheel && mWrapSelectorWheelCheck)
            if (i == mShowCount / 2) {//this will be picked
                fraction = (mItemHeight + mCurrDrawFirstItemY).toFloat() / mItemHeight
                textColor = getEvaluateColor(fraction, mTextColorNormal, mTextColorSelected)
                textSize = getEvaluateSize(fraction, mTextSizeNormal.toFloat(), mTextSizeSelected.toFloat())
                textSizeCenterYOffset = getEvaluateSize(fraction, mTextSizeNormalCenterYOffset,
                        mTextSizeSelectedCenterYOffset)
            } else if (i == mShowCount / 2 + 1) {
                textColor = getEvaluateColor(1 - fraction, mTextColorNormal, mTextColorSelected)
                textSize = getEvaluateSize(1 - fraction, mTextSizeNormal.toFloat(), mTextSizeSelected.toFloat())
                textSizeCenterYOffset = getEvaluateSize(1 - fraction, mTextSizeNormalCenterYOffset,
                        mTextSizeSelectedCenterYOffset)
            } else {
                textColor = mTextColorNormal
                textSize = mTextSizeNormal.toFloat()
                textSizeCenterYOffset = mTextSizeNormalCenterYOffset
            }
            mPaintText.color = textColor
            mPaintText.textSize = textSize

            if (index in 0..(oneRecycleSize - 1)) {
                var str: CharSequence = mDisplayedValues[index + mMinShowIndex]
                if (mTextEllipsize != null) {
                    str = TextUtils.ellipsize(str, mPaintText, (width - 2 * mItemPaddingHorizontal).toFloat(), ellipsizeType)
                }
                canvas.drawText(str.toString(), mViewCenterX,
                        y + (mItemHeight / 2).toFloat() + textSizeCenterYOffset, mPaintText)
            } else if (!TextUtils.isEmpty(mEmptyItemHint)) {
                canvas.drawText(mEmptyItemHint!!, mViewCenterX,
                        y + (mItemHeight / 2).toFloat() + textSizeCenterYOffset, mPaintText)
            }
        }
    }

    private val ellipsizeType: TextUtils.TruncateAt
        get() {
            when (mTextEllipsize) {
                TEXT_ELLIPSIZE_START -> return TextUtils.TruncateAt.START
                TEXT_ELLIPSIZE_MIDDLE -> return TextUtils.TruncateAt.MIDDLE
                TEXT_ELLIPSIZE_END -> return TextUtils.TruncateAt.END
                else -> throw IllegalArgumentException("Illegal text ellipsize type.")
            }
        }

    private fun drawLine(canvas: Canvas) {
        if (mShowDivider) {
            canvas.drawLine((paddingLeft + mDividerMarginL).toFloat(),
                    dividerY0, (mViewWidth - paddingRight - mDividerMarginR).toFloat(), dividerY0, mPaintDivider)
            canvas.drawLine((paddingLeft + mDividerMarginL).toFloat(),
                    dividerY1, (mViewWidth - paddingRight - mDividerMarginR).toFloat(), dividerY1, mPaintDivider)
        }
    }

    private fun drawHint(canvas: Canvas) {
        if (TextUtils.isEmpty(mHintText)) return
        canvas.drawText(mHintText!!,
                mViewCenterX + ((mMaxWidthOfDisplayedValues + mWidthOfHintText) / 2).toFloat() + mMarginStartOfHint.toFloat(),
                (dividerY0 + dividerY1) / 2 + mTextSizeHintCenterYOffset, mPaintHint)
    }

    private fun updateMaxWidthOfDisplayedValues() {
        val savedTextSize = mPaintText.textSize
        mPaintText.textSize = mTextSizeSelected.toFloat()
        mMaxWidthOfDisplayedValues = getMaxWidthOfTextArray(mDisplayedValues as Array<CharSequence>, mPaintText)
        mMaxWidthOfAlterArrayWithMeasureHint = getMaxWidthOfTextArray(mAlterTextArrayWithMeasureHint, mPaintText)
        mMaxWidthOfAlterArrayWithoutMeasureHint = getMaxWidthOfTextArray(mAlterTextArrayWithoutMeasureHint, mPaintText)
        mPaintText.textSize = mTextSizeHint.toFloat()
        mWidthOfAlterHint = getTextWidth(mAlterHint as CharSequence, mPaintText)
        mPaintText.textSize = savedTextSize
    }

    private fun getMaxWidthOfTextArray(array: Array<CharSequence>?, paint: Paint): Int {
        if (array == null) {
            return 0
        }
        val maxWidth = array
                .map { getTextWidth(it, paint) }
                .max()
                ?: 0
        return maxWidth
    }

    private fun getTextWidth(text: CharSequence, paint: Paint): Int {
        if (!TextUtils.isEmpty(text)) {
            return (paint.measureText(text.toString()) + 0.5f).toInt()
        }
        return 0
    }

    private fun updateMaxHeightOfDisplayedValues() {
        val savedTextSize = mPaintText.textSize
        mPaintText.textSize = mTextSizeSelected.toFloat()
        mMaxHeightOfDisplayedValues = (mPaintText.fontMetrics.bottom - mPaintText.fontMetrics.top + 0.5).toInt()
        mPaintText.textSize = savedTextSize
    }

    private fun updateContentAndIndex(newDisplayedValues: Array<String>) {
        mMinShowIndex = 0
        mMaxShowIndex = newDisplayedValues.size - 1
        mDisplayedValues = newDisplayedValues
        updateWrapStateByContent()
    }

    private fun updateContent(newDisplayedValues: Array<String>) {
        mDisplayedValues = newDisplayedValues
        updateWrapStateByContent()
    }

    //used in setDisplayedValues
    private fun updateValue() {
        updateWrapStateByContent()
        mMinShowIndex = 0
        mMaxShowIndex = mDisplayedValues.size - 1
    }

    private fun updateValueForInit() {
        updateWrapStateByContent()
        if (mMinShowIndex == -1) {
            mMinShowIndex = 0
        }
        if (mMaxShowIndex == -1) {
            mMaxShowIndex = 0
        }
        setMinAndMaxShowIndex(mMinShowIndex, mMaxShowIndex, false)
    }

    private fun updateWrapStateByContent() {
        mWrapSelectorWheelCheck = mDisplayedValues.size > mShowCount
    }

    private fun refineValueByLimit(value: Int, minValue: Int, maxValue: Int, wrap: Boolean): Int {
        var value = value
        if (wrap) {
            if (value > maxValue) {
                value = (value - maxValue) % oneRecycleSize + minValue - 1
            } else if (value < minValue) {
                value = (value - minValue) % oneRecycleSize + maxValue + 1
            }
            return value
        } else {
            if (value > maxValue) {
                value = maxValue
            } else if (value < minValue) {
                value = minValue
            }
            return value
        }
    }

    private fun stopRefreshing() {
        mHandlerInNewThread.removeMessages(HANDLER_WHAT_REFRESH)
    }

    fun stopScrolling() {
        if (mScroller != null) {
            if (!mScroller!!.isFinished) {
                mScroller!!.startScroll(0, mScroller!!.currY, 0, 0, 1)
                mScroller!!.abortAnimation()
                postInvalidate()
            }
        }
    }

    fun stopScrollingAndCorrectPosition() {
        stopScrolling()
        mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH), 0)
    }

    private fun getMsg(what: Int, arg1: Int = 0, arg2: Int = 0, obj: Any? = null): Message {
        val msg = Message.obtain()
        msg.what = what
        msg.arg1 = arg1
        msg.arg2 = arg2
        msg.obj = obj
        return msg
    }

    //===tool functions===//
    private fun isStringEqual(a: String?, b: String?): Boolean {
        if (a == null) {
            return b == null
        } else {
            return a == b
        }
    }

    private fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    private fun dp2px(context: Context, dpValue: Float): Int {
        val densityScale = context.resources.displayMetrics.density
        return (dpValue * densityScale + 0.5f).toInt()
    }

    private fun getEvaluateColor(fraction: Float, startColor: Int, endColor: Int): Int {

        val a: Int
        val r: Int
        val g: Int
        val b: Int

        val sA = (startColor and 0xff000000.toInt()).ushr(24)
        val sR = (startColor and 0x00ff0000).ushr(16)
        val sG = (startColor and 0x0000ff00).ushr(8)
        val sB = (startColor and 0x000000ff).ushr(0)

        val eA = (endColor and 0xff000000.toInt()).ushr(24)
        val eR = (endColor and 0x00ff0000).ushr(16)
        val eG = (endColor and 0x0000ff00).ushr(8)
        val eB = (endColor and 0x000000ff).ushr(0)

        a = (sA + (eA - sA) * fraction).toInt()
        r = (sR + (eR - sR) * fraction).toInt()
        g = (sG + (eG - sG) * fraction).toInt()
        b = (sB + (eB - sB) * fraction).toInt()

        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    private fun getEvaluateSize(fraction: Float, startSize: Float, endSize: Float): Float {
        return startSize + (endSize - startSize) * fraction
    }

    private fun convertCharSequenceArrayToStringArray(charSequences: Array<CharSequence>?): Array<String>? {
        if (charSequences == null) return null
        val ret = Array(charSequences.size, { "" })
        for (i in charSequences.indices) {
            ret[i] = charSequences[i].toString()
        }
        return ret
    }

    companion object {

        // default text color of not selected item
        private val DEFAULT_TEXT_COLOR_NORMAL = 0XFF333333.toInt()

        // default text color of selected item
        private val DEFAULT_TEXT_COLOR_SELECTED = 0XFFF56313.toInt()

        // default text size of normal item
        private val DEFAULT_TEXT_SIZE_NORMAL_SP = 14

        // default text size of selected item
        private val DEFAULT_TEXT_SIZE_SELECTED_SP = 16

        // default text size of hint text, the middle item's right text
        private val DEFAULT_TEXT_SIZE_HINT_SP = 14

        // distance between selected text and hint text
        private val DEFAULT_MARGIN_START_OF_HINT_DP = 8

        // distance between hint text and right of this view, used in wrap_content mode
        private val DEFAULT_MARGIN_END_OF_HINT_DP = 8

        // default divider's color
        private val DEFAULT_DIVIDER_COLOR = 0XFFF56313.toInt()

        // default divider's height
        private val DEFAULT_DIVIDER_HEIGHT = 2

        // default divider's margin to the left & right of this view
        private val DEFAULT_DIVIDER_MARGIN_HORIZONTAL = 0

        // default shown items' count, now we display 3 items, the 2nd one is selected
        private val DEFAULT_SHOW_COUNT = 3

        // default items' horizontal padding, left padding and right padding are both 5dp,
        // only used in wrap_content mode
        private val DEFAULT_ITEM_PADDING_DP_H = 5

        // default items' vertical padding, top padding and bottom padding are both 2dp,
        // only used in wrap_content mode
        private val DEFAULT_ITEM_PADDING_DP_V = 2

        // message's what argument to refresh current state, used by mHandler
        private val HANDLER_WHAT_REFRESH = 1

        // message's what argument to respond value changed event, used by mHandler
        private val HANDLER_WHAT_LISTENER_VALUE_CHANGED = 2

        // message's what argument to request layout, used by mHandlerInMainThread
        private val HANDLER_WHAT_REQUEST_LAYOUT = 3

        // interval time to scroll the distance of one item's height
        private val HANDLER_INTERVAL_REFRESH = 32//millisecond

        // in millisecond unit, default duration of scrolling an item' distance
        private val DEFAULT_INTERVAL_REVISE_DURATION = 300

        // max and min durations when scrolling from one value to another
        private val DEFAULT_MIN_SCROLL_BY_INDEX_DURATION = DEFAULT_INTERVAL_REVISE_DURATION * 1
        private val DEFAULT_MAX_SCROLL_BY_INDEX_DURATION = DEFAULT_INTERVAL_REVISE_DURATION * 2

        private val TEXT_ELLIPSIZE_START = "start"
        private val TEXT_ELLIPSIZE_MIDDLE = "middle"
        private val TEXT_ELLIPSIZE_END = "end"

        private val DEFAULT_SHOW_DIVIDER = true
        private val DEFAULT_WRAP_SELECTOR_WHEEL = true
        private val DEFAULT_CURRENT_ITEM_INDEX_EFFECT = false
        private val DEFAULT_RESPOND_CHANGE_ON_DETACH = false
        private val DEFAULT_RESPOND_CHANGE_IN_MAIN_THREAD = true
    }
}