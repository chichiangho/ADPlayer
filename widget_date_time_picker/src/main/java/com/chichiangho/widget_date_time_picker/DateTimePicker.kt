package com.chichiangho.widget_date_time_picker

import android.app.Dialog
import android.content.Context
import android.support.annotation.ColorRes
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.chichaingho.widget_date_time_picker.R
import com.chichiangho.common.extentions.format
import com.chichiangho.common.extentions.parseColor
import kotlinx.android.synthetic.main.layout_date_picker.*
import java.util.*

/**
 * Created by chichiangho on 2017/7/15.
 */

class DateTimePicker(context: Context?) : Dialog(context) {
    private var type = 0x11
    private lateinit var onDateTimePickedCallback: OnDateTimePickedCallback
    private var curDate: Date
    private var fromThen: Boolean = false
    private var untilThen: Boolean = false

    interface OnDateTimePickedCallback {
        fun onPicked(date: Date)
    }

    init {
        curDate = Date(System.currentTimeMillis())
    }

    private var themeColor: Int = 0

    fun setThemeColor(@ColorRes colorRes: Int): DateTimePicker {
        themeColor = colorRes
        return this
    }

    private fun setPickerTheme(picker: OptimizedNumberPicker, @ColorRes colorRes: Int) {
        picker.setSelectedTextColor(parseColor(colorRes))
        picker.setHintTextColor(parseColor(colorRes))
        picker.setDividerColor(parseColor(colorRes))
    }

    /**
     * set picker start time as the showDate,or cur time if showDate was not set

     * @param fromThen
     * *
     * @return
     */
    fun setFromThen(fromThen: Boolean): DateTimePicker {
        this.fromThen = fromThen
        return this
    }

    /**
     * set picker end time as the showDate,or cur time if showDate was not set.if fromThen was true,this property will be invalid
     *
     */
    fun setUntilThen(untilThen: Boolean): DateTimePicker {
        this.untilThen = untilThen
        return this
    }

    fun setShowDate(showDate: Date): DateTimePicker {
        this.curDate = showDate
        return this
    }

    fun setOnDateTimePickedCallback(onDateTimePickedCallback: OnDateTimePickedCallback): DateTimePicker {
        this.onDateTimePickedCallback = onDateTimePickedCallback
        return this
    }

    fun setType(type: Int): DateTimePicker {
        this.type = type
        return this
    }

