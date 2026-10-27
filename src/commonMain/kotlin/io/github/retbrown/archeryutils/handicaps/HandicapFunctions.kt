package io.github.retbrown.archeryutils.handicaps

import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.targets.Target

/** Convenience functions for working with handicap schemes by name. */
public object Handicaps {
    /** The names of the supported handicap schemes. */
    public val schemeNames: List<String> = listOf("AGB", "AGBold", "AA", "AA2")

    /**
     * Create a [HandicapScheme] for the requested scheme name.
     *
     * @param handicapSys one of "AGB", "AGBold", "AA", "AA2".
     * @param params optional custom values of parameters (by their Python keyword names, e.g.
     * `"datum"`, `"step"`, `"ang_0"`, `"kd"`) as appropriate for the scheme.
     * @throws IllegalArgumentException if the scheme is not recognised or a parameter name is invalid.
     */
    public fun handicapScheme(handicapSys: String, params: Map<String, Double> = emptyMap()): HandicapScheme {
        val remaining = params.toMutableMap()
        fun p(key: String, default: Double): Double = remaining.remove(key) ?: default
        val scheme: HandicapScheme =
            when (handicapSys) {
                "AGB" -> HandicapAGB(p("datum", 6.0), p("step", 3.5), p("ang_0", 5.0e-4), p("kd", 0.00365))
                "AGBold" ->
                    HandicapAGBold(
                        p("datum", 12.9), p("step", 3.6), p("ang_0", 5.0e-4),
                        p("k1", 1.429e-6), p("k2", 1.07), p("k3", 4.3), p("p1", 2.0),
                    )
                "AA" -> HandicapAA(p("ang_0", 1.0e-3), p("k0", 2.37), p("ks", 0.027), p("kd", 0.004))
                "AA2" ->
                    HandicapAA2(
                        p("ang_0", 1.0e-3), p("k0", 2.57), p("ks", 0.027),
                        p("f1", 0.815), p("f2", 0.185), p("d0", 50.0),
                    )
                else ->
                    throw IllegalArgumentException(
                        "$handicapSys is not a recognised handicap system.\n" +
                            "Please select from '${schemeNames.joinToString("', '")}'.",
                    )
            }
        require(remaining.isEmpty()) { "Unexpected parameters for $handicapSys: ${remaining.keys}" }
        return scheme
    }

    /** Calculate the average arrow score for a [target] and [handicap] using the named scheme. */
    public fun arrowScore(handicap: Double, target: Target, handicapSys: String, arwD: Double? = null): Double =
        handicapScheme(handicapSys).arrowScore(handicap, target, arwD)

    /** Calculate the expected score for all passes in a round at a given [handicap]. */
    public fun scoreForPasses(
        handicap: Double,
        rnd: Round,
        handicapSys: String,
        arwD: Double? = null,
        roundedScore: Boolean = true,
    ): DoubleArray = handicapScheme(handicapSys).scoreForPasses(handicap, rnd, arwD, roundedScore)

    /** Calculate the expected score for a round at a given [handicap]. */
    public fun scoreForRound(
        handicap: Double,
        rnd: Round,
        handicapSys: String,
        arwD: Double? = null,
        roundedScore: Boolean = true,
    ): Double = handicapScheme(handicapSys).scoreForRound(handicap, rnd, arwD, roundedScore)

    /** Calculate the handicap of a [score] on a round using the named scheme. */
    public fun handicapFromScore(
        score: Double,
        rnd: Round,
        handicapSys: String,
        arwD: Double? = null,
        intPrec: Boolean = false,
    ): Double = handicapScheme(handicapSys).handicapFromScore(score, rnd, arwD, intPrec)
}
