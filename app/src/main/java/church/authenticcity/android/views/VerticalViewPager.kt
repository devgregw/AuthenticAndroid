package church.authenticcity.android.views

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 5/29/2018 at 3:32 PM.
 * Licensed under the MIT License.
 */
class VerticalViewPager : ViewPager {
    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initialize()
    }

    fun initialize() {
        setPageTransformer(true, VerticalPageTransformer())
        overScrollMode = View.OVER_SCROLL_NEVER
        addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                disabled = position == 1
            }
        })
    }

    class VerticalPageTransformer : ViewPager.PageTransformer {
        override fun transformPage(page: View, position: Float) {
            if (position < -1)
                page.alpha = 0f
            else if (position <= 1) {
                page.alpha = 1f
                page.translationX = page.width * -position
                page.translationY = position * page.height
            } else
                page.alpha = 0f
        }
    }

    var disabled: Boolean = false

    fun swap(event: MotionEvent?): MotionEvent? {
        if (event == null)
            return null
        event.setLocation((event.y / height) * width, (event.x / width) * height)
        return event
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean = if (disabled) false else super.onTouchEvent(swap(ev))

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (disabled)
            return false
        val intercepted = super.onInterceptTouchEvent(swap(ev))
        swap(ev)
        return intercepted
    }
}