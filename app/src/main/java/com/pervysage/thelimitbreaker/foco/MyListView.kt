package com.pervysage.thelimitbreaker.foco

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ListView

class MyListView : ListView {


    var isScrollEnabled = true


    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (!isScrollEnabled) {
            return false
        }
        return super.dispatchTouchEvent(ev)
    }
}
