package com.vip.edit

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText

/**
 *AUTHOR:AbnerMing
 *DATE:2023/3/14
 *INTRODUCE:输入框
 */
class InputBoxView : AppCompatEditText {

    private var mInputCanvasStyle = 0//绘制画笔类型，空心还是实心

    private var mInputCanvasType = 0//绘制类型，默认是下滑线

    private var mBackGroundColor = Color.GRAY//背景颜色

    private var mRadius = 5f//圆角，默认为5

    private var mLineHeight = 2f//下划线的高度

    private var mLength: Int = 6//输入框的个数，默认是6个

    private var mIsAndroidKeyBoard = true//是否启用原生的软键盘

    private var mPaint: Paint? = null//画笔

    private var mCursorPaint: Paint? = null//光标画笔

    private var mRectWidth: Float = 0f//矩形的边框

    private var mTextType: Int = 0//输入框类型

    private var mSpacing: Float = 20f//间距

    private var mTextColor = Color.BLACK//内容颜色

    private var mIsCursor = true//是否有光标

    private var mCursorDirection = true//光标方向,默认纵向

    private var mCursorTwinkle = true//光标是否闪烁，默认闪烁

    private var mCursorColor = Color.BLACK//光标的颜色

    private var mCursorWidth = 2f//光标的宽度

    private var mCursorSpacing = 20f//光标的间距

    private var mTextSize = 28f//文字大小

    private var mIsAttachedToWindows: Boolean = false

    private var mSelectBackGroundColor = 0//选中的边框颜色

    private var mCursorMarginBottom = 10f//光标距离底部的距离

    //动画
    private val cursorAnim: ValueAnimator = ValueAnimator.ofInt(0, 2).apply {
        duration = 1000
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
    }

    constructor(
        context: Context
    ) : super(context) {
        initData(context)
    }


    @SuppressLint("Recycle")
    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        context.obtainStyledAttributes(attrs, R.styleable.InputBoxView)
            .apply {
                //绘制类型,目前有三种，线，矩形，圆角
                mInputCanvasType = getInt(R.styleable.InputBoxView_input_canvas_type, 0)
                //绘制画笔类型，空心还是实心
                mInputCanvasStyle = getInt(R.styleable.InputBoxView_input_canvas_style, 0)
                //输入框的背景颜色
                mBackGroundColor =
                    getColor(R.styleable.InputBoxView_input_background, mBackGroundColor)
                //输入框的选中背景颜色
                mSelectBackGroundColor =
                    getColor(
                        R.styleable.InputBoxView_input_select_background,
                        mSelectBackGroundColor
                    )
                //输入框的角度
                mRadius = getDimension(R.styleable.InputBoxView_input_radius, mRadius)
                //下划线的高度
                mLineHeight = getDimension(R.styleable.InputBoxView_input_line_height, mLineHeight)
                //输入框的长度
                mLength = getInt(R.styleable.InputBoxView_input_length, mLength)
                //输入框的间距
                mSpacing = getDimension(R.styleable.InputBoxView_input_spacing, mSpacing)
                //输入框的文字颜色
                mTextColor = getColor(R.styleable.InputBoxView_input_text_color, Color.BLACK)
                //文字大小
                mTextSize = getDimension(R.styleable.InputBoxView_input_text_size, mTextSize)
                //输入框类型
                mTextType = getInt(R.styleable.InputBoxView_input_text_type, mTextType)
                //输入是否有光标
                mIsCursor =
                    getBoolean(R.styleable.InputBoxView_input_is_cursor, true)
                //光标方向
                mCursorDirection =
                    getBoolean(R.styleable.InputBoxView_input_cursor_direction, true)
                //光标的宽度
                mCursorWidth =
                    getDimension(R.styleable.InputBoxView_input_cursor_width, mCursorWidth)
                //光标的压颜色
                mCursorColor = getColor(R.styleable.InputBoxView_input_cursor_color, Color.BLACK)
                //光标的间距
                mCursorSpacing =
                    getDimension(R.styleable.InputBoxView_input_cursor_spacing, mCursorSpacing)
                //输入框的光标是否闪烁
                mCursorTwinkle =
                    getBoolean(R.styleable.InputBoxView_input_cursor_is_twinkle, true)
                //输入框是否弹起原生的软件盘
                mIsAndroidKeyBoard =
                    getBoolean(R.styleable.InputBoxView_input_is_android_keyboard, true)
                //横向的光标距离底部的距离
                mCursorMarginBottom =
                    getDimension(R.styleable.InputBoxView_input_cursor_spacing, mCursorSpacing)
            }

