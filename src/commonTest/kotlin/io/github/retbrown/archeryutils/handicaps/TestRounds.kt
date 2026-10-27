package io.github.retbrown.archeryutils.handicaps

import io.github.retbrown.archeryutils.rounds.Pass
import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.targets.Quantity
import io.github.retbrown.archeryutils.targets.ScoringSystem
import io.github.retbrown.archeryutils.targets.Target
import kotlin.math.abs
import kotlin.test.assertTrue

/** Shorthand for building a Pass with a distance in [unit] (default metres) and diameter in cm. */
internal fun pass(
    nArrows: Int,
    scoring: ScoringSystem,
    diameter: Double,
    distance: Double,
    unit: String = "metre",
    indoor: Boolean = false,
): Pass = Pass.atTarget(nArrows, scoring, Quantity.cm(diameter), Quantity(distance, unit), indoor)

/** Approximate equality with a relative tolerance, like pytest.approx. */
internal fun assertApprox(expected: Double, actual: Double, rel: Double = 1e-6, message: String? = null) {
    val tol = maxOf(rel * abs(expected), 1e-12)
    assertTrue(
        abs(expected - actual) <= tol,
        message ?: "expected <$expected> but was <$actual> (rel tol $rel)",
    )
}

internal val york =
    Round(
        "York",
        listOf(
            pass(72, ScoringSystem.FIVE_ZONE, 122.0, 100.0, "yard"),
            pass(48, ScoringSystem.FIVE_ZONE, 122.0, 80.0, "yard"),
            pass(24, ScoringSystem.FIVE_ZONE, 122.0, 60.0, "yard"),
        ),
    )

internal val hereford =
    Round(
        "Hereford",
        listOf(
            pass(72, ScoringSystem.FIVE_ZONE, 122.0, 80.0, "yard"),
            pass(48, ScoringSystem.FIVE_ZONE, 122.0, 60.0, "yard"),
            pass(24, ScoringSystem.FIVE_ZONE, 122.0, 50.0, "yard"),
        ),
    )

internal val western =
    Round(
        "Western",
        listOf(
            pass(48, ScoringSystem.FIVE_ZONE, 122.0, 60.0, "yard"),
            pass(48, ScoringSystem.FIVE_ZONE, 122.0, 50.0, "yard"),
        ),
    )

internal val vegas300 =
    Round("Vegas 300", listOf(pass(30, ScoringSystem.TEN_ZONE, 40.0, 20.0, "yard", indoor = true)))

internal val wa1440_90 =
    Round(
        "WA1440 90m",
        listOf(
            pass(36, ScoringSystem.TEN_ZONE, 122.0, 90.0),
            pass(36, ScoringSystem.TEN_ZONE, 122.0, 70.0),
            pass(36, ScoringSystem.TEN_ZONE, 80.0, 50.0),
            pass(36, ScoringSystem.TEN_ZONE, 80.0, 30.0),
        ),
    )

internal val wa720_70 =
    Round(
        "WA 720 70m",
        listOf(
            pass(36, ScoringSystem.TEN_ZONE, 122.0, 70.0),
            pass(36, ScoringSystem.TEN_ZONE, 122.0, 70.0),
        ),
    )

internal val metric122_30 =
    Round(
        "Metric 122-30",
        listOf(
            pass(36, ScoringSystem.TEN_ZONE, 122.0, 30.0),
            pass(36, ScoringSystem.TEN_ZONE, 122.0, 30.0),
        ),
    )

internal val kings900Rec =
    Round(
        "Kings 900 (recurve)",
        List(3) {
            Pass(
                30,
                Target.fromFaceSpec(
                    mapOf(0.08 to 10, 0.12 to 8, 0.16 to 7, 0.20 to 6),
                    Quantity.cm(40.0),
                    Quantity.metres(18.0),
                    indoor = true,
                ),
            )
        },
    )

/** The round used by the pass and round score tests. */
internal val myRound =
    Round(
        "MyRound",
        listOf(
            pass(10, ScoringSystem.TEN_ZONE, 122.0, 100.0),
            pass(10, ScoringSystem.TEN_ZONE, 80.0, 80.0),
            pass(10, ScoringSystem.FIVE_ZONE, 122.0, 60.0),
        ),
    )
