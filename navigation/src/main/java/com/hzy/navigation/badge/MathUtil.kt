package com.hzy.navigation.badge

import android.graphics.PointF

/**
 * Created by ziye_huang on 2018/12/27.
 */
object MathUtil {
    private const val CIRCLE_RADIAN = Math.PI * 2

    fun getTanRadian(atan: Double, quadrant: Int): Double {
        var atan = atan
        if (atan < 0) {
            atan += CIRCLE_RADIAN / 4
        }
        atan += CIRCLE_RADIAN / 4 * (quadrant - 1)
        return atan
    }

    fun radianToAngle(radian: Double): Double {
        return 360 * (radian / CIRCLE_RADIAN)
    }

    fun getQuadrant(p: PointF, center: PointF): Int {
        if (p.x > center.x) {
            if (p.y > center.y) {
                return 4
            } else if (p.y < center.y) {
                return 1
            }
        } else if (p.x < center.x) {
            if (p.y > center.y) {
                return 3
            } else if (p.y < center.y) {
                return 2
            }
        }
        return -1
    }

    fun getPointDistance(p1: PointF, p2: PointF): Float {
        return Math.sqrt(Math.pow((p1.x - p2.x).toDouble(), 2.0) + Math.pow((p1.y - p2.y).toDouble(), 2.0))
            .toFloat()
    }

    /**
     * this formula is designed by mabeijianxi
     * website : http://blog.csdn.net/mabeijianxi/article/details/50560361
     *
     * @param circleCenter The circle center point.
     * @param radius The circle radius.
     * @param slopeLine The slope of line which cross the pMiddle.
     */
    fun getInnertangentPoints(circleCenter: PointF, radius: Float, slopeLine: Double, points: MutableList<PointF>) {
        val xOffset: Float
        val yOffset: Float
        if (null != slopeLine) {
            val radian = Math.atan(slopeLine)
            xOffset = (Math.cos(radian) * radius).toFloat()
            yOffset = (Math.sin(radian) * radius).toFloat()
        } else {
            xOffset = radius
            yOffset = 0f
        }
        points.add(PointF(circleCenter.x + xOffset, circleCenter.y + yOffset))
        points.add(PointF(circleCenter.x - xOffset, circleCenter.y - yOffset))
    }

}