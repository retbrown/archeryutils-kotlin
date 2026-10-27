package io.github.retbrown.archeryutils.handicaps

import io.github.retbrown.archeryutils.targets.Quantity
import io.github.retbrown.archeryutils.targets.ScoringSystem
import io.github.retbrown.archeryutils.targets.Target
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

private val schemes = listOf("AGB", "AGBold", "AA", "AA2")

class HandicapSchemeFactoryTest {
    @Test
    fun repr() {
        for (scheme in schemes) {
            assertEquals("<HandicapScheme: '$scheme'>", Handicaps.handicapScheme(scheme).toString())
        }
    }

    @Test
    fun invalidSystem() {
        val e = assertFailsWith<IllegalArgumentException> { Handicaps.handicapScheme("InvalidSystem") }
        assertEquals(
            "InvalidSystem is not a recognised handicap system.\nPlease select from 'AGB', 'AGBold', 'AA', 'AA2'.",
            e.message,
        )
    }

    @Test
    fun invalidParameter() {
        assertFailsWith<IllegalArgumentException> { Handicaps.handicapScheme("AGB", mapOf("banana" to 1.0)) }
    }

    @Test
    fun customParameters() {
        val scheme = Handicaps.handicapScheme("AGB", mapOf("datum" to 7.0))
        assertEquals(7.0, scheme.params.getValue("datum"))
        assertEquals(3.5, scheme.params.getValue("step"))
    }

    @Test
    fun subclassGeneration() {
        assertIs<HandicapAGB>(Handicaps.handicapScheme("AGB"))
        assertIs<HandicapAGBold>(Handicaps.handicapScheme("AGBold"))
        assertIs<HandicapAA>(Handicaps.handicapScheme("AA"))
        assertIs<HandicapAA2>(Handicaps.handicapScheme("AA2"))
    }
}

class SigmaTest {
    private val cases =
        listOf(
            Triple(25.46, "AGB", 100.0) to 0.002125743,
            Triple(25.46, "AGBold", 100.0) to 0.002149455,
            Triple(25.46, "AA", 100.0) to 0.011349271,
            Triple(25.46, "AA2", 100.0) to 0.011011017,
            Triple(-12.0, "AGB", 100.0) to 0.000585929,
            Triple(-12.0, "AGBold", 100.0) to 0.000520552,
            Triple(-12.0, "AA", 100.0) to 0.031204851,
            Triple(-12.0, "AA2", 100.0) to 0.030274820,
            Triple(200.0, "AGB", 10.0) to 0.620202925,
            Triple(200.0, "AGBold", 10.0) to 134.960599745,
            Triple(200.0, "AA", 10.0) to 7.111717503e-05,
            Triple(200.0, "AA2", 10.0) to 7.110517486e-05,
        )

    @Test
    fun sigmaTFloat() {
        for ((input, expected) in cases) {
            val (handicap, system, distance) = input
            assertApprox(expected, Handicaps.handicapScheme(system).sigmaT(handicap, distance), message = "$system $handicap")
        }
    }

    @Test
    fun sigmaTArray() {
        val actual = Handicaps.handicapScheme("AGB").sigmaT(doubleArrayOf(25.46, -12.0, 200.0), 100.0)
        val expected = doubleArrayOf(0.002125743670, 0.0005859295368, 0.861389448)
        expected.indices.forEach { assertApprox(expected[it], actual[it]) }
    }

    private val rCases =
        listOf(
            Triple(25.46, "AGB", 100.0) to 0.212574367,
            Triple(25.46, "AGBold", 100.0) to 0.214945543,
            Triple(25.46, "AA", 100.0) to 1.134927187,
            Triple(25.46, "AA2", 100.0) to 1.101101752,
            Triple(-12.0, "AGB", 56.54) to 0.028268938,
            Triple(-12.0, "AGBold", 56.54) to 0.029263504,
            Triple(-12.0, "AA", 56.54) to 1.482791802,
            Triple(-12.0, "AA2", 56.54) to 1.479459074,
            Triple(200.0, "AGB", 10.0) to 6.202029252,
            Triple(200.0, "AGBold", 10.0) to 1349.605997454,
            Triple(200.0, "AA", 10.0) to 0.0007111717503,
            Triple(200.0, "AA2", 10.0) to 0.0007110517486,
        )

