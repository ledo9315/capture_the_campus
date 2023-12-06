package com.hsfl.leo_nelly.capturethecampus

import android.widget.TextView
import androidx.databinding.BindingAdapter


object BindingAdapterUtils {
    @JvmStatic @BindingAdapter("formattedTime")
    fun setFormattedTime(textView: TextView, elapsedTime: Long?) {
        textView.text = formatElapsedTime(elapsedTime)
    }

    @JvmStatic @BindingAdapter("android:text")
    fun setFloatText(textView: TextView, value: Float?) {
        textView.text = if (value != null) String.format("Distance: %.0fm", value) else ""
    }


    @JvmStatic
    @BindingAdapter("playerName", "highScore", "totalDistance")
    fun setHighscoreText(textView: TextView, playerName: String?, highScore: Long?, totalDistance: Float?) {
        val formattedTime = if (highScore != null && highScore != Long.MAX_VALUE) {
            formatElapsedTime(highScore)
        } else {
            "No Time"
        }

        val formattedDistance = if (totalDistance != null && totalDistance != Float.MAX_VALUE) {
            String.format("%.0fm", totalDistance)
        } else {
            "No Distance"
        }

        val text = when {
            playerName.isNullOrEmpty() -> "No Highscore yet"
            else -> "$playerName\nTime: $formattedTime\nDistance: $formattedDistance"
        }
        textView.text = text
    }



    @JvmStatic
    fun formatElapsedTime(elapsedTime: Long?): String {
        if (elapsedTime == null) return "00:00"
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
