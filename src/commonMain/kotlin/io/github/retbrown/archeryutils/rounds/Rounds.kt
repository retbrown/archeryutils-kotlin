package io.github.retbrown.archeryutils.rounds

import io.github.retbrown.archeryutils.targets.Quantity
import io.github.retbrown.archeryutils.targets.ScoringSystem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** The built-in round databases, each a map of round codename to [Round]. */
public object Rounds {
    private val indoorAliases =
        setOf("i", "I", "indoors", "indoor", "in", "inside", "Indoors", "Indoor", "In", "Inside")
    private val outdoorAliases =
        setOf("o", "O", "outdoors", "outdoor", "out", "outside", "Outdoors", "Outdoor", "Out", "Outside")
    private val fieldAliases = setOf("f", "F", "field", "Field", "woods", "Woods")

    /**
     * Parse a JSON array of round definitions (the archeryutils round data format) into a map
     * keyed by round codename.
     */
    public fun fromJson(json: String): Map<String, Round> {
        val out = LinkedHashMap<String, Round>()
        for (element in Json.parseToJsonElement(json).jsonArray) {
            val round = element.jsonObject
            val codename = round.getValue("codename").jsonPrimitive.content
            val rawLocation = round["location"]?.jsonPrimitive?.contentOrNull
            val (location, indoor) =
                when (rawLocation) {
                    null -> null to false
                    in indoorAliases -> "indoor" to true
                    in outdoorAliases -> "outdoor" to false
                    in fieldAliases -> "field" to false
                    else -> null to false
                }
            val passes =
                round.getValue("passes").jsonArray.map { passElement ->
                    val p = passElement.jsonObject
                    Pass.atTarget(
                        p.getValue("n_arrows").jsonPrimitive.int,
                        ScoringSystem.fromValue(p.getValue("scoring").jsonPrimitive.content),
                        Quantity(
                            p.getValue("diameter").jsonPrimitive.double,
                            p["diameter_unit"]?.jsonPrimitive?.content ?: "cm",
                        ),
                        Quantity(
                            p.getValue("distance").jsonPrimitive.double,
                            p.getValue("dist_unit").jsonPrimitive.content,
                        ),
                        indoor = indoor,
                    )
                }
            out[codename] =
                Round(
                    round.getValue("name").jsonPrimitive.content,
                    passes,
                    codename = codename,
                    location = location,
                    body = round["body"]?.jsonPrimitive?.contentOrNull,
                    family = round["family"]?.jsonPrimitive?.contentOrNull,
                )
        }
        return out
    }

    /** Archery GB outdoor imperial rounds, keyed by codename. */
    public val agbOutdoorImperial: Map<String, Round> by lazy { fromJson(RoundData.AGB_OUTDOOR_IMPERIAL) }
    /** Archery GB outdoor metric rounds, keyed by codename. */
    public val agbOutdoorMetric: Map<String, Round> by lazy { fromJson(RoundData.AGB_OUTDOOR_METRIC) }
    /** Archery GB indoor rounds, keyed by codename. */
    public val agbIndoor: Map<String, Round> by lazy { fromJson(RoundData.AGB_INDOOR) }
    /** World Archery outdoor rounds, keyed by codename. */
    public val waOutdoor: Map<String, Round> by lazy { fromJson(RoundData.WA_OUTDOOR) }
    /** World Archery indoor rounds, keyed by codename. */
    public val waIndoor: Map<String, Round> by lazy { fromJson(RoundData.WA_INDOOR) }
    /** World Archery field rounds, keyed by codename. */
    public val waField: Map<String, Round> by lazy { fromJson(RoundData.WA_FIELD) }
    /** World Archery experimental rounds, keyed by codename. */
    public val waExperimental: Map<String, Round> by lazy { fromJson(RoundData.WA_EXPERIMENTAL) }
    /** IFAA field rounds, keyed by codename. */
    public val ifaaField: Map<String, Round> by lazy { fromJson(RoundData.IFAA_FIELD) }
    /** World Archery visually impaired rounds, keyed by codename. */
    public val waVI: Map<String, Round> by lazy { fromJson(RoundData.WA_VI) }
    /** Archery GB visually impaired rounds, keyed by codename. */
    public val agbVI: Map<String, Round> by lazy { fromJson(RoundData.AGB_VI) }
    /** Archery Australia outdoor metric rounds, keyed by codename. */
    public val aaOutdoorMetric: Map<String, Round> by lazy { fromJson(RoundData.AA_OUTDOOR_METRIC) }
    /** Archery Australia indoor rounds, keyed by codename. */
    public val aaIndoor: Map<String, Round> by lazy { fromJson(RoundData.AA_INDOOR) }
    /** Archery Australia field rounds, keyed by codename. */
    public val aaField: Map<String, Round> by lazy { fromJson(RoundData.AA_FIELD) }
    /** Miscellaneous rounds (e.g. practice distance rounds), keyed by codename. */
    public val misc: Map<String, Round> by lazy { fromJson(RoundData.MISCELLANEOUS) }

    /** All bundled rounds merged into a single map. */
    public val all: Map<String, Round> by lazy {
        buildMap {
            for (db in listOf(
                agbOutdoorImperial, agbOutdoorMetric, agbIndoor, waOutdoor, waIndoor, waField,
                waExperimental, ifaaField, waVI, agbVI, aaOutdoorMetric, aaIndoor, aaField, misc,
            )) putAll(db)
        }
    }
}
