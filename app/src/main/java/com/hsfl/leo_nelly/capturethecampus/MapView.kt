package com.hsfl.leo_nelly.capturethecampus

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.hsfl.leo_nelly.capturethecampus.R.drawable.campus_map
import com.hsfl.leo_nelly.capturethecampus.R.drawable.flag_icon_not_visited
import com.hsfl.leo_nelly.capturethecampus.R.drawable.flag_icon_visited


class MapView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val mapDrawable = AppCompatResources.getDrawable(context, campus_map)
    private val mapBitmap = (mapDrawable as BitmapDrawable).bitmap
    private val flagVisitedDrawable = AppCompatResources.getDrawable(context, flag_icon_visited)
    private val flagNotVisitedDrawable = AppCompatResources.getDrawable(context, flag_icon_not_visited)
    private val flagVisitedBitmap = (flagVisitedDrawable as BitmapDrawable).bitmap
    private val flagNotVisitedBitmap = (flagNotVisitedDrawable as BitmapDrawable).bitmap
    private var bitmapMatrix = Matrix()
    private var mapPoints: List<MapPoint> = listOf()
    var showTapHint: Boolean = false
    var onMapLongClickListener: ((Double, Double) -> Unit)? = null

    private var _viewModel: MainViewModel? = null
    var viewModel: MainViewModel?
        get() = _viewModel
        set(value) {
            _viewModel = value
        }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            Log.d("MapView", "onSingleTapUp")
            if (showTapHint) {
                Toast.makeText(context, "Long press to place a flag", Toast.LENGTH_SHORT).show()
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            viewModel?.let {
                val (lat, lng) = it.convertScreenPositionToLatLng(e.x, e.y, width, height)
                onMapLongClickListener?.invoke(lat, lng)
            }
        }
    })

    init {
        minimumHeight = 210
        minimumWidth = 210
    }

    fun setMapPoints(points: List<MapPoint>) {
        mapPoints = points
        Log.d("MapView", "$mapPoints")
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mapBitmap, bitmapMatrix, null)
        drawMapPoints(canvas)
        Log.d("MapView", "onDraw")
    }

    private fun drawMapPoints(canvas: Canvas) {
        mapPoints.forEach { mapPoint ->
            val flagBitmap = if (mapPoint.state == PointState.VISITED) flagVisitedBitmap else flagNotVisitedBitmap
            val imageX = mapPoint.mapX * width - (flagBitmap.width / 2)
            val imageY = mapPoint.mapY * height - (flagBitmap.height / 2)
            canvas.drawBitmap(flagBitmap, imageX, imageY, null)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val minW = paddingLeft + paddingRight + suggestedMinimumWidth
        val w = resolveSizeAndState(minW, widthMeasureSpec, 0)
        val minH = paddingTop + paddingBottom + suggestedMinimumHeight
        val h = resolveSizeAndState(minH, heightMeasureSpec, 0)
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmapMatrix = Matrix().apply {
            setScale(w.toFloat() / mapBitmap.width, h.toFloat() / mapBitmap.height)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
}
