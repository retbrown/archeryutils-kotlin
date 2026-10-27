package io.github.retbrown.archeryutils.rounds

import io.github.retbrown.archeryutils.targets.Quantity
import io.github.retbrown.archeryutils.targets.ScoringSystem
import io.github.retbrown.archeryutils.targets.Target
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoundTest {
    private val pass70 = Pass.atTarget(36, ScoringSystem.TEN_ZONE, 122.0, 70.0)
    private val pass60 = Pass.atTarget(36, ScoringSystem.TEN_ZONE, 122.0, 60.0)

    @Test
    fun passBasics() {
        assertEquals(36, pass70.nArrows)
        assertEquals(360.0, pass70.maxScore())
        assertEquals(70.0, pass70.distance)
        assertEquals(1.22, pass70.diameter)
        assertEquals(ScoringSystem.TEN_ZONE, pass70.scoringSystem)
        assertEquals(Quantity(70.0, "metre"), pass70.nativeDistance)
        assertEquals(Quantity(122.0, "cm"), pass70.nativeDiameter)
        assertEquals(false, pass70.indoor)
        assertEquals(36, Pass(-36, pass70.target).nArrows)
    }

    @Test
    fun passEquality() {
        assertEquals(pass70, Pass(36, Target(ScoringSystem.TEN_ZONE, 122.0, 70.0)))
        assertEquals(pass70.hashCode(), Pass.atTarget(36, ScoringSystem.TEN_ZONE, 122.0, 70.0).hashCode())
        assertNotEquals(pass70, pass60)
        assertNotEquals(pass70, Pass.atTarget(30, ScoringSystem.TEN_ZONE, 122.0, 70.0))
        assertEquals("Pass(36, ${pass70.target})", pass70.toString())
    }

    @Test
    fun roundBasics() {
        val round = Round("WA 720", listOf(pass70, pass60), "wa720", "outdoor", "WA", "wa720")
        assertEquals(72, round.nArrows)
        assertEquals(720.0, round.maxScore())
        assertEquals(Quantity(70.0, "metre"), round.maxDistance())
        assertEquals("<Round: 'WA 720'>", round.toString())
        assertEquals("wa720", round.codename)
        assertEquals("outdoor", round.location)
        assertNull(Round("x", listOf(pass70)).location)
    }

    @Test
    fun roundRequiresPasses() {
        assertFailsWith<IllegalArgumentException> { Round("empty", emptyList()) }
    }

    @Test
    fun roundEqualityIgnoresMetadata() {
        val a = Round("WA 720", listOf(pass70, pass60), location = "outdoor")
        val b = Round("WA 720", listOf(pass70, pass60), location = "indoor", body = "WA")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, Round("WA 720", listOf(pass60, pass70)))
        assertNotEquals(a, Round("Other", listOf(pass70, pass60)))
    }

    @Test
    fun maxDistanceKeepsNativeUnits() {
        val yards = Pass.atTarget(36, ScoringSystem.FIVE_ZONE, Quantity.cm(122.0), Quantity.yards(100.0))
        val metres = Pass.atTarget(36, ScoringSystem.FIVE_ZONE, Quantity.cm(122.0), Quantity.metres(50.0))
        assertEquals(Quantity(100.0, "yard"), Round("r", listOf(metres, yards)).maxDistance())
    }

    @Test
    fun info() {
        val round = Round("Worcester", listOf(Pass.atTarget(60, ScoringSystem.WORCESTER, Quantity.inches(16.0), Quantity.yards(20.0), true)))
        assertEquals(
            "A Worcester consists of 1 passes:\n\t- 60 arrows at a 16.0 inch target at 20.0 yards.",
            round.info(),
        )
    }

    @Test
    fun loadedDatabases() {
        val dbs = listOf(
            Rounds.agbOutdoorImperial, Rounds.agbOutdoorMetric, Rounds.agbIndoor, Rounds.waOutdoor,
            Rounds.waIndoor, Rounds.waField, Rounds.waExperimental, Rounds.ifaaField, Rounds.waVI,
            Rounds.agbVI, Rounds.aaOutdoorMetric, Rounds.aaIndoor, Rounds.aaField, Rounds.misc,
        )
        for (db in dbs) {
            assertTrue(db.isNotEmpty())
            for ((codename, round) in db) {
                assertEquals(codename, round.codename)
                assertTrue(round.nArrows > 0)
            }
        }
        assertEquals(dbs.sumOf { it.size }, Rounds.all.size)
    }

    @Test
    fun knownRounds() {
        val wa1440 = Rounds.waOutdoor.getValue("wa1440_90")
        assertEquals("WA 1440 (90m)", wa1440.name)
        assertEquals(144, wa1440.nArrows)
        assertEquals(1440.0, wa1440.maxScore())
        assertEquals("outdoor", wa1440.location)

        val wa18 = Rounds.waIndoor.getValue("wa18")
        assertEquals("indoor", wa18.location)
        assertTrue(wa18.indoor())
        assertEquals(600.0, wa18.maxScore())

        val field = Rounds.waField.getValues()
        assertTrue(field.all { it.location == "field" })
    }

    private fun Round.indoor() = passes.all { it.indoor }
    private fun Map<String, Round>.getValues() = values

    @Test
    fun fromJsonDefaultsAndUnits() {
        val json = """[{"name": "Test", "codename": "test", "passes": [
            {"n_arrows": 12, "diameter": 16, "diameter_unit": "inch", "scoring": "Worcester", "distance": 20, "dist_unit": "yard"}]}]"""
        val round = Rounds.fromJson(json).getValue("test")
        assertNull(round.location)
        assertNull(round.body)
        assertNull(round.family)
        assertEquals(Quantity(16.0, "inch"), round.passes[0].nativeDiameter)
        assertEquals(Quantity(20.0, "yard"), round.passes[0].nativeDistance)
    }

    @Test
    fun fromJsonUnknownScoringFails() {
        val json = """[{"name": "T", "codename": "t", "passes": [
            {"n_arrows": 12, "diameter": 16, "scoring": "nope", "distance": 20, "dist_unit": "yard"}]}]"""
        assertFailsWith<IllegalArgumentException> { Rounds.fromJson(json) }
    }
}
