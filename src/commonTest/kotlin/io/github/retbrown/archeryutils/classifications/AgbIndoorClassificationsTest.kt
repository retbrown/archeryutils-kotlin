@file:Suppress("UNCHECKED_CAST")

package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Pass
import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.rounds.Rounds
import io.github.retbrown.archeryutils.targets.Quantity
import io.github.retbrown.archeryutils.targets.ScoringSystem
import kotlin.test.Test
import kotlin.test.assertEquals

// Ported from tests/unit/classifications/test_agb_indoor.py

private val ALL_INDOOR_ROUNDS: Map<String, Round> = Rounds.agbIndoor + Rounds.waIndoor

class TestAgbIndoorClassificationScores {
    @Test
    fun testAgbIndoorClassificationScoresAges() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(AgbAge.ADULT, listOf(378, 437, 483, 518, 546, 566, 582, 593)),
            "" to arrayOf<Any?>(AgbAge.OVER_50, listOf(316, 387, 444, 488, 522, 549, 569, 583)),
            "" to arrayOf<Any?>(AgbAge.UNDER_21, listOf(316, 387, 444, 488, 522, 549, 569, 583)),
            "" to arrayOf<Any?>(AgbAge.UNDER_18, listOf(250, 326, 395, 450, 493, 526, 552, 571)),
            "" to arrayOf<Any?>(AgbAge.UNDER_16, listOf(187, 260, 336, 403, 457, 498, 530, 555)),
            "" to arrayOf<Any?>(AgbAge.UNDER_15, listOf(134, 196, 271, 346, 411, 463, 503, 534)),
            "" to arrayOf<Any?>(AgbAge.UNDER_14, listOf(92, 141, 206, 281, 355, 419, 469, 508)),
            "" to arrayOf<Any?>(AgbAge.UNDER_12, listOf(62, 98, 149, 215, 291, 364, 426, 475)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val ageGroup = c[0] as AgbAge
                val scoresExpected = c[1] as List<Int>
                val scores = agbIndoorClassificationScores(archeryRound = ALL_INDOOR_ROUNDS.getValue("portsmouth"), bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.OPEN, ageGroup = ageGroup)
                assertEquals(scoresExpected.reversed(), scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbIndoorClassificationScoresGenders() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(AgbAge.ADULT, listOf(331, 399, 454, 496, 528, 553, 572, 586)),
            "" to arrayOf<Any?>(AgbAge.UNDER_16, listOf(145, 211, 286, 360, 423, 472, 510, 539)),
            "" to arrayOf<Any?>(AgbAge.UNDER_15, listOf(134, 196, 271, 346, 411, 463, 503, 534)),
            "" to arrayOf<Any?>(AgbAge.UNDER_12, listOf(62, 98, 149, 215, 291, 364, 426, 475)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val ageGroup = c[0] as AgbAge
                val scoresExpected = c[1] as List<Int>
                val scores = agbIndoorClassificationScores(archeryRound = ALL_INDOOR_ROUNDS.getValue("portsmouth"), bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.FEMALE, ageGroup = ageGroup)
                assertEquals(scoresExpected.reversed(), scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbIndoorClassificationScoresBowstyles() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(AgbBowstyle.COMPOUND, listOf(472, 508, 532, 549, 560, 571, 583, 594)),
            "" to arrayOf<Any?>(AgbBowstyle.BAREBOW, listOf(331, 387, 433, 472, 503, 528, 549, 565)),
            "" to arrayOf<Any?>(AgbBowstyle.LONGBOW, listOf(127, 178, 240, 306, 369, 423, 466, 501)),
            "" to arrayOf<Any?>(AgbBowstyle.ENGLISHLONGBOW, listOf(127, 178, 240, 306, 369, 423, 466, 501)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val bowstyle = c[0] as AgbBowstyle
                val scoresExpected = c[1] as List<Int>
                val scores = agbIndoorClassificationScores(archeryRound = ALL_INDOOR_ROUNDS.getValue("portsmouth"), bowstyle = bowstyle, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
                assertEquals(scoresExpected.reversed(), scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbIndoorClassificationScoresNonbowstyles() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(AgbBowstyle.FLATBOW, listOf(331, 387, 433, 472, 503, 528, 549, 565)),
            "" to arrayOf<Any?>(AgbBowstyle.TRADITIONAL, listOf(331, 387, 433, 472, 503, 528, 549, 565)),
            "" to arrayOf<Any?>(AgbBowstyle.COMPOUNDLIMITED, listOf(472, 508, 532, 549, 560, 571, 583, 594)),
            "" to arrayOf<Any?>(AgbBowstyle.COMPOUNDBAREBOW, listOf(472, 508, 532, 549, 560, 571, 583, 594)),
            "" to arrayOf<Any?>(AgbBowstyle.RECURVE, listOf(378, 437, 483, 518, 546, 566, 582, 593)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val bowstyle = c[0] as AgbBowstyle
                val scoresExpected = c[1] as List<Int>
                val coaxedVals = coaxIndoorGroup(bowstyle = bowstyle, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
                val scores = agbIndoorClassificationScores(archeryRound = ALL_INDOOR_ROUNDS.getValue("portsmouth"), bowstyle = coaxedVals.bowstyle, gender = coaxedVals.gender, ageGroup = coaxedVals.ageGroup)
                assertEquals(scoresExpected.reversed(), scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbIndoorClassificationScoresMale() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(AgbBowstyle.BAREBOW, listOf(331, 387, 433, 472, 503, 528, 549, 565)),
            "" to arrayOf<Any?>(AgbBowstyle.COMPOUND, listOf(472, 508, 532, 549, 560, 571, 583, 594)),
            "" to arrayOf<Any?>(AgbBowstyle.RECURVE, listOf(378, 437, 483, 518, 546, 566, 582, 593)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val bowstyle = c[0] as AgbBowstyle
                val scoresExpected = c[1] as List<Int>
                val scores = agbIndoorClassificationScores(archeryRound = ALL_INDOOR_ROUNDS.getValue("portsmouth"), bowstyle = bowstyle, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT)
                assertEquals(scoresExpected.reversed(), scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbIndoorClassificationScoresTripleFaces() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_INDOOR_ROUNDS.getValue("portsmouth_triple"), listOf(472, 508, 532, 549, 560, 571, 583, 594)),
            "" to arrayOf<Any?>(ALL_INDOOR_ROUNDS.getValue("worcester_5_centre"), listOf(217, 246, 267, 283, 294, 300, -9999, -9999)),
            "" to arrayOf<Any?>(ALL_INDOOR_ROUNDS.getValue("vegas_300_triple"), listOf(201, 230, 252, 269, 281, 290, 297, 300)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val scoresExpected = c[1] as List<Int>
                val scores = agbIndoorClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
                assertEquals(scoresExpected.reversed(), scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbIndoorClassificationScoresInvalidRound() {
        assertFailsContaining("This round is not recognised for the purposes of indoor classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            val myRound = Round("Some Roundname", listOf(Pass.atTarget(36, ScoringSystem.fromValue("10_zone"), Quantity.cm(122.0), Quantity.metres(70.0))))
            agbIndoorClassificationScores(archeryRound = myRound, bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbIndoorClassificationScoresInvalidStringRound() {
        assertFailsContaining("This round is not recognised for the purposes of indoor classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            agbIndoorClassificationScores(archeryRound = "invalid_roundname", bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbIndoorClassificationScoresStringRound() {
        val scores = agbIndoorClassificationScores(archeryRound = "portsmouth", bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
        assertEquals(listOf(472, 508, 532, 549, 560, 571, 583, 594).reversed(), scores)
    }

    @Test
    fun testAgbIndoorClassificationScoresNonStrictRound() {
        val frostbite = Rounds.misc.getValue("frostbite")
        val scores = agbIndoorClassificationScores(archeryRound = frostbite, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT, strictRounds = false)
        assertEquals(listOf(360, 356, 349, 339, 326, 309, 286, 256), scores)
    }

    @Test
    fun testAgbIndoorClassificationScoresNonStrictRoundString() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>("portsmouth"),
            "" to arrayOf<Any?>("Portsmouth"),
            "" to arrayOf<Any?>("fake_round"),
            "" to arrayOf<Any?>(""),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                assertFailsContaining("strict_rounds is False so archery_round must be explicitly specified as a Round type instead of a string.") {
                    val scores = agbIndoorClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT, strictRounds = false)
                }
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }
}

class TestCalculateAgbIndoorClassification {
    @Test
    fun testCalculateAgbIndoorClassification() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(594, AgbAge.ADULT, AgbBowstyle.COMPOUND, "I-GMB"),
            "" to arrayOf<Any?>(582, AgbAge.OVER_50, AgbBowstyle.RECURVE, "I-MB"),
            "" to arrayOf<Any?>(520, AgbAge.UNDER_21, AgbBowstyle.BAREBOW, "I-B1"),
            "" to arrayOf<Any?>(551, AgbAge.UNDER_18, AgbBowstyle.RECURVE, "I-B1"),
            "" to arrayOf<Any?>(526, AgbAge.UNDER_18, AgbBowstyle.RECURVE, "I-B1"),
            "" to arrayOf<Any?>(449, AgbAge.UNDER_12, AgbBowstyle.COMPOUND, "I-B2"),
            "" to arrayOf<Any?>(40, AgbAge.UNDER_12, AgbBowstyle.LONGBOW, "I-A1"),
            "" to arrayOf<Any?>(12, AgbAge.UNDER_12, AgbBowstyle.LONGBOW, "UC"),
            "" to arrayOf<Any?>(40, AgbAge.UNDER_12, AgbBowstyle.ENGLISHLONGBOW, "I-A1"),
            "" to arrayOf<Any?>(1, AgbAge.UNDER_12, AgbBowstyle.ENGLISHLONGBOW, "UC"),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val score = c[0] as Number
                val ageGroup = c[1] as AgbAge
                val bowstyle = c[2] as AgbBowstyle
                val classExpected = c[3] as String
                val classReturned = calculateAgbIndoorClassification(score = score, archeryRound = ALL_INDOOR_ROUNDS.getValue("portsmouth"), bowstyle = bowstyle, gender = AgbGender.OPEN, ageGroup = ageGroup)
                assertEquals(classExpected, classReturned)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testCalculateAgbIndoorClassificationInvalidRound() {
        assertFailsContaining("This round is not recognised for the purposes of indoor classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            calculateAgbIndoorClassification(score = 400, archeryRound = "invalid_roundname", bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testCalculateAgbIndoorClassificationInvalidScores() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(1000),
            "" to arrayOf<Any?>(601),
            "" to arrayOf<Any?>(-1),
            "" to arrayOf<Any?>(-100),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val score = c[0] as Number
                assertFailsContaining("Invalid score of ${score} for a ${ALL_INDOOR_ROUNDS.getValue("portsmouth").name}. Should be in range 0-${ALL_INDOOR_ROUNDS.getValue("portsmouth").maxScore()}.") {
                    calculateAgbIndoorClassification(score = score, archeryRound = ALL_INDOOR_ROUNDS.getValue("portsmouth"), bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
                }
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbIndoorClassificationInvalidRound() {
        assertFailsContaining("This round is not recognised for the purposes of indoor classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            val myRound = Round("Some Roundname", listOf(Pass.atTarget(36, ScoringSystem.fromValue("10_zone"), Quantity.cm(122.0), Quantity.metres(70.0))))
            calculateAgbIndoorClassification(archeryRound = myRound, score = 666, bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbIndoorClassificationScoresInvalidStringRound() {
        assertFailsContaining("This round is not recognised for the purposes of indoor classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            calculateAgbIndoorClassification(archeryRound = "invalid_roundname", score = 666, bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbIndoorClassificationScoresStringRound() {
        val myClass = calculateAgbIndoorClassification(archeryRound = "portsmouth", score = 578, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
        assertEquals("I-B1", myClass)
    }

    @Test
    fun testAgbIndoorClassificationNonStrictRound() {
        val frostbite = Rounds.misc.getValue("frostbite")
        val myClass = calculateAgbIndoorClassification(archeryRound = frostbite, score = 315, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT, strictRounds = false)
        assertEquals("I-A1", myClass)
    }

    @Test
    fun testAgbIndoorClassificationNonStrictRoundString() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>("portsmouth"),
            "" to arrayOf<Any?>("Portsmouth"),
            "" to arrayOf<Any?>("fake_round"),
            "" to arrayOf<Any?>(""),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                assertFailsContaining("strict_rounds is False so archery_round must be explicitly specified as a Round type instead of a string.") {
                    val scores = calculateAgbIndoorClassification(archeryRound = archeryRound, score = 123, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT, strictRounds = false)
                }
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }
}
