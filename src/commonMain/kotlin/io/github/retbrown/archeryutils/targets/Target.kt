package io.github.retbrown.archeryutils.targets

import io.github.retbrown.archeryutils.length.Length
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

/** A mapping of a target ring diameter in metres to the score for that ring. */
public typealias FaceSpec = Map<Double, Int>

private fun rnd6(x: Double): Double = round(x * 1e6) / 1e6

/**
 * A target, defined by its scoring system, size, distance and whether it is shot indoors.
 *
 * ```
 * val my720Target = Target(ScoringSystem.TEN_ZONE, 122.0, 70.0)
 * val worcester = Target(ScoringSystem.WORCESTER, Quantity(16.0, "inches"), Quantity(20.0, "yards"))
 * val wa18 = Target(ScoringSystem.TEN_ZONE, Quantity(40.0, "cm"), Quantity(18.0, "m"), indoor = true)
 * ```
 *
 * @property scoringSystem the target face/scoring system type.
 * @property diameter the target diameter in metres.
 * @property distance the target distance in metres.
 * @property nativeDiameter the target diameter in the units it was defined in.
 * @property nativeDistance the target distance in the units it was defined in.
 * @property indoor whether the round is indoors for arrow diameter purposes.
 */
public class Target private constructor(
    public val scoringSystem: ScoringSystem,
    diameter: Quantity,
    distance: Quantity,
    public val indoor: Boolean,
    customSpec: FaceSpec?,
) {
    public val nativeDiameter: Quantity
    public val nativeDistance: Quantity
    public val diameter: Double
    public val distance: Double
    private val customFaceSpec: FaceSpec? = customSpec

    init {
        val (diam, diamUnit) =
            Length.parseOptionalUnits(diameter.value, diameter.units, SUPPORTED_DIAMETER_UNITS, "cm")
        val (dist, distUnit) =
            Length.parseOptionalUnits(distance.value, distance.units, SUPPORTED_DISTANCE_UNITS, "metre")
        nativeDiameter = Quantity(diam, diamUnit)
        nativeDistance = Quantity(dist, distUnit)
        this.diameter = Length.toMetres(diam, diamUnit)
        this.distance = Length.toMetres(dist, distUnit)
    }

    /**
     * Create a target with a built-in [scoringSystem].
     *
     * @throws IllegalArgumentException if the units are unsupported or [scoringSystem] is
     * [ScoringSystem.CUSTOM] (use [fromFaceSpec] instead).
     */
    public constructor(
        scoringSystem: ScoringSystem,
        diameter: Quantity,
        distance: Quantity,
        indoor: Boolean = false,
    ) : this(scoringSystem, diameter, distance, indoor, null)

    /** Create a target with [diameter] in centimetres and [distance] in metres. */
    public constructor(
        scoringSystem: ScoringSystem,
        diameter: Double,
        distance: Double,
        indoor: Boolean = false,
    ) : this(scoringSystem, Quantity.cm(diameter), Quantity.metres(distance), indoor)

    /**
     * The target's face specification: a mapping of ring diameters in metres to scores.
     *
     * @throws IllegalArgumentException if this is a [ScoringSystem.CUSTOM] target that was not
     * created with [fromFaceSpec].
     */
    public val faceSpec: FaceSpec by lazy {
        customFaceSpec
            ?: if (scoringSystem == ScoringSystem.CUSTOM) {
                throw IllegalArgumentException(
                    "Trying to generate face spec for custom target but no existing spec found: " +
                        "try instantiating with `Target.fromFaceSpec` instead",
                )
            } else {
                genFaceSpec(scoringSystem, this.diameter)
            }
    }

    /** The maximum numerical score possible on this target (i.e. not X). */
    public fun maxScore(): Double = (faceSpec.values.maxOrNull() ?: 0).toDouble()

    /** The minimum numerical score possible on this target (excluding miss/0). */
    public fun minScore(): Double = (faceSpec.values.minOrNull() ?: 0).toDouble()

    /** A representation of the target, e.g. `Target('10_zone', (122, 'cm'), (70, 'metre'), indoor=False)`. */
    override fun toString(): String =
        "Target('$scoringSystem', (${fmt6g(nativeDiameter.value)}, '${nativeDiameter.units}'), " +
            "(${fmt6g(nativeDistance.value)}, '${nativeDistance.units}'), indoor=${indoor.toString().replaceFirstChar { it.uppercase() }})"

    /** Targets are equal if all their parameters (including face specification) are equal. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Target) return false
        return scoringSystem == other.scoringSystem &&
            diameter == other.diameter &&
            nativeDiameter == other.nativeDiameter &&
            distance == other.distance &&
            nativeDistance == other.nativeDistance &&
            indoor == other.indoor &&
            faceSpec == other.faceSpec
    }

    /** Hash consistent with [equals]. */
    override fun hashCode(): Int {
        var result = scoringSystem.hashCode()
        result = 31 * result + diameter.hashCode()
        result = 31 * result + nativeDiameter.hashCode()
        result = 31 * result + distance.hashCode()
        result = 31 * result + nativeDistance.hashCode()
        result = 31 * result + indoor.hashCode()
        result = 31 * result + faceSpec.hashCode()
        return result
    }

    /** Constants, custom-target construction and face specification generation. */
    public companion object {
        /** Allowable units and aliases for target distances. */
        public val SUPPORTED_DISTANCE_UNITS: Set<String> = Length.yard + Length.metre

        /** Allowable units and aliases for target diameters. */
        public val SUPPORTED_DIAMETER_UNITS: Set<String> = Length.cm + Length.inch + Length.metre

        /**
         * Build a target with a custom scoring system.
         *
         * [diameter] must still be provided as it is impossible to know the nominal diameter
         * from a face specification alone. It is superseded by [faceSpec] and has no effect when
         * calculating handicaps.
         *
         * Targets must be formed of concentric rings, and the score must monotonically decrease
         * as the rings get larger.
         *
         * ```
         * // Kings of archery recurve scoring triple spot
         * val specs = mapOf(0.08 to 10, 0.12 to 8, 0.16 to 7, 0.2 to 6)
         * val target = Target.fromFaceSpec(specs, Quantity.cm(40.0), Quantity.metres(18.0))
         * ```
         *
         * @param faceSpec mapping of ring diameters to score, in [specUnits].
         * @param specUnits units of the ring diameters in [faceSpec], default metres.
         */
        public fun fromFaceSpec(
            faceSpec: FaceSpec,
            diameter: Quantity,
            distance: Quantity,
            indoor: Boolean = false,
            specUnits: String = "metre",
        ): Target {
            val (_, units) =
                Length.parseOptionalUnits(0.0, specUnits, SUPPORTED_DIAMETER_UNITS, "metre")
            val spec = LinkedHashMap<Double, Int>()
            for ((ringDiam, score) in faceSpec) spec[rnd6(Length.toMetres(ringDiam, units))] = score
            return Target(ScoringSystem.CUSTOM, diameter, distance, indoor, spec)
        }

        /**
         * Derive the face specification for a supported [system].
         *
         * @param diameter target diameter in metres.
         * @return mapping of target ring sizes in metres to score.
         *
         * ```
         * Target.genFaceSpec(ScoringSystem.WA_FIELD, 0.6)
         * // {0.06: 6, 0.12: 5, 0.24: 4, 0.36: 3, 0.48: 2, 0.6: 1}
         * ```
         */
        public fun genFaceSpec(system: ScoringSystem, diameter: Double): FaceSpec {
            val missing =
                when (system) {
                    ScoringSystem.TEN_ZONE_SIX_RING, ScoringSystem.ELEVEN_ZONE_SIX_RING -> 4
                    ScoringSystem.TEN_ZONE_FIVE_RING,
                    ScoringSystem.TEN_ZONE_FIVE_RING_COMPOUND,
                    ScoringSystem.ELEVEN_ZONE_FIVE_RING,
                    -> 5
                    ScoringSystem.WORCESTER_2_RING -> 3
                    else -> 0
                }
            val spec = LinkedHashMap<Double, Int>()
            when (system) {
                ScoringSystem.FIVE_ZONE ->
                    for (n in 1..10 step 2) spec[rnd6((n + 1) * diameter / 10)] = 10 - n
                ScoringSystem.TEN_ZONE, ScoringSystem.TEN_ZONE_SIX_RING, ScoringSystem.TEN_ZONE_FIVE_RING ->
                    for (n in 1 until 11 - missing) spec[rnd6(n * diameter / 10)] = 11 - n
                ScoringSystem.TEN_ZONE_COMPOUND, ScoringSystem.TEN_ZONE_FIVE_RING_COMPOUND -> {
                    spec[rnd6(diameter / 20)] = 10
                    for (n in 2 until 11 - missing) spec[rnd6(n * diameter / 10)] = 11 - n
                }
                ScoringSystem.ELEVEN_ZONE, ScoringSystem.ELEVEN_ZONE_SIX_RING, ScoringSystem.ELEVEN_ZONE_FIVE_RING -> {
                    spec[rnd6(diameter / 20)] = 11
                    for (n in 1 until 11 - missing) spec[rnd6(n * diameter / 10)] = 11 - n
                }
                ScoringSystem.WA_FIELD -> {
                    spec[rnd6(diameter / 10)] = 6
                    for (n in 1..5) spec[rnd6(n * diameter / 5)] = 6 - n
                }
                ScoringSystem.IFAA_FIELD ->
                    for (n in 1..5 step 2) spec[rnd6(n * diameter / 5)] = 5 - n / 2
                ScoringSystem.AA_NATIONAL_FIELD ->
                    for (n in 1..5) spec[rnd6(n * diameter / 5)] = 6 - n
                ScoringSystem.BEITER_HIT_MISS -> spec[diameter] = 1
                ScoringSystem.WORCESTER, ScoringSystem.WORCESTER_2_RING, ScoringSystem.IFAA_FIELD_EXPERT ->
                    for (n in 1 until 6 - missing) spec[rnd6(n * diameter / 5)] = 6 - n
                ScoringSystem.CUSTOM ->
                    throw IllegalArgumentException("Scoring system '${system.value}' is not supported")
            }
            return spec
        }
    }
}

/** Format like Python's `{x:.6g}`. */
internal fun fmt6g(x: Double): String {
    if (x == 0.0 || x.isNaN() || x.isInfinite()) return x.toString().removeSuffix(".0")
    val exp = floor(log10(abs(x))).toInt()
    val scale = 10.0.pow(5 - exp)
    val s = (round(x * scale) / scale).toString()
    return s.removeSuffix(".0")
}
