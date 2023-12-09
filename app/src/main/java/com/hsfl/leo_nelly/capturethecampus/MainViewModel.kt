package com.hsfl.leo_nelly.capturethecampus

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.location.Location
import android.util.Log
import android.widget.Toast
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs

class MainViewModel : ViewModel() {

    // *************** LiveData ***************
    private val _showToast: MutableLiveData<Boolean> = MutableLiveData(false)
    val showToast: MutableLiveData<Boolean> get() = _showToast

    private val _currentHighScore: MutableLiveData<HighScore> = MutableLiveData(
        HighScore( "", Long.MAX_VALUE, Float.MAX_VALUE)
    )
    val currentHighScore: MutableLiveData<HighScore> get() = _currentHighScore

    private val _isGameWon: MutableLiveData<Boolean>  = MutableLiveData(false)
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

    val selectedChallenge: MutableLiveData<Challenge> = MutableLiveData()
    val challengeName = MutableLiveData<String>()
    val challengeDescription = MutableLiveData<String>()

    // *************** Variablen ***************
    private var isGameStarted = false
    private var totalDistance: Float = 0f
    private var removedPoint: MapPoint? = null
    private var removedPointIndex: Int = -1
    private var startTime = System.currentTimeMillis()
    private var timer: Timer? = null
    private var toastShown = false
    private var currentMapPoint: MapPoint? = null

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
        isGameStarted = false
        stopTimer()
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
            _showToast.postValue(true)
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
            totalDistance += loc.distanceTo(Location("").apply {
                latitude = it.latitude
                longitude = it.longitude
            })
        }

        currentMapPoint = MapPoint(
            latitude = loc.latitude,
            longitude = loc.longitude,
            state = PointState.NOT_VISITED,
            location = loc
        )

        updateMapPosition()
        updateMapPosition()
        updateTotalDistance()
    }

    fun convertScreenPositionToLatLng(x: Float, y: Float, width: Int, height: Int): Pair<Double, Double> {
        val lng = x / width * (BR_LONGITUDE - TL_LONGITUDE) + TL_LONGITUDE
        val lat = y / height * (BR_LATITUDE - TL_LATITUDE) + TL_LATITUDE
        return Pair(lat, lng)
    }

    // *************** Hilfsmethoden und Logik ***************
    fun saveChallenge(context: Context, name: String, description: String, mapPoints: List<MapPoint>): Boolean {

        val resetMapPoints = mapPoints.map { it.copy(state = PointState.NOT_VISITED) }
        val newChallenge = Challenge(name, description, resetMapPoints)

        if (ChallengeManager.getPredefinedChallenges().any { it.name == name }) {
            Toast.makeText(context, "Challenge with this name already exists", Toast.LENGTH_SHORT).show()
            return false
        }

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(context, "Please enter a name and description", Toast.LENGTH_SHORT).show()
            return false
        }

        ChallengeManager.addChallenge(newChallenge)
        return true
    }

    private fun startTimer() {
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val currentTime = System.currentTimeMillis()
                    _elapsedTime.postValue(currentTime - startTime)
                }
            }, 0, TIMER_PERIOD)
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun resetAndStartTimer() {
        startTime = System.currentTimeMillis()
        _elapsedTime.postValue(0)
        startTimer()
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

    private fun isGameReadyForUpdate(): Boolean = isGameStarted && currentMapPoint?.location != null

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
        return mapPoint.state != PointState.VISITED && isPlayerNearPoint(currentMapPoint?.location!!, mapPoint)
    }

    private fun canVisitPoint(points: MutableList<MapPoint>, index: Int): Boolean {
        return allPreviousPointsVisited(points, index)
    }

    private fun markPointAsVisited(points: MutableList<MapPoint>, index: Int) {
        points[index] = points[index].copy(state = PointState.VISITED)
        _flagCapturedEvent.value = true
    }

    private fun checkAndShowToast(): Boolean {
        if (!toastShown) {
            _showToast.postValue(true)
            toastShown = true
        }
        return true
    }

    private fun resetToastIfNeeded(isNearInvalidPoint: Boolean) {
        if (!isNearInvalidPoint) {
            toastShown = false
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

    private fun isScoreBetter(newScore: HighScore, oldScore: HighScore?, numberOfFlags: Int): Boolean {
        val newRatio = (newScore.time + newScore.distance) / numberOfFlags
        val oldRatio = oldScore?.let { (it.time + it.distance) / numberOfFlags } ?: Float.MAX_VALUE
        return newRatio < oldRatio
    }

    private fun checkIfGameIsWon() {
        val allPointsVisited = _mapPoints.value?.all { it.state == PointState.VISITED } ?: false
        Log.d("MainViewModel","All Points are visited: $allPointsVisited")
        _isGameWon.postValue(allPointsVisited)
    }

    private fun isPlayerNearPoint(currentLocation: Location, mapPoint: MapPoint): Boolean {
        val results = FloatArray(3)
        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude, mapPoint.latitude, mapPoint.longitude, results)
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
