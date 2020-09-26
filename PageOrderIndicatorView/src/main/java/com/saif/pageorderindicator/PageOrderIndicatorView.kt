package com.saif.pageorderindicator

import android.content.Context
import android.graphics.*
import androidx.core.view.ViewCompat
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class PageOrderIndicatorView : View
{
    // Attrs
    var colorSelectedPage = Color.GREEN
    var colorSelectedPageText = Color.DKGRAY
    var colorUnSelectedPageText = Color.WHITE
    var colorSelectedPageCircleText = Color.WHITE
    var colorUnSelectedPageCircleText = Color.DKGRAY
    var pageCount = 3
        set(value) {
            field = if (value > 0) value else 1
        }
    var currentPage = 1
        set(value)
        {
            if (value in 1..pageCount)
            {
                field = value
                pageListener?.onPageChanged(value)
            }
        }
    var circleRadiusText: Float = 0f
    var cornerRadiusPage: Float = convertDPtoPX(8).toFloat()
    var circleTextPadding: Int = convertDPtoPX(4)
    var pageElevation = convertDPtoPX(4).toFloat()
    var isPageClickable = true

    // //////////////////   /////////////
    private lateinit var pageIndicatorPaint: Paint
    private lateinit var textCirclePaint: Paint
    private lateinit var textPaint: Paint
    private var mainRect = RectF()
    private var textHeight = 0
    private var clickedPage = -1
    private val isLTR by lazy { if (isViewLTR()) 1 else -1 }
    private var pageListener: OnPageListener? = null

    constructor(context: Context?) : super(context)
    {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    {
        val attrArray = context?.theme?.obtainStyledAttributes(attrs, R.styleable.PageOrderIndicatorView, 0, 0)
        try
        {
            attrArray?.let {
                colorSelectedPage = it.getColor(R.styleable.PageOrderIndicatorView_color_selected_page
                        , Color.BLACK)
                colorSelectedPageText = it.getColor(R.styleable.PageOrderIndicatorView_color_selected_page_text
                        , Color.BLACK)
                colorUnSelectedPageText = it.getColor(R.styleable.PageOrderIndicatorView_color_unSelected_page_text
                        , Color.WHITE)
                colorSelectedPageCircleText = it.getColor(R.styleable.PageOrderIndicatorView_color_selected_page_circle_text
                        , Color.WHITE)
                colorUnSelectedPageCircleText = it.getColor(R.styleable.PageOrderIndicatorView_color_unSelected_page_circle_text
                        , Color.BLACK)
                pageCount = it.getInteger(R.styleable.PageOrderIndicatorView_page_count, 1)
                currentPage = it.getInteger(R.styleable.PageOrderIndicatorView_init_page, 1)
                pageElevation = it.getDimension(R.styleable.PageOrderIndicatorView_page_elevation
                        , convertDPtoPX(4).toFloat())
                isPageClickable = it.getBoolean(R.styleable.PageOrderIndicatorView_isPageClickable, true)
                circleTextPadding = it.getDimension(R.styleable.PageOrderIndicatorView_circle_text_padding, circleTextPadding.toFloat()).toInt()
                circleRadiusText = it.getDimension(R.styleable.PageOrderIndicatorView_circle_radius_text, circleRadiusText)
                cornerRadiusPage = it.getDimension(R.styleable.PageOrderIndicatorView_corner_radius_page, cornerRadiusPage)
            }
        }
        finally {
            attrArray?.recycle()
        }

        init()
    }

    private fun init()
    {
        pageIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSelectedPage
            style = Paint.Style.FILL
            setShadowLayer(pageElevation, pageElevation * isLTR
                    , 0F, Color.GRAY)
            setLayerType(LAYER_TYPE_SOFTWARE, this)
        }

        textCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorUnSelectedPageCircleText
            style = Paint.Style.FILL
        }

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorUnSelectedPageText
            style = Paint.Style.FILL
            textSize = convertSPToPX(12F)
            textAlign = Paint.Align.CENTER
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        val desiredWidth = convertDPtoPX(200) + paddingStart + paddingEnd
        val desiredHeight = convertDPtoPX(80) + paddingStart + paddingEnd

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = if (widthMode == MeasureSpec.EXACTLY)
            widthSize
        else if (widthMode == MeasureSpec.AT_MOST)
            Math.min(widthSize, desiredWidth)
        else
            desiredWidth

        val height = if (heightMode == MeasureSpec.EXACTLY)
            heightSize
        else if (heightMode == MeasureSpec.AT_MOST)
            Math.min(heightSize, desiredHeight)
        else
            desiredHeight

        log("height= $height")

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
    {
        super.onSizeChanged(w, h, oldw, oldh)

        mainRect.top = paddingTop.toFloat() //+ pageElevation
        mainRect.bottom = height - paddingBottom.toFloat() - pageElevation

        val rectText = Rect()
        textPaint.getTextBounds("$pageCount", 0, "$pageCount".length, rectText)
        textHeight = rectText.height()
        circleRadiusText = (textHeight + circleTextPadding).toFloat()
    }

    override fun onDraw(canvas: Canvas?)
    {
        var lastXCor = getEndPoint()
        log("lastXCor=  $lastXCor")
        log("getWidthOfPageIndicator=  ${getWidthOfPageIndicator()}")

        for ( i in (pageCount-1) downTo 0)
        {
            log("i=  $i")

            val endPoint = lastXCor
            val startPoint = lastXCor - (getWidthOfPageIndicator() * isLTR)

            log("endPoint=  $endPoint")
            log("startPoint=  $startPoint")

            if (i <= currentPage-1)
            {
                mainRect.right = endPoint
                mainRect.left = startPoint - (if (i == 0) 0F else cornerRadiusPage * 1.1F * isLTR)
                canvas?.drawRoundRect(mainRect, cornerRadiusPage, cornerRadiusPage, pageIndicatorPaint)

                textCirclePaint.color = colorSelectedPageCircleText
                textPaint.color = colorSelectedPageText
            }
            else
            {
                textCirclePaint.color = colorUnSelectedPageCircleText
                textPaint.color = colorUnSelectedPageText
            }

            canvas?.drawCircle(endPoint - ((getWidthOfPageIndicator()/2) * isLTR)
                    , height/2F, circleRadiusText, textCirclePaint)
            canvas?.drawText("${i+1}"
                    , endPoint - ((getWidthOfPageIndicator()/2) * isLTR)
                    , (height/2F)+(textHeight/2), textPaint)

            lastXCor = startPoint
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean
    {
        if (event == null || !isPageClickable) return true

        val xCor = event.x
        log("xCor= $xCor")
        log("width= $width")

        when(event.action)
        {
            MotionEvent.ACTION_DOWN ->
            {
                var lastXCor = getEndPoint()
                for ( i in pageCount downTo 1)
                {
                    log("onTouchEvent,  i= $i")
                    val endPoint = lastXCor
                    val startPoint = lastXCor - (getWidthOfPageIndicator() * isLTR)

                    if (xCor in startPoint..endPoint || xCor in endPoint..startPoint)
                    {
                        clickedPage = i
                        log("onTouchEvent,  clickedPage= $clickedPage")
                        return true
                    }

                    lastXCor = startPoint
                }
            }
            MotionEvent.ACTION_UP ->
            {
                if (clickedPage != -1)
                {
                    if (pageListener == null || (pageListener != null && pageListener?.onPageClicked(clickedPage)!!))
                    {
                        currentPage = clickedPage
                        invalidate()
                    }
                }
                clickedPage = -1
            }
        }

        return true
    }

    fun setOnPageChanged(pageListener: OnPageListener)
    {
        this.pageListener = pageListener
    }

    fun nextPage()
    {
        if (currentPage+1 > pageCount)
            return

        currentPage++
        invalidate()
    }

    fun previousPage()
    {
        if (currentPage-1 < 1)
            return

        currentPage--
        invalidate()
    }

    fun goToPage(page: Int)
    {
        if (currentPage !in 1..pageCount)
            return

        currentPage = page
        invalidate()
    }

    private fun getWidthOfPageIndicator() = widthAfter / pageCount.toFloat()

    private val widthAfter: Int get() = width - paddingStart - paddingEnd - (pageElevation.toInt()*2)

    private fun getStartPoint(): Float = if (isViewLTR())
        getLeftPoint()
    else
        getRightPoint()

    private fun getEndPoint(): Float = if (isViewLTR())
        getRightPoint() - (pageElevation*2)
    else
        getLeftPoint() + (pageElevation*2)


    private fun getLeftPoint() = paddingStart.toFloat()

    private fun getRightPoint() = width - paddingEnd.toFloat()


    /** @return true if the View is Left to Right */
    private fun isViewLTR() = resources.configuration.layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR

    private fun convertDPtoPX(dp: Int): Int {
        return dp * (resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }

    private fun convertSPToPX(sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
    }

    private fun log(data: String) = Log.d("saif", data)

    private fun log(data: String, e: Throwable) = Log.e("saif", data, e)


    interface OnPageListener
    {
        /** @param position start from ( 1 -> pageCount ) */
        fun onPageChanged(position: Int)
        /** @param position start from ( 1 -> pageCount )
         * @return true to go to clicked Page*/
        fun onPageClicked(position: Int): Boolean
    }


}