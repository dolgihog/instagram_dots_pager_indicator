package com.example.olegdolgikh.myapplication

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View

class PagerIndicatorView(context: Context) : View(context) {

    private val bigRadiusPx: Int = 20
    private var itemsCount: Int = 0
    private var paddingPx = 10
    private val MAX_CIRCLE_COUNT = 7

    private val circlePaint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val circlePaintSelected = Paint().apply {
        color = Color.GREEN
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val circlePaintTransparent = Paint().apply {
        color = Color.TRANSPARENT
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private var viewPager: ViewPager? = null
    private var selectedPosition: Int = 0
    private var previousPosition: Int = 0

    private var offset: Int = 0
    private var selectedItemXCoordinate: Int = 0
    private val startXBorder get() = 4 * bigRadiusPx + 2 * paddingPx
    private val endXBorder get() = 10 * bigRadiusPx + 4 * paddingPx
    private var startCoordinate: Int = 0


    fun setViewPager(viewPager: ViewPager) {
        releaseViewPager()
        viewPager.addOnPageChangeListener(pageChangeListener)
        itemsCount = viewPager.adapter?.count ?: 0
        requestLayout()
    }

    fun releaseViewPager() {
        viewPager?.removeOnPageChangeListener(pageChangeListener)
        viewPager = null
    }


    fun setItemsCount(itemsCount: Int) {
        this.itemsCount = itemsCount
    }

    override fun onDraw(canvas: Canvas) {
        val yCoordinate = getCoordinateY()
        (-2..itemsCount - 1 + 2).forEach { index ->
            val xCoordinate = getCoordinateX(index)
            val paint = when {
                index < 0 || index > itemsCount - 1 -> circlePaintTransparent
                index == selectedPosition -> circlePaintSelected
                else -> circlePaint
            }
            val radius = if (xCoordinate < startXBorder) {
                xCoordinate.toFloat() / startXBorder * bigRadiusPx
            } else if (xCoordinate > endXBorder) {
                ((right - xCoordinate).toFloat() / (right - endXBorder)) * bigRadiusPx
            } else {
                bigRadiusPx.toFloat()
            }
            canvas.drawCircle(xCoordinate.toFloat(), yCoordinate, radius, paint)
        }
    }

    private fun getCoordinateX(position: Int): Int {
        return startCoordinate + offset + (position + 2) * (2 * bigRadiusPx) + (position + 2) * paddingPx + bigRadiusPx
    }

    private fun getCoordinateY(): Float = height.toFloat() / 2

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec);
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val circleDiameterPx = bigRadiusPx * 2
        val desiredHeight = circleDiameterPx
        val desiredWidth = when {
            itemsCount != 0 -> {
                val diameterSum = circleDiameterPx * MAX_CIRCLE_COUNT
                val paddingSum = paddingPx * (MAX_CIRCLE_COUNT - 1)
                diameterSum + paddingSum
            }
            else -> 0
        }
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> Math.min(desiredWidth, widthSize)
            MeasureSpec.UNSPECIFIED -> desiredWidth
            else -> 0
        }
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> height
            MeasureSpec.AT_MOST -> Math.min(desiredWidth, heightSize)
            MeasureSpec.UNSPECIFIED -> desiredHeight
            else -> 0
        }
        setMeasuredDimension(width, height)
    }

    private val pageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {

        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        }

        override fun onPageSelected(position: Int) {
            previousPosition = selectedPosition
            selectedPosition = position
            val direction = getDirection(position)
            selectedItemXCoordinate = getCoordinateX(selectedPosition)
            val needShift = needShift(direction)
            Log.d(javaClass.simpleName, "needShift = $needShift")
            if (needShift) {
                when (direction) {
                    Direction.Forward -> {
                        val delta = 2 * bigRadiusPx + paddingPx
                        ValueAnimator.ofInt(0, delta).apply {
                            duration = 200
                            addUpdateListener {
                                Log.d(javaClass.simpleName, "animatedValue = $animatedValue")
                                offset = -(animatedValue as Int)
                                invalidate()
                            }
                            addListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {

                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    offset = 0
                                    startCoordinate = startCoordinate - delta
                                    invalidate()
                                }

                                override fun onAnimationCancel(animation: Animator?) {

                                }

                                override fun onAnimationStart(animation: Animator?) {

                                }
                            })
                        }.start()
                    }
                    Direction.Back -> {
                        val delta = bigRadiusPx * 2 + paddingPx
                        ValueAnimator.ofInt(0, delta).apply {
                            duration = 200
                            addUpdateListener {
                                Log.d(javaClass.simpleName, "animatedValue = $animatedValue")
                                offset = animatedValue as Int
                                invalidate()
                            }
                            addListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {

                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    offset = 0
                                    startCoordinate += delta
                                    invalidate()
                                }

                                override fun onAnimationCancel(animation: Animator?) {

                                }

                                override fun onAnimationStart(animation: Animator?) {

                                }
                            })
                        }.start()
                    }
                }
            } else {
                invalidate()
            }
        }
    }

    private fun getDirection(selectedPosition: Int): Direction = if (previousPosition < selectedPosition) Direction.Forward else Direction.Back

    private fun needShift(direction: Direction): Boolean = when (direction) {
        Direction.Forward -> selectedItemXCoordinate >= endXBorder
        Direction.Back -> selectedItemXCoordinate <= startXBorder
    }

    enum class Direction {
        Forward,
        Back
    }
}