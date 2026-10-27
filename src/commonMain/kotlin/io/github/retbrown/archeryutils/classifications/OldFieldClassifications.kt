package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.rounds.Rounds

private val allOldFieldRounds: Map<String, Round> by lazy { Rounds.waField }

private val oldFieldEligible: EligibleRounds by lazy {
    EligibleRounds(
        allOldFieldRounds,
        "This round is not recognised for the purposes of old AGB field classifications.\n" +
            "Please select an appropriate option using `archeryutils.load_rounds`.",
    )
}

private val agbFieldClasses = listOf("GMB", "MB", "B", "1C", "2C", "3C")
private const val UNCLASSIFIED = "UC"

internal val oldFieldBowstyles: Set<AgbBowstyle> = AgbBowstyle.entries.toSet()
internal val oldFieldAges: Set<AgbAge> = setOf(AgbAge.ADULT, AgbAge.UNDER_18)

private fun oldFieldGroupname(bowstyle: AgbBowstyle, gender: AgbGender, ageGroup: AgbAge): String {
    require(bowstyle in oldFieldBowstyles) {
        "$bowstyle is not a recognised bowstyle for old field classifications. " +
            "Please select from `$oldFieldBowstyles`."
    }
    require(ageGroup in oldFieldAges) {
        "$ageGroup is not a recognised age group for old field classifications. " +
            "Please select from `$oldFieldAges`."
    }
    return ClassificationUtils.getGroupname(bowstyle, gender, ageGroup)
}

/** Coax a category not conforming to old field classification rules to one that does. */
public fun coaxOldFieldGroup(bowstyle: AgbBowstyle, gender: AgbGender, ageGroup: AgbAge): AgbCategory {
    val coaxAge =
        when {
            ageGroup == AgbAge.UNDER_21 || ageGroup == AgbAge.OVER_50 -> AgbAge.ADULT
            ageGroup != AgbAge.ADULT -> AgbAge.UNDER_18
            else -> ageGroup
        }
    return AgbCategory(bowstyle, gender, coaxAge)
}

private val oldAgbFieldClassifications: Map<String, List<Int>> by lazy {
    val m = HashMap<String, List<Int>>()
    fun put(b: AgbBowstyle, g: AgbGender, a: AgbAge, vararg scores: Int) {
        m[oldFieldGroupname(b, g, a)] = scores.toList()
    }
    val male = AgbGender.MALE
    val female = AgbGender.FEMALE
    val adult = AgbAge.ADULT
    put(AgbBowstyle.COMPOUND, male, adult, 393, 377, 344, 312, 279, 247)
    put(AgbBowstyle.COMPOUND, female, adult, 376, 361, 330, 299, 268, 237)
    put(AgbBowstyle.RECURVE, male, adult, 338, 317, 288, 260, 231, 203)
    put(AgbBowstyle.RECURVE, female, adult, 322, 302, 275, 247, 220, 193)
    put(AgbBowstyle.BAREBOW, male, adult, 328, 307, 279, 252, 224, 197)
    put(AgbBowstyle.BAREBOW, female, adult, 303, 284, 258, 233, 207, 182)
    put(AgbBowstyle.LONGBOW, male, adult, 201, 188, 171, 155, 137, 121)
    put(AgbBowstyle.LONGBOW, female, adult, 152, 142, 129, 117, 103, 91)
    put(AgbBowstyle.TRADITIONAL, male, adult, 262, 245, 223, 202, 178, 157)
    put(AgbBowstyle.TRADITIONAL, female, adult, 197, 184, 167, 152, 134, 118)
    put(AgbBowstyle.FLATBOW, male, adult, 262, 245, 223, 202, 178, 157)
    put(AgbBowstyle.FLATBOW, female, adult, 197, 184, 167, 152, 134, 118)
    put(AgbBowstyle.COMPOUNDLIMITED, male, adult, 338, 317, 288, 260, 231, 203)
    put(AgbBowstyle.COMPOUNDLIMITED, female, adult, 322, 302, 275, 247, 220, 193)
    put(AgbBowstyle.COMPOUNDBAREBOW, male, adult, 328, 307, 279, 252, 224, 197)
    put(AgbBowstyle.COMPOUNDBAREBOW, female, adult, 303, 284, 258, 233, 207, 182)
    val u18 = AgbAge.UNDER_18
    put(AgbBowstyle.COMPOUND, male, u18, 385, 369, 337, 306, 273, 242)
    put(AgbBowstyle.COMPOUND, female, u18, 357, 343, 314, 284, 255, 225)
    put(AgbBowstyle.RECURVE, male, u18, 311, 292, 265, 239, 213, 187)
    put(AgbBowstyle.RECURVE, female, u18, 280, 263, 239, 215, 191, 168)
    put(AgbBowstyle.BAREBOW, male, u18, 298, 279, 254, 229, 204, 179)
    put(AgbBowstyle.BAREBOW, female, u18, 251, 236, 214, 193, 172, 151)
    put(AgbBowstyle.LONGBOW, male, u18, 161, 150, 137, 124, 109, 96)
    put(AgbBowstyle.LONGBOW, female, u18, 122, 114, 103, 94, 83, 73)
    put(AgbBowstyle.TRADITIONAL, male, u18, 210, 196, 178, 161, 143, 126)
    put(AgbBowstyle.TRADITIONAL, female, u18, 158, 147, 134, 121, 107, 95)
    put(AgbBowstyle.FLATBOW, male, u18, 210, 196, 178, 161, 143, 126)
    put(AgbBowstyle.FLATBOW, female, u18, 158, 147, 134, 121, 107, 95)
    put(AgbBowstyle.COMPOUNDLIMITED, male, u18, 311, 292, 265, 239, 213, 187)
    put(AgbBowstyle.COMPOUNDLIMITED, female, u18, 280, 263, 239, 215, 191, 168)
    put(AgbBowstyle.COMPOUNDBAREBOW, male, u18, 298, 279, 254, 229, 204, 179)
    put(AgbBowstyle.COMPOUNDBAREBOW, female, u18, 251, 236, 214, 193, 172, 151)
    m
}

