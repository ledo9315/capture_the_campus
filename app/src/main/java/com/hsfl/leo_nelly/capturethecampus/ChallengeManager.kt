package com.hsfl.leo_nelly.capturethecampus

data class Challenge(
    val name: String,
    val description: String,
    val mapPoints: List<MapPoint>,
    val flagsToCapture: Int = mapPoints.size
)

class ChallengeManager {
    companion object {
        private val predefinedChallenges = mutableListOf(
            Challenge("Challenge 1", "From A-Building to Mensa", mapPointsChallenge1),
            Challenge("Challenge 2", "From Mensa to GOT-Building", mapPointsChallenge2),
            Challenge("Challenge 3", "From A-Building to GOT-Building via Mensa", mapPointsChallenge3)
        )

        fun addChallenge(challenge: Challenge) {
            predefinedChallenges.add(challenge)
        }

        fun getPredefinedChallenges(): List<Challenge> {
            return predefinedChallenges
        }
    }
}

private val mapPointsChallenge1 = listOf(
    MapPoint(54.7748, 9.4478, PointState.NOT_VISITED), // A-Building
    MapPoint(54.7755, 9.4527, PointState.NOT_VISITED)   // Mensa
)

private val mapPointsChallenge2 = listOf(
    MapPoint(54.7755, 9.4527, PointState.NOT_VISITED),  // Mensa
    MapPoint(54.7760, 9.4561, PointState.NOT_VISITED)  // GOT-Building
)

private val mapPointsChallenge3 = listOf(
    MapPoint(54.7748, 9.4478, PointState.NOT_VISITED),  // A-Building
    MapPoint(54.7755, 9.4527, PointState.NOT_VISITED),  // Mensa
    MapPoint(54.7760, 9.4561, PointState.NOT_VISITED)   // GOT-Building
)




