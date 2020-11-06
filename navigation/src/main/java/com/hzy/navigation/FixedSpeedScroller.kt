package com.hzy.navigation

import android.content.Context
import android.view.animation.Interpolator
import android.widget.Scroller

/**
 * 自定义一个Scroll类
 *
 *  在使用ViewPager的过程中，有需要直接跳转到某一个页面的情况，
 *  这个时候就需要用到ViewPager的setCurrentItem方法了，
 *  它的意思是跳转到ViewPager的指定页面，但在使用这个方法的时候有个问题，
 *  跳转的时候有滑动效果，当需要从当前页面跳转到其它页面时，
 *  跳转页面跨度过大、或者ViewPager每个页面的视觉效果相差较大时，
 *  通过这种方式实现ViewPager跳转显得很不美观，怎么办呢，
 *  我们可以去掉在使用ViewPager的setCurrentItem方法时的滑屏速度
 */
class FixedSpeedScroller : Scroller {

    private var canScroll = true

    constructor(context: Context) : super(context)
    constructor(context: Context, interpolator: Interpolator) : super(context, interpolator)
    constructor(context: Context, interpolator: Interpolator, flywheel: Boolean) : super(
        context,
        interpolator,
        flywheel
    )

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, if (canScroll) duration else 0)
    }

    fun setCanScroll(canScroll: Boolean) {
        this.canScroll = canScroll
    }

}