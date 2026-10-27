@file:Suppress("UNCHECKED_CAST")

package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Pass
import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.rounds.Rounds
import io.github.retbrown.archeryutils.targets.Quantity
import io.github.retbrown.archeryutils.targets.ScoringSystem
import kotlin.test.Test
import kotlin.test.assertEquals

// Ported from tests/unit/classifications/test_agb_outdoor.py

private val ALL_OUTDOOR_ROUNDS: Map<String, Round> = Rounds.agbOutdoorImperial + Rounds.agbOutdoorMetric + Rounds.waOutdoor

private val CUSTOM_ROUND = Round("My Custom Round", listOf(Pass.atTarget(30, ScoringSystem.fromValue("10_zone"), Quantity.cm(122.0), Quantity(70.0, "m"), indoor = false), Pass.atTarget(30, ScoringSystem.fromValue("10_zone"), Quantity.cm(80.0), Quantity(50.0, "m"), indoor = false)), location = "Outdoor Target")

class TestAgbOutdoorClassificationScores {
    @Test
    fun testAgbOutdoorClassificationScoresAges() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_90"), AgbAge.ADULT, listOf(426, 566, 717, 866, 999, 1110, 1197, 1266, 1320)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_70"), AgbAge.ADULT, listOf(185, 259, 343, 425, 496, 552, 597, 631, 659)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), AgbAge.OVER_50, listOf(364, 503, 659, 817, 960, 1079, 1173, 1247, 1305)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_90"), AgbAge.UNDER_21, listOf(313, 435, 577, 728, 877, 1008, 1117, 1203, 1270)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), AgbAge.UNDER_18, listOf(259, 373, 514, 671, 828, 969, 1086, 1179, 1252)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_60"), AgbAge.UNDER_16, listOf(227, 335, 474, 635, 799, 946, 1068, 1165, 1241)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_iii"), AgbAge.UNDER_15, listOf(270, 389, 534, 693, 849, 988, 1101, 1191, 1261)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_iv"), AgbAge.UNDER_14, listOf(396, 524, 666, 814, 952, 1070, 1166, 1242, 1301)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_v"), AgbAge.UNDER_12, listOf(406, 550, 706, 858, 992, 1104, 1193, 1263, 1317)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val ageGroup = c[1] as AgbAge
                val scoresExpected = c[2] as List<Int>
                val scores = agbOutdoorClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.OPEN, ageGroup = ageGroup)
                assertEquals(scoresExpected.reversed(), scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOutdoorClassificationScoresGenders() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), AgbAge.ADULT, listOf(392, 536, 693, 849, 988, 1101, 1191, 1261, 1316)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_iii"), AgbAge.UNDER_16, listOf(293, 418, 567, 727, 881, 1014, 1122, 1207, 1274)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_iii"), AgbAge.UNDER_15, listOf(270, 389, 534, 693, 849, 988, 1101, 1191, 1261)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_v"), AgbAge.UNDER_12, listOf(406, 550, 706, 858, 992, 1104, 1193, 1263, 1317)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val ageGroup = c[1] as AgbAge
                val scoresExpected = c[2] as List<Int>
                val scores = agbOutdoorClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.FEMALE, ageGroup = ageGroup)
                assertEquals(scoresExpected.reversed(), scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOutdoorClassificationScoresBowstyles() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_90"), AgbBowstyle.COMPOUND, AgbGender.OPEN, listOf(866, 982, 1081, 1162, 1229, 1283, 1327, 1362, 1389)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), AgbBowstyle.COMPOUND, AgbGender.FEMALE, listOf(870, 988, 1086, 1167, 1233, 1286, 1330, 1364, 1392)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_90"), AgbBowstyle.BAREBOW, AgbGender.OPEN, listOf(290, 380, 484, 598, 717, 835, 945, 1042, 1124)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), AgbBowstyle.BAREBOW, AgbGender.FEMALE, listOf(252, 338, 441, 558, 682, 806, 921, 1023, 1108)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_90"), AgbBowstyle.LONGBOW, AgbGender.OPEN, listOf(85, 124, 177, 248, 337, 445, 566, 696, 825)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), AgbBowstyle.LONGBOW, AgbGender.FEMALE, listOf(64, 94, 136, 195, 274, 373, 493, 625, 761)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), AgbBowstyle.ENGLISHLONGBOW, AgbGender.FEMALE, listOf(64, 94, 136, 195, 274, 373, 493, 625, 761)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val bowstyle = c[1] as AgbBowstyle
                val gender = c[2] as AgbGender
                val scoresExpected = c[3] as List<Int>
                val scores = agbOutdoorClassificationScores(archeryRound = archeryRound, bowstyle = bowstyle, gender = gender, ageGroup = AgbAge.ADULT)
                assertEquals(scoresExpected.reversed(), scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOutdoorClassificationScoresNonbowstyles() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_90"), AgbBowstyle.FLATBOW, AgbGender.OPEN, listOf(290, 380, 484, 598, 717, 835, 945, 1042, 1124)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), AgbBowstyle.TRADITIONAL, AgbGender.FEMALE, listOf(252, 338, 441, 558, 682, 806, 921, 1023, 1108)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), AgbBowstyle.COMPOUNDBAREBOW, AgbGender.FEMALE, listOf(870, 988, 1086, 1167, 1233, 1286, 1330, 1364, 1392)),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), AgbBowstyle.COMPOUNDLIMITED, AgbGender.FEMALE, listOf(870, 988, 1086, 1167, 1233, 1286, 1330, 1364, 1392)),
            "Check coaxing and a valid bowstyle" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), AgbBowstyle.COMPOUND, AgbGender.FEMALE, listOf(870, 988, 1086, 1167, 1233, 1286, 1330, 1364, 1392)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val bowstyle = c[1] as AgbBowstyle
                val gender = c[2] as AgbGender
                val scoresExpected = c[3] as List<Int>
                val coaxedVals = coaxOutdoorGroup(bowstyle = bowstyle, gender = gender, ageGroup = AgbAge.ADULT)
                val scores = agbOutdoorClassificationScores(archeryRound = archeryRound, bowstyle = coaxedVals.bowstyle, gender = coaxedVals.gender, ageGroup = coaxedVals.ageGroup)
                assertEquals(scoresExpected.reversed(), scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOutdoorClassificationScoresMale() {
        val scores = agbOutdoorClassificationScores(ALL_OUTDOOR_ROUNDS.getValue("wa1440_90"), AgbBowstyle.BAREBOW, AgbGender.MALE, AgbAge.ADULT)
        assertEquals(listOf(1124, 1042, 945, 835, 717, 598, 484, 380, 290), scores)
    }

    @Test
    fun testAgbOutdoorClassificationScoresSmallFaces() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_90_small"), listOf(866, 982, 1081, 1162, 1229, 1283, 1327, 1362, 1389)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val scoresExpected = c[1] as List<Int>
                val scores = agbOutdoorClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
                assertEquals(scoresExpected.reversed(), scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOutdoorClassificationScoresInvalidRound() {
        assertFailsContaining("This round is not recognised for the purposes of outdoor classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            val myRound = Round("Some Roundname", listOf(Pass.atTarget(36, ScoringSystem.fromValue("10_zone"), Quantity.cm(122.0), Quantity.metres(70.0))))
            agbOutdoorClassificationScores(archeryRound = myRound, bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbOutdoorClassificationScoresInvalidStringRound() {
        assertFailsContaining("This round is not recognised for the purposes of outdoor classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            agbOutdoorClassificationScores(archeryRound = "invalid_roundname", bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbOutdoorClassificationScoresStringRound() {
        val scores = agbOutdoorClassificationScores(archeryRound = "wa1440_70", bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        assertEquals(listOf(870, 988, 1086, 1167, 1233, 1286, 1330, 1364, 1392).reversed(), scores)
    }

    @Test
    fun testAgbOutdoorClassificationScoresNonStrict() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("st_george"), listOf(961, 947, 924, 891, 848, 795, 729, 651, 562)),
            "" to arrayOf<Any?>(CUSTOM_ROUND, listOf(579, 567, 552, 533, 510, 482, 447, 405, 354)),
            "" to arrayOf<Any?>(Rounds.misc.getValue("frostbite"), listOf(360, 357, 353, 347, 339, 330, 318, 304, 286)),
            "" to arrayOf<Any?>(Rounds.agbIndoor.getValue("portsmouth"), listOf(-9999, -9999, 598, 592, 583, 572, 557, 539, 516)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val expectedScores = c[1] as List<Int>
                val scores = agbOutdoorClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT, strictRounds = false, strictDistance = false)
                assertEquals(expectedScores, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOutdoorClassificationScoresNonStrictRound() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "Check long non-prestige returns all classifications." to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("st_george"), AgbBowstyle.COMPOUND, AgbGender.OPEN, AgbAge.ADULT, listOf(961, 947, 924, 891, 848, 795, 729, 651, 562)),
            "Check short 720 prestige no-longer returns all classifications." to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_70"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, -9999, 496, 425, 343, 259, 185)),
            "Check wrong prestige returns distance-limited classifications." to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_50_c"), AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, -9999, -9999, -9999, 212, 159, 117)),
            "Check custom round (70m) returns up to B2." to arrayOf<Any?>(CUSTOM_ROUND, AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, -9999, 413, 354, 285, 215, 153)),
            "Check frostbite returns up to A3." to arrayOf<Any?>(Rounds.misc.getValue("frostbite"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, -9999, -9999, -9999, -9999, -9999, 188)),
            "Check indoor 20y returns nothing for adult." to arrayOf<Any?>(Rounds.agbIndoor.getValue("portsmouth"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, -9999, -9999, -9999, -9999, -9999, -9999)),
            "Check indoor 20y returns up to B3 for U12." to arrayOf<Any?>(Rounds.agbIndoor.getValue("portsmouth"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.UNDER_12, listOf(-9999, -9999, -9999, -9999, -9999, 211, 149, 101, 66)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val bowstyle = c[1] as AgbBowstyle
                val gender = c[2] as AgbGender
                val ageGroup = c[3] as AgbAge
                val expectedScores = c[4] as List<Int>
                val scores = agbOutdoorClassificationScores(archeryRound = archeryRound, bowstyle = bowstyle, gender = gender, ageGroup = ageGroup, strictRounds = false, strictDistance = true)
                assertEquals(expectedScores, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOutdoorClassificationScoresNonStrictRoundString() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>("wa1440_90"),
            "" to arrayOf<Any?>("york"),
            "" to arrayOf<Any?>("fake_round"),
            "" to arrayOf<Any?>(""),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                assertFailsContaining("strict_rounds is False so archery_round must be explicitly specified as a Round type instead of a string.") {
                    val scores = agbOutdoorClassificationScores(archeryRound = archeryRound, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT, strictRounds = false)
                }
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOutdoorClassificationScoresNonStrictDistance() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "Check long non-prestige only returns to B1." to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("st_george"), AgbBowstyle.COMPOUND, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, 891, 848, 795, 729, 651, 562)),
            "Check 720 prestige still returns all classifications." to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_70"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, listOf(659, 631, 597, 552, 496, 425, 343, 259, 185)),
            "Check wrong prestige returns up to B1." to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_50_c"), AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT, listOf(-9999, -9999, -9999, 406, 341, 274, 212, 159, 117)),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val bowstyle = c[1] as AgbBowstyle
                val gender = c[2] as AgbGender
                val ageGroup = c[3] as AgbAge
                val expectedScores = c[4] as List<Int>
                val scores = agbOutdoorClassificationScores(archeryRound = archeryRound, bowstyle = bowstyle, gender = gender, ageGroup = ageGroup, strictRounds = true, strictDistance = false)
                assertEquals(expectedScores, scores)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }
}

class TestCalculateAgbOutdoorClassification {
    @Test
    fun testCalculateAgbOutdoorClassification() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_90"), 1390, AgbAge.ADULT, AgbBowstyle.COMPOUND, "EMB"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), 1382, AgbAge.OVER_50, AgbBowstyle.COMPOUND, "GMB"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_90"), 900, AgbAge.UNDER_21, AgbBowstyle.BAREBOW, "MB"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), 1269, AgbAge.UNDER_18, AgbBowstyle.COMPOUND, "B1"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa1440_70"), 969, AgbAge.UNDER_18, AgbBowstyle.RECURVE, "B1"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_v"), 992, AgbAge.UNDER_12, AgbBowstyle.RECURVE, "B2"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_v"), 222, AgbAge.UNDER_12, AgbBowstyle.LONGBOW, "A1"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_v"), 91, AgbAge.UNDER_12, AgbBowstyle.LONGBOW, "UC"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_v"), 1, AgbAge.UNDER_12, AgbBowstyle.LONGBOW, "UC"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_v"), 250, AgbAge.UNDER_12, AgbBowstyle.LONGBOW, "A1"),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val score = c[1] as Number
                val ageGroup = c[2] as AgbAge
                val bowstyle = c[3] as AgbBowstyle
                val classExpected = c[4] as String
                val classReturned = calculateAgbOutdoorClassification(archeryRound = archeryRound, score = score, bowstyle = bowstyle, gender = AgbGender.OPEN, ageGroup = ageGroup)
                assertEquals(classExpected, classReturned)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testCalculateAgbOutdoorClassificationPrestigeDist() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_70"), 720, AgbAge.ADULT, AgbBowstyle.COMPOUND, "B2"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_50_b"), 720, AgbAge.ADULT, AgbBowstyle.COMPOUND, "A1"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_50_c"), 720, AgbAge.ADULT, AgbBowstyle.COMPOUND, "EMB"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_80_30"), 720, AgbAge.ADULT, AgbBowstyle.COMPOUND, "A3"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_80_30"), 720, AgbAge.UNDER_14, AgbBowstyle.COMPOUND, "B3"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_80_30"), 720, AgbAge.UNDER_12, AgbBowstyle.COMPOUND, "EMB"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_122_50"), 720, AgbAge.UNDER_16, AgbBowstyle.COMPOUND, "B2"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_60"), 720, AgbAge.OVER_50, AgbBowstyle.RECURVE, "EMB"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_60"), 720, AgbAge.UNDER_18, AgbBowstyle.RECURVE, "EMB"),
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("metric_122_50"), 720, AgbAge.UNDER_16, AgbBowstyle.RECURVE, "EMB"),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val score = c[1] as Number
                val ageGroup = c[2] as AgbAge
                val bowstyle = c[3] as AgbBowstyle
                val classExpected = c[4] as String
                val classReturned = calculateAgbOutdoorClassification(archeryRound = archeryRound, score = score, bowstyle = bowstyle, gender = AgbGender.OPEN, ageGroup = ageGroup)
                assertEquals(classExpected, classReturned)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testCalculateAgbOutdoorClassificationInvalidRound() {
        assertFailsContaining("This round is not recognised for the purposes of outdoor classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            calculateAgbOutdoorClassification(archeryRound = "invalid_roundname", score = 400, bowstyle = AgbBowstyle.RECURVE, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testCalculateAgbOutdoorClassificationInvalidScores() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(3000),
            "" to arrayOf<Any?>(1441),
            "" to arrayOf<Any?>(-1),
            "" to arrayOf<Any?>(-100),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val score = c[0] as Number
                assertFailsContaining("Invalid score of ${score} for a ${ALL_OUTDOOR_ROUNDS.getValue("wa1440_90").name}. Should be in range 0-${ALL_OUTDOOR_ROUNDS.getValue("wa1440_90").maxScore()}.") {
                    calculateAgbOutdoorClassification(score = score, archeryRound = ALL_OUTDOOR_ROUNDS.getValue("wa1440_90"), bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT)
                }
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOutdoorClassificationInvalidRound() {
        assertFailsContaining("This round is not recognised for the purposes of outdoor classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            val myRound = Round("Some Roundname", listOf(Pass.atTarget(36, ScoringSystem.fromValue("10_zone"), Quantity.cm(122.0), Quantity.metres(70.0))))
            calculateAgbOutdoorClassification(archeryRound = myRound, score = 666, bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbOutdoorClassificationInvalidStringRound() {
        assertFailsContaining("This round is not recognised for the purposes of outdoor classification.\nPlease select an appropriate option using `archeryutils.load_rounds`.") {
            calculateAgbOutdoorClassification(archeryRound = "invalid_roundname", score = 666, bowstyle = AgbBowstyle.BAREBOW, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        }
    }

    @Test
    fun testAgbOutdoorClassificationStringRound() {
        val myClass = calculateAgbOutdoorClassification(archeryRound = "wa1440_70", score = 1200, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.FEMALE, ageGroup = AgbAge.ADULT)
        assertEquals("B3", myClass)
    }

    @Test
    fun testAgbOutdoorClassificationNonStrict() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("st_george"), 930, "MB"),
            "" to arrayOf<Any?>(CUSTOM_ROUND, 500, "B3"),
            "" to arrayOf<Any?>(Rounds.misc.getValue("frostbite"), 358, "GMB"),
            "" to arrayOf<Any?>(Rounds.agbIndoor.getValue("portsmouth"), 599, "MB"),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                val score = c[1] as Number
                val expectedClass = c[2] as Any
                val frostbite = Rounds.misc.getValue("frostbite")
                val myClass = calculateAgbOutdoorClassification(archeryRound = archeryRound, score = score, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT, strictRounds = false, strictDistance = false)
                assertEquals(expectedClass, myClass)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOutdoorClassificationNonStrictRound() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "Check long non-prestige can give MB." to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("st_george"), AgbBowstyle.COMPOUND, AgbGender.OPEN, AgbAge.ADULT, 950, "GMB"),
            "Check short 720 prestige no-longer gives MB." to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_70"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, 720, "B2"),
            "Check wrong prestige returns distance-limited classification." to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_50_c"), AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT, 720, "A1"),
            "Check frostbite allows A3." to arrayOf<Any?>(Rounds.misc.getValue("frostbite"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, 300, "A3"),
            "Check indoor 20y returns nothing for adult." to arrayOf<Any?>(Rounds.agbIndoor.getValue("portsmouth"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, 600, "UC"),
            "Check indoor 20y returns max B3 for U12." to arrayOf<Any?>(Rounds.agbIndoor.getValue("portsmouth"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.UNDER_12, 600, "B3"),
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
                val myClass = calculateAgbOutdoorClassification(archeryRound = archeryRound, score = score, bowstyle = bowstyle, gender = gender, ageGroup = ageGroup, strictRounds = false, strictDistance = true)
                assertEquals(expectedClass, myClass)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOutdoorClassificationNonStrictRoundString() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "" to arrayOf<Any?>("wa1440_90"),
            "" to arrayOf<Any?>("york"),
            "" to arrayOf<Any?>("fake_round"),
            "" to arrayOf<Any?>(""),
        )
        for ((index, case) in cases.withIndex()) {
            val (caseId, c) = case
            try {
                val archeryRound = c[0] as Any
                assertFailsContaining("strict_rounds is False so archery_round must be explicitly specified as a Round type instead of a string.") {
                    val scores = calculateAgbOutdoorClassification(archeryRound = archeryRound, score = 123, bowstyle = AgbBowstyle.COMPOUND, gender = AgbGender.OPEN, ageGroup = AgbAge.ADULT, strictRounds = false)
                }
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }

    @Test
    fun testAgbOutdoorClassificationNonStrictDistance() {
        val cases: List<Pair<String, Array<Any?>>> = listOf(
            "Check short non-prestige returns up to B1." to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("windsor_30"), AgbBowstyle.LONGBOW, AgbGender.OPEN, AgbAge.ADULT, 972, "B1"),
            "Check 720 prestige still allows all classifications." to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_70"), AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT, 720, "EMB"),
            "Check wrong prestige unrestricted by distanceto B1." to arrayOf<Any?>(ALL_OUTDOOR_ROUNDS.getValue("wa720_50_c"), AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT, 720, "B1"),
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
                val myClass = calculateAgbOutdoorClassification(archeryRound = archeryRound, score = score, bowstyle = bowstyle, gender = gender, ageGroup = ageGroup, strictRounds = true, strictDistance = false)
                assertEquals(expectedClass, myClass)
            } catch (e: Throwable) {
                throw AssertionError("case $index ($caseId): ${e.message}", e)
            }
        }
    }
}
