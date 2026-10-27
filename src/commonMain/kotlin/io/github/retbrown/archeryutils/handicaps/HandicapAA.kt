package io.github.retbrown.archeryutils.handicaps

import kotlin.math.exp
import kotlin.math.sqrt

/**
 * The original Archery Australia handicap scheme by J. Park.
 *
 * Using non-default parameter values may produce results that do not match the official
 * values for this scheme. Only change them if you are experimenting.
 *
 * @param ang0 datum angle used in scheme.
 * @param k0 handicap offset to set scratch point.
 * @param ks change with each handicap step as a geometric progression.
 * @param kd distance scaling factor.
 */
public class HandicapAA(
    private val ang0: Double = 1.0e-3,
    private val k0: Double = 2.37,
    private val ks: Double = 0.027,
    private val kd: Double = 0.004,
) : HandicapScheme(
        name = "AA",
        arwDOut = 5.0e-3,
        arwDIn = 9.3e-3,
        // AA uses an ascending scale with round. All numbers typically in [-250, 175]
        descScale = false,
        scaleBounds = doubleArrayOf(-250.0, 175.0),
        maxScoreRoundingLim = 0.5,
    ) {
    override val params: Map<String, Double> = mapOf("ang_0" to ang0, "k0" to k0, "ks" to ks, "kd" to kd)

    // Factor of sqrt(2) to deal with factor of 2 in differing definitions of sigma between
    // AGB and AA, required so code elsewhere is unchanged.
    override fun sigmaT(handicap: Double, dist: Double): Double =
        sqrt(2.0) * ang0 * exp(k0 - ks * handicap + kd * dist)
}

/**
 * The updated (2014) Archery Australia handicap scheme by J. Park.
 *
 * Using non-default parameter values may produce results that do not match the official
 * values for this scheme. Only change them if you are experimenting.
 *
 * @param ang0 datum angle used in scheme.
 * @param k0 handicap offset to set scratch point.
 * @param ks change with each handicap step as a geometric progression.
 * @param f1 'linear' scaling factor.
 * @param f2 'quadratic' scaling factor.
 * @param d0 normalisation distance in metres.
 */
public class HandicapAA2(
    private val ang0: Double = 1.0e-3,
    private val k0: Double = 2.57,
    private val ks: Double = 0.027,
    private val f1: Double = 0.815,
    private val f2: Double = 0.185,
    private val d0: Double = 50.0,
) : HandicapScheme(
        name = "AA2",
        arwDOut = 5.0e-3,
        arwDIn = 9.3e-3,
        descScale = false,
        scaleBounds = doubleArrayOf(-250.0, 175.0),
        maxScoreRoundingLim = 0.5,
    ) {
    override val params: Map<String, Double> =
        mapOf("ang_0" to ang0, "k0" to k0, "ks" to ks, "f1" to f1, "f2" to f2, "d0" to d0)

    override fun sigmaT(handicap: Double, dist: Double): Double =
        sqrt(2.0) * ang0 * exp(k0 - ks * handicap) * (f1 + f2 * dist / d0)
}
