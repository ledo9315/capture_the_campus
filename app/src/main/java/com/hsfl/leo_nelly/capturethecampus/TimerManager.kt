package com.hsfl.leo_nelly.capturethecampus

import java.util.*

class TimerManager {
    private var timer: Timer? = null

    fun startTimer(period: Long, action: () -> Unit) {
        stopTimer()
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    action()
                }
            }, 0, period)
        }
    }

    fun stopTimer() {
        timer?.cancel()
        timer = null
    }
}
