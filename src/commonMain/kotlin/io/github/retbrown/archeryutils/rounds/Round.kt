package io.github.retbrown.archeryutils.rounds

import io.github.retbrown.archeryutils.targets.Quantity
import io.github.retbrown.archeryutils.targets.ScoringSystem
import io.github.retbrown.archeryutils.targets.Target

/**
 * A pass of arrows shot at a target: the sub-unit of a [Round], e.g. a single distance
 * in a round or half of a single-distance round.
 *
 * ```
 * val my720Pass = Pass(36, Target(ScoringSystem.TEN_ZONE, 122.0, 70.0))
 * ```
 *
 * @property nArrows number of arrows in this pass.
 * @property target the [Target] shot at.
 */
public class Pass(nArrows: Int, public val target: Target) {
    public val nArrows: Int = kotlin.math.abs(nArrows)

    /** The target scoring system. */
    public val scoringSystem: ScoringSystem get() = target.scoringSystem

    /** The target diameter in metres. */
    public val diameter: Double get() = target.diameter

    /** The target distance in metres. */
    public val distance: Double get() = target.distance

    /** The target diameter in its native units. */
    public val nativeDiameter: Quantity get() = target.nativeDiameter

    /** The target distance in its native units. */
    public val nativeDistance: Quantity get() = target.nativeDistance

    /** Whether the target is indoors. */
    public val indoor: Boolean get() = target.indoor

    /** The maximum numerical score possible on this pass (not counting Xs). */
    public fun maxScore(): Double = nArrows * target.maxScore()

    /** A representation of the pass, e.g. `Pass(36, Target(...))`. */
    override fun toString(): String = "Pass($nArrows, $target)"

    /** Passes are equal if they have the same number of arrows and equal targets. */
    override fun equals(other: Any?): Boolean =
        this === other || (other is Pass && nArrows == other.nArrows && target == other.target)

    /** Hash consistent with [equals]. */
    override fun hashCode(): Int = 31 * nArrows + target.hashCode()

    /** Alternative constructors for [Pass]. */
    public companion object {
        /**
         * Create a Pass directly from the parameters of its target. Shares the behaviour and
         * defaults of the [Target] constructor.
         */
        public fun atTarget(
            nArrows: Int,
            scoringSystem: ScoringSystem,
            diameter: Quantity,
            distance: Quantity,
            indoor: Boolean = false,
        ): Pass = Pass(nArrows, Target(scoringSystem, diameter, distance, indoor))

        /** Create a Pass with target [diameter] in centimetres and [distance] in metres. */
        public fun atTarget(
            nArrows: Int,
            scoringSystem: ScoringSystem,
            diameter: Double,
            distance: Double,
            indoor: Boolean = false,
        ): Pass = Pass(nArrows, Target(scoringSystem, diameter, distance, indoor))
    }
}

/**
 * An archery round made up of a number of [Pass]es, e.g. for different distances.
 *
 * ```
 * val my720Pass = Pass.atTarget(36, ScoringSystem.TEN_ZONE, 122.0, 70.0)
 * val my720Round = Round("WA 720", listOf(my720Pass, my720Pass))
 * ```
 *
 * @property name formal name of the round.
 * @property passes the passes making up the round.
 * @property codename a machine readable identifier for the round.
 * @property location where the round is shot (e.g. "indoor", "outdoor", "field").
 * @property body the governing body the round belongs to.
 * @property family the family the round belongs to (e.g. wa1440, western).
 * @property nArrows total number of arrows in this round.
 * @throws IllegalArgumentException if [passes] is empty.
 */
public class Round(
    public val name: String,
    passes: Iterable<Pass>,
    public val codename: String = "",
    public val location: String? = null,
    public val body: String? = null,
    public val family: String? = null,
) {
    public val passes: List<Pass> = passes.toList()
    public val nArrows: Int

    init {
        require(this.passes.isNotEmpty()) { "passes must contain at least one Pass object but none supplied." }
        nArrows = this.passes.sumOf { it.nArrows }
    }

    /** The maximum numerical score possible on this round (not counting Xs). */
    public fun maxScore(): Double = passes.sumOf { it.maxScore() }

    /**
     * The maximum distance shot on this round, in whatever units it was defined in
     * (no conversion is made).
     */
    public fun maxDistance(): Quantity = passes.maxBy { it.distance }.nativeDistance

    /** A human readable summary of each [Pass] in the round. */
    public fun info(): String = buildString {
        append("A $name consists of ${passes.size} passes:")
        for (pass in passes) {
            val (diam, diamUnits) = pass.nativeDiameter
            val (dist, distUnits) = pass.nativeDistance
            append("\n\t- ${pass.nArrows} arrows at a ${diam.fmt1()} $diamUnits target at ${dist.fmt1()} ${distUnits}s.")
        }
    }

    /** A short representation, e.g. `<Round: 'WA 720'>`. */
    override fun toString(): String = "<Round: '$name'>"

    /** Equality is based on name and passes only; location/body/family do not affect behaviour. */
    override fun equals(other: Any?): Boolean =
        this === other || (other is Round && name == other.name && passes == other.passes)

    /** Hash consistent with [equals]. */
    override fun hashCode(): Int = 31 * name.hashCode() + passes.hashCode()
}

private fun Double.fmt1(): String {
    val tenths = kotlin.math.round(this * 10).toLong()
    return "${tenths / 10}.${kotlin.math.abs(tenths % 10)}"
}