    @Test
    fun sigmaRFloat() {
        for ((input, expected) in rCases) {
            val (handicap, system, distance) = input
            assertApprox(expected, Handicaps.handicapScheme(system).sigmaR(handicap, distance), message = "$system $handicap")
        }
    }

    @Test
    fun sigmaRArray() {
        val actual = Handicaps.handicapScheme("AGB").sigmaR(doubleArrayOf(25.46, -12.0, 200.0), 100.0)
        val expected = doubleArrayOf(0.2125743670, 0.05859295, 86.1389448)
        expected.indices.forEach { assertApprox(expected[it], actual[it]) }
    }
}

class ArrowScoreTest {
    @Test
    fun differentHandicapSystems() {
        val cases =
            listOf(
                listOf("AGB", false, null, 9.134460979),
                listOf("AGB", true, null, 9.207981127),
                listOf("AGB", false, 7.2e-3, 9.167309048),
                listOf("AGBold", false, null, 8.983801507),
                listOf("AGBold", true, null, 8.983801507),
                listOf("AGBold", false, 5.5e-3, 8.952543551),
                listOf("AA", false, null, 1.825614895),
                listOf("AA", true, null, 1.901506952),
                listOf("AA", false, 7.2e-3, 1.864288808),
                listOf("AA2", false, null, 1.818143484),
                listOf("AA2", true, null, 1.893767531),
                listOf("AA2", false, 7.2e-3, 1.856680406),
            )
        for (case in cases) {
            val system = case[0] as String
            val indoor = case[1] as Boolean
            val arwD = case[2] as Double?
            val expected = case[3] as Double
            val target = Target(ScoringSystem.TEN_ZONE_FIVE_RING_COMPOUND, 40.0, 20.0, indoor)
            assertApprox(expected, Handicaps.handicapScheme(system).arrowScore(20.0, target, arwD), message = "$system $indoor $arwD")
            assertApprox(expected, Handicaps.arrowScore(20.0, target, system, arwD), message = "$system $indoor $arwD")
        }
    }

    @Test
    fun differentTargetFaces() {
        val cases =
            listOf(
                ScoringSystem.FIVE_ZONE to 7.044047485,
                ScoringSystem.TEN_ZONE to 7.547210123,
                ScoringSystem.TEN_ZONE_COMPOUND to 7.481017199,
                ScoringSystem.TEN_ZONE_SIX_RING to 7.397557278,
                ScoringSystem.TEN_ZONE_FIVE_RING to 7.059965360,
                ScoringSystem.TEN_ZONE_FIVE_RING_COMPOUND to 6.993772436,
                ScoringSystem.ELEVEN_ZONE to 7.574675221,
                ScoringSystem.ELEVEN_ZONE_FIVE_RING to 7.087430458,
                ScoringSystem.WA_FIELD to 4.115600784,
                ScoringSystem.IFAA_FIELD to 4.265744101,
                ScoringSystem.IFAA_FIELD_EXPERT to 4.021942762,
                ScoringSystem.BEITER_HIT_MISS to 0.9998380401,
                ScoringSystem.WORCESTER to 4.021942762,
                ScoringSystem.WORCESTER_2_RING to 3.346414597,
            )
        for ((face, expected) in cases) {
            val target = Target(face, 80.0, 50.0, false)
            assertApprox(expected, Handicaps.handicapScheme("AGB").arrowScore(38.0, target), message = "$face")
            assertApprox(expected, Handicaps.arrowScore(38.0, target, "AGB"), message = "$face")
        }
    }

    @Test
    fun emptySpec() {
        val target = Target.fromFaceSpec(emptyMap(), Quantity.cm(10.0), Quantity.metres(10.0))
        assertEquals(0.0, Handicaps.arrowScore(50.0, target, "AGB"))
    }

