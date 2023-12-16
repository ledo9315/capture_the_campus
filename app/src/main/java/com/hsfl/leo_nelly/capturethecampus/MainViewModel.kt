package com.hsfl.leo_nelly.capturethecampus

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.abs

class MainViewModel : ViewModel() {

    // *************** LiveData ***************
    private val _currentHighScore: MutableLiveData<HighScore> = MutableLiveData(
        HighScore("", Long.MAX_VALUE, Float.MAX_VALUE)
    )
    val currentHighScore: MutableLiveData<HighScore> get() = _currentHighScore

    private val _isGameWon: MutableLiveData<Boolean> = MutableLiveData(false)
    val isGameWon: MutableLiveData<Boolean> get() = _isGameWon

    private val _mapX: MutableLiveData<Float> = MutableLiveData()
    val mapX: MutableLiveData<Float> get() = _mapX

    private val _mapY: MutableLiveData<Float> = MutableLiveData()
    val mapY: MutableLiveData<Float> get() = _mapY

    private val _elapsedTime: MutableLiveData<Long> = MutableLiveData()
    val elapsedTime: MutableLiveData<Long> get() = _elapsedTime

    private val _totalDistanceLiveData: MutableLiveData<Float> = MutableLiveData()
    val totalDistanceLiveData: MutableLiveData<Float> get() = _totalDistanceLiveData

    private val _previousNames: MutableLiveData<List<String>> = MutableLiveData(listOf())
    val previousNames: MutableLiveData<List<String>> get() = _previousNames

    private val _playerName: MutableLiveData<String> = MutableLiveData("")
    val playerName: MutableLiveData<String> get() = _playerName

    private val _mapPoints: MutableLiveData<List<MapPoint>> = MutableLiveData()
    val mapPoints: MutableLiveData<List<MapPoint>> get() = _mapPoints

    private val _visitedFlagsCount = MutableLiveData<String>()
    val visitedFlagsCount: LiveData<String> get() = _visitedFlagsCount

    private val _flagCapturedEvent = MutableLiveData<Boolean>()
    val flagCapturedEvent: LiveData<Boolean> get() = _flagCapturedEvent

    private val _flagSetEvent = MutableLiveData<Boolean>()
    val flagSetEvent: LiveData<Boolean> get() = _flagSetEvent

    private val _showMessage: MutableLiveData<Boolean> = MutableLiveData(false)
    val showMessage: MutableLiveData<Boolean> get() = _showMessage

    val selectedChallenge: MutableLiveData<Challenge?> = MutableLiveData()
    val challengeName = MutableLiveData<String>()
    val challengeDescription = MutableLiveData<String>()

    // *************** Variablen ***************
    private var isGameStarted = false
    private var totalDistance: Float = 0f
    private var removedPoint: MapPoint? = null
    private var removedPointIndex: Int = -1
    private var startTime = System.currentTimeMillis()
    private var messageShown = false
    private var currentMapPoint: MapPoint? = null
    private var lastKnownMapPoints: List<MapPoint>? = null
    private val timerManager = TimerManager()

    // *************** Konstanten ***************
    companion object {
        private const val PROXIMITY_RADIUS = 5.0
        private const val TOLERANCE = 0.001
        private const val TIMER_PERIOD = 10L
        private const val TL_LATITUDE = 54.778514
        private const val TL_LONGITUDE = 9.442749
        private const val BR_LATITUDE = 54.769009
        private const val BR_LONGITUDE = 9.464722
    }


    // *************** Spielmethoden ***************
    fun startGame(resetPoints: Boolean = false) {
        if (resetPoints) {
            resetMapPointsStatus()
            resetTotalDistance()
        }
        isGameStarted = true
        resetAndStartTimer()
    }

    fun stopGame() {
        val challengeName = selectedChallenge.value?.name
        val elapsedTime = _elapsedTime.value

        if (challengeName != null && elapsedTime != null) {
            completeChallenge(challengeName, elapsedTime)
        }

        isGameStarted = false
        timerManager.stopTimer()
        updateHighScore()
    }

    fun addPlayerName(name: String) {
        val names = _previousNames.value?.toMutableList() ?: mutableListOf()
        if (!names.contains(name)) {
            names.add(name)
            _previousNames.value = names
        }
        _playerName.value = name
    }

    fun addMapPoint(lat: Double, lng: Double) {
        if (!isNearbyPoint(lat, lng)) {
            val (mapX, mapY) = calculateMapXY(lat, lng)
            val point = MapPoint(lat, lng, PointState.NOT_VISITED, mapX, mapY)
            val newPoints = (_mapPoints.value ?: mutableListOf()) + point
            _mapPoints.postValue(newPoints)
            _flagSetEvent.value = true
        } else {
            _showMessage.postValue(true)
        }
    }

