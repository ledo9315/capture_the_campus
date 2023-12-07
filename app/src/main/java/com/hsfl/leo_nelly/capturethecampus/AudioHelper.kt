package com.hsfl.leo_nelly.capturethecampus

import android.content.Context
import android.media.MediaPlayer

class AudioHelper {


    companion object {

        private var mediaPlayer: MediaPlayer? = null

        fun playSound(context: Context, soundResourceId: Int) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, soundResourceId).apply {
                start()
                setOnCompletionListener { mp ->
                    mp.release()
                    mediaPlayer = null
                }
            }
        }

    }

}