    @Test
    fun unsortedSpec() {
        fun target(spec: Map<Double, Int>) = Target.fromFaceSpec(spec, Quantity.cm(10.0), Quantity.metres(10.0))
        val sBar = Handicaps.arrowScore(50.0, target(linkedMapOf(0.1 to 1, 0.2 to 2, 0.3 to 3)), "AA")
        val reversed = Handicaps.arrowScore(50.0, target(linkedMapOf(0.3 to 3, 0.2 to 2, 0.1 to 1)), "AA")
        val unordered = Handicaps.arrowScore(50.0, target(linkedMapOf(0.1 to 1, 0.3 to 3, 0.2 to 2)), "AA")
        assertEquals(sBar, reversed)
        assertEquals(sBar, unordered)
    }

    @Test
    fun arrayHandicaps() {
        val target = Target.fromFaceSpec(mapOf(0.1 to 3, 0.2 to 5), Quantity.cm(10.0), Quantity.metres(10.0))
        val handicaps = doubleArrayOf(10.0, 20.0, 30.0, 40.0)
        val sBar = Handicaps.handicapScheme("AGB").arrowScore(handicaps, target)
        assertEquals(handicaps.size, sBar.size)
    }
}

class ScoreForPassesTest {
    @Test
    fun floatPassScores() {
        val cases =
            mapOf(
                "AGB" to doubleArrayOf(79.845655370, 76.647150342, 86.889064625),
                "AGBold" to doubleArrayOf(80.595119295, 76.161067484, 86.013051689),
                "AA" to doubleArrayOf(7.813903155, 6.287938401, 22.267136351),
                "AA2" to doubleArrayOf(8.266044100, 6.465486423, 22.394520484),
            )
        for ((system, expected) in cases) {
            val direct = Handicaps.scoreForPasses(20.0, myRound, system, null, false)
            val viaScheme = Handicaps.handicapScheme(system).scoreForPasses(20.0, myRound, null, false)
            expected.indices.forEach {
                assertApprox(expected[it], direct[it], message = system)
                assertApprox(expected[it], viaScheme[it], message = system)
            }
        }
    }

    @Test
    fun roundedPassScores() {
        val cases =
            mapOf(
                "AGB" to doubleArrayOf(80.0, 77.0, 87.0),
                "AGBold" to doubleArrayOf(81.0, 76.0, 86.0),
            )
        for ((system, expected) in cases) {
            assertEquals(expected.toList(), Handicaps.scoreForPasses(20.0, myRound, system, null, true).toList())
            assertEquals(
                expected.toList(),
                Handicaps.handicapScheme(system).scoreForPasses(20.0, myRound, null, true).toList(),
            )
        }
    }
}

class ScoreForRoundTest {
    @Test
    fun floatRoundScore() {
        val cases = mapOf("AGB" to 243.381870343, "AGBold" to 242.769238469, "AA" to 36.368977908, "AA2" to 37.126051009)
        for ((system, expected) in cases) {
            assertApprox(expected, Handicaps.scoreForRound(20.0, myRound, system, null, false), message = system)
            assertApprox(expected, Handicaps.handicapScheme(system).scoreForRound(20.0, myRound, null, false), message = system)
        }
    }

    @Test
    fun roundedRoundScore() {
        // AGBold differs to AGB in that it rounds rather than rounding up.
        for ((system, expected) in mapOf("AGB" to 244.0, "AGBold" to 243.0)) {
            assertEquals(expected, Handicaps.scoreForRound(20.0, myRound, system, null, true))
            assertEquals(expected, Handicaps.handicapScheme(system).scoreForRound(20.0, myRound, null, true))
        }
    }

    @Test
    fun arrayHandicaps() {
        val scores = Handicaps.handicapScheme("AGB").scoreForRound(doubleArrayOf(10.0, 50.0, 100.0), wa1440_90)
        assertEquals(listOf(1356.0, 887.0, 91.0), scores.toList())
    }

    @Test
    fun customScoring() {
        assertEquals(896.0, Handicaps.scoreForRound(20.0, kings900Rec, "AGB", null, true))
    }
}

class HandicapFromScoreTest {
    @Test
    fun scoreOverMax() {
        val round =
            io.github.retbrown.archeryutils.rounds.Round(
                "TestRound",
                listOf(pass(10, ScoringSystem.TEN_ZONE, 122.0, 50.0), pass(10, ScoringSystem.TEN_ZONE, 80.0, 50.0)),
            )
        val e = assertFailsWith<IllegalArgumentException> { Handicaps.handicapFromScore(9999.0, round, "AGB") }
        assertEquals("The score of 9999 provided is greater than the maximum of 200 for a TestRound.", e.message)
    }

