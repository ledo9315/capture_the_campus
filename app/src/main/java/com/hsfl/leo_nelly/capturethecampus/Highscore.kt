package com.hsfl.leo_nelly.capturethecampus

data class HighScore(
    val challengeId: String, // oder challengeName
    val name: String,
    val time: Long,
    val distance: Float
)
