package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Round
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Value used in classification score lists where a classification cannot be achieved. */
public const val INVALID_CLASS_SCORE: Int = -9999

/**
 * AGB age group data.
 *
 * @property desc optional human readable description of the age group.
 * @property ageGroup the age group name.
 * @property open handicap adjustments by class for the Open gender.
 * @property female handicap adjustments by class for the Female gender.
 * @property sighted handicap adjustments by class for sighted bowstyles.
 * @property unsighted handicap adjustments by class for unsighted bowstyles.
 * @property step handicap step between age groups.
 */
public data class AgbAgeData(
    val desc: String?,
    val ageGroup: String,
    val open: List<Double>,
    val female: List<Double>,
    val sighted: List<Double>,
    val unsighted: List<Double>,
    val step: Int,
)

/**
 * AGB bowstyle handicap data.
 *
 * @property bowstyle the bowstyle name.
 * @property datumOut outdoor handicap datum.
 * @property classStepOut outdoor handicap step between classes.
 * @property genderStepOut outdoor handicap step between genders.
 * @property ageStepOut outdoor handicap step between age groups.
 * @property datumIn indoor handicap datum.
 * @property classStepIn indoor handicap step between classes.
 * @property genderStepIn indoor handicap step between genders.
 * @property ageStepIn indoor handicap step between age groups.
 * @property datumField field handicap datum.
 * @property classStepField field handicap step between classes.
 * @property genderStepField field handicap step between genders.
 * @property ageStepField field handicap step between age groups.
 */
public data class AgbBowstyleData(
    val bowstyle: String,
    val datumOut: Double,
    val classStepOut: Double,
    val genderStepOut: Double,
    val ageStepOut: Double,
    val datumIn: Double,
    val classStepIn: Double,
    val genderStepIn: Double,
    val ageStepIn: Double,
    val datumField: Double,
    val classStepField: Double,
    val genderStepField: Double,
    val ageStepField: Double,
)

/**
 * AGB classification names.
 *
 * @property location the location the classifications apply to (e.g. indoor, outdoor).
 * @property classes the short classification names, highest first.
 * @property classesLong the long classification names, in the same order as [classes].
 */
public data class AgbClassificationData(
    val location: String,
    val classes: List<String>,
    val classesLong: List<String>,
)

/** Utilities to assist in calculations of classifications. */
public object ClassificationUtils {
    /** Read the embedded AGB age categories, keyed by [AgbAge] name (e.g. `"OVER_50"`). */
    public fun readAgesJson(json: String = ClassificationData.AGB_AGES): Map<String, AgbAgeData> =
        Json.parseToJsonElement(json).jsonObject.mapValues { (_, v) ->
            val o = v.jsonObject
            AgbAgeData(
                desc = o["desc"]?.jsonPrimitive?.content,
                ageGroup = o.getValue("age_group").jsonPrimitive.content,
                open = o.doubles("open"),
                female = o.doubles("female"),
                sighted = o.doubles("sighted"),
                unsighted = o.doubles("unsighted"),
                step = o.getValue("step").jsonPrimitive.int,
            )
        }

    /** Read the embedded AGB bowstyle data, keyed by [AgbBowstyle] name (e.g. `"COMPOUND"`). */
    public fun readBowstylesJson(
        json: String = ClassificationData.AGB_BOWSTYLES,
    ): Map<String, AgbBowstyleData> =
        Json.parseToJsonElement(json).jsonObject.mapValues { (_, v) ->
            val o = v.jsonObject
            AgbBowstyleData(
                bowstyle = o.getValue("bowstyle").jsonPrimitive.content,
                datumOut = o.double("datum_out"),
                classStepOut = o.double("classStep_out"),
                genderStepOut = o.double("genderStep_out"),
                ageStepOut = o.double("ageStep_out"),
                datumIn = o.double("datum_in"),
                classStepIn = o.double("classStep_in"),
                genderStepIn = o.double("genderStep_in"),
                ageStepIn = o.double("ageStep_in"),
                datumField = o.double("datum_field"),
                classStepField = o.double("classStep_field"),
                genderStepField = o.double("genderStep_field"),
                ageStepField = o.double("ageStep_field"),
            )
        }

    /** Read the embedded AGB gender names (`"Male"`, `"Female"`, `"Open"`). */
    public fun readGendersJson(json: String = ClassificationData.AGB_GENDERS): List<String> =
        Json.parseToJsonElement(json).jsonObject.getValue("genders").jsonArray.map {
            it.jsonPrimitive.content
        }

    /**
     * Read the embedded AGB classification names for a [classSystem]:
     * `"agb_indoor"`, `"agb_outdoor"` or `"agb_field"`.
     *
     * @throws IllegalArgumentException if an unknown classification system is specified.
     */
    public fun readClassesJson(classSystem: String): AgbClassificationData {
        val json =
            when (classSystem) {
                "agb_indoor" -> ClassificationData.AGB_CLASSES_IN
                // Field classifications are same as outdoor
                "agb_outdoor", "agb_field" -> ClassificationData.AGB_CLASSES_OUT
                else ->
                    throw IllegalArgumentException(
                        "Unexpected classification system specified. " +
                            "Expected one of 'agb_indoor', 'agb_outdoor', 'agb_field'.",
                    )
            }
        val o = Json.parseToJsonElement(json).jsonObject
        return AgbClassificationData(
            location = o.getValue("location").jsonPrimitive.content,
            classes = o.getValue("classes").jsonArray.map { it.jsonPrimitive.content },
            classesLong = o.getValue("classes_long").jsonArray.map { it.jsonPrimitive.content },
        )
    }

