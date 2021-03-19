package com.seagazer.halo

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import kotlin.math.sqrt

/**
 * A view container can play halo animation when get focused like HUAWEI TV.
 *
 * Author: Seagazer
 * Date: 2021/3/11
 */
class Halo @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val TAG = "Halo"
        const val SHAPE_RECT = 0
        const val SHAPE_ROUND_RECT = 1
        const val SHAPE_CIRCLE = 2
        const val DEFAULT_HALO_STROKE_WIDTH = 4f
        const val DEFAULT_HALO_DURATION = 8000
    }

    private var degrees = 0f
    private var shapeType = SHAPE_RECT
    private var cornerRadius = 0
    private var centerX = 0f
    private var centerY = 0f
    private var haloDuration = 0
    private lateinit var holeBitmap: Bitmap
    private lateinit var shaderBitmap: Bitmap
    private var shaderLeft = 0f
    private var shaderTop = 0f

    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
    }

    /**
     * Set insert edge between content view and halo frame.
     */
    var insertEdge = 0
        set(value) {
            field = value
            checkHaloWidth()
            setupInsertEdge()
        }

    /**
     * Set the width of halo frame.
     */
    var haloStrokeWidth = DEFAULT_HALO_STROKE_WIDTH
        set(value) {
            field = value
            checkHaloWidth()
            createHole()
        }

    /**
     * Set the color of halo frame.
     */
    var haloColor = Color.WHITE
        set(value) {
            field = value
            createHalo()
        }

    private lateinit var haloAnimator: ValueAnimator

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        // setup focus config
        isClickable = true
        isFocusable = true
        isFocusableInTouchMode = true
        descendantFocusability = FOCUS_BEFORE_DESCENDANTS
        setBackgroundColor(Color.TRANSPARENT)
        // get attr
        val attr = context.obtainStyledAttributes(attrs, R.styleable.Halo)
        haloDuration = attr.getInt(R.styleable.Halo_haloDuration, DEFAULT_HALO_DURATION)
        haloColor = attr.getInt(R.styleable.Halo_haloColor, Color.WHITE)
        shapeType = attr.getInt(R.styleable.Halo_haloShape, SHAPE_RECT)
        haloStrokeWidth = attr.getDimension(R.styleable.Halo_haloWidth, DEFAULT_HALO_STROKE_WIDTH)
        cornerRadius = attr.getDimensionPixelOffset(R.styleable.Halo_haloCornerRadius, 0)
        insertEdge = attr.getDimensionPixelOffset(R.styleable.Halo_haloInsertEdge, DEFAULT_HALO_STROKE_WIDTH.toInt())
        attr.recycle()
        // check shape
        if (shapeType == SHAPE_ROUND_RECT) {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(Rect(0, 0, width, height), cornerRadius.toFloat())
                }
            }
            clipToOutline = true
        } else if (shapeType == SHAPE_CIRCLE) {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(Rect(0, 0, width, height), width / 2f)
                }
            }
            clipToOutline = true
        }
        // setup content edge
        setupInsertEdge()
        // initAnimation
        setupAnimation()
    }

    private fun setupAnimation() {
        haloAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                val f = it.animatedValue as Float
                degrees = f * 360
                if (isFocused && isAttachedToWindow) {
                    invalidate()
                } else {
                    cancel()
                }
            }
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            duration = haloDuration.toLong()
        }
    }

    private fun setupInsertEdge() {
        if (insertEdge != 0) {
            setPadding(insertEdge, insertEdge, insertEdge, insertEdge)
        }
    }

    private fun createHalo() {
        if (width > 0 && height > 0) {
            val shaderBound = sqrt((width * width + height * height).toDouble()).toInt()
            shaderBitmap = Bitmap.createBitmap(shaderBound, shaderBound, Bitmap.Config.ARGB_8888)
            val shaderCanvas = Canvas(shaderBitmap)
            val shaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                //      0.625      0.75       0.875
                //           +++++++++++++++++
                // white  0.5+---------------+0 white
                //           +++++++++++++++++
                //      0.375      0.25       0.125
                val shader = SweepGradient(shaderBound / 2f, shaderBound / 2f,
                        intArrayOf(haloColor, Color.TRANSPARENT, Color.TRANSPARENT, haloColor, Color.TRANSPARENT, Color.TRANSPARENT, haloColor),
                        floatArrayOf(0f, 0.125f, 0.375f, 0.5f, 0.625f, 0.875f, 1f)
                )
                this.shader = shader
            }
            shaderCanvas.drawCircle(shaderBound / 2f, shaderBound / 2f, shaderBound.toFloat(), shaderPaint)
            shaderLeft = -(shaderBound - width) / 2f
            shaderTop = -(shaderBound - height) / 2f
        }
    }

    private fun createHole() {
        if (width > 0 && height > 0) {
            val holeWidth = width - haloStrokeWidth * 2
            val holeHeight = height - haloStrokeWidth * 2
            holeBitmap = Bitmap.createBitmap(holeWidth.toInt(), holeHeight.toInt(), Bitmap.Config.ARGB_8888)
            val holeCanvas = Canvas(holeBitmap)
            val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            when (shapeType) {
                SHAPE_RECT -> {
                    holeCanvas.drawRect(0f, 0f, holeWidth, holeHeight, holePaint)
                }
                SHAPE_ROUND_RECT -> {
                    holeCanvas.drawRoundRect(0f, 0f, holeWidth, holeHeight, cornerRadius.toFloat(), cornerRadius.toFloat(), holePaint)
                }
                SHAPE_CIRCLE -> {
                    holeCanvas.drawCircle(holeWidth / 2f, holeHeight / 2f, holeWidth / 2f, holePaint)
                }
            }
        }
    }

    private fun checkHaloWidth() {
        if (insertEdge != 0 && haloStrokeWidth > insertEdge) {
            throw RuntimeException("The halo width must be smaller than insertEdge, current haloWidth is $haloStrokeWidth, insertEdge is $insertEdge.")
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (shapeType == SHAPE_CIRCLE && w != h) {
            throw RuntimeException("The shape type is circle, the width must be the same as height!")
        } else if (shapeType == SHAPE_ROUND_RECT && cornerRadius == 0) {
            Log.w(TAG, "Are you sure not set the corner radius?")
        }
        centerX = w / 2f
        centerY = h / 2f
        // create shader bitmap
        createHalo()
        // create hole bitmap
        createHole()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        if (isFocused && canvas != null) {
            canvas.drawBitmap(holeBitmap, haloStrokeWidth, haloStrokeWidth, null)
            canvas.let {
                canvas.save()
                canvas.rotate(degrees, centerX, centerY)
                canvas.drawBitmap(shaderBitmap, shaderLeft, shaderTop, holePaint)
                canvas.restore()
            }
        }
        super.dispatchDraw(canvas)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) {
            haloAnimator.start()
        } else {
            haloAnimator.end()
        }
        invalidate()
    }

    override fun onDetachedFromWindow() {
        haloAnimator.end()
        haloAnimator.cancel()
        super.onDetachedFromWindow()
    }


}