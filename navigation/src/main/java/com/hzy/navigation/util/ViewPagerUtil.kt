package com.hzy.navigation.util

import androidx.viewpager.widget.ViewPager
import com.hzy.navigation.FixedSpeedScroller

object ViewPagerUtil {

    /**
     * 设置ViewPager的滑动速度
     */
    fun setViewPagerScrollSpeed(viewPager: ViewPager, canScroll: Boolean) {
        try {
            var mScroller = ViewPager::class.java.getDeclaredField("mScroller")
            mScroller.isAccessible = true
            val scroller = FixedSpeedScroller(viewPager.context)
            scroller.setCanScroll(canScroll)
            mScroller.set(viewPager, scroller)
        } catch (e: Exception) {
        }
    }
}