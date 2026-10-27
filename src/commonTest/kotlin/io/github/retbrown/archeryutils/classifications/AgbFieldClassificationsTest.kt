@file:Suppress("UNCHECKED_CAST")

package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Pass
import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.rounds.Rounds
import io.github.retbrown.archeryutils.targets.Quantity
import io.github.retbrown.archeryutils.targets.ScoringSystem
import kotlin.test.Test
import kotlin.test.assertEquals

// Ported from tests/unit/classifications/test_agb_field.py

private val ALL_AGBFIELD_ROUNDS: Map<String, Round> = Rounds.waField

private val CUSTOM_ROUND = Round("My Custom Round", listOf(Pass.atTarget(30, ScoringSystem.fromValue("WA_field"), Quantity.cm(80.0), Quantity(48.5, "m"), indoor = false), Pass.atTarget(30, ScoringSystem.fromValue("WA_field"), Quantity.cm(60.0), Quantity(46.3, "m"), indoor = false), Pass.atTarget(30, ScoringSystem.fromValue("WA_field"), Quantity.cm(40.0), Quantity(23.25, "m"), indoor = false), Pass.atTarget(30, ScoringSystem.fromValue("WA_field"), Quantity.cm(20.0), Quantity(12.75, "m"), indoor = false)), location = "Field")

