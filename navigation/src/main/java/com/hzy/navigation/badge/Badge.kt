package com.hzy.navigation.badge

import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.view.View


/**
 * Created by ziye_huang on 2018/12/27.
 */
interface Badge {

    fun bindTargetView(view: View): Badge

    fun getTargetView(): View

    fun setBadgeCount(badgeCount: Int): Badge

    fun getBadgeCount(): Int

    fun setBadgeText(badgeText: String): Badge

    fun getBadgeText(): String?

    fun setExactMode(isExact: Boolean): Badge

    fun isExactMode(): Boolean

    fun setShowShadow(showShadow: Boolean): Badge

    fun isShowShadow(): Boolean

    fun setBadgeBackgroundColor(color: Int): Badge

    fun getBadgeBackgroundColor(): Int

    fun setBadgeBackground(drawable: Drawable): Badge

    fun setBadgeBackground(drawable: Drawable, clip: Boolean): Badge

    fun getBadgeBackground(): Drawable?

    fun setBadgeTextColor(color: Int): Badge

    fun getBadgeTextColor(): Int

    fun setBadgeTextSize(size: Float, isSpValue: Boolean): Badge

    fun getBadgeTextSize(isSpValue: Boolean): Float

    fun getBadgePadding(isDpValue: Boolean): Float

    fun setBadgePadding(padding: Float, isDpValue: Boolean): Badge

    fun setBadgeGravity(gravity: Int): Badge

    fun getBadgeGravity(): Int

    fun setGravityOffset(offset: Float, isDpValue: Boolean): Badge

    fun setGravityOffset(offsetX: Float, offsetY: Float, isDpValue: Boolean): Badge

    fun getGravityOffsetX(isDpValue: Boolean): Float

    fun getGravityOffsetY(isDpValue: Boolean): Float

    fun setOnDragStateChangedListener(listener: OnDragStateChangedListener): Badge

    fun stroke(color: Int, width: Float, isDpValue: Boolean): Badge

    fun isDraggable(): Boolean

    fun getDragCenter(): PointF?

    fun hide(animate: Boolean)

    interface OnDragStateChangedListener {
        fun onDragStateChanged(dragState: DragState, badge: Badge, targetView: View)
    }
}