package com.capstone.map.Utility

import android.animation.TimeInterpolator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import com.mapbox.geojson.Point

internal class PuckAnimationEvaluator(
    private val keyPoints: Array<Point>
) : TimeInterpolator, TypeEvaluator<Point> {

    private var interpolator: TimeInterpolator? = null

    override fun getInterpolation(input: Float): Float =
        interpolator?.getInterpolation(input) ?: input

    override fun evaluate(fraction: Float, startValue: Point, endValue: Point): Point {
        if (interpolator == null) {
            // we defer creation of TimeInterpolator until we know startValue
            interpolator = ConstantVelocityInterpolator(startValue, keyPoints)
        }
        return POINT.evaluate(fraction, startValue, endValue)
    }

    fun reset() {
        interpolator = null
    }

    fun installIn(animator: ValueAnimator) {
        reset()
        animator.interpolator = this
        animator.setEvaluator(this)
    }

    companion object {
        private val POINT = TypeEvaluator<Point> { fraction, startValue, endValue ->
            Point.fromLngLat(
                startValue.longitude() + fraction * (endValue.longitude() - startValue.longitude()),
                startValue.latitude() + fraction * (endValue.latitude() - startValue.latitude())
            )
        }
    }

}