package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.rounds.Rounds
import kotlin.math.max

/** Classification data for one category of the AGB field scheme. */
internal class FieldGroupData(
    val classes: List<String>,
    val classesLong: List<String>,
    val classHc: DoubleArray,
    val maxDistance: Double,
    val minDists: DoubleArray,
)

private val allFieldRounds: Map<String, Round> by lazy { Rounds.waField }

private val fieldEligible: EligibleRounds by lazy {
    EligibleRounds(
        allFieldRounds,
        "This round is not recognised for the purposes of field classification.\n" +
            "Please select an appropriate option using `archeryutils.load_rounds`.",
    )
}

internal val fieldBowstyles: Set<AgbBowstyle> = AgbBowstyle.entries.toSet()

internal val sightedBowstyles: Set<AgbBowstyle> =
    setOf(AgbBowstyle.COMPOUND, AgbBowstyle.RECURVE, AgbBowstyle.COMPOUNDLIMITED)

internal val fieldAges: Set<AgbAge> =
    setOf(
        AgbAge.OVER_50, AgbAge.ADULT, AgbAge.UNDER_18, AgbAge.UNDER_16, AgbAge.UNDER_15,
        AgbAge.UNDER_14, AgbAge.UNDER_12,
    )

private fun fieldGroupname(bowstyle: AgbBowstyle, gender: AgbGender, ageGroup: AgbAge): String {
    require(bowstyle in fieldBowstyles) {
        "$bowstyle is not a recognised bowstyle for field classifications. " +
            "Please select from `$fieldBowstyles`."
    }
    require(ageGroup in fieldAges) {
        "$ageGroup is not a recognised age group for field classifications. " +
            "Please select from `$fieldAges`."
    }
    return ClassificationUtils.getGroupname(bowstyle, gender, ageGroup)
}

/** Coax a category not conforming to field classification rules to one that does. */
public fun coaxFieldGroup(bowstyle: AgbBowstyle, gender: AgbGender, ageGroup: AgbAge): AgbCategory =
    AgbCategory(bowstyle, gender, if (ageGroup == AgbAge.UNDER_21) AgbAge.ADULT else ageGroup)

/**
 * Assign minimum distances per classification and the maximum distance for a category.
 *
 * Sighted bowstyles shoot red pegs, unsighted shoot blue pegs (or yellow/white for juniors).
 * EMB to Bowman require a minimum appropriate distance; Archer tiers can be shot at shorter
 * pegs (min dist reduced by 10m for each tier, down to 30m).
 */
private fun assignFieldDists(bowstyle: AgbBowstyle, age: AgbAgeData): Pair<DoubleArray, Double> {
    val (minD, maxD) = if (bowstyle in sightedBowstyles) age.sighted else age.unsighted
    val minDists = DoubleArray(9) { if (it < 6) minD else max(minD - 10 * (it - 5), 30.0) }
    return minDists to maxD
}

private val agbFieldClassifications: Map<String, FieldGroupData> by lazy {
    val ageData = ClassificationUtils.readAgesJson()
    val bowstyleData = ClassificationUtils.readBowstylesJson()
    val classInfo = ClassificationUtils.readClassesJson("agb_field")
    val out = HashMap<String, FieldGroupData>()
    for (bowstyle in fieldBowstyles) {
        for (gender in AgbGender.entries) {
            for (age in fieldAges) {
                val ageInfo = ageData.getValue(age.name)
                val bowInfo = bowstyleData.getValue(bowstyle.name)
                val (minDists, maxDistance) = assignFieldDists(bowstyle, ageInfo)
                val deltaHc =
                    ClassificationUtils.getAgeGenderStep(
                        gender, ageInfo.step, bowInfo.ageStepField, bowInfo.genderStepField,
                    )
                val classHc =
                    DoubleArray(classInfo.classes.size) {
                        bowInfo.datumField + deltaHc + (it - 2) * bowInfo.classStepField
                    }
                out[fieldGroupname(bowstyle, gender, age)] =
                    FieldGroupData(classInfo.classes, classInfo.classesLong, classHc, maxDistance, minDists)
            }
        }
    }
    out
}