    /** Generate a single string id for a particular category, e.g. `ADULT_OPEN_RECURVE`. */
    public fun getGroupname(bowstyle: AgbBowstyle, gender: AgbGender, ageGroup: AgbAge): String =
        "${ageGroup.name}_${gender.name}_${bowstyle.name}"

    /**
     * Calculate the AGB age and gender step for classification tables.
     *
     * Contains a tricky fiddle for aligning Open and Female under 15 scores and below, and a
     * necessary check to ensure that gender step doesn't overtake age step when doing this.
     *
     * @param ageCat age category as an integer (number of age steps below adult, e.g. 50+=1, U14=5).
     * @param ageStep age group handicap step for this category.
     * @param genderStep gender handicap step for this category.
     * @return age and gender handicap step for this category's MB relative to datum.
     */
    public fun getAgeGenderStep(
        gender: AgbGender,
        ageCat: Int,
        ageStep: Double,
        genderStep: Double,
    ): Double {
        val under16 = 3 // U16 is the fourth age category after 50+, adult, and U18

        // There is a danger that gender step overtakes age step at U15/U16 interface.
        // If this happens set to age step to align U16 with U16.
        if (gender == AgbGender.FEMALE && ageCat == under16 && ageStep < genderStep) {
            return ageCat * ageStep + ageStep
        }
        // For females under 16 or older apply gender step and age steps
        if (gender == AgbGender.FEMALE && ageCat <= under16) return genderStep + ageCat * ageStep
        // Default case for open, and females aged U15 or younger - apply only age steps
        return ageCat * ageStep
    }

    /** Strip the spot suffixes (`_triple`, `_5_centre`, `_small`) from a round codename. */
    public fun stripSpots(roundname: String): String =
        roundname.replace("_triple", "").replace("_5_centre", "").replace("_small", "")

    private val compoundCodenames =
        mapOf(
            "bray_i" to "bray_i_compound",
            "bray_i_triple" to "bray_i_compound_triple",
            "bray_ii" to "bray_ii_compound",
            "bray_ii_triple" to "bray_ii_compound_triple",
            "stafford" to "stafford_compound",
            "portsmouth" to "portsmouth_compound",
            "portsmouth_triple" to "portsmouth_compound_triple",
            "vegas" to "vegas_compound",
            "wa18" to "wa18_compound",
            "wa18_triple" to "wa18_compound_triple",
            "wa25" to "wa25_compound",
            "wa25_triple" to "wa25_compound_triple",
        )

    /** Convert any indoor round with special compound scoring to the compound codename. */
    public fun getCompoundCodename(roundCodename: String): String =
        compoundCodenames[roundCodename] ?: roundCodename

    /**
     * Check for repeated scores in a classification table and fix where needed.
     *
     * Forces at least 1 point separation between classes, increasing where needed up to
     * the maximum score. Modifies [scores] in place and returns it.
     *
     * @param scores classification scores from high to low.
     * @param maxScore the maximum possible score on the round in question.
     */
    public fun fixRepeatedScores(scores: MutableList<Int>, maxScore: Double): MutableList<Int> {
        for (i in scores.size - 1 downTo 1) {
            if (scores[i] < 0) {
                // If previous score is invalid then set the rest to invalid
                scores[i - 1] = INVALID_CLASS_SCORE
            } else if (scores[i - 1] <= scores[i] && scores[i - 1] >= 0) {
                // Next score (i-1) is valid and equal to (or below) the previous score (i)
                scores[i - 1] =
                    if (scores[i].toDouble() == maxScore) INVALID_CLASS_SCORE else scores[i] + 1
            }
        }
        return scores
    }
}

private fun JsonObject.doubles(key: String): List<Double> =
    getValue(key).jsonArray.map { it.jsonPrimitive.double }

private fun JsonObject.double(key: String): Double = getValue(key).jsonPrimitive.double

/**
 * Resolves a round (or round codename) against a set of eligible rounds.
 * Shared by all classification schemes.
 */
internal class EligibleRounds(val rounds: Map<String, Round>, private val errorMessage: String) {
    /** Resolve [round] or [codename] to a (round, codename) pair, or throw. */
    fun check(round: Round?, codename: String?): Pair<Round, String> {
        if (codename != null) {
            rounds[codename]?.let { return it to codename }
        } else if (round != null) {
            rounds.entries.firstOrNull { it.value == round }?.let { return it.value to it.key }
        }
        throw IllegalArgumentException(errorMessage)
    }
}

internal const val STRICT_ROUNDS_MESSAGE: String =
    "strict_rounds is False so archery_round must be explicitly specified as " +
        "a Round type instead of a string."

internal fun invalidScoreMessage(score: Double, round: Round): String =
    "Invalid score of ${score.pyStr()} for a ${round.name}. " +
        "Should be in range 0-${round.maxScore()}."

/** Format a score like Python prints it: whole numbers without a decimal point. */
internal fun Double.pyStr(): String = if (this == kotlin.math.floor(this) && kotlin.math.abs(this) < 1e15) toLong().toString() else toString()