class TestAgbFieldClassificationScores {
    @Test
    fun testAgbFieldClassificationScoresAges() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbAge.ADULT, listOf(336, 311, 283, 249, 212, 173, 135, 101, 74)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbAge.OVER_50, listOf(321, 294, 263, 227, 188, 149, 114, 84, 60)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbAge.UNDER_18, listOf(305, 275, 241, 203, 164, 127, 94, 68, 48)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbAge.UNDER_12, listOf(224, 185, 146, 111, 82, 58, 41, 28, 19)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val ageGroup = c[1] as AgbAge
                val scoresExpected = c[2] as List<Int>
                val scores = agbFieldClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.OPEN, ageGroup = ageGroup)
                assertEquals(scoresExpected, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbFieldClassificationScoresNonages() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbAge.UNDER_21, listOf(336, 311, 283, 249, 212, 173, 135, 101, 74)),
            "Test valid values pass through coaxing unchanged" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbAge.ADULT, listOf(336, 311, 283, 249, 212, 173, 135, 101, 74)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val ageGroup = c[1] as AgbAge
                val scoresExpected = c[2] as List<Int>
                val coaxedVals = coaxFieldGroup(bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.OPEN, ageGroup = ageGroup)
                val scores = agbFieldClassificationScores(archeryRound = archeryRound, bowstyle = coaxedVals.bowstyle, gender = coaxedVals.gender, ageGroup = coaxedVals.ageGroup)
                assertEquals(scoresExpected, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbFieldClassificationScoresMale() {
        val scores = agbFieldClassificationScores(archeryRound = ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.MALE, ageGroup = AgbAge.ADULT)
        assertEquals(listOf(336, 311, 283, 249, 212, 173, 135, 101, 74), scores)
    }

    @Test
    fun testAgbFieldClassificationScoresGenders() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbGender.OPEN, AgbAge.ADULT, listOf(336, 311, 283, 249, 212, 173, 135, 101, 74)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbGender.FEMALE, AgbAge.ADULT, listOf(315, 287, 255, 218, 179, 140, 106, 78, 55)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbGender.OPEN, AgbAge.UNDER_18, listOf(305, 275, 241, 203, 164, 127, 94, 68, 48)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbGender.FEMALE, AgbAge.UNDER_18, listOf(280, 247, 209, 170, 132, 99, 72, 51, 35)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val gender = c[1] as AgbGender
                val ageGroup = c[2] as AgbAge
                val scoresExpected = c[3] as List<Int>
                val scores = agbFieldClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.BAREBOW, gender = gender, ageGroup = ageGroup)
                assertEquals(scoresExpected, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbFieldClassificationScoresBowstyles() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), AgbBowstyle.COMPOUND, listOf(408, 391, 369, 345, 318, 286, 248, 204, 157)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_12_red_unmarked"), AgbBowstyle.COMPOUND, listOf(-9999, -9999, -9999, 173, 159, 143, 124, 102, 79)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), AgbBowstyle.COMPOUNDLIMITED, listOf(369, 347, 322, 293, 259, 219, 176, 133, 96)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbBowstyle.COMPOUNDBAREBOW, listOf(343, 321, 296, 268, 235, 200, 164, 129, 99)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), AgbBowstyle.RECURVE, listOf(369, 343, 314, 279, 237, 189, 139, 96, 62)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbBowstyle.BAREBOW, listOf(336, 311, 283, 249, 212, 173, 135, 101, 74)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbBowstyle.TRADITIONAL, listOf(309, 283, 252, 218, 182, 146, 114, 86, 63)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbBowstyle.FLATBOW, listOf(273, 244, 212, 179, 146, 116, 90, 68, 51)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbBowstyle.LONGBOW, listOf(241, 209, 176, 143, 114, 88, 67, 49, 36)),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), AgbBowstyle.ENGLISHLONGBOW, listOf(241, 209, 176, 143, 114, 88, 67, 49, 36)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val bowstyle = c[1] as AgbBowstyle
                val scoresExpected = c[2] as List<Int>
                val scores = agbFieldClassificationScores(archeryRound = archeryRound, bowstyle = bowstyle, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
                assertEquals(scoresExpected, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbFieldClassificationScoresInvalidRound() {
        assertFailsContaining("This round is not recognised for the purposes of field classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            val myRound = Round("Some Roundname", listOf(Pass.atTarget(36, ScoringSystem.fromValue("10_zone"), Quantity.cm(122.0), Quantity.metres(70.0))))
            agbFieldClassificationScores(archeryRound = myRound, bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbFieldClassificationScoresInvalidStringRound() {
        assertFailsContaining("This round is not recognised for the purposes of field classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            agbFieldClassificationScores(archeryRound = "invalid_roundname", bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbFieldClassificationScoresStringRound() {
        val scores = agbFieldClassificationScores(archeryRound = "wa_field_24_blue_marked", bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
        assertEquals(listOf(336, 311, 283, 249, 212, 173, 135, 101, 74), scores)
    }

    @Test
    fun testAgbFieldClassificationScoresNonStrict() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_12_white_marked"), listOf(185, 174, 162, 149, 134, 116, 97, 77, 58)),
            "" to arrayOf<Any?>(CUSTOM_ROUND, listOf(480, 426, 364, 296, 228, 167, 118, 80, 54)),
            "" to arrayOf<Any?>(Rounds.waOutdoor.getValue("wa1440_90"), listOf(1042, 926, 793, 652, 514, 389, 283, 199, 136)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val expectedScores = c[1] as List<Int>
                val scores = agbFieldClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT, strictRounds = false, strictDistance = false)
                assertEquals(expectedScores, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbFieldClassificationScoresNonStrictRound() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "Check red-peg returns no classifications for unsighted." to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, -9999, -9999, -9999, -9999, -9999, -9999)),
            "Check wrong 24-target returns distance-limited classifications." to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_yellow_marked"), AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, -9999, -9999, -9999, 178, 140, 106)),
            "Check custom round (48.5m) returns up to A2." to arrayOf<Any?>(CUSTOM_ROUND, AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, -9999, -9999, -9999, -9999, 167, 108)),
            "Check frostbite returns A3." to arrayOf<Any?>(Rounds.misc.getValue("frostbite"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, -9999, -9999, -9999, -9999, -9999, 128)),
            "Check frostbite returns all for U12." to arrayOf<Any?>(Rounds.misc.getValue("frostbite"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.UNDER_12, listOf(286, 256, 217, 170, 123, 82, 52, 32, 19)),
            "Check 20y returns nothing." to arrayOf<Any?>(Rounds.agbIndoor.getValue("portsmouth"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, -9999, -9999, -9999, -9999, -9999, -9999)),
            "Check 60m 1440 returns nothing for unsighted." to arrayOf<Any?>(Rounds.waOutdoor.getValue("wa1440_60"), AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, -9999, -9999, -9999, -9999, -9999, -9999)),
            "Check 60m 1440 returns scores for sighted." to arrayOf<Any?>(Rounds.waOutdoor.getValue("wa1440_60"), AgbBowstyle.COMPOUND, AgbGender.OPEN, AgbAge.ADULT, listOf(1423, 1400, 1366, 1322, 1264, 1189, 1091, 965, 810)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val bowstyle = c[1] as AgbBowstyle
                val gender = c[2] as AgbGender
                val ageGroup = c[3] as AgbAge
                val expectedScores = c[4] as List<Int>
                val scores = agbFieldClassificationScores(archeryRound = archeryRound, bowstyle = bowstyle, gender = gender, ageGroup = ageGroup, strictRounds = false, strictDistance = true)
                assertEquals(expectedScores, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbFieldClassificationScoresNonStrictRoundString() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>("wa_field_24_red_marked"),
            "" to arrayOf<Any?>("fake_round"),
            "" to arrayOf<Any?>(""),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                assertFailsContaining("strict_rounds is False so archery_round must be explicitly specified as a Round type instead of a string.") {
                    val scores = agbFieldClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT, strictRounds = false)
                }
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbFieldClassificationScoresNonStrictDistance() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "Check red-peg returns scores for unsighted." to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT, listOf(306, 277, 243, 204, 164, 125, 91, 64, 44)),
            "Check 12-target red-peg returns B1 scores for unsighted." to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_12_red_marked"), AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, 102, 82, 63, 46, 32, 22)),
            "Check short peg 24-target returns all scores." to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_yellow_marked"), AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT, listOf(360, 338, 313, 285, 253, 217, 178, 140, 106)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val bowstyle = c[1] as AgbBowstyle
                val gender = c[2] as AgbGender
                val ageGroup = c[3] as AgbAge
                val expectedScores = c[4] as List<Int>
                val scores = agbFieldClassificationScores(archeryRound = archeryRound, bowstyle = bowstyle, gender = gender, ageGroup = ageGroup, strictRounds = true, strictDistance = false)
                assertEquals(expectedScores, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }
}

class TestCalculateAgbFieldClassification {
    @Test
    fun testCalculateAgbFieldClassification() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), 400, AgbAge.ADULT, AgbBowstyle.COMPOUND, "GMB"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_12_red_marked"), 200, AgbAge.ADULT, AgbBowstyle.COMPOUND, "B1"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 400, AgbAge.ADULT, AgbBowstyle.COMPOUND, "A1"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_yellow_marked"), 400, AgbAge.ADULT, AgbBowstyle.COMPOUND, "A2"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_white_marked"), 400, AgbAge.ADULT, AgbBowstyle.COMPOUND, "A3"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), 337, AgbAge.OVER_50, AgbBowstyle.RECURVE, "GMB"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 177, AgbAge.UNDER_18, AgbBowstyle.TRADITIONAL, "B1"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), 177, AgbAge.UNDER_18, AgbBowstyle.TRADITIONAL, "UC"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_12_blue_marked"), 88, AgbAge.UNDER_18, AgbBowstyle.TRADITIONAL, "B1"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_yellow_marked"), 400, AgbAge.UNDER_18, AgbBowstyle.TRADITIONAL, "A1"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_white_marked"), 400, AgbAge.UNDER_18, AgbBowstyle.TRADITIONAL, "A2"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 143, AgbAge.UNDER_12, AgbBowstyle.FLATBOW, "EMB"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 96, AgbAge.UNDER_12, AgbBowstyle.LONGBOW, "EMB"),
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
                val classReturned = calculateAgbFieldClassification(archeryRound = archeryRound, score = score, bowstyle = bowstyle, gender = AgbGender.OPEN, ageGroup = ageGroup)
                assertEquals(classExpected, classReturned)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testCalculateAgbFieldClassificationNonages() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), 306, AgbAge.UNDER_21, AgbBowstyle.BAREBOW, "MB"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), 306, AgbAge.UNDER_21, AgbBowstyle.BAREBOW, "UC"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), 306, AgbAge.UNDER_21, AgbBowstyle.RECURVE, "B1"),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val score = c[1] as Number
                val ageGroup = c[2] as AgbAge
                val bowstyle = c[3] as AgbBowstyle
                val classExpected = c[4] as String
                val coaxedVals = coaxFieldGroup(bowstyle = bowstyle, gender = AgbGender.OPEN, ageGroup = ageGroup)
                val classReturned = calculateAgbFieldClassification(archeryRound = archeryRound, score = score, bowstyle = coaxedVals.bowstyle, gender = coaxedVals.gender, ageGroup = coaxedVals.ageGroup)
                assertEquals(classExpected, classReturned)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testCalculateAgbFieldClassificationInvalidRounds() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_unmarked"), 400, AgbBowstyle.ENGLISHLONGBOW, "UC"),
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), 337, AgbBowstyle.BAREBOW, "UC"),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val score = c[1] as Number
                val bowstyle = c[2] as AgbBowstyle
                val classExpected = c[3] as String
                val classReturned = calculateAgbFieldClassification(archeryRound = archeryRound, score = score, bowstyle = bowstyle, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
                assertEquals(classExpected, classReturned)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testCalculateAgbFieldClassificationInvalidScores() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(1000),
            "" to arrayOf<Any?>(433),
            "" to arrayOf<Any?>(-1),
            "" to arrayOf<Any?>(-100),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val score = c[0] as Number
                assertFailsContaining("Invalid score of ${score} for a ${ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked").name}. Should be in range 0-${ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked").maxScore()}.") {
                    calculateAgbFieldClassification(score = score, archeryRound = ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_blue_marked"), bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
                }
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbFieldClassificationInvalidRound() {
        assertFailsContaining("This round is not recognised for the purposes of field classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            val myRound = Round("Some Roundname", listOf(Pass.atTarget(36, ScoringSystem.fromValue("10_zone"), Quantity.cm(122.0), Quantity.metres(70.0))))
            calculateAgbFieldClassification(archeryRound = myRound, score = 333, bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbFieldClassificationScoresInvalidStringRound() {
        assertFailsContaining("This round is not recognised for the purposes of field classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            calculateAgbFieldClassification(archeryRound = "invalid_roundname", score = 333, bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbFieldClassificationScoresStringRound() {
        val myClass = calculateAgbFieldClassification(archeryRound = "wa_field_24_blue_marked", score = 252, bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
        assertEquals("B1", myClass)
    }

    @Test
    fun testAgbFieldClassificationNonStrict() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_12_white_marked"), 214, "MB"),
            "" to arrayOf<Any?>(CUSTOM_ROUND, 500, "B3"),
            "" to arrayOf<Any?>(Rounds.misc.getValue("frostbite"), 358, "GMB"),
            "" to arrayOf<Any?>(Rounds.agbIndoor.getValue("portsmouth"), 595, "MB"),
            "Check max score on Portsmouth capped to GMB." to arrayOf<Any?>(Rounds.agbIndoor.getValue("portsmouth"), 600, "GMB"),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val score = c[1] as Number
                val expectedClass = c[2] as Any
                val frostbite = Rounds.misc.getValue("frostbite")
                val myClass = calculateAgbFieldClassification(archeryRound = archeryRound, score = score, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT, strictRounds = false, strictDistance = false)
                assertEquals(expectedClass, myClass)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbFieldClassificationNonStrictRound() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "Check non-24 target red peg can give MB." to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_12_red_marked"), AgbBowstyle.COMPOUND, AgbGender.OPEN, AgbAge.ADULT, 214, "EMB"),
            "Check short 24-target prestige limited to A." to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_yellow_marked"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, 412, "A2"),
            "Check custom round (48.5m) returns max A2." to arrayOf<Any?>(CUSTOM_ROUND, AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, 720, "A2"),
            "Check frostbite returns A3." to arrayOf<Any?>(Rounds.misc.getValue("frostbite"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, 300, "A3"),
            "Check indoor 20y returns nothing." to arrayOf<Any?>(Rounds.agbIndoor.getValue("portsmouth"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, 600, "UC"),
            "Check 60m 1440 returns UC for unsighted." to arrayOf<Any?>(Rounds.waOutdoor.getValue("wa1440_60"), AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT, 1100, "UC"),
            "Check 60m 1440 returns MB for sighted." to arrayOf<Any?>(Rounds.waOutdoor.getValue("wa1440_60"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, 1400, "EMB"),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val bowstyle = c[1] as AgbBowstyle
                val gender = c[2] as AgbGender
                val ageGroup = c[3] as AgbAge
                val score = c[4] as Number
                val expectedClass = c[5] as Any
                val myClass = calculateAgbFieldClassification(archeryRound = archeryRound, score = score, bowstyle = bowstyle, gender = gender, ageGroup = ageGroup, strictRounds = false, strictDistance = true)
                assertEquals(expectedClass, myClass)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbFieldClassificationNonStrictRoundString() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>("wa_field_24_red_marked"),
            "" to arrayOf<Any?>("fake_round"),
            "" to arrayOf<Any?>(""),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                assertFailsContaining("strict_rounds is False so archery_round must be explicitly specified as a Round type instead of a string.") {
                    val scores = calculateAgbFieldClassification(archeryRound = archeryRound, score = 123, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT, strictRounds = false)
                }
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbFieldClassificationNonStrictDistance() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "Check red-peg returns MB scores for unsighted." to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_red_marked"), AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT, 360, "EMB"),
            "Check 12-target red-peg returns B1 for unsighted." to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_12_red_marked"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, 175, "B1"),
            "Check short peg 24-target returns MB." to arrayOf<Any?>(ALL_AGBFIELD_ROUNDS.getValue("wa_field_24_yellow_marked"), AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT, 340, "GMB"),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val bowstyle = c[1] as AgbBowstyle
                val gender = c[2] as AgbGender
                val ageGroup = c[3] as AgbAge
                val score = c[4] as Number
                val expectedClass = c[5] as Any
                val myClass = calculateAgbFieldClassification(archeryRound = archeryRound, score = score, bowstyle = bowstyle, gender = gender, ageGroup = ageGroup, strictRounds = true, strictDistance = false)
                assertEquals(expectedClass, myClass)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }
}
