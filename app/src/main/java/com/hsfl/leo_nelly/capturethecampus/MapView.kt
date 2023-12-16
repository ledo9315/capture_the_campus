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
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.snackbar.Snackbar


class MapView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val mapBitmap = loadBitmap(context, R.drawable.campus_map)
    private val flagVisitedBitmap = loadBitmap(context, R.drawable.flag_icon_visited)
    private val flagNotVisitedBitmap = loadBitmap(context, R.drawable.flag_icon_not_visited)
    private var bitmapMatrix = Matrix()
    private var mapPoints: List<MapPoint> = listOf()
    var onMapLongClickListener: ((Double, Double) -> Unit)? = null
    private var _viewModel: MainViewModel? = null
    var viewModel: MainViewModel?
        get() = _viewModel
        set(value) {
            _viewModel = value
        }

    private val textPaint = createTextPaint(Color.BLACK)
    private val whiteTextPaint = createTextPaint(Color.WHITE)
    private val strokePaint = createTextPaint(Color.BLACK).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onSingleTapUp(e: MotionEvent): Boolean {

                if (mapPoints.isNotEmpty() && !viewModel?.isGameReadyForUpdate()!!) {
                    Snackbar.make(this@MapView, "Long press to place a flag", Snackbar.LENGTH_SHORT)
                        .setActionTextColor(Color.RED)
                        .setAction("OK") {}
                        .show()
                }

                Log.d("MapView", "onSingleTapUp")
                return true
            }
            override fun onLongPress(e: MotionEvent) {
                viewModel?.let {
                    val (lat, lng) = it.convertScreenPositionToLatLng(e.x, e.y, width, height)
                    // callback bei LongPress
                    onMapLongClickListener?.invoke(lat, lng)
                }
                Log.d("MapView", "onLongPress")
            }
        })

    init {
        minimumHeight = 210
        minimumWidth = 210
    }

    fun setMapPoints(points: List<MapPoint>) {
        mapPoints = points
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mapBitmap, bitmapMatrix, null)
        drawMapPoints(canvas)

        if (mapPoints.isEmpty()) {
            // wenn keine MapPoints vorhanden sind, wird ein Hinweis angezeigt
            canvas.drawText("Long press to place a flag", width / 2f, height / 2f, whiteTextPaint)
            canvas.drawText("Long press to place a flag", width / 2f, height / 2f, strokePaint)
        }
    }

    private fun drawMapPoints(canvas: Canvas) {
        mapPoints.forEachIndexed { index, mapPoint ->
            val flagBitmap =
                // wenn der Punkt besucht wurde, wird die Flagge mit dem grünen Haken angezeigt, sonst nur die Flagge
                if (mapPoint.state == PointState.VISITED) flagVisitedBitmap else flagNotVisitedBitmap
            drawFlagWithIndex(canvas, mapPoint, flagBitmap, index + 1)
        }
    }

    private fun drawFlagWithIndex(
        canvas: Canvas,
        mapPoint: MapPoint,
        flagBitmap: Bitmap,
        index: Int
    ) {
        val imageX = mapPoint.mapX * width - (flagBitmap.width / 8)
        val imageY = mapPoint.mapY * height - (flagBitmap.height)

        // wenn der Punkt noch nicht besucht wurde, wird der Index angezeigt
        if (mapPoint.state == PointState.NOT_VISITED) {
            canvas.drawText(index.toString(), imageX + 50, imageY + 100, textPaint)
        }
        canvas.drawBitmap(flagBitmap, imageX, imageY, null)
    }

    private fun loadBitmap(context: Context, resId: Int): Bitmap {
        return (AppCompatResources.getDrawable(context, resId) as BitmapDrawable).bitmap
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
            // Skalierung der Bitmap, damit die Bitmap die gesamte View ausfüllt
            setScale(w.toFloat() / mapBitmap.width, h.toFloat() / mapBitmap.height)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private fun createTextPaint(color: Int): Paint {
        return Paint().apply {
            this.color = color
            textSize = 80f
            textAlign = Paint.Align.CENTER
        }
    }
}