/**
 * Calculate the old (pre-2023) AGB field classification from a score.
 *
 * Sighted bowstyles can only be classified on a red 24-target round, unsighted on a blue one;
 * otherwise `"UC"` is returned.
 *
 * Note that the argument order ([archeryRound] before [score]) matches the Python library.
 *
 * @throws IllegalArgumentException if the round is not valid for this scheme or if an invalid
 * score or category is provided.
 */
public fun calculateAgbOldFieldClassification(
    archeryRound: Round,
    score: Number,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
): String = calculateOldField(archeryRound, null, score.toDouble(), bowstyle, gender, ageGroup)

/** As [calculateAgbOldFieldClassification] but specifying the round by its codename. */
public fun calculateAgbOldFieldClassification(
    archeryRound: String,
    score: Number,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
): String = calculateOldField(null, archeryRound, score.toDouble(), bowstyle, gender, ageGroup)

private fun calculateOldField(
    roundObj: Round?,
    roundCode: String?,
    score: Double,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
): String {
    val (archeryRound, roundname) = oldFieldEligible.check(roundObj, roundCode)
    require(score >= 0 && score <= archeryRound.maxScore()) { invalidScoreMessage(score, archeryRound) }

    val groupScores = oldAgbFieldClassifications.getValue(oldFieldGroupname(bowstyle, gender, ageGroup))

    // Sighted can have any Red 24, unsighted can have any blue 24
    if (bowstyle in sightedBowstyles && "wa_field_24_red_" !in roundname) return UNCLASSIFIED
    if (bowstyle !in sightedBowstyles && "wa_field_24_blue_" !in roundname) return UNCLASSIFIED

    for ((classification, classScore) in agbFieldClasses.zip(groupScores)) {
        if (classScore > score) continue
        return classification
    }
    return UNCLASSIFIED
}

/** Old AGB field classification scores for a category (GMB -> 3C). */
public fun agbOldFieldClassificationScores(
    archeryRound: Round,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
): List<Int> {
    oldFieldEligible.check(archeryRound, null)
    return oldAgbFieldClassifications.getValue(oldFieldGroupname(bowstyle, gender, ageGroup))
}

/** As above but specifying the round by its codename. */
public fun agbOldFieldClassificationScores(
    archeryRound: String,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
): List<Int> {
    oldFieldEligible.check(null, archeryRound)
    return oldAgbFieldClassifications.getValue(oldFieldGroupname(bowstyle, gender, ageGroup))
}