/**
 * Calculate the AGB field classification from a score.
 *
 * @param strictRounds whether to enforce valid AGB field rounds and the 24-target round rule for
 * MB tiers. If false then [archeryRound] must be a [Round].
 * @param strictDistance whether to enforce age-dependent distance restrictions.
 * @return abbreviation of the classification appropriate for this score, or `"UC"`.
 * @throws IllegalArgumentException if the round is not valid for this scheme (when [strictRounds]
 * is enabled) or if an invalid score or category is provided.
 */
public fun calculateAgbFieldClassification(
    score: Number,
    archeryRound: Round,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
    strictDistance: Boolean = true,
): String =
    calculateField(score.toDouble(), archeryRound, null, bowstyle, gender, ageGroup, strictRounds, strictDistance)

/** As [calculateAgbFieldClassification] but specifying the round by its codename. */
public fun calculateAgbFieldClassification(
    score: Number,
    archeryRound: String,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
    strictDistance: Boolean = true,
): String =
    calculateField(score.toDouble(), null, archeryRound, bowstyle, gender, ageGroup, strictRounds, strictDistance)

/** Enforce unmarked/mixed being the same score as marked. */
private fun fieldCheck(roundObj: Round?, roundCode: String?): Pair<Round, String> {
    val (_, name) = fieldEligible.check(roundObj, roundCode)
    val marked = name.replace("unmarked", "marked").replace("mixed", "marked")
    return allFieldRounds.getValue(marked) to marked
}

private fun calculateField(
    score: Double,
    roundObj: Round?,
    roundName: String?,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean,
    strictDistance: Boolean,
): String {
    val archeryRound: Round
    if (strictRounds) {
        archeryRound = fieldCheck(roundObj, roundName).first
    } else {
        require(roundName == null) { STRICT_ROUNDS_MESSAGE }
        archeryRound = roundObj!!
    }
    require(score >= 0 && score <= archeryRound.maxScore()) { invalidScoreMessage(score, archeryRound) }

    val allClassScores = fieldScores(archeryRound, null, bowstyle, gender, ageGroup, strictRounds, strictDistance)
    val groupData = agbFieldClassifications.getValue(fieldGroupname(bowstyle, gender, ageGroup))
    for ((classname, classScore) in groupData.classes.zip(allClassScores)) {
        if (classScore < 0 || classScore > score) continue
        return classname
    }
    return "UC"
}

/**
 * Calculate AGB field classification scores for a category and round.
 *
 * Classes that cannot be achieved are given a fill value of [INVALID_CLASS_SCORE] (`-9999`).
 *
 * @return scores required for each classification (EMB -> A3) in descending order.
 */
public fun agbFieldClassificationScores(
    archeryRound: Round,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
    strictDistance: Boolean = true,
): List<Int> = fieldScores(archeryRound, null, bowstyle, gender, ageGroup, strictRounds, strictDistance)

/** As above but specifying the round by its codename. */
public fun agbFieldClassificationScores(
    archeryRound: String,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
    strictDistance: Boolean = true,
): List<Int> = fieldScores(null, archeryRound, bowstyle, gender, ageGroup, strictRounds, strictDistance)

private fun fieldScores(
    roundObj: Round?,
    roundCode: String?,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean,
    strictDistance: Boolean,
): List<Int> {
    val archeryRound: Round
    if (strictRounds) {
        archeryRound = fieldCheck(roundObj, roundCode).first
    } else {
        require(roundCode == null) { STRICT_ROUNDS_MESSAGE }
        archeryRound = roundObj!!
    }

    val groupData = agbFieldClassifications.getValue(fieldGroupname(bowstyle, gender, ageGroup))

    return classScoresFromHandicaps(groupData.classHc, archeryRound, "AGB") { scores ->
        // Based on round length (24 targets for MB)
        if (strictRounds && "wa_field_24_" !in archeryRound.codename) {
            for (i in 0 until 3) scores[i] = INVALID_CLASS_SCORE
        }
        if (strictDistance) {
            val roundMaxDist = archeryRound.maxDistance().value
            for (i in scores.indices) {
                // Is round too short?
                if (groupData.minDists[i] > roundMaxDist) scores[i] = INVALID_CLASS_SCORE
                // Is peg too long (i.e. red peg for unsighted)?
                if (groupData.maxDistance < roundMaxDist) scores[i] = INVALID_CLASS_SCORE
            }
        }
    }
}