    fun addMapPoint() {
        val currentLatitude = currentMapPoint?.latitude ?: 0.0
        val currentLongitude = currentMapPoint?.longitude ?: 0.0
        addMapPoint(currentLatitude, currentLongitude)
    }

    fun setMapPoints(mapPoints: List<MapPoint>) {
        _mapPoints.value = mapPoints.map { point ->
            val (mapX, mapY) = calculateMapXY(point.latitude, point.longitude)
            point.copy(mapX = mapX, mapY = mapY)
        }
    }

    private fun calculateMapXY(lat: Double, lng: Double): Pair<Float, Float> {
        val mapX = ((lng - TL_LONGITUDE) / (BR_LONGITUDE - TL_LONGITUDE)).toFloat()
        val mapY = ((lat - TL_LATITUDE) / (BR_LATITUDE - TL_LATITUDE)).toFloat()
        return Pair(mapX, mapY)
    }

    fun resetMapPointsStatus() {
        val currentPoints = _mapPoints.value ?: listOf()
        val resetPoints = currentPoints.map { it.copy(state = PointState.NOT_VISITED) }
        _mapPoints.postValue(resetPoints)
    }

    fun resetTotalDistance() {
        totalDistance = 0f
        _totalDistanceLiveData.postValue(totalDistance)
    }

    fun resetChallengeInput() {
        challengeName.value = ""
        challengeDescription.value = ""
    }

    fun removePoint(position: Int) {
        val currentPoints = _mapPoints.value?.toMutableList() ?: return
        if (position in currentPoints.indices) {
            removedPoint = currentPoints.removeAt(position)
            removedPointIndex = position
            _mapPoints.postValue(currentPoints)
        }
    }

    fun restorePoint() {
        val currentPoints = _mapPoints.value?.toMutableList() ?: return
        removedPoint?.let {
            currentPoints.add(removedPointIndex.coerceAtLeast(0), it)
            _mapPoints.postValue(currentPoints)
        }
        removedPoint = null
        removedPointIndex = -1
    }

    fun setLocation(loc: Location) {
        currentMapPoint?.let {
            totalDistance += calculateDistanceBetweenPoints(loc, it)
        }

        currentMapPoint = MapPoint(
            latitude = loc.latitude,
            longitude = loc.longitude,
            state = PointState.NOT_VISITED,
            location = loc
        )

        updateMapPosition()
        updateTotalDistance()
    }

    private fun calculateDistanceBetweenPoints(from: Location, to: MapPoint): Float {
        return from.distanceTo(Location("").apply {
            latitude = to.latitude
            longitude = to.longitude
        })
    }

    fun convertScreenPositionToLatLng(
        x: Float,
        y: Float,
        width: Int,
        height: Int
    ): Pair<Double, Double> {
        val lng = x / width * (BR_LONGITUDE - TL_LONGITUDE) + TL_LONGITUDE
        val lat = y / height * (BR_LATITUDE - TL_LATITUDE) + TL_LATITUDE
        return Pair(lat, lng)
    }

    private fun completeChallenge(challengeName: String, elapsedTime: Long) {
        val challenge = ChallengeManager.getPredefinedChallenges().find { it.name == challengeName }
        challenge?.let {
            if (elapsedTime < it.bestTime) {
                ChallengeManager.updateBestTime(challengeName, elapsedTime)
            }
        }
    }

    private fun resetAndStartTimer() {
        startTime = System.currentTimeMillis()
        _elapsedTime.value = 0
        timerManager.startTimer(TIMER_PERIOD) {
            val currentTime = System.currentTimeMillis()
            _elapsedTime.postValue(currentTime - startTime)
        }
    }

    fun updateVisitedFlagsCount() {
        val visitedFlags = _mapPoints.value?.count { it.state == PointState.VISITED } ?: 0
        val totalFlags = _mapPoints.value?.size ?: 0

        _visitedFlagsCount.value = "${visitedFlags}/$totalFlags"
    }

    private fun updateMapPosition() {
        currentMapPoint?.let { point ->
            val x = ((point.longitude - TL_LONGITUDE) / (BR_LONGITUDE - TL_LONGITUDE)).toFloat()
            val y = ((point.latitude - TL_LATITUDE) / (BR_LATITUDE - TL_LATITUDE)).toFloat()
            _mapX.postValue(x)
            _mapY.postValue(y)
        }
    }

    private fun updateTotalDistance() {
        _totalDistanceLiveData.postValue(totalDistance)
    }

