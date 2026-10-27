package io.github.retbrown.archeryutils.handicaps

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Extra tests of the by-name convenience wrappers, scheme parameters and table options. */
class HandicapCoverageTest {
    @Test
    fun byNameWrappersMatchSchemeMethods() {
        for (name in Handicaps.schemeNames) {
            val scheme = Handicaps.handicapScheme(name)
            assertContentEquals(
                scheme.scoreForPasses(20.0, york, null, true),
                Handicaps.scoreForPasses(20.0, york, name),
                name,
            )
            assertEquals(scheme.scoreForRound(20.0, york, null, true), Handicaps.scoreForRound(20.0, york, name), name)
            assertEquals(
                scheme.scoreForRound(20.0, york, 0.0075, false),
                Handicaps.scoreForRound(20.0, york, name, arwD = 0.0075, roundedScore = false),
                name,
            )
            val score = Handicaps.scoreForRound(20.0, york, name)
            assertEquals(
                scheme.handicapFromScore(score, york, null, true),
                Handicaps.handicapFromScore(score, york, name, intPrec = true),
                name,
            )
        }
    }

    @Test
    fun schemeParamsExposed() {
        assertEquals(setOf("datum", "step", "ang_0", "k1", "k2", "k3", "p1"), Handicaps.handicapScheme("AGBold").params.keys)
        assertEquals(setOf("datum", "step", "ang_0", "kd"), Handicaps.handicapScheme("AGB").params.keys)
        assertEquals(setOf("ang_0", "k0", "ks", "kd"), Handicaps.handicapScheme("AA").params.keys)
        assertEquals(setOf("ang_0", "k0", "ks", "f1", "f2", "d0"), Handicaps.handicapScheme("AA2").params.keys)
    }

    @Test
    fun tablePrintAndCsv() {
        val table = HandicapTable("AGB", doubleArrayOf(1.0, 2.0, 3.0), listOf(york, hereford))
        table.print()
        val csv = table.toCsv().lines()
        assertEquals(4, csv.size)
        assertContains(csv[0], "York")
        assertTrue(csv[1].startsWith("1.0, "))
    }

    @Test
    fun tableWithoutCleanGapsKeepsHandicaps() {
        val hcs = doubleArrayOf(10.0, 11.0, 12.0)
        val table = HandicapTable("AGB", hcs, listOf(york), cleanGaps = false)
        assertContentEquals(hcs, table.hcs)
        assertEquals(3, table.table.size)
    }

    @Test
    fun tableSingleHandicapAndCleanGaps() {
        val table = HandicapTable("AGB", doubleArrayOf(20.0), listOf(york))
        assertContentEquals(doubleArrayOf(20.0), table.hcs)
        assertEquals(1, table.table.size)
        assertEquals(20.0, table.table[0][0])
    }

    @Test
    fun cleanGapsBlanksRepeatedScores() {
        // At very high handicaps a short round gives the same score for adjacent handicaps.
        val hcs = DoubleArray(10) { 140.0 + it }
        val round = vegas300
        val clean = HandicapTable("AGB", hcs, listOf(round), cleanGaps = true)
        val dirty = HandicapTable("AGB", hcs, listOf(round), cleanGaps = false)
        assertEquals(dirty.table.size, clean.table.size)
        val repeats = (0 until dirty.table.size - 1).count { dirty.table[it][1] == dirty.table[it + 1][1] }
        val blanks = clean.table.count { it[1] == HandicapTable.FILL }
        assertTrue(repeats > 0, "test needs adjacent equal scores")
        assertTrue(blanks > 0, "repeated scores should be blanked")
        assertTrue(clean.format().lines().any { it.endsWith("             ") })
    }

    @Test
    fun tableDecimalHandicapsAndFloatScores() {
        val table =
            HandicapTable(
                "AGB",
                doubleArrayOf(1.5, 2.0, 2.5),
                listOf(york),
                roundedScores = false,
                intPrec = false,
            )
        val lines = table.format().lines()
        assertEquals(4, lines.size)
        assertTrue(lines[1].trimStart().startsWith("1.5"))
        assertContentEquals(doubleArrayOf(1.5, 2.0, 2.5), table.hcs)
    }

    @Test
    fun tableFloatScoresNonRoundedBlankNaN() {
        val row = HandicapTable.formatRow(doubleArrayOf(1.0, Double.NaN), 0, false)
        assertEquals(1, row.trim().length)
    }

    @Test
    fun formatFixedSpecialValues() {
        assertEquals("nan", formatFixed(Double.NaN, 2))
        assertEquals("inf", formatFixed(Double.POSITIVE_INFINITY, 2))
        assertEquals("-inf", formatFixed(Double.NEGATIVE_INFINITY, 2))
        assertEquals("0.50", formatFixed(0.5, 2))
        assertEquals("-1.25", formatFixed(-1.25, 2))
        assertEquals("2.00", formatFixed(2.0, 2))
        assertEquals(1e30.toString(), formatFixed(1e30, 2))
    }

    @Test
    fun decimalPlacesScientificNotation() {
        assertEquals(5, decimalPlaces(1e-5))
        assertEquals(6, decimalPlaces(1.5e-5))
        assertEquals(1, decimalPlaces(2.5))
        assertEquals(1, decimalPlaces(3.0))
    }
}
