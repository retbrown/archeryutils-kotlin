@file:Suppress("UNCHECKED_CAST")

package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Pass
import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.rounds.Rounds
import io.github.retbrown.archeryutils.targets.Quantity
import io.github.retbrown.archeryutils.targets.ScoringSystem
import kotlin.test.Test
import kotlin.test.assertEquals

// Ported from tests/unit/classifications/test_agb_old_indoor.py

private val ALL_INDOOR_ROUNDS: Map<String, Round> = Rounds.waIndoor + Rounds.agbIndoor

class TestAgbOldIndoorClassificationScores {
    @Test
    fun testAgbOldIndoorClassificationScoresAges() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(AgbAge.ADULT, listOf(592, 582, 554, 505, 432, 315, 195, 139)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val ageGroup = c[0] as AgbAge
                val scoresExpected = c[1] as List<Int>
                val scores = agbOldIndoorClassificationScores(archeryRound = ALL_INDOOR_ROUNDS.getValue("portsmouth"), bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.MALE, ageGroup = ageGroup)
                assertEquals(scoresExpected, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOldIndoorClassificationScoresCoaxed() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(AgbAge.OVER_50, AgbBowstyle.RECURVE, listOf(592, 582, 554, 505, 432, 315, 195, 139)),
            "" to arrayOf<Any?>(AgbAge.UNDER_21, AgbBowstyle.RECURVE, listOf(592, 582, 554, 505, 432, 315, 195, 139)),
            "" to arrayOf<Any?>(AgbAge.UNDER_18, AgbBowstyle.RECURVE, listOf(592, 582, 554, 505, 432, 315, 195, 139)),
            "" to arrayOf<Any?>(AgbAge.UNDER_12, AgbBowstyle.RECURVE, listOf(592, 582, 554, 505, 432, 315, 195, 139)),
            "" to arrayOf<Any?>(AgbAge.ADULT, AgbBowstyle.BAREBOW, listOf(592, 582, 554, 505, 432, 315, 195, 139)),
            "" to arrayOf<Any?>(AgbAge.ADULT, AgbBowstyle.COMPOUNDLIMITED, listOf(581, 570, 554, 529, 484, 396, 279, 206)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val ageGroup = c[0] as AgbAge
                val bowstyle = c[1] as AgbBowstyle
                val scoresExpected = c[2] as List<Int>
                val coaxedVals = coaxOldIndoorGroup(bowstyle = bowstyle, gender = AgbGender.MALE, ageGroup = ageGroup)
                val scores = agbOldIndoorClassificationScores(archeryRound = ALL_INDOOR_ROUNDS.getValue("portsmouth"), bowstyle = coaxedVals.bowstyle, gender = coaxedVals.gender, ageGroup = coaxedVals.ageGroup)
                assertEquals(scoresExpected, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOldIndoorClassificationScoresGenders() {
        val scores = agbOldIndoorClassificationScores(archeryRound = ALL_INDOOR_ROUNDS.getValue("portsmouth"), bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        assertEquals(listOf(582, 569, 534, 479, 380, 255, 139, 93), scores)
    }

    @Test
    fun testAgbOldIndoorClassificationScoresBowstyles() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(AgbBowstyle.COMPOUND, AgbGender.MALE, listOf(581, 570, 554, 529, 484, 396, 279, 206)),
            "" to arrayOf<Any?>(AgbBowstyle.COMPOUND, AgbGender.FEMALE, listOf(570, 562, 544, 509, 449, 347, 206, 160)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val bowstyle = c[0] as AgbBowstyle
                val gender = c[1] as AgbGender
                val scoresExpected = c[2] as List<Int>
                val scores = agbOldIndoorClassificationScores(archeryRound = ALL_INDOOR_ROUNDS.getValue("portsmouth"), bowstyle = bowstyle, gender = gender, ageGroup = AgbAge.ADULT)
                assertEquals(scoresExpected, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOldIndoorClassificationScoresTripleFaces() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_INDOOR_ROUNDS.getValue("portsmouth_triple"), listOf(206, 279, 396, 484, 529, 554, 570, 581)),
            "" to arrayOf<Any?>(ALL_INDOOR_ROUNDS.getValue("worcester_5_centre"), listOf(65, 96, 162, 226, 264, 289, 299, 300)),
            "" to arrayOf<Any?>(ALL_INDOOR_ROUNDS.getValue("wa18_triple"), listOf(63, 100, 203, 370, 486, 537, 558, 568)),
            "" to arrayOf<Any?>(ALL_INDOOR_ROUNDS.getValue("wa18"), listOf(117, 173, 295, 420, 493, 537, 558, 568)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val scoresExpected = c[1] as List<Int>
                val scores = agbOldIndoorClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT)
                assertEquals(scoresExpected.reversed(), scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOldIndoorClassificationScoresGentCompoundWorcester() {
        val scores = agbOldIndoorClassificationScores(archeryRound = ALL_INDOOR_ROUNDS.getValue("worcester"), bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT)
        assertEquals(listOf(300, 299, 289, 264, 226, 162, 96, 65), scores)
    }

    @Test
    fun testAgbOldIndoorClassificationScoresInvalidRound() {
        assertFailsContaining("This round is not recognised for the purposes of indoor classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            val myRound = Round("Some Roundname", listOf(Pass.atTarget(36, ScoringSystem.fromValue("10_zone"), Quantity.cm(122.0), Quantity.metres(70.0))))
            agbOldIndoorClassificationScores(archeryRound = myRound, bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbOldIndoorClassificationScoresInvalidStringRound() {
        assertFailsContaining("This round is not recognised for the purposes of indoor classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            agbOldIndoorClassificationScores(archeryRound = "invalid_roundname", bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbOldIndoorClassificationScoresStringRound() {
        val scores = agbOldIndoorClassificationScores(archeryRound = "portsmouth", bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT)
        assertEquals(listOf(581, 570, 554, 529, 484, 396, 279, 206), scores)
    }

    @Test
    fun testAgbOldIndoorClassificationScoresNonStrictRound() {
        val frostbite = Rounds.misc.getValue("frostbite")
        val scores = agbOldIndoorClassificationScores(archeryRound = frostbite, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT, strictRounds = false)
        assertEquals(listOf(357, 351, 336, 310, 269, 195, 110, 68), scores)
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
                    val scores = agbOldIndoorClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT, strictRounds = false)
                }
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }
}

class TestCalculateAgbOldIndoorClassification {
    @Test
    fun testCalculateAgbOldIndoorClassification() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(400, AgbGender.MALE, "F"),
            "" to arrayOf<Any?>(337, AgbGender.FEMALE, "F"),
            "" to arrayOf<Any?>(592, AgbGender.MALE, "A"),
            "" to arrayOf<Any?>(582, AgbGender.FEMALE, "A"),
            "" to arrayOf<Any?>(581, AgbGender.MALE, "C"),
            "" to arrayOf<Any?>(120, AgbGender.MALE, "UC"),
            "" to arrayOf<Any?>(1, AgbGender.MALE, "UC"),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val score = c[0] as Number
                val gender = c[1] as AgbGender
                val classExpected = c[2] as String
                val classReturned = calculateAgbOldIndoorClassification(score = score, archeryRound = ALL_INDOOR_ROUNDS.getValue("portsmouth"), bowstyle = AgbBowstyle.RECURVE, gender = gender, ageGroup = AgbAge.ADULT)
                assertEquals(classExpected, classReturned)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testCalculateAgbOldIndoorClassificationInvalidScores() {
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
                    calculateAgbOldIndoorClassification(score = score, archeryRound = ALL_INDOOR_ROUNDS.getValue("portsmouth"), bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT)
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
            calculateAgbOldIndoorClassification(archeryRound = myRound, score = 666, bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbIndoorClassificationScoresInvalidStringRound() {
        assertFailsContaining("This round is not recognised for the purposes of indoor classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            calculateAgbOldIndoorClassification(archeryRound = "invalid_roundname", score = 666, bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbIndoorClassificationScoresStringRound() {
        val myClass = calculateAgbOldIndoorClassification(archeryRound = "portsmouth", score = 578, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT)
        assertEquals("B", myClass)
    }

    @Test
    fun testAgbOldIndoorClassificationNonStrictRound() {
        val frostbite = Rounds.misc.getValue("frostbite")
        val myClass = calculateAgbOldIndoorClassification(archeryRound = frostbite, score = 315, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT, strictRounds = false)
        assertEquals("D", myClass)
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
                    val scores = calculateAgbOldIndoorClassification(archeryRound = archeryRound, score = 123, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT, strictRounds = false)
                }
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }
}
