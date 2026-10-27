package io.github.retbrown.archeryutils.handicaps

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HandicapTableTest {
    private val header = "      Handicap          York      Hereford\n"

    @Test
    fun repr() {
        val table = HandicapTable("AGB", doubleArrayOf(1.0, 2.0, 3.0), listOf(york, hereford))
        assertEquals("<HandicapTable: 'AGB'>", table.toString())
    }

    @Test
    fun tableAsStringIntPrec() {
        val expected =
            header +
                "             1          1281              \n" +
                "             2          1279          1294\n" +
                "             3          1276          1293"
        val rounded = HandicapTable("AGB", doubleArrayOf(1.0, 2.0, 3.0), listOf(york, hereford))
        assertEquals(expected, rounded.format())
        // int_prec=false with rounded scores forces int_prec=true
        val forced =
            HandicapTable("AGB", doubleArrayOf(1.0, 2.0, 3.0), listOf(york, hereford), intPrec = false, roundedScores = true)
        assertEquals(expected, forced.format())
    }

    @Test
    fun tableAsStringFloats() {
        val table =
            HandicapTable(
                "AGB",
                doubleArrayOf(1.0, 2.0, 3.0),
                listOf(york, hereford),
                roundedScores = false,
                intPrec = false,
            )
        assertEquals(
            header +
                "             1 1280.95163666 1293.85033513\n" +
                "             2 1278.16257099 1293.14707366\n" +
                "             3 1275.05905507 1292.28105576",
            table.format(),
        )
    }

    @Test
    fun handicapDecimalPlaces() {
        val table = HandicapTable("AGB", doubleArrayOf(1.20, 2.0, 3.0), listOf(york, hereford))
        assertEquals(
            header +
                "           1.2          1281              \n" +
                "           2.0          1279          1294\n" +
                "           3.0          1276          1293",
            table.format(),
        )
    }

    @Test
    fun abbreviate() {
        assertEquals("C", HandicapTable.abbreviate("Compound"))
        assertEquals("R Tr Ports", HandicapTable.abbreviate("Recurve Triple Portsmouth"))
        assertEquals("St G Worc", HandicapTable.abbreviate("Short Gents Worcester"))
    }

    @Test
    fun tableAA() {
        val table = HandicapTable("AA", doubleArrayOf(1.0, 2.0, 3.0), listOf(york, hereford))
        assertEquals(
            header +
                "             1            79           136\n" +
                "             2            83           142\n" +
                "             3            87           149",
            table.format(),
        )
    }

    @Test
    fun noRounds() {
        val e = assertFailsWith<IllegalArgumentException> { HandicapTable("AGB", doubleArrayOf(1.0), emptyList()) }
        assertEquals("No rounds provided for handicap table.", e.message)
    }

    @Test
    fun formatFixedMatchesPython() {
        assertEquals("0.10000000000000001", formatFixed(0.1, 17))
        assertEquals("0.30000000000000004", formatFixed(0.1 + 0.2, 17))
        assertEquals("0.60000000000000009", formatFixed(0.6000000000000001, 17))
        assertEquals("1280.95163666", formatFixed(1280.951636660001, 8))
        assertEquals("2", formatFixed(2.5, 0)) // half to even
        assertEquals("4", formatFixed(3.5, 0))
        assertEquals("-0.50", formatFixed(-0.5, 2))
        assertEquals("1.00", formatFixed(0.999, 2))
        assertEquals("0.00000100", formatFixed(1e-6, 8))
    }

    @Test
    fun decimalPlacesMatchesPython() {
        assertEquals(1, decimalPlaces(1.0))
        assertEquals(1, decimalPlaces(1.2))
        assertEquals(17, decimalPlaces(0.30000000000000004))
        assertEquals(5, decimalPlaces(1.0e-5))
    }
}
