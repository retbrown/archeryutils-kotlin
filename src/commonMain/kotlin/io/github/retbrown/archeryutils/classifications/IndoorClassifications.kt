package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.rounds.Rounds

/** Classification data for one category of the AGB indoor scheme. */
internal class IndoorGroupData(
    val classes: List<String>,
    val classesLong: List<String>,
    val classHc: DoubleArray,
)

private val allIndoorRounds: Map<String, Round> by lazy { Rounds.agbIndoor + Rounds.waIndoor }

private val indoorEligible: EligibleRounds by lazy {
    EligibleRounds(
        allIndoorRounds,
        "This round is not recognised for the purposes of indoor classification.\n" +
            "Please select an appropriate option using `archeryutils.load_rounds`.",
    )
}

internal val indoorBowstyles: Set<AgbBowstyle> =
    setOf(AgbBowstyle.COMPOUND, AgbBowstyle.RECURVE, AgbBowstyle.BAREBOW, AgbBowstyle.LONGBOW)

private fun indoorGroupname(bowstyle: AgbBowstyle, gender: AgbGender, ageGroup: AgbAge): String {
    require(bowstyle in indoorBowstyles) {
        "$bowstyle is not a recognised bowstyle for indoor classifications. " +
            "Please select from `$indoorBowstyles`."
    }
    return ClassificationUtils.getGroupname(bowstyle, gender, ageGroup)
}

/** Coax a category not conforming to indoor classification rules to one that does. */
public fun coaxIndoorGroup(bowstyle: AgbBowstyle, gender: AgbGender, ageGroup: AgbAge): AgbCategory {
    val coaxBowstyle =
        when (bowstyle) {
            AgbBowstyle.FLATBOW, AgbBowstyle.TRADITIONAL -> AgbBowstyle.BAREBOW
            AgbBowstyle.COMPOUNDLIMITED, AgbBowstyle.COMPOUNDBAREBOW -> AgbBowstyle.COMPOUND
            else -> bowstyle
        }
    return AgbCategory(coaxBowstyle, gender, ageGroup)
}

private val agbIndoorClassifications: Map<String, IndoorGroupData> by lazy {
    val ageData = ClassificationUtils.readAgesJson()
    val bowstyleData = ClassificationUtils.readBowstylesJson()
    val classInfo = ClassificationUtils.readClassesJson("agb_indoor")
    val out = HashMap<String, IndoorGroupData>()
    for (bowstyle in indoorBowstyles) {
        for (gender in AgbGender.entries) {
            for (age in AgbAge.entries) {
                val bowInfo = bowstyleData.getValue(bowstyle.name)
                val deltaHc =
                    ClassificationUtils.getAgeGenderStep(
                        gender, ageData.getValue(age.name).step, bowInfo.ageStepIn, bowInfo.genderStepIn,
                    )
                val classHc =
                    DoubleArray(classInfo.classes.size) {
                        bowInfo.datumIn + deltaHc + (it - 1) * bowInfo.classStepIn
                    }
                out[indoorGroupname(bowstyle, gender, age)] =
                    IndoorGroupData(classInfo.classes, classInfo.classesLong, classHc)
            }
        }
    }
    out
}

/**
 * Calculate the AGB indoor classification from a score.
 *
 * Appropriate for 2023 ArcheryGB age groups and classifications.
 *
 * @param strictRounds whether to enforce valid AGB indoor rounds only. If false then
 * [archeryRound] must be a [Round] (not a codename) for explicitness.
 * @return abbreviation of the classification appropriate for this score, or `"UC"`.
 * @throws IllegalArgumentException if the round is not valid for this scheme (when [strictRounds]
 * is enabled) or if an invalid score or category is provided.
 */
public fun calculateAgbIndoorClassification(
    score: Number,
    archeryRound: Round,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
): String = calculateIndoor(score.toDouble(), archeryRound, null, bowstyle, gender, ageGroup, strictRounds)

/** As [calculateAgbIndoorClassification] but specifying the round by its codename. */
public fun calculateAgbIndoorClassification(
    score: Number,
    archeryRound: String,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
): String = calculateIndoor(score.toDouble(), null, archeryRound, bowstyle, gender, ageGroup, strictRounds)

private fun calculateIndoor(
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
        archeryRound = indoorEligible.check(roundObj, roundName).first
    } else {
        require(roundName == null) { STRICT_ROUNDS_MESSAGE }
        archeryRound = roundObj!!
    }
    require(score >= 0 && score <= archeryRound.maxScore()) { invalidScoreMessage(score, archeryRound) }

    val allClassScores = indoorScores(archeryRound, null, bowstyle, gender, ageGroup, strictRounds)
    val groupData = agbIndoorClassifications.getValue(indoorGroupname(bowstyle, gender, ageGroup))
    for ((classname, classScore) in groupData.classes.zip(allClassScores)) {
        if (classScore < 0 || classScore > score) continue
        return classname
    }
    return "UC"
}

/**
 * Calculate AGB indoor classification scores for a category and round.
 *
 * Classes that cannot be achieved are given a fill value of [INVALID_CLASS_SCORE] (`-9999`).
 *
 * @return scores required for each classification in descending order.
 */
public fun agbIndoorClassificationScores(
    archeryRound: Round,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
): List<Int> = indoorScores(archeryRound, null, bowstyle, gender, ageGroup, strictRounds)

/** As above but specifying the round by its codename. */
public fun agbIndoorClassificationScores(
    archeryRound: String,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
): List<Int> = indoorScores(null, archeryRound, bowstyle, gender, ageGroup, strictRounds)

private fun indoorScores(
    roundObj: Round?,
    roundCode: String?,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean,
): List<Int> {
    val archeryRound: Round
    if (strictRounds) {
        var (_, roundname) = indoorEligible.check(roundObj, roundCode)
        // enforce compound scoring
        if (bowstyle == AgbBowstyle.COMPOUND) roundname = ClassificationUtils.getCompoundCodename(roundname)
        // For score purposes in classifications we use the full face, not the triple.
        archeryRound = allIndoorRounds.getValue(ClassificationUtils.stripSpots(roundname))
    } else {
        require(roundCode == null) { STRICT_ROUNDS_MESSAGE }
        archeryRound = roundObj!!
    }

    val groupData = agbIndoorClassifications.getValue(indoorGroupname(bowstyle, gender, ageGroup))
    return classScoresFromHandicaps(groupData.classHc, archeryRound, "AGB")
}
