package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Round
import io.github.retbrown.archeryutils.rounds.Rounds

/** Classification data for one category of the AGB outdoor scheme. */
internal class OutdoorGroupData(
    val classes: List<String>,
    val maxDistances: List<Double>,
    val classesLong: List<String>,
    val classHc: DoubleArray,
    val minDists: DoubleArray,
    val prestigeRounds: List<String>,
)

private val allOutdoorRounds: Map<String, Round> by lazy {
    Rounds.agbOutdoorImperial + Rounds.agbOutdoorMetric + Rounds.waOutdoor
}

private val outdoorEligible: EligibleRounds by lazy {
    EligibleRounds(
        allOutdoorRounds,
        "This round is not recognised for the purposes of outdoor classification.\n" +
            "Please select an appropriate option using `archeryutils.load_rounds`.",
    )
}

internal val outdoorBowstyles: Set<AgbBowstyle> =
    setOf(AgbBowstyle.COMPOUND, AgbBowstyle.RECURVE, AgbBowstyle.BAREBOW, AgbBowstyle.LONGBOW)

private val under15AndYounger = setOf(AgbAge.UNDER_15, AgbAge.UNDER_14, AgbAge.UNDER_12)

private fun outdoorGroupname(bowstyle: AgbBowstyle, gender: AgbGender, ageGroup: AgbAge): String {
    require(bowstyle in outdoorBowstyles) {
        "$bowstyle is not a recognised bowstyle for outdoor classifications. " +
            "Please select from `$outdoorBowstyles`."
    }
    return ClassificationUtils.getGroupname(bowstyle, gender, ageGroup)
}

/** Coax a category not conforming to outdoor classification rules to one that does. */
public fun coaxOutdoorGroup(bowstyle: AgbBowstyle, gender: AgbGender, ageGroup: AgbAge): AgbCategory {
    val coaxBowstyle =
        when (bowstyle) {
            AgbBowstyle.FLATBOW, AgbBowstyle.TRADITIONAL -> AgbBowstyle.BAREBOW
            AgbBowstyle.COMPOUNDLIMITED, AgbBowstyle.COMPOUNDBAREBOW -> AgbBowstyle.COMPOUND
            else -> bowstyle
        }
    return AgbCategory(coaxBowstyle, gender, ageGroup)
}

/**
 * Minimum distance (in metres) required by category for each classification (EMB -> A3).
 *
 * Anything B1 and beyond requires max distance for men U16 and older; anything B2 and beyond
 * requires max distance for women, and men U15 and younger. Then step down a distance with each
 * classification.
 */
private fun assignMinDist(gender: AgbGender, ageGroup: AgbAge, maxDists: List<Double>): DoubleArray {
    // Use metres because corresponding yards distances are >= metric ones
    val dists = doubleArrayOf(90.0, 70.0, 60.0, 50.0, 40.0, 30.0, 20.0, 15.0)
    val minMaxDist = maxDists.min()
    val maxDistIndex = dists.indexOfFirst { it == minMaxDist }
    require(maxDistIndex >= 0) { "$minMaxDist is not in list of distances" }

    // U15 opens and younger step down for B2 and below to align with female scores/hcs
    val idxs =
        if (gender == AgbGender.OPEN && ageGroup !in under15AndYounger) {
            intArrayOf(0, 0, 0, 0, 1, 2, 3, 4, 5)
        } else {
            // All other categories require max dist for B1 and B2 then step down
            intArrayOf(0, 0, 0, 0, 0, 1, 2, 3, 4)
        }
    return DoubleArray(idxs.size) { dists[(idxs[it] + maxDistIndex).coerceIn(0, dists.size - 1)] }
}

