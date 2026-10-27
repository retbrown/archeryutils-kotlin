package io.github.retbrown.archeryutils.targets

import kotlin.test.Test
import kotlin.test.assertEquals

/** Face specification generation for every built-in scoring system. */
class FaceSpecTest {
    private fun spec(system: ScoringSystem, diameter: Double = 1.0) = Target.genFaceSpec(system, diameter)

    @Test
    fun tenZoneVariants() {
        assertEquals(10, spec(ScoringSystem.TEN_ZONE).size)
        assertEquals(6, spec(ScoringSystem.TEN_ZONE_SIX_RING).size)
        assertEquals(5, spec(ScoringSystem.TEN_ZONE_FIVE_RING).size)
        assertEquals(10, spec(ScoringSystem.TEN_ZONE_COMPOUND).size)
        assertEquals(0.05, spec(ScoringSystem.TEN_ZONE_COMPOUND).entries.first { it.value == 10 }.key)
        assertEquals(5, spec(ScoringSystem.TEN_ZONE_FIVE_RING_COMPOUND).size)
        assertEquals(mapOf(0.1 to 10, 0.2 to 9, 0.3 to 8, 0.4 to 7, 0.5 to 6), spec(ScoringSystem.TEN_ZONE_FIVE_RING))
    }

    @Test
    fun elevenZoneVariants() {
        assertEquals(11, spec(ScoringSystem.ELEVEN_ZONE).size)
        assertEquals(7, spec(ScoringSystem.ELEVEN_ZONE_SIX_RING).size)
        assertEquals(6, spec(ScoringSystem.ELEVEN_ZONE_FIVE_RING).size)
        assertEquals(11, spec(ScoringSystem.ELEVEN_ZONE_FIVE_RING).maxOf { it.value })
    }

    @Test
    fun fieldAndWorcesterVariants() {
        assertEquals(mapOf(0.1 to 6, 0.2 to 5, 0.4 to 4, 0.6 to 3, 0.8 to 2, 1.0 to 1), spec(ScoringSystem.WA_FIELD))
        assertEquals(mapOf(0.2 to 5, 0.6 to 4, 1.0 to 3), spec(ScoringSystem.IFAA_FIELD))
        assertEquals(mapOf(0.2 to 5, 0.4 to 4, 0.6 to 3, 0.8 to 2, 1.0 to 1), spec(ScoringSystem.IFAA_FIELD_EXPERT))
        assertEquals(mapOf(0.2 to 5, 0.4 to 4, 0.6 to 3, 0.8 to 2, 1.0 to 1), spec(ScoringSystem.WORCESTER))
        assertEquals(mapOf(0.2 to 5, 0.4 to 4), spec(ScoringSystem.WORCESTER_2_RING))
        assertEquals(mapOf(0.2 to 5, 0.4 to 4, 0.6 to 3, 0.8 to 2, 1.0 to 1), spec(ScoringSystem.AA_NATIONAL_FIELD))
        assertEquals(mapOf(1.0 to 1), spec(ScoringSystem.BEITER_HIT_MISS))
    }

    @Test
    fun everySupportedSystemGeneratesMonotonicSpec() {
        for (system in ScoringSystem.entries.filter { it != ScoringSystem.CUSTOM }) {
            val s = spec(system, 1.22)
            assertEquals(true, s.isNotEmpty(), system.value)
            val scores = s.entries.sortedBy { it.key }.map { it.value }
            assertEquals(scores.sortedDescending(), scores, system.value)
        }
    }

    @Test
    fun fromValueRoundTrips() {
        for (system in ScoringSystem.entries) {
            assertEquals(system, ScoringSystem.fromValue(system.value))
            assertEquals(system.value, system.toString())
        }
    }

    @Test
    fun quantityHelpers() {
        assertEquals(Quantity(1.0, "cm"), Quantity.cm(1.0))
        assertEquals(Quantity(1.0, "metre"), Quantity.metres(1.0))
        assertEquals(Quantity(1.0, "yard"), Quantity.yards(1.0))
        assertEquals(Quantity(1.0, "inch"), Quantity.inches(1.0))
    }

    @Test
    fun emptyCustomSpecHasZeroScores() {
        val t = Target.fromFaceSpec(emptyMap(), Quantity.cm(40.0), Quantity.metres(18.0))
        assertEquals(0.0, t.maxScore())
        assertEquals(0.0, t.minScore())
    }
}
