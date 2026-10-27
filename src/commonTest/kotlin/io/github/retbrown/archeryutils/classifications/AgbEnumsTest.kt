package io.github.retbrown.archeryutils.classifications

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// Counterpart of tests/unit/classifications/test_agb_data.py
@Suppress("DEPRECATION")
class AgbEnumsTest {
    @Test
    fun deprecatedAgeAliasesMapToNewNames() {
        assertSame(AgbAge.OVER_50, AgbAge.AGE_50_PLUS)
        assertSame(AgbAge.ADULT, AgbAge.AGE_ADULT)
        assertSame(AgbAge.UNDER_21, AgbAge.AGE_UNDER_21)
        assertSame(AgbAge.UNDER_18, AgbAge.AGE_UNDER_18)
        assertSame(AgbAge.UNDER_16, AgbAge.AGE_UNDER_16)
        assertSame(AgbAge.UNDER_15, AgbAge.AGE_UNDER_15)
        assertSame(AgbAge.UNDER_14, AgbAge.AGE_UNDER_14)
        assertSame(AgbAge.UNDER_12, AgbAge.AGE_UNDER_12)
    }

    @Test
    fun aliases() {
        assertSame(AgbGender.OPEN, AgbGender.MALE)
        assertSame(AgbBowstyle.LONGBOW, AgbBowstyle.ENGLISHLONGBOW)
        assertEquals(2, AgbGender.entries.size)
        assertEquals(8, AgbBowstyle.entries.size)
    }
}
