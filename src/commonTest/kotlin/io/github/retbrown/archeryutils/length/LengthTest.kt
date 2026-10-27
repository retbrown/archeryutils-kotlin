package io.github.retbrown.archeryutils.length

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LengthTest {
    private fun assertClose(expected: Double, actual: Double, tol: Double = 1e-4) =
        assertTrue(kotlin.math.abs(expected - actual) <= tol * maxOf(1.0, kotlin.math.abs(expected)), "$expected != $actual")

    @Test
    fun unitsAvailable() {
        assertEquals(setOf("cm", "inch", "metre", "yard"), Length.knownUnits)
    }

    @Test
    fun aliasesAvailable() {
        assertTrue("cm" in Length.cm)
        assertTrue("inch" in Length.inch)
        assertTrue("metre" in Length.metre)
        assertTrue("yard" in Length.yard)
        assertTrue("cms" in Length.cm)
        assertTrue("inches" in Length.inch)
        assertTrue("metres" in Length.metre)
        assertTrue("yards" in Length.yard)
    }

    @Test
    fun conversionToMetres() {
        assertClose(10.0, Length.toMetres(10.0, "metre"))
        assertClose(0.1, Length.toMetres(10.0, "cm"))
        assertClose(0.254, Length.toMetres(10.0, "inch"))
        assertClose(9.144, Length.toMetres(10.0, "yard"))
    }

    @Test
    fun conversionFromMetres() {
        assertClose(10.0, Length.fromMetres(10.0, "metre"))
        assertClose(1000.0, Length.fromMetres(10.0, "cm"))
        assertClose(393.7008, Length.fromMetres(10.0, "inch"))
        assertClose(10.93613, Length.fromMetres(10.0, "yard"))
    }

    @Test
    fun unknownUnitFails() {
        assertFailsWith<IllegalArgumentException> { Length.toMetres(1.0, "banana") }
        assertFailsWith<IllegalArgumentException> { Length.fromMetres(1.0, "banana") }
        assertFailsWith<IllegalArgumentException> { Length.definitiveUnit("banana") }
    }

    @Test
    fun unitNameCoercion() {
        assertEquals("metre", Length.definitiveUnit("m"))
        assertEquals("cm", Length.definitiveUnit("centimetre"))
        assertEquals("inch", Length.definitiveUnit("Inch"))
        assertEquals("yard", Length.definitiveUnit("yd"))
    }

    @Test
    fun aliasReduction() {
        assertEquals(setOf("inch", "cm"), Length.definitiveUnits(Length.inch + Length.cm))
    }

    private val supported = Length.metre + Length.yard

    @Test
    fun optionalUnitParsing() {
        assertEquals(10.0 to "metre", Length.parseOptionalUnits(10.0, null, supported, "metre"))
        assertEquals(10.1 to "metre", Length.parseOptionalUnits(10.1, null, supported, "metre"))
        assertEquals(10.0 to "metre", Length.parseOptionalUnits(10.0, "Metres", supported, "metre"))
        assertEquals(10.0 to "yard", Length.parseOptionalUnits(10.0, "yds", supported, "metre"))
    }

    @Test
    fun optionalUnitParsingUnitNotSupported() {
        val e = assertFailsWith<IllegalArgumentException> {
            Length.parseOptionalUnits(10.0, "banana", supported, "metre")
        }
        assertTrue(e.message!!.startsWith("Unit 'banana' not recognised. Select from"))
    }

    @Test
    fun optionalUnitParsingDefaultNotSupported() {
        val e = assertFailsWith<IllegalArgumentException> {
            Length.parseOptionalUnits(10.0, null, supported, "cm")
        }
        assertTrue("must be in supported units" in e.message!!)
    }
}
