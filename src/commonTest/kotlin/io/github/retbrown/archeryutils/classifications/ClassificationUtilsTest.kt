package io.github.retbrown.archeryutils.classifications

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// Ported from tests/unit/classifications/test_classification_utils.py
class ClassificationUtilsTest {
    @Test
    fun getGroupname() {
        val cases =
            listOf(
                Triple(AgbBowstyle.BAREBOW, AgbAge.ADULT, AgbGender.OPEN) to "ADULT_OPEN_BAREBOW",
                Triple(AgbBowstyle.BAREBOW, AgbAge.UNDER_18, AgbGender.OPEN) to "UNDER_18_OPEN_BAREBOW",
                Triple(AgbBowstyle.RECURVE, AgbAge.UNDER_18, AgbGender.FEMALE) to "UNDER_18_FEMALE_RECURVE",
                // Check English Longbow becomes Longbow
                Triple(AgbBowstyle.ENGLISHLONGBOW, AgbAge.ADULT, AgbGender.FEMALE) to "ADULT_FEMALE_LONGBOW",
                // Check backwards compatibility of Male category
                Triple(AgbBowstyle.BAREBOW, AgbAge.ADULT, AgbGender.MALE) to "ADULT_OPEN_BAREBOW",
            )
        for ((args, expected) in cases) {
            val (bowstyle, age, gender) = args
            assertEquals(expected, ClassificationUtils.getGroupname(bowstyle, gender, age))
        }
    }

    @Test
    fun stripSpots() {
        val cases =
            mapOf(
                "portsmouth" to "portsmouth",
                "portsmouth_triple" to "portsmouth",
                "portsmouth_compound" to "portsmouth_compound",
                "portsmouth_compound_triple" to "portsmouth_compound",
                "portsmouth_triple_compound" to "portsmouth_compound",
                "worcester_5_centre" to "worcester",
            )
        for ((name, expected) in cases) assertEquals(expected, ClassificationUtils.stripSpots(name))
    }

    @Test
    fun fixRepeatedScores() {
        val cases =
            listOf(
                Triple(listOf(100, 95, 90, 85, 80), 100.0, listOf(100, 95, 90, 85, 80)),
                Triple(listOf(100, 95, 90, 90, 80), 100.0, listOf(100, 95, 91, 90, 80)),
                Triple(listOf(100, 95, 90, 90, 90, 80), 100.0, listOf(100, 95, 92, 91, 90, 80)),
                Triple(listOf(100, 100, 100, 95, 90), 100.0, listOf(-9999, -9999, 100, 95, 90)),
                Triple(listOf(80, 80, 80, 80), 100.0, listOf(83, 82, 81, 80)),
                Triple(listOf(100, 100, 100), 100.0, listOf(-9999, -9999, 100)),
                Triple(listOf(90), 100.0, listOf(90)),
                Triple(listOf(85, 85), 100.0, listOf(86, 85)),
                // Real example that caught edge case in the code
                Triple(
                    listOf(500, 499, 498, 497, 496, 425, 343, 259, 185),
                    500.0,
                    listOf(500, 499, 498, 497, 496, 425, 343, 259, 185),
                ),
            )
        for ((scores, maxScore, expected) in cases) {
            assertEquals(expected, ClassificationUtils.fixRepeatedScores(scores.toMutableList(), maxScore))
        }
    }

    @Test
    fun getAgeGenderStep() {
        // Open: age steps only
        assertEquals(13.0, ClassificationUtils.getAgeGenderStep(AgbGender.OPEN, 2, 6.5, 5.0))
        // Female U16 and older: gender step plus age steps
        assertEquals(5.0 + 2 * 6.5, ClassificationUtils.getAgeGenderStep(AgbGender.FEMALE, 2, 6.5, 5.0))
        // Female U16 where gender step overtakes age step: align with open U16 + 1 step
        assertEquals(3 * 4.0 + 4.0, ClassificationUtils.getAgeGenderStep(AgbGender.FEMALE, 3, 4.0, 5.0))
        // Female U15 and younger: age steps only
        assertEquals(4 * 6.5, ClassificationUtils.getAgeGenderStep(AgbGender.FEMALE, 4, 6.5, 5.0))
    }

    @Test
    fun readClassesJsonUnknownSystem() {
        assertFailsWith<IllegalArgumentException> { ClassificationUtils.readClassesJson("agb_unknown") }
    }

    @Test
    fun embeddedDataLoads() {
        assertEquals(setOf("Male", "Female", "Open"), ClassificationUtils.readGendersJson().toSet())
        assertEquals(AgbAge.entries.map { it.name }.toSet(), ClassificationUtils.readAgesJson().keys)
        assertEquals(AgbBowstyle.entries.map { it.name }.toSet(), ClassificationUtils.readBowstylesJson().keys)
        assertEquals(9, ClassificationUtils.readClassesJson("agb_outdoor").classes.size)
        assertEquals(8, ClassificationUtils.readClassesJson("agb_indoor").classes.size)
    }
}
