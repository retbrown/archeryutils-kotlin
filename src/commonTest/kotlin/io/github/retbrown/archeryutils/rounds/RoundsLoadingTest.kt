package io.github.retbrown.archeryutils.rounds

import io.github.retbrown.archeryutils.targets.Quantity
import io.github.retbrown.archeryutils.targets.ScoringSystem
import io.github.retbrown.archeryutils.targets.Target
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Counterparts of the Python `load_rounds` behaviour and the Pass/Round container tests. */
class RoundsLoadingTest {
    private fun roundJson(location: String?, extra: String = ""): String {
        val loc = if (location == null) "" else """"location": "$location","""
        return """[{"name": "T", "codename": "t", $loc $extra
            "passes": [{"n_arrows": 12, "diameter": 40, "scoring": "10_zone", "distance": 18, "dist_unit": "metre"}]}]"""
    }

    @Test
    fun locationAliasesAreNormalised() {
        for (alias in listOf("i", "I", "indoors", "indoor", "in", "inside", "Indoors", "Indoor", "In", "Inside")) {
            val round = Rounds.fromJson(roundJson(alias)).getValue("t")
            assertEquals("indoor", round.location, alias)
            assertTrue(round.passes.all { it.indoor }, alias)
        }
        for (alias in listOf("o", "O", "outdoors", "outdoor", "out", "outside", "Outdoors", "Outdoor", "Out", "Outside")) {
            val round = Rounds.fromJson(roundJson(alias)).getValue("t")
            assertEquals("outdoor", round.location, alias)
            assertFalse(round.passes.any { it.indoor }, alias)
        }
        for (alias in listOf("f", "F", "field", "Field", "woods", "Woods")) {
            val round = Rounds.fromJson(roundJson(alias)).getValue("t")
            assertEquals("field", round.location, alias)
            assertFalse(round.passes.any { it.indoor }, alias)
        }
    }

    @Test
    fun unknownOrMissingLocationIsNull() {
        assertNull(Rounds.fromJson(roundJson("moon")).getValue("t").location)
        assertNull(Rounds.fromJson(roundJson(null)).getValue("t").location)
        assertFalse(Rounds.fromJson(roundJson("moon")).getValue("t").passes[0].indoor)
    }

    @Test
    fun bodyAndFamilyAreLoaded() {
        val round = Rounds.fromJson(roundJson("indoor", """"body": "WA", "family": "wa18","""), ).getValue("t")
        assertEquals("WA", round.body)
        assertEquals("wa18", round.family)
    }

    @Test
    fun multipleRoundsKeepOrderAndDuplicateCodenamesOverwrite() {
        val json = """[
            {"name": "A", "codename": "a", "passes": [{"n_arrows": 1, "diameter": 40, "scoring": "10_zone", "distance": 18, "dist_unit": "m"}]},
            {"name": "B", "codename": "b", "passes": [{"n_arrows": 1, "diameter": 40, "scoring": "10_zone", "distance": 18, "dist_unit": "m"}]},
            {"name": "C", "codename": "a", "passes": [{"n_arrows": 2, "diameter": 40, "scoring": "10_zone", "distance": 18, "dist_unit": "m"}]}]"""
        val rounds = Rounds.fromJson(json)
        assertEquals(listOf("a", "b"), rounds.keys.toList())
        assertEquals("C", rounds.getValue("a").name)
    }

    private val target = Target(ScoringSystem.TEN_ZONE, 122.0, 70.0)

    @Test
    fun passAndRoundHashing() {
        val p1 = Pass(36, target)
        val p2 = Pass(36, Target(ScoringSystem.TEN_ZONE, 122.0, 70.0))
        val p3 = Pass(36, Target(ScoringSystem.TEN_ZONE, 122.0, 60.0))
        assertEquals(p1.hashCode(), p2.hashCode())
        assertEquals(1, setOf(p1, p2).size)
        assertEquals(2, setOf(p1, p3).size)
        assertEquals(2, mapOf(p1 to "a", p3 to "b").size)

        val r1 = Round("R", listOf(p1, p3))
        val r2 = Round("R", listOf(p2, p3))
        assertEquals(r1.hashCode(), r2.hashCode())
        assertEquals(1, setOf(r1, r2).size)
        assertEquals(2, setOf(r1, Round("R", listOf(p3, p1))).size)
    }

    @Test
    fun passWithCustomTarget() {
        val custom = Target.fromFaceSpec(mapOf(0.1 to 10, 0.2 to 5), Quantity.cm(40.0), Quantity.metres(18.0))
        val pass = Pass(30, custom)
        assertEquals(300.0, pass.maxScore())
        assertEquals(ScoringSystem.CUSTOM, pass.scoringSystem)
    }

    @Test
    fun roundIterableIsCopied() {
        val source = mutableListOf(Pass(36, target))
        val round = Round("R", source)
        source.add(Pass(36, target))
        assertEquals(1, round.passes.size)
        assertEquals(36, round.nArrows)
    }

    @Test
    fun roundMaxDistanceOutOfOrder() {
        val near = Pass.atTarget(36, ScoringSystem.TEN_ZONE, 122.0, 30.0)
        val far = Pass.atTarget(36, ScoringSystem.TEN_ZONE, 122.0, 90.0)
        assertEquals(Quantity(90.0, "metre"), Round("R", listOf(near, far, near)).maxDistance())
    }
}
