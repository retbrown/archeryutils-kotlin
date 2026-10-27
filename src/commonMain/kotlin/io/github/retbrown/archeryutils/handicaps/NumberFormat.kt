package io.github.retbrown.archeryutils.handicaps

/**
 * Minimal exact number formatting, matching Python's `f"{x:.{dp}f}"` behaviour, since common
 * Kotlin has no `String.format`.
 */
internal fun formatFixed(x: Double, dp: Int): String {
    if (x.isNaN()) return "nan"
    if (x.isInfinite()) return if (x > 0) "inf" else "-inf"
    val bits = x.toRawBits()
    val negative = bits < 0
    val expField = ((bits ushr 52) and 0x7FF).toInt()
    var mant = bits and 0xFFFFFFFFFFFFFL
    val ex: Int
    if (expField == 0) {
        ex = -1074
    } else {
        mant = mant or (1L shl 52)
        ex = expField - 1075
    }

    var intPart: Long
    val digits = IntArray(dp)
    var roundUp = false
    if (ex >= 0) {
        if (ex > 10) return x.toString() // beyond Long range; not expected for handicap data
        intPart = mant shl ex
    } else {
        val k = -ex
        intPart = if (k >= 64) 0L else mant ushr k
        // fractional numerator f over 2^k, as 32-bit limbs (little endian)
        val limbs = LongArray(k / 32 + 3)
        val fracMant = if (k >= 64) mant else mant and ((1L shl k) - 1)
        limbs[0] = fracMant and 0xFFFFFFFFL
        limbs[1] = fracMant ushr 32
        val q = k / 32
        val r = k % 32
        for (i in 0 until dp) {
            var carry = 0L
            for (j in limbs.indices) {
                val v = limbs[j] * 10 + carry
                limbs[j] = v and 0xFFFFFFFFL
                carry = v ushr 32
            }
            val combined = limbs[q] or (limbs[q + 1] shl 32)
            digits[i] = (combined ushr r).toInt()
            limbs[q] = limbs[q] and ((1L shl r) - 1)
            for (j in q + 1 until limbs.size) limbs[j] = 0
        }
        // Round half to even on the exact remainder.
        val halfBit = k - 1
        val hq = halfBit / 32
        val hr = halfBit % 32
        val halfSet = (limbs[hq] ushr hr) and 1L == 1L
        if (halfSet) {
            var lower = (limbs[hq] and ((1L shl hr) - 1)) != 0L
            for (j in 0 until hq) if (limbs[j] != 0L) lower = true
            val lastDigit = if (dp > 0) digits[dp - 1] else (intPart % 10).toInt()
            roundUp = lower || lastDigit % 2 == 1
        }
    }
    if (roundUp) {
        var i = dp - 1
        while (i >= 0) {
            if (digits[i] == 9) {
                digits[i] = 0
                i--
            } else {
                digits[i]++
                break
            }
        }
        if (i < 0) intPart++
    }
    val sb = StringBuilder()
    if (negative) sb.append('-')
    sb.append(intPart)
    if (dp > 0) {
        sb.append('.')
        for (d in digits) sb.append(d)
    }
    return sb.toString()
}

/**
 * Number of decimal places in the shortest round-trip representation of [d], like
 * `abs(Decimal(str(d)).as_tuple().exponent)` in Python (so `1.0` gives 1).
 */
internal fun decimalPlaces(d: Double): Int {
    val s = d.toString()
    val ePos = s.indexOf('E')
    val mantissa = if (ePos >= 0) s.substring(0, ePos) else s
    val exp10 = if (ePos >= 0) s.substring(ePos + 1).toInt() else 0
    val dot = mantissa.indexOf('.')
    var frac = if (dot >= 0) mantissa.length - dot - 1 else 0
    // Kotlin writes e.g. 1.0E-5; Python writes 1e-05 (no trailing ".0").
    if (ePos >= 0 && mantissa.endsWith(".0")) frac = 0
    val exponent = -(frac - exp10)
    return if (exponent < 0) -exponent else exponent
}
