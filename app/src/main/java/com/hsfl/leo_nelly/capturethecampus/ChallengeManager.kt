package com.hsfl.leo_nelly.capturethecampus

import android.content.Context
import android.widget.Toast

data class Challenge(
    val name: String,
    val description: String,
    val mapPoints: List<MapPoint>,
    val flagsToCapture: Int = mapPoints.size,
    var bestTime: Long = Long.MAX_VALUE
) {
    companion object {
        fun create(name: String, description: String, mapPoints: List<MapPoint>) =
            Challenge(name, description, mapPoints.map { it.copy(state = PointState.NOT_VISITED) })
    }
}

object ChallengeManager {
    private val predefinedChallenges = mutableListOf(
        Challenge.create("Challenge 1", "From A-Building to Mensa", mapPointsChallenge1),
        Challenge.create("Challenge 2", "From Mensa to GOT-Building", mapPointsChallenge2),
        Challenge.create("Challenge 3", "From A-Building to GOT-Building via Mensa", mapPointsChallenge3)
    )

    fun addChallengeAtPosition(position: Int, challenge: Challenge) {
        if (position in 0..predefinedChallenges.size) {
            predefinedChallenges.add(position, challenge)
        } else {
            predefinedChallenges.add(challenge)
        }
    }

    fun getPredefinedChallenges(): List<Challenge> = predefinedChallenges

    fun removeChallenge(challengeName: String) {
        predefinedChallenges.removeAll { it.name == challengeName }
    }

    fun updateBestTime(challengeName: String, newBestTime: Long) =
        predefinedChallenges.find { it.name == challengeName }?.apply { bestTime = newBestTime }

    fun saveChallenge(
        context: Context,
        challengeName: String,
        challengeDescription: String,
        mapPoints: List<MapPoint>
    ): Boolean {

        // Prüfung, ob alle Felder ausgefüllt sind
        if (challengeName.isBlank() || challengeDescription.isBlank() || mapPoints.isEmpty()) {
            Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        // Prüfung, ob eine identische Challenge bereits existiert
        if (predefinedChallenges.any { challenge ->
                challenge.mapPoints.size == mapPoints.size && challenge.mapPoints.containsAll(mapPoints)
            }) {
            Toast.makeText(context, "This challenge already exists", Toast.LENGTH_SHORT).show()
            return false
        }

        // Challenge erstellen und hinzufügen
        val challenge = Challenge.create(challengeName, challengeDescription, mapPoints)
        predefinedChallenges.add(challenge)
        return true
    }
}

private val a_building = MapPoint(54.7748, 9.4478, PointState.NOT_VISITED)
private val mensa = MapPoint(54.7755, 9.4527, PointState.NOT_VISITED)
private val got_building = MapPoint(54.7760, 9.4561, PointState.NOT_VISITED)

// Challenge 1: A-Gebäude -> Mensa
private val mapPointsChallenge1 = listOf(
    a_building,
    mensa
)

// Challenge 2: Mensa -> GOT-Gebäude
private val mapPointsChallenge2 = listOf(
    mensa,
    got_building
)

// Challenge 3: A-Gebäude -> Mensa -> GOT-Gebäude
private val mapPointsChallenge3 = listOf(
    a_building,
    mensa,
    got_building
)