    fun updateMapPointsStatus() {
        if (!isGameReadyForUpdate()) return

        val currentPoints = _mapPoints.value?.toMutableList() ?: return
        val isNearInvalidPoint = updatePointsAndCheckProximity(currentPoints)

        resetToastIfNeeded(isNearInvalidPoint)
        _mapPoints.postValue(currentPoints)
        checkIfGameIsWon()
    }

    fun isGameReadyForUpdate(): Boolean = isGameStarted && currentMapPoint?.location != null

    private fun updatePointsAndCheckProximity(points: MutableList<MapPoint>): Boolean {
        updateVisitedFlagsCount()
        var isNearInvalidPoint = false
        points.forEachIndexed { index, mapPoint ->
            if (isMapPointNotVisitedAndNearby(mapPoint)) {
                if (canVisitPoint(points, index)) {
                    markPointAsVisited(points, index)
                } else {
                    isNearInvalidPoint = checkAndShowToast()
                }
            }
        }
        return isNearInvalidPoint
    }

    private fun isMapPointNotVisitedAndNearby(mapPoint: MapPoint): Boolean {
        return mapPoint.state != PointState.VISITED && isPlayerNearPoint(
            currentMapPoint?.location!!,
            mapPoint
        )
    }

    private fun canVisitPoint(points: MutableList<MapPoint>, index: Int): Boolean {
        return allPreviousPointsVisited(points, index)
    }

    private fun markPointAsVisited(points: MutableList<MapPoint>, index: Int) {
        points[index] = points[index].copy(state = PointState.VISITED)
        _flagCapturedEvent.value = true
    }

    private fun checkAndShowToast(): Boolean {
        if (!messageShown) {
            _showMessage.postValue(true)
            messageShown = true
        }
        return true
    }

    private fun resetToastIfNeeded(isNearInvalidPoint: Boolean) {
        if (!isNearInvalidPoint) {
            messageShown = false
        }
    }

    private fun allPreviousPointsVisited(points: List<MapPoint>, upToIndex: Int): Boolean {
        if (upToIndex == 0) return true
        return points.subList(0, upToIndex).all { it.state == PointState.VISITED }
    }

    private fun updateHighScore() {
        val playerName = _playerName.value ?: return
        val numberOfFlags = _mapPoints.value?.count { it.state == PointState.VISITED } ?: 0
        if (numberOfFlags == 0) return

        val currentScore = HighScore(
            name = playerName,
            time = _elapsedTime.value ?: Long.MAX_VALUE,
            distance = _totalDistanceLiveData.value ?: Float.MAX_VALUE
        )

        if (isScoreBetter(currentScore, _currentHighScore.value, numberOfFlags)) {
            _currentHighScore.value = currentScore
        }
    }

    fun resetHighScoreIfNeeded() {
        if (haveMapPointsChanged()) {
            _currentHighScore.value = HighScore("", Long.MAX_VALUE, Float.MAX_VALUE)
            lastKnownMapPoints = _mapPoints.value?.toList()
        }
    }

    private fun haveMapPointsChanged(): Boolean {
        val currentPoints = _mapPoints.value
        return currentPoints != lastKnownMapPoints
    }

    private fun isScoreBetter(
        newScore: HighScore,
        oldScore: HighScore?,
        numberOfFlags: Int
    ): Boolean {
        val newRatio = (newScore.time + newScore.distance) / numberOfFlags
        val oldRatio = oldScore?.let { (it.time + it.distance) / numberOfFlags } ?: Float.MAX_VALUE
        return newRatio < oldRatio
    }

    private fun checkIfGameIsWon() {
        val allPointsVisited = _mapPoints.value?.all { it.state == PointState.VISITED } ?: false
        Log.d("MainViewModel", "All Points are visited: $allPointsVisited")
        _isGameWon.postValue(allPointsVisited)
    }

    private fun isPlayerNearPoint(currentLocation: Location, mapPoint: MapPoint): Boolean {
        val results = FloatArray(3)
        Location.distanceBetween(
            currentLocation.latitude,
            currentLocation.longitude,
            mapPoint.latitude,
            mapPoint.longitude,
            results
        )
        return results[0] < PROXIMITY_RADIUS
    }

    fun isPlayerNearAnyPoint(): Boolean {
        val currentLocation = currentMapPoint?.location ?: return false
        return _mapPoints.value?.any { isPlayerNearPoint(currentLocation, it) } == true
    }

    private fun isNearbyPoint(lat: Double, lng: Double): Boolean {
        val currentPoints = _mapPoints.value ?: return false
        return currentPoints.any { abs(it.latitude - lat) < TOLERANCE && abs(it.longitude - lng) < TOLERANCE }
    }

    fun handleEvent(eventType: EventType) {
        when (eventType) {
            EventType.FlagCaptured -> _flagCapturedEvent.value = false
            EventType.FlagSet -> _flagSetEvent.value = false
        }
    }
}