/** Assign appropriate outdoor prestige rounds for a category. */
private fun assignOutdoorPrestige(
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    age: AgbAge,
    maxDists: List<Double>,
): List<String> {
    // Lists of prestige rounds defined by 'codename' of 'Round' class
    // WARNING: do not change these without also addressing the prestige round code.
    val prestigeImperial =
        listOf("york", "hereford", "bristol_i", "bristol_ii", "bristol_iii", "bristol_iv", "bristol_v")
    val prestigeMetric =
        listOf(
            "wa1440_90", "wa1440_90_small", "wa1440_70", "wa1440_70_small", "wa1440_60",
            "wa1440_60_small", "metric_i", "metric_ii", "metric_iii", "metric_iv", "metric_v",
        )
    val prestige720 =
        listOf("wa720_70", "wa720_60", "metric_122_50", "wa720_40", "metric_122_40", "metric_122_30")
    val prestige720Compound =
        listOf("wa720_50_c", "metric_80_50", "wa720_40_c", "metric_80_40", "metric_80_30")
    val prestige720Barebow =
        listOf("wa720_50_b", "metric_122_50", "wa720_40", "metric_122_40", "wa720_30_b", "metric_122_30")

    val prestigeRounds = mutableListOf<String>()
    val distanceCheck = mutableListOf<String>()

    // 720 rounds - bowstyle dependent
    when (bowstyle) {
        AgbBowstyle.COMPOUND -> {
            // Everyone gets the 'adult' 720
            prestigeRounds.addAll(prestige720Compound.subList(0, 2))
            // Check rest for junior eligible shorter rounds
            distanceCheck.addAll(prestige720Compound.subList(2, prestige720Compound.size))
            // Additional fix for U15 who get the 40m round
            if (age in under15AndYounger) prestigeRounds.addAll(prestige720Compound.subList(2, 4))
        }
        AgbBowstyle.BAREBOW -> {
            prestigeRounds.add(prestige720Barebow[0])
            distanceCheck.addAll(prestige720Barebow.subList(1, prestige720Barebow.size))
            // Additional fix for U15 who get the 30m round (and by extension the 40m)
            if (age in under15AndYounger) {
                prestigeRounds.addAll(prestige720Barebow.subList(2, prestige720Barebow.size))
            }
        }
        else -> {
            prestigeRounds.add(prestige720[0])
            distanceCheck.addAll(prestige720.subList(1, prestige720.size))
            // Additional fix for U15 who get the 40m round
            if (age in under15AndYounger) prestigeRounds.addAll(prestige720.subList(3, 5))
            // Additional fix for Open 50+, U18, and U16 recurve/longbow
            if (gender == AgbGender.OPEN) {
                if (age == AgbAge.OVER_50 || age == AgbAge.UNDER_18) {
                    prestigeRounds.add(prestige720[1]) // 60m
                } else if (age == AgbAge.UNDER_16) {
                    prestigeRounds.add(prestige720[2]) // 50m
                }
            }
        }
    }

    // Imperial and 1440 rounds - check based on distance
    distanceCheck.addAll(prestigeImperial)
    distanceCheck.addAll(prestigeMetric)

    val minMaxDist = maxDists.min()
    for (roundname in distanceCheck) {
        if (allOutdoorRounds.getValue(roundname).maxDistance().value >= minMaxDist) {
            prestigeRounds.add(roundname)
        }
    }
    return prestigeRounds
}

private val agbOutdoorClassifications: Map<String, OutdoorGroupData> by lazy {
    val ageData = ClassificationUtils.readAgesJson()
    val bowstyleData = ClassificationUtils.readBowstylesJson()
    val classInfo = ClassificationUtils.readClassesJson("agb_outdoor")
    val out = HashMap<String, OutdoorGroupData>()
    for (bowstyle in outdoorBowstyles) {
        for (gender in AgbGender.entries) {
            for (age in AgbAge.entries) {
                val groupname = outdoorGroupname(bowstyle, gender, age)
                val ageInfo = ageData.getValue(age.name)
                val bowInfo = bowstyleData.getValue(bowstyle.name)
                // Use metres as corresponding yards >= metric
                val maxDists = if (gender == AgbGender.OPEN) ageInfo.open else ageInfo.female

                val deltaHc =
                    ClassificationUtils.getAgeGenderStep(
                        gender, ageInfo.step, bowInfo.ageStepOut, bowInfo.genderStepOut,
                    )
                val classHc =
                    DoubleArray(classInfo.classes.size) {
                        bowInfo.datumOut + deltaHc + (it - 2) * bowInfo.classStepOut
                    }
                out[groupname] =
                    OutdoorGroupData(
                        classes = classInfo.classes,
                        maxDistances = maxDists,
                        classesLong = classInfo.classesLong,
                        classHc = classHc,
                        minDists = assignMinDist(gender, age, maxDists),
                        prestigeRounds = assignOutdoorPrestige(bowstyle, gender, age, maxDists),
                    )
            }
        }
    }
    out
}

/**
 * Calculate the AGB outdoor classification from a score.
 *
 * Appropriate for 2023 ArcheryGB age groups and classifications.
 *
 * @param score numerical score on the round to calculate the classification for.
 * @param archeryRound an archeryutils [Round] suitable for this scheme.
 * @param strictRounds whether to enforce valid AGB outdoor rounds and apply prestige round rules.
 * If false then prestige rounds no longer default to allow MB classifications and any max-distance
 * rounds will return MB-tier classifications.
 * @param strictDistance whether to enforce age-dependent distance restrictions.
 * @return abbreviation of the classification appropriate for this score, or `"UC"`.
 * @throws IllegalArgumentException if the round is not valid for this scheme (when [strictRounds]
 * is enabled) or if an invalid score or category is provided.
 */
