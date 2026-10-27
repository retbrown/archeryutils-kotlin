package io.github.retbrown.archeryutils.handicaps

import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.targets.FaceSpec
import io.github.retbrown.archeryutils.targets.Target
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sign

/**
 * Abstract base class representing a generic handicap scheme.
 *
 * Calculates arrow and round scores for a variety of target faces of given distance and
 * diameter, and the inverse: the handicap corresponding to a given score.
 *
 * References:
 * - The construction of the graduated handicap tables for target archery. Lane, D (2013)
 * - New AGB: Atkinson, J
 * - Modelling archers' scores at different distances to quantify score loss due to
 *   equipment selection and technique errors. Park, J (2014)
 *
 * @property name the name of the handicap scheme.
 * @property arwDOut diameter of an outdoor arrow in metres for this scheme.
 * @property arwDIn diameter of an indoor arrow in metres for this scheme.
 * @property descScale whether the scheme uses a descending scale (lower handicap is better).
 * @property scaleBounds reasonable lower and upper bounds on the handicap scale, for bounding searches.
 * @property maxScoreRoundingLim limit to round the max score to when searching; depends on the
 * scheme rounding method e.g. round() vs. ceil().
 */
public abstract class HandicapScheme(
    public val name: String,
    public val arwDOut: Double,
    public val arwDIn: Double,
    public val descScale: Boolean,
    scaleBounds: DoubleArray,
    public val maxScoreRoundingLim: Double,
) {
    /** Reasonable lower and upper bounds on the handicap scale. */
    public val scaleBounds: DoubleArray = scaleBounds.copyOf()

    /** The parameters of the scheme, keyed by name. */
    public abstract val params: Map<String, Double>

    /**
     * Calculate angular deviation in radians for a given [handicap] and distance [dist] in metres.
     */
    public abstract fun sigmaT(handicap: Double, dist: Double): Double

    /** Calculate angular deviation for each of the [handicaps] at distance [dist] in metres. */
    public fun sigmaT(handicaps: DoubleArray, dist: Double): DoubleArray =
        DoubleArray(handicaps.size) { sigmaT(handicaps[it], dist) }

    /**
     * Calculate radial deviation (standard deviation as a proxy for 'group size') in metres
     * for a given [handicap] and distance [dist] in metres.
     */
    public fun sigmaR(handicap: Double, dist: Double): Double = dist * sigmaT(handicap, dist)

    /** Calculate radial deviation for each of the [handicaps] at distance [dist] in metres. */
    public fun sigmaR(handicaps: DoubleArray, dist: Double): DoubleArray =
        DoubleArray(handicaps.size) { sigmaR(handicaps[it], dist) }

    /**
     * Calculate the average arrow score for a given [target] and [handicap] rating.
     *
     * @param arwD user-specified arrow diameter in metres; the scheme default for
     * indoor/outdoor targets is used if null.
     */
    public fun arrowScore(handicap: Double, target: Target, arwD: Double? = null): Double {
        val diameter = arwD ?: if (target.indoor) arwDIn else arwDOut
        return sBar(target.faceSpec, diameter / 2.0, sigmaR(handicap, target.distance))
    }

    /** Calculate the average arrow score for each of the [handicaps]. */
    public fun arrowScore(handicaps: DoubleArray, target: Target, arwD: Double? = null): DoubleArray =
        DoubleArray(handicaps.size) { arrowScore(handicaps[it], target, arwD) }

    /**
     * Calculate expected score directly from target ring sizes.
     *
     * Assumes target rings are concentric and score decreases monotonically as ring sizes increase.
     *
     * @param targetSpecs mapping of target ring diameters in metres to points scored.
     * @param arwRad arrow radius in metres.
     * @param sigR standard deviation of group size in metres.
     */
    private fun sBar(targetSpecs: FaceSpec, arwRad: Double, sigR: Double): Double {
        val sorted = targetSpecs.entries.sortedBy { it.key }
        val ringScores = sorted.map { it.value } + 0
        val maxScore = ringScores.max()
        var total = 0.0
        for (i in sorted.indices) {
            val scoreDrop = ringScores[i] - ringScores[i + 1]
            val x = (arwRad + sorted[i].key / 2) / sigR
            total += scoreDrop * exp(-(x * x))
        }
        return maxScore - total
    }

    /**
     * Calculate the expected score for each pass in a round at a given [handicap].
     *
     * @param roundedScore whether to round each pass score to an integer value. Note the sum of
     * rounded passes may not be the same as the rounded round score.
     * @return the average score for each pass in the round.
     */
    public fun scoreForPasses(
        handicap: Double,
        rnd: Round,
        arwD: Double? = null,
        roundedScore: Boolean = true,
    ): DoubleArray {
        val scores = DoubleArray(rnd.passes.size) { i ->
            rnd.passes[i].nArrows * arrowScore(handicap, rnd.passes[i].target, arwD)
        }
        return if (roundedScore) DoubleArray(scores.size) { roundedScore(scores[it]) } else scores
    }

    /**
     * Calculate the expected score for a round at a given [handicap].
     *
     * @param roundedScore whether to round the score to an integer value.
     */
    public fun scoreForRound(
        handicap: Double,
        rnd: Round,
        arwD: Double? = null,
        roundedScore: Boolean = true,
    ): Double {
        val total = scoreForPasses(handicap, rnd, arwD, roundedScore = false).sum()
        return if (roundedScore) roundedScore(total) else total
    }

    /** Calculate the expected score for a round for each of the [handicaps]. */
    public fun scoreForRound(
        handicaps: DoubleArray,
        rnd: Round,
        arwD: Double? = null,
        roundedScore: Boolean = true,
    ): DoubleArray = DoubleArray(handicaps.size) { scoreForRound(handicaps[it], rnd, arwD, roundedScore) }

    /**
     * Round a decimal score to an integer value.
     *
     * Most schemes use plain rounding (half to even) as implemented here. Schemes that use
     * floor or ceil override this.
     */
    protected open fun roundedScore(score: Double): Double = round(score)

    /**
     * Calculate the handicap of a given [score] on a given round.
     *
     * @param arwD user-specified arrow diameter in metres.
     * @param intPrec return an integer valued handicap as would appear in handicap tables.
     * Otherwise the result is accurate to numerical precision from the rootfinder. For the
     * maximum score the first handicap that achieves it is returned.
     * @throws IllegalArgumentException if [score] is above the round maximum or not positive.
     */
    public fun handicapFromScore(
        score: Double,
        rnd: Round,
        arwD: Double? = null,
        intPrec: Boolean = false,
    ): Double {
        val maxScore = rnd.maxScore()
        require(score <= maxScore) {
            "The score of ${fmtScore(score)} provided is greater than the maximum of " +
                "${fmtScore(maxScore)} for a ${rnd.name}."
        }
        require(score > 0.0) {
            "The score of ${fmtScore(score)} provided is less than or equal to zero so cannot have a handicap."
        }

        if (score == maxScore) return maxScoreHandicap(rnd, arwD, intPrec)

        var handicap = rootfindScoreHandicap(score, rnd, arwD)

        if (intPrec) {
            handicap = if (descScale) ceil(handicap) else floor(handicap)

            // Check that you can't get the same score from a larger handicap when working in integers
            val hstep = if (descScale) 1.0 else -1.0
            while (true) {
                handicap += hstep
                val scInt = scoreForRound(handicap, rnd, arwD, roundedScore = true)
                if (scInt < score) {
                    handicap -= hstep // undo the iteration that caused the flag to raise
                    break
                }
            }
        }
        return handicap
    }

    /**
     * Get the handicap for the maximum score on a round.
     *
     * Starts high and drops down until no longer rounding to max score,
     * i.e. >= max_score - 1.0 for ceil(), and >= max_score - 0.5 for round().
     */
    private fun maxScoreHandicap(rnd: Round, arwD: Double?, intPrec: Boolean): Double {
        val maxScore = rnd.maxScore()
        var handicap: Double
        var deltaHc: Double
        if (descScale) {
            handicap = scaleBounds.min()
            deltaHc = 1.0
        } else {
            handicap = scaleBounds.max()
            deltaHc = -1.0
        }
        val target = maxScore - maxScoreRoundingLim
        fun checkScore(h: Double) = scoreForRound(h, rnd, arwD, roundedScore = false)

        // Work down (coarse) to where we would round or ceil to max score
        while (checkScore(handicap) > target) handicap += deltaHc

        // Step back extra after overshoot and reduce step size
        handicap -= 1.01 * deltaHc
        deltaHc /= 100

        // Work down (fine) to where we would round or ceil to max score
        while (checkScore(handicap) > target) handicap += deltaHc

        handicap -= deltaHc // Undo final iteration that overshoots

        if (intPrec) handicap = if (descScale) floor(handicap) else ceil(handicap)
        return handicap
    }

    /**
     * Get the handicap for a general score on a round through Brent's rootfinding algorithm.
     *
     * Based on the SciPy implementation:
     * https://github.com/scipy/scipy/blob/dde39b7/scipy/optimize/Zeros/brentq.c
     */
    private fun rootfindScoreHandicap(score: Double, rnd: Round, arwD: Double?): Double {
        val xInit = scaleBounds
        val fInit = doubleArrayOf(fRoot(xInit[0], score, rnd, arwD), fRoot(xInit[1], score, rnd, arwD))
        val xtol = 1.0e-16
        val rtol = 0.0
        var xblk = 0.0
        var fblk = 0.0
        var scur = 0.0
        var spre = 0.0
        var stry: Double
        var handicap = 0.0

        var xcur: Double
        var xpre: Double
        var fcur: Double
        var fpre: Double
        if (abs(fInit[1]) <= fInit[0]) {
            xcur = xInit[1]
            xpre = xInit[0]
            fcur = fInit[1]
            fpre = fInit[0]
        } else {
            xpre = xInit[1]
            xcur = xInit[0]
            fpre = fInit[1]
            fcur = fInit[0]
        }

        for (iter in 0 until 25) {
            if (fpre != 0.0 && fcur != 0.0 && sign(fpre) != sign(fcur)) {
                xblk = xpre
                fblk = fpre
                spre = xcur - xpre
                scur = xcur - xpre
            }
            if (abs(fblk) < abs(fcur)) {
                // xpre <- xcur, xcur <- xblk, xblk <- xpre
                val oldXcur = xcur
                xpre = oldXcur
                xcur = xblk
                xblk = oldXcur
                // fpre <- fcur, fcur <- fblk, fblk <- fpre
                val oldFcur = fcur
                fpre = oldFcur
                fcur = fblk
                fblk = oldFcur
            }

            val delta = (xtol + rtol * abs(xcur)) / 2.0
            val sbis = (xblk - xcur) / 2.0

            if (fcur == 0.0 || abs(sbis) < delta) {
                handicap = xcur
                break
            }

            if (abs(spre) > delta && abs(fcur) < abs(fpre)) {
                stry =
                    if (xpre == xblk) {
                        val denom = fcur - xpre
                        -fcur * (xcur - xpre) / (if (denom != 0.0) denom else xtol)
                    } else {
                        val dpre = (fpre - fcur) / (xpre - xcur)
                        val dblk = (fblk - fcur) / (xblk - xcur)
                        -fcur * (fblk - fpre) / (fblk * dpre - fpre * dblk)
                    }

                if (2 * abs(stry) < min(abs(spre), 3 * abs(sbis) - delta)) {
                    // accept step
                    spre = scur
                    scur = stry
                } else {
                    // bisect
                    spre = sbis
                    scur = sbis
                }
            } else {
                // bisect
                spre = sbis
                scur = sbis
            }
            xpre = xcur
            fpre = fcur
            if (abs(scur) > delta) {
                xcur += scur
            } else if (sbis > 0) {
                xcur += delta
            } else {
                xcur -= delta
            }

            fcur = fRoot(xcur, score, rnd, arwD)
            handicap = xcur
        }
        return handicap
    }

    /** The error between the predicted score at [hcEst] and the desired [scoreEst]. */
    private fun fRoot(hcEst: Double, scoreEst: Double, roundEst: Round, arwD: Double?): Double =
        scoreForRound(hcEst, roundEst, arwD, roundedScore = false) - scoreEst

    /** A short representation, e.g. `<HandicapScheme: 'AGB'>`. */
    override fun toString(): String = "<HandicapScheme: '$name'>"
}

private fun fmtScore(x: Double): String = if (x == floor(x) && abs(x) < 1e15) x.toLong().toString() else x.toString()
