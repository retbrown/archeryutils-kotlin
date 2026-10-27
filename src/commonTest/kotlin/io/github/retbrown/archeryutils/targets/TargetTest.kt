package io.github.retbrown.archeryutils.targets

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TargetTest {
    @Test
    fun repr() {
        assertEquals(
            "Target('10_zone', (122, 'cm'), (50, 'metre'), indoor=False)",
            Target(ScoringSystem.TEN_ZONE, 122.0, 50.0).toString(),
        )
        assertEquals(
            "Target('5_zone', (16, 'inch'), (20, 'yard'), indoor=True)",
            Target(ScoringSystem.FIVE_ZONE, Quantity(16.0, "inches"), Quantity(20.0, "yd"), true).toString(),
        )
        assertEquals(
            "Target('10_zone', (122.5, 'cm'), (18.3, 'metre'), indoor=False)",
            Target(ScoringSystem.TEN_ZONE, 122.5, 18.3).toString(),
        )
    }

    @Test
    fun equalityAndHash() {
        val a = Target(ScoringSystem.TEN_ZONE, 122.0, 50.0)
        val b = Target(ScoringSystem.TEN_ZONE, 122.0, 50.0)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, Target(ScoringSystem.TEN_ZONE, 122.0, 50.0, indoor = true))
        assertNotEquals(a, Target(ScoringSystem.FIVE_ZONE, 122.0, 50.0))
        assertNotEquals(a, Target(ScoringSystem.TEN_ZONE, Quantity(1.22, "metre"), Quantity.metres(50.0)))
        assertNotEquals<Any>(a, "target")
        assertEquals(1, setOf(a, b).size)
        assertEquals(2, mapOf(a to 1, Target(ScoringSystem.WA_FIELD, 60.0, 30.0) to 2).size)
    }

    @Test
    fun invalidSystem() {
        val e = assertFailsWith<IllegalArgumentException> { ScoringSystem.fromValue("Unknown") }
        assertTrue(e.message!!.startsWith("Invalid Target Face Type specified."))
    }

    @Test
    fun unitsHandling() {
        assertFailsWith<IllegalArgumentException> {
            Target(ScoringSystem.TEN_ZONE, Quantity(122.0, "cm"), Quantity(50.0, "cm"))
        }
        assertFailsWith<IllegalArgumentException> {
            Target(ScoringSystem.TEN_ZONE, Quantity(122.0, "yard"), Quantity(50.0, "m"))
        }
        val yard = Target(ScoringSystem.TEN_ZONE, 122.0, 100.0)
        assertEquals(100.0, yard.distance)
        val t = Target(ScoringSystem.TEN_ZONE, Quantity(1.22, "Metres"), Quantity(50.0, "yds"))
        assertEquals(1.22, t.diameter)
        assertEquals(45.72, t.distance, 1e-9)
        assertEquals(Quantity(1.22, "metre"), t.nativeDiameter)
        assertEquals(Quantity(50.0, "yard"), t.nativeDistance)
        assertEquals(0.4064, Target(ScoringSystem.WORCESTER, Quantity(16.0, "inch"), Quantity.yards(20.0)).diameter, 1e-9)
    }

    @Test
    fun defaultIndoor() {
        assertFalse(Target(ScoringSystem.TEN_ZONE, 122.0, 50.0).indoor)
    }

    private val maxScores = mapOf(
        ScoringSystem.FIVE_ZONE to 9, ScoringSystem.TEN_ZONE to 10, ScoringSystem.TEN_ZONE_COMPOUND to 10,
        ScoringSystem.TEN_ZONE_SIX_RING to 10, ScoringSystem.TEN_ZONE_FIVE_RING to 10,
        ScoringSystem.TEN_ZONE_FIVE_RING_COMPOUND to 10, ScoringSystem.ELEVEN_ZONE to 11,
        ScoringSystem.ELEVEN_ZONE_SIX_RING to 11, ScoringSystem.ELEVEN_ZONE_FIVE_RING to 11,
        ScoringSystem.WA_FIELD to 6, ScoringSystem.IFAA_FIELD to 5, ScoringSystem.IFAA_FIELD_EXPERT to 5,
        ScoringSystem.AA_NATIONAL_FIELD to 5, ScoringSystem.WORCESTER to 5, ScoringSystem.WORCESTER_2_RING to 5,
        ScoringSystem.BEITER_HIT_MISS to 1,
    )

    private val minScores = mapOf(
        ScoringSystem.FIVE_ZONE to 1, ScoringSystem.TEN_ZONE to 1, ScoringSystem.TEN_ZONE_COMPOUND to 1,
        ScoringSystem.TEN_ZONE_SIX_RING to 5, ScoringSystem.TEN_ZONE_FIVE_RING to 6,
        ScoringSystem.TEN_ZONE_FIVE_RING_COMPOUND to 6, ScoringSystem.ELEVEN_ZONE to 1,
        ScoringSystem.ELEVEN_ZONE_SIX_RING to 5, ScoringSystem.ELEVEN_ZONE_FIVE_RING to 6,
        ScoringSystem.WA_FIELD to 1, ScoringSystem.IFAA_FIELD to 3, ScoringSystem.IFAA_FIELD_EXPERT to 1,
        ScoringSystem.AA_NATIONAL_FIELD to 1, ScoringSystem.WORCESTER to 1, ScoringSystem.WORCESTER_2_RING to 4,
        ScoringSystem.BEITER_HIT_MISS to 1,
    )

    @Test
    fun maxAndMinScore() {
        for ((system, expected) in maxScores) {
            assertEquals(expected.toDouble(), Target(system, 122.0, 50.0).maxScore(), system.value)
        }
        for ((system, expected) in minScores) {
            assertEquals(expected.toDouble(), Target(system, 122.0, 50.0).minScore(), system.value)
        }
    }

    @Test
    fun faceSpecs() {
        assertEquals(
            mapOf(0.244 to 9, 0.488 to 7, 0.732 to 5, 0.976 to 3, 1.22 to 1),
            Target(ScoringSystem.FIVE_ZONE, 122.0, 30.0).faceSpec,
        )
        assertEquals(
            mapOf(0.08 to 10, 0.16 to 9, 0.24 to 8, 0.32 to 7, 0.4 to 6, 0.48 to 5, 0.56 to 4, 0.64 to 3, 0.72 to 2, 0.8 to 1),
            Target(ScoringSystem.TEN_ZONE, 80.0, 30.0).faceSpec,
        )
        assertEquals(
            mapOf(0.02 to 11, 0.04 to 10, 0.08 to 9, 0.12 to 8, 0.16 to 7, 0.2 to 6, 0.24 to 5, 0.28 to 4, 0.32 to 3, 0.36 to 2, 0.4 to 1),
            Target(ScoringSystem.ELEVEN_ZONE, 40.0, 30.0).faceSpec,
        )
        assertEquals(
            mapOf(0.02 to 11, 0.04 to 10, 0.08 to 9, 0.12 to 8, 0.16 to 7, 0.2 to 6, 0.24 to 5),
            Target(ScoringSystem.ELEVEN_ZONE_SIX_RING, 40.0, 30.0).faceSpec,
        )
        assertEquals(
            mapOf(0.08 to 6, 0.16 to 5, 0.32 to 4, 0.48 to 3, 0.64 to 2, 0.8 to 1),
            Target(ScoringSystem.WA_FIELD, 80.0, 30.0).faceSpec,
        )
        assertEquals(mapOf(0.1 to 5, 0.3 to 4, 0.5 to 3), Target(ScoringSystem.IFAA_FIELD, 50.0, 30.0).faceSpec)
        assertEquals(
            mapOf(0.04 to 5, 0.08 to 4, 0.12 to 3, 0.16 to 2, 0.2 to 1),
            Target(ScoringSystem.AA_NATIONAL_FIELD, 20.0, 30.0).faceSpec,
        )
        assertEquals(mapOf(0.06 to 1), Target(ScoringSystem.BEITER_HIT_MISS, 6.0, 30.0).faceSpec)
        assertEquals(
            mapOf(0.08128 to 5, 0.16256 to 4, 0.24384 to 3, 0.32512 to 2, 0.4064 to 1),
            Target(ScoringSystem.WORCESTER, Quantity(16.0, "inch"), Quantity.metres(30.0)).faceSpec,
        )
        assertEquals(
            mapOf(0.08 to 10, 0.16 to 9, 0.24 to 8, 0.32 to 7, 0.4 to 6, 0.48 to 5),
            Target(ScoringSystem.TEN_ZONE_SIX_RING, 80.0, 30.0).faceSpec,
        )
        assertEquals(
            mapOf(0.02 to 10, 0.08 to 9, 0.12 to 8, 0.16 to 7, 0.2 to 6),
            Target(ScoringSystem.TEN_ZONE_FIVE_RING_COMPOUND, 40.0, 30.0).faceSpec,
        )
    }

    @Test
    fun faceSpecWrongConstructor() {
        val target = Target(ScoringSystem.CUSTOM, 10.0, 10.0)
        val e = assertFailsWith<IllegalArgumentException> { target.faceSpec }
        assertTrue("fromFaceSpec" in e.message!!)
    }

    @Test
    fun genFaceSpecUnsupported() {
        assertFailsWith<IllegalArgumentException> { Target.genFaceSpec(ScoringSystem.CUSTOM, 1.0) }
    }

    @Test
    fun fromFaceSpec() {
        val specs = mapOf(0.08 to 10, 0.12 to 8, 0.16 to 7, 0.2 to 6)
        val target = Target.fromFaceSpec(specs, Quantity.cm(40.0), Quantity.metres(18.0))
        assertEquals(ScoringSystem.CUSTOM, target.scoringSystem)
        assertEquals(specs, target.faceSpec)
        assertEquals(10.0, target.maxScore())
        assertEquals(6.0, target.minScore())

        val cm = Target.fromFaceSpec(mapOf(8.0 to 10, 12.0 to 8), Quantity.cm(40.0), Quantity.metres(18.0), specUnits = "cm")
        assertEquals(mapOf(0.08 to 10, 0.12 to 8), cm.faceSpec)
        assertFailsWith<IllegalArgumentException> {
            Target.fromFaceSpec(specs, Quantity.cm(40.0), Quantity.metres(18.0), specUnits = "banana")
        }
    }

    @Test
    fun fromFaceSpecEquality() {
        val a = Target.fromFaceSpec(mapOf(0.08 to 10), Quantity.cm(40.0), Quantity.metres(18.0))
        val b = Target.fromFaceSpec(mapOf(0.08 to 10), Quantity.cm(40.0), Quantity.metres(18.0))
        val c = Target.fromFaceSpec(mapOf(0.08 to 9), Quantity.cm(40.0), Quantity.metres(18.0))
        assertEquals(a, b)
        assertNotEquals(a, c)
    }
}
