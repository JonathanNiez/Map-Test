package com.capstone.map.Utility

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer
import com.mapbox.maps.plugin.locationcomponent.LocationProvider
import java.util.concurrent.CopyOnWriteArraySet

class NavigationLocationProvider : LocationProvider{
    private val TAG = "NavLocProvider"

    private val locationConsumers = CopyOnWriteArraySet<LocationConsumer>()

    var lastLocation: Location? = null
        private set
    var lastKeyPoints: List<Location> = emptyList()
        private set

    @SuppressLint("MissingPermission")
    override fun registerLocationConsumer(locationConsumer: LocationConsumer) {
        if (locationConsumers.add(locationConsumer)) {
            ifNonNull(lastLocation, lastKeyPoints) { location, keyPoints ->
                locationConsumer.notifyLocationUpdates(
                    location,
                    keyPoints,
                    latLngTransitionOptions = {
                        this.duration = 0
                    },
                    bearingTransitionOptions = {
                        this.duration = 0
                    }
                )
            }

            Log.i(TAG, "registerLocationConsumer")
        }
    }

    override fun unRegisterLocationConsumer(locationConsumer: LocationConsumer) {
        locationConsumers.remove(locationConsumer)
    }

    fun changePosition(
        location: Location,
        keyPoints: List<Location> = emptyList(),
        latLngTransitionOptions: (ValueAnimator.() -> Unit)? = null,
        bearingTransitionOptions: (ValueAnimator.() -> Unit)? = null
    ) {
        locationConsumers.forEach {
            it.notifyLocationUpdates(
                location,
                keyPoints,
                latLngTransitionOptions,
                bearingTransitionOptions
            )
        }
        lastLocation = location
        lastKeyPoints = keyPoints
    }

    private fun LocationConsumer.notifyLocationUpdates(
        location: Location,
        keyPoints: List<Location>,
        latLngTransitionOptions: (ValueAnimator.() -> Unit)? = null,
        bearingTransitionOptions: (ValueAnimator.() -> Unit)? = null
    ) {
        val latLngUpdates = if (keyPoints.isNotEmpty()) {
            keyPoints.map { Point.fromLngLat(it.longitude, it.latitude) }.toTypedArray()
        } else {
            arrayOf(Point.fromLngLat(location.longitude, location.latitude))
        }
        val bearingUpdates = if (keyPoints.isNotEmpty()) {
            keyPoints.map { it.bearing.toDouble() }.toDoubleArray()
        } else {
            doubleArrayOf(location.bearing.toDouble())
        }

        this.onLocationUpdated(
            location = latLngUpdates,
            options = locationAnimatorOptions(latLngUpdates, latLngTransitionOptions)
        )
        this.onBearingUpdated(bearing = bearingUpdates, options = bearingTransitionOptions)
    }

    private fun locationAnimatorOptions(
        keyPoints: Array<Point>,
        clientOptions: (ValueAnimator.() -> Unit)?
    ): (ValueAnimator.() -> Unit) {
        val evaluator = PuckAnimationEvaluator(keyPoints)
        return {
            // TODO: Remove setDuration once patched in MapsSDK https://github.com/mapbox/mapbox-maps-android/issues/1446
            duration = LocationComponentConstants.DEFAULT_INTERVAL_MILLIS
            evaluator.installIn(this)
            clientOptions?.also { apply(it) }
        }
    }

}