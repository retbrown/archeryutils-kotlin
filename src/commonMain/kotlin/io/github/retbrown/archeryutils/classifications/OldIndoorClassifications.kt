package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.rounds.Rounds

/** Classification data for one category of the old AGB indoor scheme. */
internal class OldIndoorGroupData(val classes: List<String>, val classHc: DoubleArray)

private val allOldIndoorRounds: Map<String, Round> by lazy { Rounds.agbIndoor + Rounds.waIndoor }

private val oldIndoorEligible: EligibleRounds by lazy {
    EligibleRounds(
        allOldIndoorRounds,
        "This round is not recognised for the purposes of indoor classification.\n" +
            "Please select an appropriate option using `archeryutils.load_rounds`.",
    )
}

internal val oldIndoorBowstyles: Set<AgbBowstyle> = setOf(AgbBowstyle.COMPOUND, AgbBowstyle.RECURVE)
internal val oldIndoorAges: Set<AgbAge> = setOf(AgbAge.ADULT)

private fun oldIndoorGroupname(bowstyle: AgbBowstyle, gender: AgbGender, ageGroup: AgbAge): String {
    require(bowstyle in oldIndoorBowstyles) {
        "$bowstyle is not a recognised bowstyle for old indoor classifications. " +
            "Please select from `$oldIndoorBowstyles`."
    }
    require(ageGroup in oldIndoorAges) {
        "$ageGroup is not a recognised age group for old indoor classifications. " +
            "Please select from `$oldIndoorAges`."
    }
    return ClassificationUtils.getGroupname(bowstyle, gender, ageGroup)
}

/** Coax a category not conforming to old indoor classification rules to one that does. */
public fun coaxOldIndoorGroup(bowstyle: AgbBowstyle, gender: AgbGender, ageGroup: AgbAge): AgbCategory {
    val coaxBowstyle =
        when (bowstyle) {
            AgbBowstyle.COMPOUND, AgbBowstyle.COMPOUNDLIMITED, AgbBowstyle.COMPOUNDBAREBOW ->
                AgbBowstyle.COMPOUND
            else -> AgbBowstyle.RECURVE
        }
    return AgbCategory(coaxBowstyle, gender, AgbAge.ADULT)
}

private val agbOldIndoorClassifications: Map<String, OldIndoorGroupData> by lazy {
    val classes = listOf("A", "B", "C", "D", "E", "F", "G", "H")
    fun data(vararg hc: Int) = OldIndoorGroupData(classes, DoubleArray(hc.size) { hc[it].toDouble() })
    mapOf(
        oldIndoorGroupname(AgbBowstyle.COMPOUND, AgbGender.MALE, AgbAge.ADULT) to
            data(5, 12, 24, 37, 49, 62, 73, 79),
        oldIndoorGroupname(AgbBowstyle.COMPOUND, AgbGender.FEMALE, AgbAge.ADULT) to
            data(12, 18, 30, 43, 55, 67, 79, 83),
        oldIndoorGroupname(AgbBowstyle.RECURVE, AgbGender.MALE, AgbAge.ADULT) to
            data(14, 21, 33, 46, 58, 70, 80, 85),
        oldIndoorGroupname(AgbBowstyle.RECURVE, AgbGender.FEMALE, AgbAge.ADULT) to
            data(21, 27, 39, 51, 64, 75, 85, 90),
    )
}

/**
 * Calculate the old (pre-2023) AGB indoor classification from a score.
 *
 * @param strictRounds whether to enforce valid AGB indoor rounds only. If false then
 * [archeryRound] must be a [Round].
 * @return abbreviation of the classification appropriate for this score, or `"UC"`.
 * @throws IllegalArgumentException if the round is not valid for this scheme (when [strictRounds]
 * is enabled) or if an invalid score or category is provided.
 */
public fun calculateAgbOldIndoorClassification(
    score: Number,
    archeryRound: Round,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
): String = calculateOldIndoor(score.toDouble(), archeryRound, null, bowstyle, gender, ageGroup, strictRounds)

/** As [calculateAgbOldIndoorClassification] but specifying the round by its codename. */
public fun calculateAgbOldIndoorClassification(
    score: Number,
    archeryRound: String,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
): String = calculateOldIndoor(score.toDouble(), null, archeryRound, bowstyle, gender, ageGroup, strictRounds)

private fun calculateOldIndoor(
    score: Double,
    roundObj: Round?,
    roundName: String?,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean,
): String {
    val archeryRound: Round
    if (strictRounds) {
        archeryRound = oldIndoorEligible.check(roundObj, roundName).first
    } else {
        require(roundName == null) { STRICT_ROUNDS_MESSAGE }
        archeryRound = roundObj!!
    }
    require(score >= 0 && score <= archeryRound.maxScore()) { invalidScoreMessage(score, archeryRound) }

    val classScores = oldIndoorScores(archeryRound, null, bowstyle, gender, ageGroup, strictRounds)
    val groupData = agbOldIndoorClassifications.getValue(oldIndoorGroupname(bowstyle, gender, ageGroup))
    // NB No fiddle for Worcester required with this logic...
    for ((classname, classScore) in groupData.classes.zip(classScores)) {
        if (classScore > score) continue
        return classname
    }
    return "UC"
}

/** Calculate old AGB indoor classification scores for a category and round (A -> H). */
public fun agbOldIndoorClassificationScores(
    archeryRound: Round,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
): List<Int> = oldIndoorScores(archeryRound, null, bowstyle, gender, ageGroup, strictRounds)

/** As above but specifying the round by its codename. */
public fun agbOldIndoorClassificationScores(
    archeryRound: String,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
): List<Int> = oldIndoorScores(null, archeryRound, bowstyle, gender, ageGroup, strictRounds)

private fun oldIndoorScores(
    roundObj: Round?,
    roundCode: String?,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean,
): List<Int> {
    var archeryRound: Round
    if (strictRounds) {
        var (r, roundname) = oldIndoorEligible.check(roundObj, roundCode)
        archeryRound = r
        // enforce compound scoring
        if (bowstyle == AgbBowstyle.COMPOUND) {
            roundname = ClassificationUtils.getCompoundCodename(roundname)
            archeryRound = allOldIndoorRounds.getValue(roundname)
        }
        // Strip spots from AGB rounds to enforce full-face
        if (listOf("portsmouth", "worcester", "bray_i", "bray_ii").any { it in roundname }) {
            archeryRound = allOldIndoorRounds.getValue(ClassificationUtils.stripSpots(roundname))
        }
    } else {
        require(roundCode == null) { STRICT_ROUNDS_MESSAGE }
        archeryRound = roundObj!!
    }

    val groupData = agbOldIndoorClassifications.getValue(oldIndoorGroupname(bowstyle, gender, ageGroup))
    return groupData.classHc.map { hcScoreForRound(it, archeryRound, "AGBold").toInt() }
}