    override fun show() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_date_picker)
        setCanceledOnTouchOutside(true)

        if (themeColor > 0) {
            perform.setTextColor(context.resources.getColor(themeColor))
            setPickerTheme(yearPicker, themeColor)
            setPickerTheme(monthPicker, themeColor)
            setPickerTheme(dayPicker, themeColor)
            setPickerTheme(hourPicker, themeColor)
            setPickerTheme(minutePicker, themeColor)
        }

        val lp = window.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.BOTTOM
        window.attributes = lp
        window.setBackgroundDrawableResource(R.color.white)

        cancel.setOnClickListener { dismiss() }
        perform.setOnClickListener {
            onDateTimePickedCallback.onPicked(curDate)
            dismiss()
        }

        val calendar = Calendar.getInstance()
        calendar.time = curDate

        if (type and TYPE_DATE > 0 && type and TYPE_TIME > 0) {
            resultTv.text = curDate.format("yyyy-MM-dd HH:mm")
        } else if (type and TYPE_DATE > 0) {
            resultTv.text = curDate.format("yyyy-MM-dd")
        } else if (type and TYPE_TIME > 0) {
            resultTv.text = curDate.format("HH:mm")
        }

        if (type and TYPE_DATE > 0) {
            dayPicker.visibility = View.VISIBLE
            when {
                fromThen -> {
                    dayPicker.displayedValues = getDisplayValues(calendar.get(Calendar.DAY_OF_MONTH), 31)
                    dayPicker.minValue = calendar.get(Calendar.DAY_OF_MONTH)
                    dayPicker.maxValue = getMaxDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
                }
                untilThen -> {
                    dayPicker.displayedValues = getDisplayValues(1, calendar.get(Calendar.DAY_OF_MONTH))
                    dayPicker.minValue = 1
                    dayPicker.maxValue = calendar.get(Calendar.DAY_OF_MONTH)
                }
                else -> {
                    dayPicker.displayedValues = getDisplayValues(1, 31)
                    dayPicker.minValue = 1
                    dayPicker.maxValue = getMaxDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
                }
            }
            dayPicker.setOnValueChangedListener(object : OptimizedNumberPicker.OnValueChangeListener {
                override fun onValueChange(picker: OptimizedNumberPicker, oldVal: Int, newVal: Int) {
                    changeHour(calendar)
                    changeMinute(calendar)
                    updateCurdate()
                }
            })
            dayPicker.value = calendar.get(Calendar.DAY_OF_MONTH)

            yearPicker.visibility = View.VISIBLE
            var startYear = 1970
            var endYear = 2199
            if (fromThen)
                startYear = calendar.get(Calendar.YEAR)
            else if (untilThen)
                endYear = calendar.get(Calendar.YEAR)
            yearPicker.displayedValues = getDisplayValues(startYear, endYear)
            yearPicker.minValue = startYear
            yearPicker.maxValue = endYear
            yearPicker.setOnValueChangedListener(object : OptimizedNumberPicker.OnValueChangeListener {
                override fun onValueChange(picker: OptimizedNumberPicker, oldVal: Int, newVal: Int) {
                    changeMonth(calendar)
                    changeDay(calendar)
                    changeHour(calendar)
                    changeMinute(calendar)
                    updateCurdate()
                }
            })
            yearPicker.value = calendar.get(Calendar.YEAR)

            monthPicker.visibility = View.VISIBLE
            when {
                fromThen -> {
                    monthPicker.displayedValues = getDisplayValues(calendar.get(Calendar.MONTH) + 1, 13)
                    monthPicker.minValue = calendar.get(Calendar.MONTH) + 1
                    monthPicker.maxValue = 12
                }
                untilThen -> {
                    monthPicker.displayedValues = getDisplayValues(1, calendar.get(Calendar.MONTH) + 1)
                    monthPicker.minValue = 1
                    monthPicker.maxValue = calendar.get(Calendar.MONTH) + 1
                }
                else -> {
                    monthPicker.displayedValues = getDisplayValues(1, 12)
                    monthPicker.minValue = 1
                    monthPicker.maxValue = 12
                }
            }
            monthPicker.setOnValueChangedListener(object : OptimizedNumberPicker.OnValueChangeListener {
                override fun onValueChange(picker: OptimizedNumberPicker, oldVal: Int, newVal: Int) {
                    changeDay(calendar)
                    changeHour(calendar)
                    changeMinute(calendar)
                    updateCurdate()
                }
            })
            monthPicker.value = calendar.get(Calendar.MONTH) + 1
        }

        if (type and TYPE_TIME > 0) {
            hourPicker.visibility = View.VISIBLE
            when {
                fromThen -> {
                    hourPicker.displayedValues = getDisplayValues(calendar.get(Calendar.HOUR_OF_DAY), 23)
                    hourPicker.minValue = calendar.get(Calendar.HOUR_OF_DAY)
                    hourPicker.maxValue = 23
                }
                untilThen -> {
                    hourPicker.displayedValues = getDisplayValues(0, calendar.get(Calendar.HOUR_OF_DAY))
                    hourPicker.minValue = 0
                    hourPicker.maxValue = calendar.get(Calendar.HOUR_OF_DAY)
                }
                else -> {
                    hourPicker.displayedValues = getDisplayValues(0, 23)
                    hourPicker.minValue = 0
                    hourPicker.maxValue = 23
                }
            }
            hourPicker.value = calendar.get(Calendar.HOUR_OF_DAY)
            hourPicker.setOnValueChangedListener(object : OptimizedNumberPicker.OnValueChangeListener {
                override fun onValueChange(picker: OptimizedNumberPicker, oldVal: Int, newVal: Int) {
                    changeMinute(calendar)
                    updateCurdate()
                }
            })
            minutePicker.visibility = View.VISIBLE
            when {
                fromThen -> {
                    minutePicker.displayedValues = getDisplayValues(calendar.get(Calendar.MINUTE), 59)
                    minutePicker.minValue = calendar.get(Calendar.MINUTE)
                    minutePicker.maxValue = 59
                }
                untilThen -> {
                    minutePicker.displayedValues = getDisplayValues(0, calendar.get(Calendar.MINUTE))
                    minutePicker.minValue = 0
                    minutePicker.maxValue = calendar.get(Calendar.MINUTE)
                }
                else -> {
                    minutePicker.displayedValues = getDisplayValues(0, 59)
                    minutePicker.minValue = 0
                    minutePicker.maxValue = 59
                }
            }
            minutePicker.value = calendar.get(Calendar.MINUTE)
            minutePicker.setOnValueChangedListener(object : OptimizedNumberPicker.OnValueChangeListener {
                override fun onValueChange(picker: OptimizedNumberPicker, oldVal: Int, newVal: Int) {
                    updateCurdate()
                }
            })
        }
        super.show()
    }

    private fun changeMonth(calendar: Calendar) {
        if (type and TYPE_DATE > 0) {
            if (yearPicker.value == calendar.get(Calendar.YEAR)) {
                if (fromThen) {
                    monthPicker.refreshByNewDisplayedValues(getDisplayValues(calendar.get(Calendar.MONTH) + 1, 13))
                    monthPicker.minValue = calendar.get(Calendar.MONTH) + 1
                    monthPicker.maxValue = 12
                } else if (untilThen) {
                    monthPicker.refreshByNewDisplayedValues(getDisplayValues(1, calendar.get(Calendar.MONTH) + 1))
                    monthPicker.minValue = 1
                    monthPicker.maxValue = calendar.get(Calendar.MONTH) + 1
                }
            } else if (monthPicker.displayedValues?.size ?: 0 < 12) {
                monthPicker.refreshByNewDisplayedValues(getDisplayValues(1, 12))
                monthPicker.minValue = 1
                monthPicker.maxValue = 12
            }
        }
    }

    private fun changeDay(calendar: Calendar) {
        if (type and TYPE_DATE > 0) {
            if (yearPicker.value == calendar.get(Calendar.YEAR) && monthPicker.value == calendar.get(Calendar.MONTH) + 1) {
                if (fromThen) {
                    dayPicker.refreshByNewDisplayedValues(getDisplayValues(calendar.get(Calendar.DAY_OF_MONTH), 31))
                    dayPicker.minValue = calendar.get(Calendar.DAY_OF_MONTH)
                    dayPicker.maxValue = getMaxDay(yearPicker.value, monthPicker.value)
                } else if (untilThen) {
                    dayPicker.refreshByNewDisplayedValues(getDisplayValues(1, calendar.get(Calendar.DAY_OF_MONTH)))
                    dayPicker.minValue = 1
                    dayPicker.maxValue = calendar.get(Calendar.DAY_OF_MONTH)
                }
            } else {
                if (dayPicker.displayedValues?.size ?: 0 < 31) {
                    dayPicker.refreshByNewDisplayedValues(getDisplayValues(1, 31))
                    dayPicker.minValue = 1
                }
                dayPicker.maxValue = getMaxDay(yearPicker.value, monthPicker.value)
            }
        }
    }

    private fun changeHour(calendar: Calendar) {
        if (type and TYPE_TIME > 0) {
            if (yearPicker.value == calendar.get(Calendar.YEAR) && monthPicker.value == calendar.get(Calendar.MONTH) + 1
                    && dayPicker.value == calendar.get(Calendar.DAY_OF_MONTH)) {
                if (fromThen) {
                    hourPicker.refreshByNewDisplayedValues(getDisplayValues(calendar.get(Calendar.HOUR_OF_DAY), 23))
                    hourPicker.minValue = calendar.get(Calendar.HOUR_OF_DAY)
                    hourPicker.maxValue = 23
                } else if (untilThen) {
                    hourPicker.refreshByNewDisplayedValues(getDisplayValues(0, calendar.get(Calendar.HOUR_OF_DAY)))
                    hourPicker.minValue = 0
                    hourPicker.maxValue = calendar.get(Calendar.HOUR_OF_DAY)
                }
            } else if (hourPicker.displayedValues?.size ?: 0 < 24) {
                hourPicker.refreshByNewDisplayedValues(getDisplayValues(0, 23))
                hourPicker.minValue = 0
                hourPicker.maxValue = 23
            }
        }
    }

    private fun changeMinute(calendar: Calendar) {
        if (type and TYPE_TIME > 0) {
            if ((type and TYPE_DATE > 0 && yearPicker.value == calendar.get(Calendar.YEAR) && monthPicker.value == calendar.get(Calendar.MONTH) + 1
                            && dayPicker.value == calendar.get(Calendar.DAY_OF_MONTH) || type and TYPE_DATE == 0) && hourPicker.value == calendar.get(Calendar.HOUR_OF_DAY)) {
                if (fromThen) {
                    minutePicker.refreshByNewDisplayedValues(getDisplayValues(calendar.get(Calendar.MINUTE), 59))
                    minutePicker.minValue = calendar.get(Calendar.MINUTE)
                    minutePicker.maxValue = 59
                } else if (untilThen) {
                    minutePicker.refreshByNewDisplayedValues(getDisplayValues(0, calendar.get(Calendar.MINUTE)))
                    minutePicker.minValue = 0
                    minutePicker.maxValue = calendar.get(Calendar.MINUTE)
                }
            } else if (minutePicker.displayedValues?.size ?: 0 < 60) {
                minutePicker.refreshByNewDisplayedValues(getDisplayValues(0, 59))
                minutePicker.minValue = 0
                minutePicker.maxValue = 59
            }
        }
    }

    private fun getMaxDay(year: Int, month: Int): Int {
        return when (month) {
            2 ->
                if (year % 4 == 0 && (year % 100 == 0 && year % 400 == 0 || year % 100 != 0)) {
                    29
                } else {
                    28
                }
            4, 6, 9, 11 ->
                30
            else ->
                31
        }
    }

    private fun updateCurdate() {
        if (type and TYPE_DATE > 0 && type and TYPE_TIME > 0) {
            curDate = Date(yearPicker.value - 1900, monthPicker.value - 1, dayPicker.value, hourPicker.value, minutePicker.value)
            resultTv.text = curDate.format("yyyy-MM-dd HH:mm")
        } else if (type and TYPE_DATE > 0) {
            curDate = Date(yearPicker.value - 1900, monthPicker.value - 1, dayPicker.value, 0, 0)
            resultTv.text = curDate.format("yyyy-MM-dd")
        } else if (type and TYPE_TIME > 0) {
            curDate = Date(0, 0, 0, hourPicker.value, minutePicker.value)
            resultTv.text = curDate.format("HH:mm")
        }
    }

    private fun getDisplayValues(from: Int, to: Int): Array<String> {
        val mDisplayedValues = Array(to - from + 1, { "" })
        for (i in from..to) {
            mDisplayedValues[i - from] = (if (i < 10) "0" else "") + i
        }
        return mDisplayedValues
    }

    companion object {
        const val TYPE_DATE = 0x01
        const val TYPE_TIME = 0x10
    }
}