        initData(context)
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:初始化
     */
    private fun initData(context: Context) {
        setBackgroundColor(Color.TRANSPARENT)//设置背景颜色为透明
        filters = arrayOf<InputFilter>(LengthFilter(mLength))//设置最大输入
        isFocusable = false//默认情况下不获取焦点
        setTextColor(Color.TRANSPARENT)//设置颜色透明
        isCursorVisible = false//设置无光标

        mPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        //创建光标画笔
        mCursorPaint = Paint().apply {
            color = mCursorColor
        }


    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //每个输入框的宽度=屏幕的宽-左右的边距-输入框直接的间距/输入框的个数
        mRectWidth = (width - mSpacing * (mLength - 1)) / mLength
        //绘制输入框
        canvasInputBox(canvas)
        //绘制内容
        drawText(canvas)
        if (mIsCursor) {
            //绘制光标
            drawCursor(canvas)
        }

    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:绘制内容
     */
    private fun drawText(canvas: Canvas?) {
        mPaint!!.apply {
            style = Paint.Style.FILL
            color = mTextColor//设置内容颜色
            textSize = mTextSize
        }

        if (!TextUtils.isEmpty(text)) {
            for (a in text!!.indices) {
                val content = text!![a].toString()
                var endContent = content

                if (mTextType == 1) {
                    endContent = "*"
                } else if (mTextType == 2) {
                    endContent = "●"
                }

                val rect = Rect()
                mPaint!!.getTextBounds(endContent, 0, content.length, rect)
                val w = mPaint!!.measureText(endContent)//获取文字的宽
                //获取文字的X坐标
                val textX = ((a + 1) * mRectWidth) + a * mSpacing - mRectWidth / 2 - w / 2
                val h = rect.height()
                //获取文字的Y坐标
                var textY = (height + h) / 2.0f
                //针对星号做特殊处理
                if (mTextType == 1) {
                    textY += mTextSize / 3
                }

                canvas?.drawText(endContent, textX, textY, mPaint!!)
            }
        }
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:绘制光标
     */
    private fun drawCursor(canvas: Canvas?) {
        mCursorPaint!!.apply {
            strokeWidth = mCursorWidth
            isAntiAlias = true
        }
        //需要根据当前输入的位置，计算光标的绘制位置
        val len = text?.length
        if (len!! < mLength) {
            if (mCursorDirection) {
                //纵向光标
                val rectWidth = ((len + 1) * mRectWidth) + len * mSpacing - mRectWidth / 2
                canvas?.drawLine(
                    rectWidth,
                    mCursorSpacing,
                    rectWidth,
                    height - mCursorSpacing,
                    mCursorPaint!!
                )
            } else {
                val endX = ((len + 1) * mRectWidth) + len * mSpacing
                val startX = endX - mRectWidth
                //横向光标
                canvas?.drawLine(
                    startX + mCursorSpacing,
                    height.toFloat() - mCursorMarginBottom,//减去距离底部的边距
                    endX - mCursorSpacing,
                    height.toFloat() - mCursorMarginBottom,
                    mCursorPaint!!
                )
            }
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event?.action == MotionEvent.ACTION_DOWN) {
            //手指按下
            showKeyboard()
            text?.let { setSelection(it.length) }
            return false
        }

        return super.onTouchEvent(event)
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:弹起软件盘
     */
    private fun showKeyboard() {
        if (mIsAndroidKeyBoard) {
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            val im =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    ?: return
            im.showSoftInput(this, InputMethodManager.SHOW_FORCED)
        } else {
            //启用自定义的软键盘
            if (mKeyBoard != null) {
                mKeyBoard?.invoke()
            }
        }
    }


    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (!mIsAttachedToWindows) return

        //输入框的光标是否闪烁
        if (mCursorTwinkle) {
            if ((text?.length ?: 0) >= mLength) {
                cursorAnim.takeIf { it.isStarted || it.isRunning }?.end()
            } else if (!cursorAnim.isRunning) {
                cursorAnim.start()
            }
        }

        val endContent = text.toString()
        if (endContent.length == mLength) {
            //一样的话，进行回调
            mEndContentResult?.invoke(endContent)
        }

        mChangeContent?.invoke(endContent)

    }


    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:获取最终的输入结果
     */
    private var mEndContentResult: ((String) -> Unit?)? = null
    fun inputEndResult(block: (result: String) -> Unit) {
        mEndContentResult = block
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:获取连续的输入结果
     */
    private var mChangeContent: ((String) -> Unit?)? = null
    fun inputChangeContent(block: (result: String) -> Unit) {
        mChangeContent = block
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:显示自己定义的软件盘
     */
    private var mKeyBoard: (() -> Unit?)? = null
    fun showKeyBoard(block: () -> Unit) {
        mKeyBoard = block
    }


    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:绘制输入框
     */
    private fun canvasInputBox(canvas: Canvas?) {
        mPaint!!.apply {
            color = mBackGroundColor//设置背景颜色
            strokeCap = Paint.Cap.ROUND//圆角线
        }


        for (a in 0 until mLength) {

            val textLength = text.toString().length//当前输入的长度

            if (mSelectBackGroundColor != 0) {
                var paintStyle = Paint.Style.STROKE
                when (mInputCanvasStyle) {
                    0 -> {
                        paintStyle = Paint.Style.STROKE
                    }
                    1 -> {
                        paintStyle = Paint.Style.FILL
                    }
                    2 -> {
                        paintStyle = Paint.Style.FILL_AND_STROKE
                    }
                }
                if (a == textLength) {
                    mPaint!!.apply {
                        style = paintStyle
                        color = mSelectBackGroundColor//设置选中背景颜色
                    }
                } else {
                    mPaint!!.apply {
                        style = paintStyle
                        color = mBackGroundColor//设置背景颜色
                    }

                }
            }

            val left = a * mRectWidth + a * mSpacing
            val top = 0f
            val right = (a + 1) * mRectWidth + a * mSpacing
            val bottom = height.toFloat()

            when (mInputCanvasType) {
                0 -> {
                    //绘制下划线
                    canvas?.drawRoundRect(
                        left,
                        bottom - mLineHeight,
                        right,
                        bottom,
                        mRadius,
                        mRadius,
                        mPaint!!
                    )
                }
                1 -> {
                    //绘制矩形
                    canvas?.drawRect(left, top, right, bottom, mPaint!!)
                }
                2 -> {
                    //绘制圆角矩形
                    canvas?.drawRoundRect(left, top, right, bottom, mRadius, mRadius, mPaint!!)
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = measuredHeight
        if (heightMode == MeasureSpec.AT_MOST) {
            height = 96
        }
        setMeasuredDimension(measuredWidth, height)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mIsAttachedToWindows = true
        if (mCursorTwinkle) {
            //不在运行，开启动画
            if (!cursorAnim.isRunning) {
                cursorAnim.start()
            }
            cursorAnim.addUpdateListener {
                val v = it.animatedValue as Int
                if (v == 0) {
                    mCursorPaint?.color = Color.TRANSPARENT
                } else {
                    mCursorPaint?.color = mCursorColor
                }
                postInvalidate()
            }
        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mIsAttachedToWindows = false
        if (mCursorTwinkle) {
            if (cursorAnim.isRunning || cursorAnim.isStarted) {
                cursorAnim.end()
            }
            cursorAnim.removeAllUpdateListeners()
        }

        hideInputMethod()

    }


    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:隐藏软件盘
     */
    fun hideInputMethod() {
        if (mIsAndroidKeyBoard) {
            val imm: InputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0) //强制隐藏
        }
    }


    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:清空内容
     */
    fun clearContent() {
        mEndContent = ""
        setText("")
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:设置内容
     */
    private var mEndContent = ""
    fun setContent(content: String) {
        mEndContent += content
        setText(mEndContent)
    }

}