package io.github.retbrown.archeryutils.handicaps

import io.github.retbrown.archeryutils.rounds.Round
import kotlin.math.floor
import kotlin.math.truncate

/**
 * Generate and store a handicap table.
 *
 * Column 0 of [table] holds the handicap, and columns 1.. the score for each round in [roundList].
 *
 * @param handicapSys the handicap scheme to use.
 * @param hcs handicap values to calculate scores for.
 * @param roundList the rounds to show in the handicap table.
 * @param roundedScores round scores to integer values.
 * @param intPrec display numbers in the table as integers rather than decimals. Rounded scores
 * force this to true.
 * @param cleanGaps clean out gaps of repeated scores (using only the first occurrence). Gaps are
 * filled with [FILL] (when [intPrec]) or NaN.
 * @param arrowD user-specified arrow diameter in metres.
 * @throws IllegalArgumentException if no rounds are provided.
 */
public class HandicapTable(
    handicapSys: HandicapScheme,
    hcs: DoubleArray,
    public val roundList: List<Round>,
    public val roundedScores: Boolean = true,
    intPrec: Boolean = true,
    public val cleanGaps: Boolean = true,
    arrowD: Double? = null,
) {
    /** Create a table using a named handicap scheme: "AGB", "AGBold", "AA" or "AA2". */
    public constructor(
        handicapSys: String,
        hcs: DoubleArray,
        roundList: List<Round>,
        roundedScores: Boolean = true,
        intPrec: Boolean = true,
        cleanGaps: Boolean = true,
        arrowD: Double? = null,
    ) : this(Handicaps.handicapScheme(handicapSys), hcs, roundList, roundedScores, intPrec, cleanGaps, arrowD)

    /** The handicap scheme used to construct this table. */
    public val hcSys: HandicapScheme = handicapSys

    /** Whether table numbers are displayed as integers. */
    public val intPrec: Boolean = intPrec || roundedScores

    /** The handicap values in the table (after any gap-cleaning padding rows are removed). */
    public var hcs: DoubleArray
        private set

    /** The generated table: one row per handicap, `[handicap, score for round 1, ...]`. */
    public var table: Array<DoubleArray>
        private set

    init {
        require(roundList.isNotEmpty()) { "No rounds provided for handicap table." }

        // if cleaning gaps add a row to top/bottom of table to catch out of range repeats
        var h = hcs
        if (cleanGaps) {
            val deltaS = if (h.size > 1) h[1] - h[0] else 1.0
            val deltaE = if (h.size > 1) h[h.size - 1] - h[h.size - 2] else 1.0
            h = doubleArrayOf(h[0] - deltaS) + h + doubleArrayOf(h[h.size - 1] + deltaE)
        }

        var t =
            Array(h.size) { row ->
                DoubleArray(roundList.size + 1) { col ->
                    if (col == 0) {
                        h[row]
                    } else {
                        val s = handicapSys.scoreForRound(h[row], roundList[col - 1], arrowD, roundedScores)
                        if (this.intPrec) truncate(s) else s
                    }
                }
            }

        if (cleanGaps) {
            t = cleanRepeated(t)
            h = h.copyOfRange(1, h.size - 1)
            t = t.copyOfRange(1, t.size - 1)
        }
        this.hcs = h
        this.table = t
    }

    /** Keep only the first instance of a score in the handicap table. */
    private fun cleanRepeated(input: Array<DoubleArray>): Array<DoubleArray> {
        // NB: This assumes scores are running highest to lowest, so flip tables
        // with an ascending scale (AA and AA2) before operating.
        val flip = !hcSys.descScale
        val t = if (flip) input.reversedArray() else input.copyOf()
        for (irow in 0 until t.size - 1) {
            for (jscore in t[irow].indices) {
                if (t[irow][jscore] == t[irow + 1][jscore]) {
                    t[irow] = t[irow].copyOf().also { it[jscore] = if (intPrec) FILL else Double.NaN }
                }
            }
        }
        return if (flip) t.reversedArray() else t
    }

    /** A short representation, e.g. `<HandicapTable: 'AGB'>`. */
    override fun toString(): String = "<HandicapTable: '${hcSys.name}'>"

    /** The handicap table formatted as a string for printing/saving. */
    public fun format(): String {
        val roundNames = roundList.map { abbreviate(it.name) }
        val header = (listOf("Handicap") + roundNames).joinToString("") { it.padStart(COLUMN_WIDTH) }
        // Auto-set the number of decimal places to display handicaps to
        val hcDp =
            if (hcs.all { it - floor(it) <= 0.0 }) 0 else hcs.maxOf { decimalPlaces(it) }
        val rows = table.map { formatRow(it, hcDp, intPrec) }
        return (listOf(header) + rows).joinToString("\n")
    }

    /** Print the handicap table to stdout. */
    public fun print(): Unit = println(format())

    /** The handicap table as comma separated values, with a header line. */
    public fun toCsv(): String {
        val header = "# handicap, ${roundList.joinToString(",") { it.name }}'"
        return (listOf(header) + table.map { row -> row.joinToString(", ") { it.toString() } }).joinToString("\n")
    }

    /** Constants and helpers for [HandicapTable]. */
    public companion object {
        /** Marker for a cleaned-out (repeated) score in an integer precision table. */
        public const val FILL: Double = -9999.0

        private const val COLUMN_WIDTH = 14

        private val abbreviations =
            mapOf(
                "Compound" to "C", "Recurve" to "R", "Triple" to "Tr", "Centre" to "C",
                "Portsmouth" to "Ports", "Worcester" to "Worc", "Short" to "St", "Long" to "Lg",
                "Small" to "Sm", "Gents" to "G", "Ladies" to "L",
            )

        /** Generate headings using abbreviations to keep table output concise. */
        internal fun abbreviate(name: String): String =
            name.split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ") { abbreviations[it] ?: it }

        /** Format the appearance of a handicap table row. */
        internal fun formatRow(row: DoubleArray, hcDp: Int = 0, intPrec: Boolean = false): String {
            val handicapStr =
                if (hcDp == 0) {
                    row[0].toLong().toString().padStart(COLUMN_WIDTH)
                } else {
                    formatFixed(row[0], hcDp).padStart(COLUMN_WIDTH)
                }
            val sb = StringBuilder(handicapStr)
            for (i in 1 until row.size) {
                val x = row[i]
                if (intPrec) {
                    sb.append(if (x == FILL) "".padStart(COLUMN_WIDTH) else x.toLong().toString().padStart(COLUMN_WIDTH))
                } else {
                    sb.append(if (x.isNaN()) "".padStart(COLUMN_WIDTH) else formatFixed(x, 8).padStart(COLUMN_WIDTH))
                }
            }
            return sb.toString()
        }
    }
}
