package com.hzy.navigation.badge

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcelable
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.SparseArray
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout


/**
 * Created by ziye_huang on 2018/12/27.
 */
open class BadgeView : View, Badge {

    protected var mColorBackground: Int = 0
    protected var mColorBackgroundBorder: Int = 0
    protected var mColorBadgeText: Int = 0
    protected var mDrawableBackground: Drawable? = null
    protected var mBitmapClip: Bitmap? = null
    protected var mDrawableBackgroundClip: Boolean = false
    protected var mBackgroundBorderWidth: Float = 0.toFloat()
    protected var mBadgeTextSize: Float = 0.toFloat()
    protected var mBadgePadding: Float = 0.toFloat()
    protected var mBadgeCount: Int = 0
    protected var mBadgeText: String? = null
    protected var mDraggable: Boolean = false
    protected var mDragging: Boolean = false
    protected var mExact: Boolean = false
    protected var mShowShadow: Boolean = false
    protected var mBadgeGravity: Int = 0
    protected var mGravityOffsetX: Float = 0.toFloat()
    protected var mGravityOffsetY: Float = 0.toFloat()
    protected var mDefalutRadius: Float = 0.toFloat()
    protected var mFinalDragDistance: Float = 0.toFloat()
    protected var mDragQuadrant: Int = 0
    protected var mDragOutOfRange: Boolean = false
    protected var mWidth: Int = 0
    protected var mHeight: Int = 0
    protected var mAnimator: BadgeAnimator? = null
    protected var mDragStateChangedListener: Badge.OnDragStateChangedListener? = null
    protected var mActivityRoot: ViewGroup? = null

