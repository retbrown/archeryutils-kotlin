package io.github.retbrown.archeryutils.handicaps

import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.pow

/**
 * The Archery GB handicap scheme: the 2023 scheme by Jack Atkinson.
 *
 * Using non-default parameter values may produce results that do not match the official
 * values for this scheme. Only change them if you are experimenting.
 *
 * @param datum offset to set scratch point.
 * @param step percentage change in deviation per handicap step.
 * @param ang0 datum angle used in scheme.
 * @param kd factor controlling excess dispersion scaling with distance.
 */
public class HandicapAGB(
    private val datum: Double = 6.0,
    private val step: Double = 3.5,
    private val ang0: Double = 5.0e-4,
    private val kd: Double = 0.00365,
) : HandicapScheme(
        name = "AGB",
        arwDOut = 5.5e-3,
        arwDIn = 9.3e-3,
        // AGB uses a descending scale with ceil. All numbers typically in [-75, 300]
        descScale = true,
        scaleBounds = doubleArrayOf(-75.0, 300.0),
        maxScoreRoundingLim = 1.0,
    ) {
    override val params: Map<String, Double> =
        mapOf("datum" to datum, "step" to step, "ang_0" to ang0, "kd" to kd)

    override fun sigmaT(handicap: Double, dist: Double): Double =
        ang0 * (1.0 + step / 100.0).pow(handicap + datum) * exp(kd * dist)

    // Override rounding method for AGB to always round up to the next highest score.
    override fun roundedScore(score: Double): Double = ceil(score)
}

/**
 * The old (pre-2023) Archery GB handicap scheme by D. Lane.
 *
 * Using non-default parameter values may produce results that do not match the official
 * values for this scheme. Only change them if you are experimenting.
 *
 * @param datum offset to set scratch point.
 * @param step percentage change in deviation per handicap step.
 * @param ang0 datum angle used in scheme.
 * @param k1 constant k1 in the handicap equation.
 * @param k2 constant k2 in the handicap equation.
 * @param k3 constant k3 in the handicap equation.
 * @param p1 exponent of distance scaling.
 */
public class HandicapAGBold(
    private val datum: Double = 12.9,
    private val step: Double = 3.6,
    private val ang0: Double = 5.0e-4,
    private val k1: Double = 1.429e-6,
    private val k2: Double = 1.07,
    private val k3: Double = 4.3,
    private val p1: Double = 2.0,
) : HandicapScheme(
        name = "AGBold",
        arwDOut = 7.14e-3,
        arwDIn = 7.14e-3,
        // AGBold uses a descending scale with round. All numbers typically in [-75, 300]
        descScale = true,
        scaleBounds = doubleArrayOf(-75.0, 300.0),
        maxScoreRoundingLim = 0.5,
    ) {
    override val params: Map<String, Double> =
        mapOf("datum" to datum, "step" to step, "ang_0" to ang0, "k1" to k1, "k2" to k2, "k3" to k3, "p1" to p1)

    override fun sigmaT(handicap: Double, dist: Double): Double {
        val kFactor = k1 * k2.pow(handicap + k3)
        val fFactor = 1.0 + kFactor * dist.pow(p1)
        return ang0 * (1.0 + step / 100.0).pow(handicap + datum) * fFactor
    }
}
