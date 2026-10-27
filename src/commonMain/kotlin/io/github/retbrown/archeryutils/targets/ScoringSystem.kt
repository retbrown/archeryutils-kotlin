package io.github.retbrown.archeryutils.targets

/** All scoring systems that archeryutils knows how to handle by default. */
public enum class ScoringSystem(
    /** The string identifier of the scoring system, as used in round data files. */
    public val value: String,
) {
    FIVE_ZONE("5_zone"),
    TEN_ZONE("10_zone"),
    TEN_ZONE_COMPOUND("10_zone_compound"),
    TEN_ZONE_SIX_RING("10_zone_6_ring"),
    TEN_ZONE_FIVE_RING("10_zone_5_ring"),
    TEN_ZONE_FIVE_RING_COMPOUND("10_zone_5_ring_compound"),
    ELEVEN_ZONE("11_zone"),
    ELEVEN_ZONE_SIX_RING("11_zone_6_ring"),
    ELEVEN_ZONE_FIVE_RING("11_zone_5_ring"),
    WA_FIELD("WA_field"),
    IFAA_FIELD("IFAA_field"),
    IFAA_FIELD_EXPERT("IFAA_field_expert"),
    AA_NATIONAL_FIELD("AA_national_field"),
    BEITER_HIT_MISS("Beiter_hit_miss"),
    WORCESTER("Worcester"),
    WORCESTER_2_RING("Worcester_2_ring"),
    CUSTOM("Custom"),
    ;

    /** The string identifier of the scoring system (same as [value]). */
    override fun toString(): String = value

    /** Lookup helpers for [ScoringSystem]. */
    public companion object {
        /**
         * Look up a scoring system by its string [value], e.g. `"10_zone"`.
         *
         * @throws IllegalArgumentException if [value] is not a supported scoring system.
         */
        public fun fromValue(value: String): ScoringSystem =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException(
                    "Invalid Target Face Type specified.\n" +
                        "Please select from '${entries.joinToString("', '") { it.value }}'.",
                )
    }
}
