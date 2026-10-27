package io.github.retbrown.archeryutils.classifications

/**
 * Archery GB genders.
 *
 * The Open category is equivalent to Male for scores following 2025 rule changes.
 * Scores remain the same, only branding has changed, so [MALE] is an alias of [OPEN].
 */
public enum class AgbGender {
    /** Open category (equivalent to Male for scores following 2025 rule changes). */
    OPEN,

    /** Female category. */
    FEMALE,
    ;

    /** Aliases for [AgbGender] entries. */
    public companion object {
        /** Alias of [OPEN]. */
        public val MALE: AgbGender = OPEN
    }
}

/** Archery GB age groups. The 50+ category is represented with [OVER_50]. */
public enum class AgbAge {
    /** Over 50 (50+). */
    OVER_50,
    /** Adult (senior). */
    ADULT,
    /** Under 21. */
    UNDER_21,
    /** Under 18. */
    UNDER_18,
    /** Under 16. */
    UNDER_16,
    /** Under 15. */
    UNDER_15,
    /** Under 14. */
    UNDER_14,
    /** Under 12. */
    UNDER_12,
    ;

    /** Deprecated `AGE_*` aliases for [AgbAge] entries, kept for compatibility with the Python names. */
    public companion object {
        /** Deprecated alias of [OVER_50]. */
        @Deprecated("Use OVER_50 instead.", ReplaceWith("OVER_50"))
        public val AGE_50_PLUS: AgbAge = OVER_50

        /** Deprecated alias of [ADULT]. */
        @Deprecated("Use ADULT instead.", ReplaceWith("ADULT"))
        public val AGE_ADULT: AgbAge = ADULT

        /** Deprecated alias of [UNDER_21]. */
        @Deprecated("Use UNDER_21 instead.", ReplaceWith("UNDER_21"))
        public val AGE_UNDER_21: AgbAge = UNDER_21

        /** Deprecated alias of [UNDER_18]. */
        @Deprecated("Use UNDER_18 instead.", ReplaceWith("UNDER_18"))
        public val AGE_UNDER_18: AgbAge = UNDER_18

        /** Deprecated alias of [UNDER_16]. */
        @Deprecated("Use UNDER_16 instead.", ReplaceWith("UNDER_16"))
        public val AGE_UNDER_16: AgbAge = UNDER_16

        /** Deprecated alias of [UNDER_15]. */
        @Deprecated("Use UNDER_15 instead.", ReplaceWith("UNDER_15"))
        public val AGE_UNDER_15: AgbAge = UNDER_15

        /** Deprecated alias of [UNDER_14]. */
        @Deprecated("Use UNDER_14 instead.", ReplaceWith("UNDER_14"))
        public val AGE_UNDER_14: AgbAge = UNDER_14

        /** Deprecated alias of [UNDER_12]. */
        @Deprecated("Use UNDER_12 instead.", ReplaceWith("UNDER_12"))
        public val AGE_UNDER_12: AgbAge = UNDER_12
    }
}

/**
 * Archery GB bowstyles.
 *
 * Under AGB rules Longbow and English Longbow are identical, but English Longbow is used in
 * field to avoid confusion with World Archery rules in which "Longbow" is equivalent to the
 * Archery GB Flatbow. [ENGLISHLONGBOW] is therefore an alias of [LONGBOW].
 */
public enum class AgbBowstyle {
    /** Compound. */
    COMPOUND,
    /** Recurve. */
    RECURVE,
    /** Barebow. */
    BAREBOW,
    /** Longbow (English Longbow in field). */
    LONGBOW,
    /** Traditional. */
    TRADITIONAL,
    /** Flatbow. */
    FLATBOW,
    /** Compound Limited. */
    COMPOUNDLIMITED,
    /** Compound Barebow. */
    COMPOUNDBAREBOW,
    ;

    /** Aliases for [AgbBowstyle] entries. */
    public companion object {
        /** Alias of [LONGBOW]. */
        public val ENGLISHLONGBOW: AgbBowstyle = LONGBOW
    }
}

/**
 * An archer's category: bowstyle, gender and age group.
 *
 * @property bowstyle the archer's bowstyle.
 * @property gender the archer's gender category.
 * @property ageGroup the archer's age group.
 */
public data class AgbCategory(
    val bowstyle: AgbBowstyle,
    val gender: AgbGender,
    val ageGroup: AgbAge,
)
