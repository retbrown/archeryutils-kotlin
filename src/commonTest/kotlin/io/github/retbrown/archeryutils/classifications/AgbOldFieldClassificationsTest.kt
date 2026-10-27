@file:Suppress("UNCHECKED_CAST")

package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Pass
import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.rounds.Rounds
import io.github.retbrown.archeryutils.targets.Quantity
import io.github.retbrown.archeryutils.targets.ScoringSystem
import kotlin.test.Test
import kotlin.test.assertEquals

// Ported from tests/unit/classifications/test_agb_old_field.py

private val ALL_AGBFIELD_ROUNDS: Map<String, Round> = Rounds.waField

class TestAgbOldFieldClassificationScores {
    @Test
    fun testAgbOldFieldClassificationScoresAges() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbAge.ADULT, listOf(328, 307, 279, 252, 224, 197)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbAge.UNDER_18, listOf(298, 279, 254, 229, 204, 179)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val ageGroup = c[1] as AgbAge
                val scoresExpected = c[2] as List<Int>
                val scores = agbOldFieldClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.MALE, ageGroup = ageGroup)
                assertEquals(scoresExpected, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOldFieldClassificationScoresCoaxedAges() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbAge.OVER_50, listOf(328, 307, 279, 252, 224, 197)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbAge.UNDER_21, listOf(328, 307, 279, 252, 224, 197)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbAge.UNDER_18, listOf(298, 279, 254, 229, 204, 179)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbAge.ADULT, listOf(328, 307, 279, 252, 224, 197)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val ageGroup = c[1] as AgbAge
                val scoresExpected = c[2] as List<Int>
                val coaxedVals = coaxOldFieldGroup(bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.MALE, ageGroup = ageGroup)
                val scores = agbOldFieldClassificationScores(archeryRound = archeryRound, bowstyle = coaxedVals.bowstyle, gender = coaxedVals.gender, ageGroup = coaxedVals.ageGroup)
                assertEquals(scoresExpected, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOldFieldClassificationScoresGenders() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbGender.MALE, AgbAge.ADULT, listOf(328, 307, 279, 252, 224, 197)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbGender.FEMALE, AgbAge.ADULT, listOf(303, 284, 258, 233, 207, 182)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbGender.MALE, AgbAge.UNDER_18, listOf(298, 279, 254, 229, 204, 179)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbGender.FEMALE, AgbAge.UNDER_18, listOf(251, 236, 214, 193, 172, 151)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val gender = c[1] as AgbGender
                val ageGroup = c[2] as AgbAge
                val scoresExpected = c[3] as List<Int>
                val scores = agbOldFieldClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.BAREBOW, gender = gender, ageGroup = ageGroup)
                assertEquals(scoresExpected, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOldFieldClassificationScoresBowstyles() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), AgbBowstyle.COMPOUND, listOf(393, 377, 344, 312, 279, 247)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), AgbBowstyle.RECURVE, listOf(338, 317, 288, 260, 231, 203)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbBowstyle.BAREBOW, listOf(328, 307, 279, 252, 224, 197)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbBowstyle.TRADITIONAL, listOf(262, 245, 223, 202, 178, 157)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbBowstyle.FLATBOW, listOf(262, 245, 223, 202, 178, 157)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbBowstyle.LONGBOW, listOf(201, 188, 171, 155, 137, 121)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbBowstyle.ENGLISHLONGBOW, listOf(201, 188, 171, 155, 137, 121)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val bowstyle = c[1] as AgbBowstyle
                val scoresExpected = c[2] as List<Int>
                val scores = agbOldFieldClassificationScores(archeryRound = archeryRound, bowstyle = bowstyle, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT)
                assertEquals(scoresExpected, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOldFieldClassificationScoresInvalidRound() {
        assertFailsContaining("This round is not recognised for the purposes of old AGB field classifications.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            val myRound = Round("Some Roundname", listOf(Pass.atTarget(36, ScoringSystem.fromValue("10_zone"), Quantity.cm(122.0), Quantity.metres(70.0))))
            agbOldFieldClassificationScores(archeryRound = myRound, bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbOldFieldClassificationScoresInvalidStringRound() {
        assertFailsContaining("This round is not recognised for the purposes of old AGB field classifications.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            agbOldFieldClassificationScores(archeryRound = "invalid_roundname", bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbOldFieldClassificationScoresStringRound() {
        val scores = agbOldFieldClassificationScores(archeryRound = "wa_field_24_blue_marked", bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT)
        assertEquals(listOf(328, 307, 279, 252, 224, 197), scores)
    }
}

class TestCalculateOldAgbFieldClassification {
    @Test
    fun testCalculateAgbOldFieldClassification() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), 400, AgbAge.ADULT, AgbBowstyle.COMPOUND, "GMB"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 177, AgbAge.UNDER_18, AgbBowstyle.TRADITIONAL, "1C"),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val score = c[1] as Number
                val ageGroup = c[2] as AgbAge
                val bowstyle = c[3] as AgbBowstyle
                val classExpected = c[4] as String
                val classReturned = calculateAgbOldFieldClassification(archeryRound = archeryRound, score = score, bowstyle = bowstyle, gender = AgbGender.MALE, ageGroup = ageGroup)
                assertEquals(classExpected, classReturned)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testCalculateAgbOldFieldClassificationCoaxedAges() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), 337, AgbAge.OVER_50, AgbBowstyle.RECURVE, "MB"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 306, AgbAge.UNDER_21, AgbBowstyle.BAREBOW, "B"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 143, AgbAge.UNDER_12, AgbBowstyle.FLATBOW, "2C"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 96, AgbAge.UNDER_12, AgbBowstyle.LONGBOW, "3C"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 1, AgbAge.UNDER_12, AgbBowstyle.LONGBOW, "UC"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 1, AgbAge.UNDER_12, AgbBowstyle.ENGLISHLONGBOW, "UC"),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val score = c[1] as Number
                val ageGroup = c[2] as AgbAge
                val bowstyle = c[3] as AgbBowstyle
                val classExpected = c[4] as String
                val coaxedVals = coaxOldFieldGroup(bowstyle = bowstyle, gender = AgbGender.MALE, ageGroup = ageGroup)
                val classReturned = calculateAgbOldFieldClassification(archeryRound = archeryRound, score = score, bowstyle = coaxedVals.bowstyle, gender = coaxedVals.gender, ageGroup = coaxedVals.ageGroup)
                assertEquals(classExpected, classReturned)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testCalculateAgbOldFieldClassificationInvalidRounds() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 400, AgbBowstyle.COMPOUND, "UC"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), 337, AgbBowstyle.BAREBOW, "UC"),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val score = c[1] as Number
                val bowstyle = c[2] as AgbBowstyle
                val classExpected = c[3] as String
                val classReturned = calculateAgbOldFieldClassification(archeryRound = archeryRound, score = score, bowstyle = bowstyle, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT)
                assertEquals(classExpected, classReturned)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testCalculateAgbOldFieldClassificationInvalidScores() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 1000),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 433),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), -1),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), -100),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Round
                val score = c[1] as Number
                assertFailsContaining("Invalid score of ${score} for a ${archeryRound.name}. Should be in range 0-${archeryRound.maxScore()}.") {
                    calculateAgbOldFieldClassification(score = score, archeryRound = archeryRound, bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT)
                }
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOldFieldClassificationScoresInvalidStringRound() {
        assertFailsContaining("This round is not recognised for the purposes of old AGB field classifications.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            calculateAgbOldFieldClassification(archeryRound = "invalid_roundname", score = 333, bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbOldFieldClassificationScoresStringRound() {
        val myClass = calculateAgbOldFieldClassification(archeryRound = "wa_field_24_blue_marked", score = 252, bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT)
        assertEquals("1C", myClass)
    }
}