    protected lateinit var mBadgeTextRect: RectF
    protected lateinit var mBadgeBackgroundRect: RectF
    protected lateinit var mDragPath: Path
    protected lateinit var mBadgeTextFontMetrics: Paint.FontMetrics
    protected lateinit var mBadgeCenter: PointF
    protected lateinit var mDragCenter: PointF
    protected lateinit var mRowBadgeCenter: PointF
    protected lateinit var mControlPoint: PointF
    protected lateinit var mInnerTangentPoints: MutableList<PointF>
    protected lateinit var mTargetView: View
    protected lateinit var mBadgeTextPaint: TextPaint
    protected lateinit var mBadgeBackgroundPaint: Paint
    protected lateinit var mBadgeBackgroundBorderPaint: Paint

    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mBadgeTextRect = RectF()
        mBadgeBackgroundRect = RectF()
        mDragPath = Path()
        mBadgeCenter = PointF()
        mDragCenter = PointF()
        mRowBadgeCenter = PointF()
        mControlPoint = PointF()
        mInnerTangentPoints = ArrayList()
        mBadgeTextPaint = TextPaint()
        mBadgeTextPaint.isAntiAlias = true
        mBadgeTextPaint.isSubpixelText = true
        mBadgeTextPaint.isFakeBoldText = true
        mBadgeTextPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        mBadgeBackgroundPaint = Paint()
        mBadgeBackgroundPaint.isAntiAlias = true
        mBadgeBackgroundPaint.style = Paint.Style.FILL
        mBadgeBackgroundBorderPaint = Paint()
        mBadgeBackgroundBorderPaint.isAntiAlias = true
        mBadgeBackgroundBorderPaint.style = Paint.Style.STROKE
        mColorBackground = -0x17b1c0
        mColorBadgeText = -0x1
        mBadgeTextSize = dp2px(11f).toFloat()
        mBadgePadding = dp2px(5f).toFloat()
        mBadgeCount = 0
        mBadgeGravity = Gravity.END or Gravity.TOP
        mGravityOffsetX = dp2px(1f).toFloat()
        mGravityOffsetY = dp2px(1f).toFloat()
        mFinalDragDistance = dp2px(90f).toFloat()
        mShowShadow = true
        mDrawableBackgroundClip = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            translationZ = 1000f
        }
    }

    override fun bindTargetView(targetView: View): Badge {
        if (targetView == null) {
            throw IllegalStateException("targetView can not be null")
        }
        if (parent != null) {
            (parent as ViewGroup).removeView(this)
        }
        val targetParent = targetView.parent
        if (targetParent != null && targetParent is ViewGroup) {
            mTargetView = targetView
            if (targetParent is BadgeContainer) {
                targetParent.addView(this)
            } else {
                val targetContainer = targetParent as ViewGroup
                val index = targetContainer!!.indexOfChild(targetView)
                val targetParams = targetView.layoutParams
                targetContainer!!.removeView(targetView)
                val badgeContainer = BadgeContainer(context)
                if (targetContainer is RelativeLayout) {
                    badgeContainer.id = targetView.id
                }
                targetContainer!!.addView(badgeContainer, index, targetParams)
                badgeContainer.addView(targetView)
                badgeContainer.addView(this)
            }
        } else {
            throw IllegalStateException("targetView must have a parent")
        }
        return this
    }

    override fun getTargetView(): View {
        return mTargetView
    }

    /**
     * @param badgeCount equal to zero badge will be hidden, less than zero show dot
     */
    override fun setBadgeCount(badgeCount: Int): Badge {
        mBadgeCount = badgeCount
        if (mBadgeCount < 0) {
            mBadgeText = ""
        } else if (mBadgeCount > 99) {
            mBadgeText = if (mExact) (mBadgeCount).toString() else "99+"
        } else if (mBadgeCount in 1..99) {
            mBadgeText = (mBadgeCount).toString()
        } else if (mBadgeCount == 0) {
            mBadgeText = null
        }
        measureText()
        invalidate()
        return this
    }

    override fun getBadgeCount(): Int {
        return mBadgeCount
    }

    override fun setBadgeText(badgeText: String): Badge {
        mBadgeText = badgeText
        mBadgeCount = 1
        measureText()
        invalidate()
        return this
    }

    override fun getBadgeText(): String? {
        return mBadgeText
    }

    override fun setExactMode(isExact: Boolean): Badge {
        mExact = isExact
        if (mBadgeCount > 99) {
            setBadgeCount(mBadgeCount)
        }
        return this
    }

    override fun isExactMode(): Boolean {
        return mExact
    }

    override fun setShowShadow(showShadow: Boolean): Badge {
        mShowShadow = showShadow
        invalidate()
        return this
    }

    override fun isShowShadow(): Boolean {
        return mShowShadow
    }

    override fun setBadgeBackgroundColor(bgColor: Int): Badge {
        mColorBackground = bgColor
        if (mColorBackground == Color.TRANSPARENT) {
            mBadgeTextPaint.xfermode = null
        } else {
            mBadgeTextPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }
        invalidate()
        return this
    }

    override fun getBadgeBackgroundColor(): Int {
        return mColorBackground
    }

    override fun setBadgeBackground(drawable: Drawable): Badge {
        return setBadgeBackground(drawable, false)
    }

    override fun setBadgeBackground(drawable: Drawable, clip: Boolean): Badge {
        mDrawableBackgroundClip = clip
        mDrawableBackground = drawable
        createClipLayer()
        invalidate()
        return this
    }

    override fun getBadgeBackground(): Drawable? {
        return mDrawableBackground
    }

    override fun setBadgeTextColor(textColor: Int): Badge {
        mColorBadgeText = textColor
        invalidate()
        return this
    }

    override fun getBadgeTextColor(): Int {
        return mColorBadgeText
    }

    override fun setBadgeTextSize(textSize: Float, isSpValue: Boolean): Badge {
        mBadgeTextSize = if (isSpValue) dp2px(textSize).toFloat() else textSize
        measureText()
        invalidate()
        return this
    }

    override fun getBadgeTextSize(isSpValue: Boolean): Float {
        return if (isSpValue) px2dp(mBadgeTextSize).toFloat() else mBadgeTextSize
    }

    override fun setBadgePadding(padding: Float, isDpValue: Boolean): Badge {
        mBadgePadding = if (isDpValue) dp2px(padding).toFloat() else padding
        createClipLayer()
        invalidate()
        return this
    }

    override fun getBadgePadding(isDpValue: Boolean): Float {
        return if (isDpValue) px2dp(mBadgePadding).toFloat() else mBadgePadding
    }

    override fun isDraggable(): Boolean {
        return mDraggable
    }

    /**
     * @param gravity only support Gravity.START | Gravity.TOP , Gravity.END | Gravity.TOP ,
     * Gravity.START | Gravity.BOTTOM , Gravity.END | Gravity.BOTTOM ,
     * Gravity.CENTER , Gravity.CENTER | Gravity.TOP , Gravity.CENTER | Gravity.BOTTOM ,
     * Gravity.CENTER | Gravity.START , Gravity.CENTER | Gravity.END
     */
    override fun setBadgeGravity(gravity: Int): Badge {
        if ((gravity == (Gravity.START or Gravity.TOP) ||
                    gravity == (Gravity.END or Gravity.TOP) ||
                    gravity == (Gravity.START or Gravity.BOTTOM) ||
                    gravity == (Gravity.END or Gravity.BOTTOM) ||
                    gravity == (Gravity.CENTER) ||
                    gravity == (Gravity.CENTER or Gravity.TOP) ||
                    gravity == (Gravity.CENTER or Gravity.BOTTOM) ||
                    gravity == (Gravity.CENTER or Gravity.START) ||
                    gravity == (Gravity.CENTER or Gravity.END))
        ) {
            mBadgeGravity = gravity
            invalidate()
        } else {
            throw IllegalStateException(
                ("only support Gravity.START | Gravity.TOP , Gravity.END | Gravity.TOP , " +
                        "Gravity.START | Gravity.BOTTOM , Gravity.END | Gravity.BOTTOM , Gravity.CENTER" +
                        " , Gravity.CENTER | Gravity.TOP , Gravity.CENTER | Gravity.BOTTOM ," +
                        "Gravity.CENTER | Gravity.START , Gravity.CENTER | Gravity.END")
            )
        }
        return this
    }

    override fun getBadgeGravity(): Int {
        return mBadgeGravity
    }

    override fun setGravityOffset(offset: Float, isDpValue: Boolean): Badge {
        return setGravityOffset(offset, offset, isDpValue)
    }

    override fun setGravityOffset(offsetX: Float, offsetY: Float, isDpValue: Boolean): Badge {
        mGravityOffsetX = if (isDpValue) dp2px(offsetX).toFloat() else offsetX
        mGravityOffsetY = if (isDpValue) dp2px(offsetY).toFloat() else offsetY
        invalidate()
        return this
    }

    override fun getGravityOffsetX(isDpValue: Boolean): Float {
        return if (isDpValue) px2dp(mGravityOffsetX).toFloat() else mGravityOffsetX
    }

    override fun getGravityOffsetY(isDpValue: Boolean): Float {
        return if (isDpValue) px2dp(mGravityOffsetY).toFloat() else mGravityOffsetY
    }

    override fun hide(animate: Boolean) {
        if (animate && mActivityRoot != null) {
            initRowBadgeCenter()
            animateHide(mRowBadgeCenter)
        } else {
            setBadgeCount(0)
        }
    }

    override fun stroke(color: Int, width: Float, isDpValue: Boolean): Badge {
        mColorBackgroundBorder = color
        mBackgroundBorderWidth = if (isDpValue) dp2px(width).toFloat() else width
        invalidate()
        return this
    }

    override fun getDragCenter(): PointF? {
        return if (mDraggable && mDragging) mDragCenter else null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mActivityRoot == null) findViewRoot(mTargetView)
    }

    private fun findViewRoot(view: View) {
        mActivityRoot = view.rootView as ViewGroup
        if (mActivityRoot == null) {
            findActivityRoot(view)
        }
    }

    private fun findActivityRoot(view: View) {
        if (view.parent != null && view.parent is View) {
            findActivityRoot(view.parent as View)
        } else if (view is ViewGroup) {
            mActivityRoot = view
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val x = event.x
                val y = event.y
                if (mDraggable && event.getPointerId(event.actionIndex) == 0
                    && x > mBadgeBackgroundRect.left && x < mBadgeBackgroundRect.right &&
                    y > mBadgeBackgroundRect.top && y < mBadgeBackgroundRect.bottom
                    && mBadgeText != null
                ) {
                    initRowBadgeCenter()
                    mDragging = true
                    updataListener(DragState.STATE_START)
                    mDefalutRadius = dp2px(7f).toFloat()
                    parent.requestDisallowInterceptTouchEvent(true)
                    screenFromWindow(true)
                    mDragCenter.x = event.rawX
                    mDragCenter.y = event.rawY
                }
            }
            MotionEvent.ACTION_MOVE -> if (mDragging) {
                mDragCenter.x = event.rawX
                mDragCenter.y = event.rawY
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> if (event.getPointerId(
                    event.actionIndex
                ) == 0 && mDragging
            ) {
                mDragging = false
                onPointerUp()
            }
        }
        return mDragging || super.onTouchEvent(event)
    }

    private fun onPointerUp() {
        if (mDragOutOfRange) {
            animateHide(mDragCenter)
            updataListener(DragState.STATE_SUCCEED)
        } else {
            reset()
            updataListener(DragState.STATE_CANCELED)
        }
    }

    protected fun createBadgeBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            mBadgeBackgroundRect.width().toInt() + dp2px(3f),
            mBadgeBackgroundRect.height().toInt() + dp2px(3f), Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawBadge(canvas, PointF(canvas.width / 2f, canvas.height / 2f), getBadgeCircleRadius())
        return bitmap
    }

    protected fun screenFromWindow(screen: Boolean) {
        if (parent != null) {
            (parent as ViewGroup).removeView(this)
        }
        if (screen) {
            mActivityRoot!!.addView(
                this, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        } else {
            bindTargetView(mTargetView)
        }
    }

    private fun showShadowImpl(showShadow: Boolean) {
        var x = dp2px(1f)
        var y = dp2px(1.5f)
        when (mDragQuadrant) {
            1 -> {
                x = dp2px(1f)
                y = dp2px(-1.5f)
            }
            2 -> {
                x = dp2px(-1f)
                y = dp2px(-1.5f)
            }
            3 -> {
                x = dp2px(-1f)
                y = dp2px(1.5f)
            }
            4 -> {
                x = dp2px(1f)
                y = dp2px(1.5f)
            }
        }
        mBadgeBackgroundPaint.setShadowLayer(
            if (showShadow)
                dp2px(2f).toFloat()
            else
                0f, x.toFloat(), y.toFloat(), 0x33000000
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
    }

    override fun onDraw(canvas: Canvas) {
        if (mAnimator != null && mAnimator!!.isRunning) {
            mAnimator!!.draw(canvas)
            return
        }
        if (mBadgeText != null) {
            initPaints()
            val badgeRadius = getBadgeCircleRadius()
            val startCircleRadius =
                mDefalutRadius * (1 - MathUtil.getPointDistance(
                    mRowBadgeCenter,
                    mDragCenter
                ) / mFinalDragDistance)
            if (mDraggable && mDragging) {
                mDragQuadrant = MathUtil.getQuadrant(mDragCenter, mRowBadgeCenter)
                showShadowImpl(mShowShadow)
                mDragOutOfRange = startCircleRadius < dp2px(1.5f)
                if (mDragOutOfRange) {
                    updataListener(DragState.STATE_DRAGGING_OUT_OF_RANGE)
                    drawBadge(canvas, mDragCenter, badgeRadius)
                } else {
                    updataListener(DragState.STATE_DRAGGING)
                    drawDragging(canvas, startCircleRadius, badgeRadius)
                    drawBadge(canvas, mDragCenter, badgeRadius)
                }
            } else {
                findBadgeCenter()
                drawBadge(canvas, mBadgeCenter, badgeRadius)
            }
        }
    }

    private fun initPaints() {
        showShadowImpl(mShowShadow)
        mBadgeBackgroundPaint.color = mColorBackground
        mBadgeBackgroundBorderPaint.color = mColorBackgroundBorder
        mBadgeBackgroundBorderPaint.strokeWidth = mBackgroundBorderWidth
        mBadgeTextPaint.color = mColorBadgeText
        mBadgeTextPaint.textAlign = Paint.Align.CENTER
    }

    private fun drawDragging(canvas: Canvas, startRadius: Float, badgeRadius: Float) {
        val dy = mDragCenter.y - mRowBadgeCenter.y
        val dx = mDragCenter.x - mRowBadgeCenter.x
        mInnerTangentPoints.clear()
        if (dx != 0f) {
            val k1 = (dy / dx).toDouble()
            val k2 = -1 / k1
            MathUtil.getInnertangentPoints(mDragCenter, badgeRadius, k2, mInnerTangentPoints)
            MathUtil.getInnertangentPoints(mRowBadgeCenter, startRadius, k2, mInnerTangentPoints)
        } else {
            MathUtil.getInnertangentPoints(mDragCenter, badgeRadius, 0.0, mInnerTangentPoints)
            MathUtil.getInnertangentPoints(mRowBadgeCenter, startRadius, 0.0, mInnerTangentPoints)
        }
        mDragPath.reset()
        mDragPath.addCircle(
            mRowBadgeCenter.x, mRowBadgeCenter.y, startRadius,
            if (mDragQuadrant == 1 || mDragQuadrant == 2) Path.Direction.CCW else Path.Direction.CW
        )
        mControlPoint.x = (mRowBadgeCenter.x + mDragCenter.x) / 2.0f
        mControlPoint.y = (mRowBadgeCenter.y + mDragCenter.y) / 2.0f
        mDragPath.moveTo(mInnerTangentPoints[2].x, mInnerTangentPoints[2].y)
        mDragPath.quadTo(
            mControlPoint.x,
            mControlPoint.y,
            mInnerTangentPoints[0].x,
            mInnerTangentPoints[0].y
        )
        mDragPath.lineTo(mInnerTangentPoints[1].x, mInnerTangentPoints[1].y)
        mDragPath.quadTo(
            mControlPoint.x,
            mControlPoint.y,
            mInnerTangentPoints[3].x,
            mInnerTangentPoints[3].y
        )
        mDragPath.lineTo(mInnerTangentPoints[2].x, mInnerTangentPoints[2].y)
        mDragPath.close()
        canvas.drawPath(mDragPath, mBadgeBackgroundPaint)

        //draw dragging border
        if (mColorBackgroundBorder != 0 && mBackgroundBorderWidth > 0) {
            mDragPath.reset()
            mDragPath.moveTo(mInnerTangentPoints[2].x, mInnerTangentPoints[2].y)
            mDragPath.quadTo(
                mControlPoint.x,
                mControlPoint.y,
                mInnerTangentPoints[0].x,
                mInnerTangentPoints[0].y
            )
            mDragPath.moveTo(mInnerTangentPoints[1].x, mInnerTangentPoints[1].y)
            mDragPath.quadTo(
                mControlPoint.x,
                mControlPoint.y,
                mInnerTangentPoints[3].x,
                mInnerTangentPoints[3].y
            )
            val startY: Float
            val startX: Float
            if (mDragQuadrant == 1 || mDragQuadrant == 2) {
                startX = mInnerTangentPoints[2].x - mRowBadgeCenter.x
                startY = mRowBadgeCenter.y - mInnerTangentPoints[2].y
            } else {
                startX = mInnerTangentPoints[3].x - mRowBadgeCenter.x
                startY = mRowBadgeCenter.y - mInnerTangentPoints[3].y
            }
            val startAngle = 360 - MathUtil.radianToAngle(
                MathUtil.getTanRadian(
                    Math.atan((startY / startX).toDouble()),
                    if (mDragQuadrant - 1 == 0) 4 else mDragQuadrant - 1
                )
            ).toFloat()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mDragPath.addArc(
                    mRowBadgeCenter.x - startRadius, mRowBadgeCenter.y - startRadius,
                    mRowBadgeCenter.x + startRadius, mRowBadgeCenter.y + startRadius, startAngle,
                    180f
                )
            } else {
                mDragPath.addArc(
                    RectF(
                        mRowBadgeCenter.x - startRadius, mRowBadgeCenter.y - startRadius,
                        mRowBadgeCenter.x + startRadius, mRowBadgeCenter.y + startRadius
                    ), startAngle, 180f
                )
            }
            canvas.drawPath(mDragPath, mBadgeBackgroundBorderPaint)
        }
    }

    private fun drawBadge(canvas: Canvas, center: PointF, radius: Float) {
        var radius = radius
        if (center.x == -1000f && center.y == -1000f) {
            return
        }
        if (mBadgeText!!.isEmpty() || mBadgeText!!.length == 1) {
            mBadgeBackgroundRect.left = center.x - radius.toInt()
            mBadgeBackgroundRect.top = center.y - radius.toInt()
            mBadgeBackgroundRect.right = center.x + radius.toInt()
            mBadgeBackgroundRect.bottom = center.y + radius.toInt()
            if (mDrawableBackground != null) {
                drawBadgeBackground(canvas)
            } else {
                canvas.drawCircle(center.x, center.y, radius, mBadgeBackgroundPaint)
                if (mColorBackgroundBorder != 0 && mBackgroundBorderWidth > 0) {
                    canvas.drawCircle(center.x, center.y, radius, mBadgeBackgroundBorderPaint)
                }
            }
        } else {
            mBadgeBackgroundRect.left = center.x - (mBadgeTextRect.width() / 2f + mBadgePadding)
            mBadgeBackgroundRect.top =
                center.y - (mBadgeTextRect.height() / 2f + mBadgePadding * 0.5f)
            mBadgeBackgroundRect.right = center.x + (mBadgeTextRect.width() / 2f + mBadgePadding)
            mBadgeBackgroundRect.bottom =
                center.y + (mBadgeTextRect.height() / 2f + mBadgePadding * 0.5f)
            radius = mBadgeBackgroundRect.height() / 2f
            if (mDrawableBackground != null) {
                drawBadgeBackground(canvas)
            } else {
                canvas.drawRoundRect(mBadgeBackgroundRect, radius, radius, mBadgeBackgroundPaint)
                if (mColorBackgroundBorder != 0 && mBackgroundBorderWidth > 0) {
                    canvas.drawRoundRect(
                        mBadgeBackgroundRect,
                        radius,
                        radius,
                        mBadgeBackgroundBorderPaint
                    )
                }
            }
        }
        if (!mBadgeText!!.isEmpty()) {
            canvas.drawText(
                mBadgeText!!, center.x,
                (mBadgeBackgroundRect.bottom + mBadgeBackgroundRect.top
                        - mBadgeTextFontMetrics.bottom - mBadgeTextFontMetrics.top) / 2f,
                mBadgeTextPaint
            )
        }
    }

    private fun drawBadgeBackground(canvas: Canvas) {
        mBadgeBackgroundPaint.setShadowLayer(0f, 0f, 0f, 0)
        val left = mBadgeBackgroundRect.left.toInt()
        val top = mBadgeBackgroundRect.top.toInt()
        var right = mBadgeBackgroundRect.right.toInt()
        var bottom = mBadgeBackgroundRect.bottom.toInt()
        if (mDrawableBackgroundClip) {
            right = left + mBitmapClip!!.width
            bottom = top + mBitmapClip!!.height
            canvas.saveLayer(
                left.toFloat(),
                top.toFloat(),
                right.toFloat(),
                bottom.toFloat(),
                null,
                Canvas.ALL_SAVE_FLAG
            )
        }
        mDrawableBackground!!.setBounds(left, top, right, bottom)
        mDrawableBackground!!.draw(canvas)
        if (mDrawableBackgroundClip) {
            mBadgeBackgroundPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            canvas.drawBitmap(mBitmapClip!!, left.toFloat(), top.toFloat(), mBadgeBackgroundPaint)
            canvas.restore()
            mBadgeBackgroundPaint.xfermode = null
            if (mBadgeText!!.isEmpty() || mBadgeText!!.length == 1) {
                canvas.drawCircle(
                    mBadgeBackgroundRect.centerX(), mBadgeBackgroundRect.centerY(),
                    mBadgeBackgroundRect.width() / 2f, mBadgeBackgroundBorderPaint
                )
            } else {
                canvas.drawRoundRect(
                    mBadgeBackgroundRect,
                    mBadgeBackgroundRect.height() / 2, mBadgeBackgroundRect.height() / 2,
                    mBadgeBackgroundBorderPaint
                )
            }
        } else {
            canvas.drawRect(mBadgeBackgroundRect, mBadgeBackgroundBorderPaint)
        }
    }

    private fun createClipLayer() {
        if (mBadgeText == null) {
            return
        }
        if (!mDrawableBackgroundClip) {
            return
        }
        if (mBitmapClip != null && !mBitmapClip!!.isRecycled) {
            mBitmapClip!!.recycle()
        }
        val radius = getBadgeCircleRadius()
        if (mBadgeText!!.isEmpty() || mBadgeText!!.length == 1) {
            mBitmapClip = Bitmap.createBitmap(
                radius.toInt() * 2, radius.toInt() * 2,
                Bitmap.Config.ARGB_4444
            )
            val srcCanvas = Canvas(mBitmapClip!!)
            srcCanvas.drawCircle(
                srcCanvas.width / 2f, srcCanvas.height / 2f,
                srcCanvas.width / 2f, mBadgeBackgroundPaint
            )
        } else {
            mBitmapClip = Bitmap.createBitmap(
                (mBadgeTextRect.width() + mBadgePadding * 2).toInt(),
                (mBadgeTextRect.height() + mBadgePadding).toInt(), Bitmap.Config.ARGB_4444
            )
            val srcCanvas = Canvas(mBitmapClip!!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                srcCanvas.drawRoundRect(
                    0f,
                    0f,
                    srcCanvas.width.toFloat(),
                    srcCanvas.height.toFloat(),
                    srcCanvas.height / 2f,
                    srcCanvas.height / 2f,
                    mBadgeBackgroundPaint
                )
            } else {
                srcCanvas.drawRoundRect(
                    RectF(0f, 0f, srcCanvas.width.toFloat(), srcCanvas.height.toFloat()),
                    srcCanvas.height / 2f, srcCanvas.height / 2f, mBadgeBackgroundPaint
                )
            }
        }
    }

    private fun getBadgeCircleRadius(): Float {
        return if (mBadgeText!!.isEmpty()) {
            mBadgePadding
        } else if (mBadgeText!!.length == 1) {
            if (mBadgeTextRect.height() > mBadgeTextRect.width())
                mBadgeTextRect.height() / 2f + mBadgePadding * 0.5f
            else
                mBadgeTextRect.width() / 2f + mBadgePadding * 0.5f
        } else {
            mBadgeBackgroundRect.height() / 2f
        }
    }

    private fun findBadgeCenter() {
        val rectWidth = if (mBadgeTextRect.height() > mBadgeTextRect.width())
            mBadgeTextRect.height()
        else
            mBadgeTextRect.width()
        when (mBadgeGravity) {
            Gravity.START or Gravity.TOP -> {
                mBadgeCenter.x = mGravityOffsetX + mBadgePadding + rectWidth / 2f
                mBadgeCenter.y = mGravityOffsetY + mBadgePadding + mBadgeTextRect.height() / 2f
            }
            Gravity.START or Gravity.BOTTOM -> {
                mBadgeCenter.x = mGravityOffsetX + mBadgePadding + rectWidth / 2f
                mBadgeCenter.y =
                    mHeight - (mGravityOffsetY + mBadgePadding + mBadgeTextRect.height() / 2f)
            }
            Gravity.END or Gravity.TOP -> {
                mBadgeCenter.x = mWidth - (mGravityOffsetX + mBadgePadding + rectWidth / 2f)
                mBadgeCenter.y = mGravityOffsetY + mBadgePadding + mBadgeTextRect.height() / 2f
            }
            Gravity.END or Gravity.BOTTOM -> {
                mBadgeCenter.x = mWidth - (mGravityOffsetX + mBadgePadding + rectWidth / 2f)
                mBadgeCenter.y =
                    mHeight - (mGravityOffsetY + mBadgePadding + mBadgeTextRect.height() / 2f)
            }
            Gravity.CENTER -> {
                mBadgeCenter.x = mWidth / 2f
                mBadgeCenter.y = mHeight / 2f
            }
            Gravity.CENTER or Gravity.TOP -> {
                mBadgeCenter.x = mWidth / 2f
                mBadgeCenter.y = mGravityOffsetY + mBadgePadding + mBadgeTextRect.height() / 2f
            }
            Gravity.CENTER or Gravity.BOTTOM -> {
                mBadgeCenter.x = mWidth / 2f
                mBadgeCenter.y =
                    mHeight - (mGravityOffsetY + mBadgePadding + mBadgeTextRect.height() / 2f)
            }
            Gravity.CENTER or Gravity.START -> {
                mBadgeCenter.x = mGravityOffsetX + mBadgePadding + rectWidth / 2f
                mBadgeCenter.y = mHeight / 2f
            }
            Gravity.CENTER or Gravity.END -> {
                mBadgeCenter.x = mWidth - (mGravityOffsetX + mBadgePadding + rectWidth / 2f)
                mBadgeCenter.y = mHeight / 2f
            }
        }
        initRowBadgeCenter()
    }

    private fun measureText() {
        mBadgeTextRect.left = 0f
        mBadgeTextRect.top = 0f
        if (TextUtils.isEmpty(mBadgeText)) {
            mBadgeTextRect.right = 0f
            mBadgeTextRect.bottom = 0f
        } else {
            mBadgeTextPaint.textSize = mBadgeTextSize
            mBadgeTextRect.right = mBadgeTextPaint.measureText(mBadgeText)
            mBadgeTextFontMetrics = mBadgeTextPaint.fontMetrics
            mBadgeTextRect.bottom = mBadgeTextFontMetrics.descent - mBadgeTextFontMetrics.ascent
        }
        createClipLayer()
    }

    private fun initRowBadgeCenter() {
        val screenPoint = IntArray(2)
        getLocationOnScreen(screenPoint)
        mRowBadgeCenter.x = mBadgeCenter.x + screenPoint[0]
        mRowBadgeCenter.y = mBadgeCenter.y + screenPoint[1]
    }

    protected fun animateHide(center: PointF) {
        if (mBadgeText == null) {
            return
        }
        if (mAnimator == null || !mAnimator!!.isRunning) {
            screenFromWindow(true)
            mAnimator = BadgeAnimator(createBadgeBitmap(), center, this)
            mAnimator!!.start()
            setBadgeCount(0)
        }
    }

    fun reset() {
        mDragCenter.x = -1000f
        mDragCenter.y = -1000f
        mDragQuadrant = 4
        screenFromWindow(false)
        parent.requestDisallowInterceptTouchEvent(false)
        invalidate()
    }

    private fun updataListener(state: DragState) {
        mDragStateChangedListener?.onDragStateChanged(state, this, mTargetView)
    }

    override fun setOnDragStateChangedListener(listener: Badge.OnDragStateChangedListener): Badge {
        mDraggable = listener != null
        mDragStateChangedListener = listener
        return this
    }

    private inner class BadgeContainer(context: Context) : ViewGroup(context) {

        override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
            if (parent !is RelativeLayout) {
                super.dispatchRestoreInstanceState(container)
            }
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.layout(0, 0, child.measuredWidth, child.measuredHeight)
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            var targetView: View? = null
            var badgeView: View? = null
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child !is BadgeView) {
                    targetView = child
                } else {
                    badgeView = child
                }
            }
            if (targetView == null) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            } else {
                targetView.measure(widthMeasureSpec, heightMeasureSpec)
                if (badgeView != null) {
                    badgeView!!.measure(
                        View.MeasureSpec.makeMeasureSpec(
                            targetView.measuredWidth,
                            View.MeasureSpec.EXACTLY
                        ),
                        View.MeasureSpec.makeMeasureSpec(
                            targetView.measuredHeight,
                            View.MeasureSpec.EXACTLY
                        )
                    )
                }
                setMeasuredDimension(targetView.measuredWidth, targetView.measuredHeight)
            }
        }
    }

    private fun dp2px(dp: Float): Int {
        return (resources.displayMetrics.density * dp + 0.5f).toInt()
    }

    private fun px2dp(px: Float): Int {
        return (px / resources.displayMetrics.density + 0.5f).toInt()
    }
}