public fun calculateAgbOutdoorClassification(
    score: Number,
    archeryRound: Round,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
    strictDistance: Boolean = true,
): String =
    calculateOutdoor(score.toDouble(), archeryRound, null, bowstyle, gender, ageGroup, strictRounds, strictDistance)

/**
 * As [calculateAgbOutdoorClassification] but specifying the round by its codename.
 * Requires [strictRounds].
 */
public fun calculateAgbOutdoorClassification(
    score: Number,
    archeryRound: String,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
    strictDistance: Boolean = true,
): String =
    calculateOutdoor(score.toDouble(), null, archeryRound, bowstyle, gender, ageGroup, strictRounds, strictDistance)

private fun calculateOutdoor(
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
        archeryRound = outdoorEligible.check(roundObj, roundName).first
    } else {
        require(roundName == null) { STRICT_ROUNDS_MESSAGE }
        archeryRound = roundObj!!
    }

    require(score >= 0 && score <= archeryRound.maxScore()) { invalidScoreMessage(score, archeryRound) }

    val allClassScores =
        outdoorScores(archeryRound, null, bowstyle, gender, ageGroup, strictRounds, strictDistance)

    val groupData = agbOutdoorClassifications.getValue(outdoorGroupname(bowstyle, gender, ageGroup))
    // Of the classes available, what is the highest classification this score gets?
    // < 0 handles invalid classes & max scores, > score handles higher classifications
    for ((classname, classScore) in groupData.classes.zip(allClassScores)) {
        if (classScore < 0 || classScore > score) continue
        return classname
    }
    return "UC"
}

/**
 * Calculate AGB outdoor classification scores for a category and round.
 *
 * Classes that cannot be achieved are given a fill value of [INVALID_CLASS_SCORE] (`-9999`).
 *
 * @return scores required for each classification (EMB -> A3) in descending order.
 * @throws IllegalArgumentException if the round is not valid for this scheme (when [strictRounds]
 * is enabled).
 */
public fun agbOutdoorClassificationScores(
    archeryRound: Round,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
    strictDistance: Boolean = true,
): List<Int> = outdoorScores(archeryRound, null, bowstyle, gender, ageGroup, strictRounds, strictDistance)

/** As above but specifying the round by its codename. Requires [strictRounds]. */
public fun agbOutdoorClassificationScores(
    archeryRound: String,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean = true,
    strictDistance: Boolean = true,
): List<Int> = outdoorScores(null, archeryRound, bowstyle, gender, ageGroup, strictRounds, strictDistance)

private fun outdoorScores(
    roundObj: Round?,
    roundCode: String?,
    bowstyle: AgbBowstyle,
    gender: AgbGender,
    ageGroup: AgbAge,
    strictRounds: Boolean,
    strictDistance: Boolean,
): List<Int> {
    val archeryRound: Round
    val roundname: String
    if (strictRounds) {
        val (_, name) = outdoorEligible.check(roundObj, roundCode)
        roundname = name
        archeryRound = allOutdoorRounds.getValue(ClassificationUtils.stripSpots(name))
    } else {
        require(roundCode == null) { STRICT_ROUNDS_MESSAGE }
        archeryRound = roundObj!!
        // If a custom round has been passed use codename for prestige checks
        // This assumes users do not set the codename to an already existing codename
        roundname = archeryRound.codename
    }

    val groupname = outdoorGroupname(bowstyle, gender, ageGroup)
    val groupData = agbOutdoorClassifications.getValue(groupname)
    val isPrestige = roundname in groupData.prestigeRounds

    return classScoresFromHandicaps(groupData.classHc, archeryRound, "AGB") { scores ->
        // Is it a prestige round? If not remove MB scores
        if (strictRounds && !isPrestige) {
            for (i in 0 until 3) scores[i] = INVALID_CLASS_SCORE
        }
        // Restrict scores based on distance, unless we are enforcing strict round rules and this
        // is a prestige round in which case all classifications are available.
        if (strictDistance && !(strictRounds && isPrestige)) {
            val roundMaxDist = archeryRound.maxDistance().value
            for (i in scores.indices) {
                if (groupData.minDists[i] > roundMaxDist) scores[i] = INVALID_CLASS_SCORE
            }
        }
    }
}
