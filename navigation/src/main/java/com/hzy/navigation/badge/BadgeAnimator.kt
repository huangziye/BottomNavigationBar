package com.hzy.navigation.badge

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.*
import java.lang.ref.WeakReference
import java.util.*

/**
 * Animation borrowed from https://github.com/tyrantgit/ExplosionField
 * Created by ziye_huang on 2018/12/27.
 */
class BadgeAnimator(badgeBitmap: Bitmap, center: PointF, badge: BadgeView) : ValueAnimator() {
    private var mFragments: Array<Array<BitmapFragment>>
    private var mWeakBadge: WeakReference<BadgeView> = WeakReference(badge)

    init {
        setFloatValues(0f, 1f)
        duration = 500
        mFragments = getFragments(badgeBitmap, center)
        addUpdateListener {
            val badgeView = mWeakBadge.get()
            if (null == badgeView || !badgeView.isShown) {
                cancel()
            } else {
                badgeView.invalidate()
            }
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                mWeakBadge.get()?.reset()
            }
        })
    }

    fun draw(canvas: Canvas) {
        for (i in 0 until mFragments.size) {
            for (j in 0 until mFragments[i].size) {
                val bitmapFragment = mFragments[i][j]
                val value = animatedValue.toString().toFloat()
                bitmapFragment.update(value, canvas)
            }
        }
    }

    private fun getFragments(badgeBitmap: Bitmap, center: PointF): Array<Array<BitmapFragment>> {
        val width = badgeBitmap.width
        val height = badgeBitmap.height
        val fragmentSize = Math.min(width, height) / 6f
        val startX = center.x - width / 2f
        val startY = center.y - height / 2f
        val fragments =
            Array((height / fragmentSize).toInt()) { Array((width / fragmentSize).toInt()) { BitmapFragment() } }
        for (i in 0 until fragments.size) {
            for (j in 0 until fragments[i].size) {
                val bitmapFragment = BitmapFragment()
                bitmapFragment.mColor = badgeBitmap.getPixel(((j * fragmentSize).toInt()), (i * fragmentSize).toInt())
                bitmapFragment.x = startX + j * fragmentSize
                bitmapFragment.y = startY + i * fragmentSize
                bitmapFragment.mSize = fragmentSize
                bitmapFragment.maxSize = Math.max(width, height)
                fragments[i][j] = bitmapFragment
            }
        }
        badgeBitmap.recycle()
        return fragments
    }

    private class BitmapFragment {
        private val mRandom: Random = Random()
        private val mPaint: Paint = Paint()
        var mColor = Color.WHITE
        var x: Float = 0f
        var y: Float = 0f
        var mSize: Float = 0f
        var maxSize: Int = 0

        init {
            mPaint.isAntiAlias = true
            mPaint.style = Paint.Style.FILL
        }

        fun update(value: Float, canvas: Canvas) {
            mPaint.color = mColor
            x += 0.1f * mRandom.nextInt(maxSize) * (mRandom.nextFloat() - 0.5f)
            y += 0.1f * mRandom.nextInt(maxSize) * (mRandom.nextFloat() - 0.5f)
            canvas.drawCircle(x, y, mSize - value * mSize, mPaint)
        }
    }
}