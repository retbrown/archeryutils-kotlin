package io.github.retbrown.archeryutils.length

/**
 * Conversion of quantities and unit aliases.
 *
 * Contains common abbreviations, pluralisations and capitalisations for supported units
 * as sets to allow easy membership checks.
 */
public object Length {
    /** All recognised aliases for the yard unit. */
    public val yard: Set<String> =
        setOf("Yard", "yard", "Yards", "yards", "Y", "y", "Yd", "yd", "Yds", "yds")

    /** All recognised aliases for the metre unit. */
    public val metre: Set<String> =
        setOf("Metre", "metre", "Metres", "metres", "M", "m", "Ms", "ms")

    /** All recognised aliases for the centimetre unit. */
    public val cm: Set<String> =
        setOf("Centimetre", "centimetre", "Centimetres", "centimetres", "CM", "cm", "CMs", "cms")

    /** All recognised aliases for the inch unit. */
    public val inch: Set<String> = setOf("Inch", "inch", "Inches", "inches")

    // Add aliases and conversions to any new supported units here.
    private val aliases: Map<String, Set<String>> =
        mapOf("yard" to yard, "metre" to metre, "cm" to cm, "inch" to inch)

    private val conversionsToMetres: Map<String, Double> =
        mapOf("metre" to 1.0, "yard" to 0.9144, "cm" to 0.01, "inch" to 0.0254)

    private val reversed: Map<String, String> =
        conversionsToMetres.keys.flatMap { name -> aliases.getValue(name).map { it to name } }.toMap()

    private val conversions: Map<String, Double> =
        reversed.mapValues { (_, name) -> conversionsToMetres.getValue(name) }

    /** The definitive names of all units that can be converted by this object. */
    public val knownUnits: Set<String> = definitiveUnits(conversions.keys)

    private fun factor(unit: String): Double =
        conversions[unit] ?: throw IllegalArgumentException("Unit '$unit' not recognised.")

    /**
     * Convert a distance in any supported [unit] to metres.
     *
     * ```
     * Length.toMetres(10.0, "inches") // 0.254
     * ```
     */
    public fun toMetres(value: Double, unit: String): Double = factor(unit) * value

    /**
     * Convert a distance in metres to the specified [unit].
     *
     * ```
     * Length.fromMetres(18.3, "yards") // 20.0131
     * ```
     */
    public fun fromMetres(metreValue: Double, unit: String): Double = metreValue / factor(unit)

    /**
     * Convert any string [alias] representing a distance unit to a single definitive name.
     *
     * ```
     * Length.definitiveUnit("Metres") // "metre"
     * ```
     */
    public fun definitiveUnit(alias: String): String =
        reversed[alias] ?: throw IllegalArgumentException("Unit '$alias' not recognised.")

    /** Reduce a collection of unit [aliases] to just their definitive names. */
    public fun definitiveUnits(aliases: Collection<String>): Set<String> =
        aliases.map { definitiveUnit(it) }.toSet()

    /**
     * Parse a [value] with optional [units] and validate the units.
     *
     * Returns the value along with the definitive name of its unit. When [units] is null the
     * [default] unit is used.
     *
     * @param supported the set of units (and aliases) that are acceptable.
     * @param default the unit used when none is given; must be in [supported].
     * @throws IllegalArgumentException if [default] or [units] are not in [supported].
     */
    public fun parseOptionalUnits(
        value: Double,
        units: String?,
        supported: Set<String>,
        default: String = "metre",
    ): Pair<Double, String> {
        require(default in supported) { "Default unit '$default' must be in supported units" }
        val unit = units ?: default
        require(unit in supported) {
            "Unit '$unit' not recognised. Select from ${definitiveUnits(supported)}."
        }
        return value to definitiveUnit(unit)
    }
}