    @Test
    fun scoreOfZeroOrBelow() {
        for (score in listOf(0.0, -9999.0)) {
            val e = assertFailsWith<IllegalArgumentException> { Handicaps.handicapFromScore(score, wa720_70, "AGB") }
            assertTrue(e.message!!.endsWith("provided is less than or equal to zero so cannot have a handicap."))
        }
    }

    @Test
    fun maximumScore() {
        val cases =
            listOf(
                Triple("AGB", metric122_30, 720.0) to 11.0,
                Triple("AA", metric122_30, 720.0) to 107.0,
                Triple("AGB", western, 864.0) to 9.0,
                Triple("AGBold", western, 864.0) to 6.0,
                Triple("AGB", vegas300, 300.0) to 3.0,
                Triple("AA", vegas300, 300.0) to 119.0,
            )
        for ((input, expected) in cases) {
            val (system, round, max) = input
            assertEquals(expected, Handicaps.handicapFromScore(max, round, system, null, true), "$system ${round.name}")
        }
    }

    @Test
    fun maximumScoreFloat() {
        assertApprox(11.75, Handicaps.handicapFromScore(720.0, metric122_30, "AGB", null, false))
        assertApprox(106.98, Handicaps.handicapFromScore(720.0, metric122_30, "AA", null, false))
    }

    @Test
    fun intPrecision() {
        val cases =
            listOf(
                Triple("AGB", wa720_70, 700.0) to 1.0,
                Triple("AGBold", wa720_70, 700.0) to 1.0,
                Triple("AA", wa720_70, 700.0) to 119.0,
                Triple("AGB", wa720_70, 500.0) to 44.0,
                Triple("AGBold", wa720_70, 500.0) to 40.0,
                Triple("AA", wa720_70, 500.0) to 64.0,
                // Score on the lower bound of a band:
                Triple("AGB", wa720_70, 283.0) to 63.0,
                Triple("AGBold", wa720_70, 286.0) to 53.0,
                Triple("AA", wa720_70, 280.0) to 39.0,
                // Score on upper bound of a band:
                Triple("AGB", wa720_70, 486.0) to 46.0,
                Triple("AGBold", wa720_70, 488.0) to 41.0,
                Triple("AA", wa720_70, 491.0) to 62.0,
                // Scores that give negative AGB handicaps:
                Triple("AGB", wa720_70, 710.0) to -5.0,
                Triple("AGBold", wa720_70, 710.0) to -5.0,
                // Scores that give 0 AA handicaps:
                Triple("AA", wa720_70, 52.0) to 0.0,
                // Multiple distance round:
                Triple("AGB", wa1440_90, 850.0) to 52.0,
                Triple("AGBold", wa1440_90, 850.0) to 46.0,
                Triple("AA", wa1440_90, 850.0) to 53.0,
            )
        for ((input, expected) in cases) {
            val (system, round, score) = input
            assertEquals(expected, Handicaps.handicapFromScore(score, round, system, null, true), "$system ${round.name} $score")
        }
    }

    @Test
    fun decimal() {
        val cases =
            listOf(
                Triple("AGB", wa720_70, 500.0) to 43.474880980,
                Triple("AGBold", wa720_70, 500.0) to 39.056931372,
                Triple("AA", wa720_70, 500.0) to 64.197993398,
                Triple("AGB", wa1440_90, 850.0) to 51.775514610,
                Triple("AGBold", wa1440_90, 850.0) to 45.303733163,
                Triple("AA", wa1440_90, 850.0) to 53.545592683,
            )
        for ((input, expected) in cases) {
            val (system, round, score) = input
            assertApprox(expected, Handicaps.handicapFromScore(score, round, system, null, false), message = "$system ${round.name}")
        }
    }

    @Test
    fun customScoring() {
        assertEquals(20.0, Handicaps.handicapFromScore(896.0, kings900Rec, "AGB", intPrec = true))
    }

    @Test
    fun existingSchemeReused() {
        val scheme = Handicaps.handicapScheme("AGB")
        assertSame(scheme, scheme)
    }
}
