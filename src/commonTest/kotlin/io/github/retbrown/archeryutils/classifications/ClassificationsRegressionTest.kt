package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.rounds.Rounds
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Regression tests for classifications.
 *
 * Compares against values calculated by the Python library (its syrupy snapshots), looping over
 * all eligible rounds, ages, genders and bowstyles for each classification type.
 */
class ClassificationsRegressionTest {
    private val allIndoorRounds = Rounds.agbIndoor + Rounds.waIndoor
    private val allOutdoorRounds = Rounds.agbOutdoorImperial + Rounds.agbOutdoorMetric + Rounds.waOutdoor
    private val allFieldRounds = Rounds.waField

    private val targetBowstyles =
        listOf(AgbBowstyle.COMPOUND, AgbBowstyle.RECURVE, AgbBowstyle.BAREBOW, AgbBowstyle.LONGBOW)
    private val fieldAges =
        listOf(
            AgbAge.OVER_50, AgbAge.ADULT, AgbAge.UNDER_18, AgbAge.UNDER_16, AgbAge.UNDER_15,
            AgbAge.UNDER_14, AgbAge.UNDER_12,
        )

    private fun check(
        snapshots: Map<String, String>,
        key: String,
        rounds: Map<String, Round>,
        scores: (Round) -> List<Int>,
    ) {
        val expected =
            Json.parseToJsonElement(snapshots.getValue(key)).jsonObject.mapValues { (_, v) ->
                v.jsonArray.map { it.jsonPrimitive.int }
            }
        val actual = rounds.values.associate { it.codename to scores(it) }
        assertEquals(expected.keys, actual.keys, "round codenames for $key")
        for ((codename, exp) in expected) assertEquals(exp, actual.getValue(codename), "$key / $codename")
    }

    @Test
    fun indoorClassificationScores() {
        for (bowstyle in targetBowstyles) for (gender in AgbGender.entries) for (age in AgbAge.entries) {
            check(IndoorSnapshots.data, "$age-$gender-$bowstyle", allIndoorRounds) {
                agbIndoorClassificationScores(it, bowstyle, gender, age)
            }
        }
    }

    @Test
    fun outdoorClassificationScores() {
        for (bowstyle in targetBowstyles) for (gender in AgbGender.entries) for (age in AgbAge.entries) {
            check(OutdoorSnapshots.data, "$age-$gender-$bowstyle", allOutdoorRounds) {
                agbOutdoorClassificationScores(it, bowstyle, gender, age)
            }
        }
    }

    @Test
    fun fieldClassificationScores() {
        for (bowstyle in AgbBowstyle.entries) for (gender in AgbGender.entries) for (age in fieldAges) {
            check(FieldSnapshots.data, "$age-$gender-$bowstyle", allFieldRounds) {
                agbFieldClassificationScores(it, bowstyle, gender, age)
            }
        }
    }

    @Test
    fun oldIndoorClassificationScores() {
        for (bowstyle in listOf(AgbBowstyle.COMPOUND, AgbBowstyle.RECURVE)) for (gender in AgbGender.entries) {
            check(OldIndoorSnapshots.data, "$gender-$bowstyle", allIndoorRounds) {
                agbOldIndoorClassificationScores(it, bowstyle, gender, AgbAge.ADULT)
            }
        }
    }

    @Test
    fun oldFieldClassificationScores() {
        for (bowstyle in AgbBowstyle.entries) for (gender in AgbGender.entries) {
            for (age in listOf(AgbAge.ADULT, AgbAge.UNDER_18)) {
                check(OldFieldSnapshots.data, "$age-$gender-$bowstyle", allFieldRounds) {
                    agbOldFieldClassificationScores(it, bowstyle, gender, age)
                }
            }
        }
    }
}
