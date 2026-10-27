package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Round
import kotlin.test.assertContains
import kotlin.test.assertFailsWith

/**
 * Test helpers. The Python tests pass either a `Round` or a codename `str` for the same argument;
 * these wrappers accept `Any` and dispatch to the matching public overload.
 */

internal fun assertFailsContaining(message: String, block: () -> Unit) {
    val e = assertFailsWith<IllegalArgumentException> { block() }
    assertContains(e.message ?: "", message)
}

private fun badRound(round: Any): Nothing = throw IllegalArgumentException("Unsupported round type: $round")

internal fun agbOutdoorClassificationScores(
    archeryRound: Any,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge, strictRounds: Boolean = true, strictDistance: Boolean = true,
): List<Int> =
    when (archeryRound) {
        is Round -> agbOutdoorClassificationScores(archeryRound, bowstyle, gender, ageGroup, strictRounds, strictDistance)
        is String -> agbOutdoorClassificationScores(archeryRound, bowstyle, gender, ageGroup, strictRounds, strictDistance)
        else -> badRound(archeryRound)
    }

internal fun calculateAgbOutdoorClassification(
    score: Number,
    archeryRound: Any,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge, strictRounds: Boolean = true, strictDistance: Boolean = true,
): String =
    when (archeryRound) {
        is Round -> calculateAgbOutdoorClassification(score, archeryRound, bowstyle, gender, ageGroup, strictRounds, strictDistance)
        is String -> calculateAgbOutdoorClassification(score, archeryRound, bowstyle, gender, ageGroup, strictRounds, strictDistance)
        else -> badRound(archeryRound)
    }

internal fun agbIndoorClassificationScores(
    archeryRound: Any,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge, strictRounds: Boolean = true,
): List<Int> =
    when (archeryRound) {
        is Round -> agbIndoorClassificationScores(archeryRound, bowstyle, gender, ageGroup, strictRounds)
        is String -> agbIndoorClassificationScores(archeryRound, bowstyle, gender, ageGroup, strictRounds)
        else -> badRound(archeryRound)
    }

internal fun calculateAgbIndoorClassification(
    score: Number,
    archeryRound: Any,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge, strictRounds: Boolean = true,
): String =
    when (archeryRound) {
        is Round -> calculateAgbIndoorClassification(score, archeryRound, bowstyle, gender, ageGroup, strictRounds)
        is String -> calculateAgbIndoorClassification(score, archeryRound, bowstyle, gender, ageGroup, strictRounds)
        else -> badRound(archeryRound)
    }

internal fun agbFieldClassificationScores(
    archeryRound: Any,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge, strictRounds: Boolean = true, strictDistance: Boolean = true,
): List<Int> =
    when (archeryRound) {
        is Round -> agbFieldClassificationScores(archeryRound, bowstyle, gender, ageGroup, strictRounds, strictDistance)
        is String -> agbFieldClassificationScores(archeryRound, bowstyle, gender, ageGroup, strictRounds, strictDistance)
        else -> badRound(archeryRound)
    }

internal fun calculateAgbFieldClassification(
    score: Number,
    archeryRound: Any,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge, strictRounds: Boolean = true, strictDistance: Boolean = true,
): String =
    when (archeryRound) {
        is Round -> calculateAgbFieldClassification(score, archeryRound, bowstyle, gender, ageGroup, strictRounds, strictDistance)
        is String -> calculateAgbFieldClassification(score, archeryRound, bowstyle, gender, ageGroup, strictRounds, strictDistance)
        else -> badRound(archeryRound)
    }

internal fun agbOldIndoorClassificationScores(
    archeryRound: Any,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge, strictRounds: Boolean = true,
): List<Int> =
    when (archeryRound) {
        is Round -> agbOldIndoorClassificationScores(archeryRound, bowstyle, gender, ageGroup, strictRounds)
        is String -> agbOldIndoorClassificationScores(archeryRound, bowstyle, gender, ageGroup, strictRounds)
        else -> badRound(archeryRound)
    }

internal fun calculateAgbOldIndoorClassification(
    score: Number,
    archeryRound: Any,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge, strictRounds: Boolean = true,
): String =
    when (archeryRound) {
        is Round -> calculateAgbOldIndoorClassification(score, archeryRound, bowstyle, gender, ageGroup, strictRounds)
        is String -> calculateAgbOldIndoorClassification(score, archeryRound, bowstyle, gender, ageGroup, strictRounds)
        else -> badRound(archeryRound)
    }

internal fun agbOldFieldClassificationScores(
    archeryRound: Any,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
): List<Int> =
    when (archeryRound) {
        is Round -> agbOldFieldClassificationScores(archeryRound, bowstyle, gender, ageGroup)
        is String -> agbOldFieldClassificationScores(archeryRound, bowstyle, gender, ageGroup)
        else -> badRound(archeryRound)
    }

internal fun calculateAgbOldFieldClassification(
    archeryRound: Any,
    score: Number,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
): String =
    when (archeryRound) {
        is Round -> calculateAgbOldFieldClassification(archeryRound, score, bowstyle, gender, ageGroup)
        is String -> calculateAgbOldFieldClassification(archeryRound, score, bowstyle, gender, ageGroup)
        else -> badRound(archeryRound)
    }

