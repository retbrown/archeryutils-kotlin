package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.rounds.Rounds
import kotlin.test.Test
import kotlin.test.assertEquals

/** Validation of unsupported bowstyle / age group combinations for each classification scheme. */
class ClassificationValidationTest {
    private val outdoor = Rounds.waOutdoor.getValue("wa1440_90")
    private val indoor = Rounds.waIndoor.getValue("wa18")
    private val field = Rounds.waField.getValue("wa_field_24_blue_marked")
    private val oldIndoor = Rounds.agbIndoor.getValue("portsmouth")

    @Test
    fun outdoorRejectsUnsupportedBowstyle() {
        assertFailsContaining("not a recognised bowstyle for outdoor") {
            agbOutdoorClassificationScores(outdoor, AgbBowstyle.TRADITIONAL, AgbGender.OPEN, AgbAge.ADULT)
        }
    }

    @Test
    fun indoorRejectsUnsupportedBowstyleAndAge() {
        assertFailsContaining("not a recognised bowstyle for indoor") {
            agbIndoorClassificationScores(indoor, AgbBowstyle.TRADITIONAL, AgbGender.OPEN, AgbAge.ADULT)
        }
    }

    @Test
    fun fieldRejectsUnsupportedAge() {
        assertFailsContaining("not a recognised age group for field") {
            agbFieldClassificationScores(field, AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.UNDER_21)
        }
    }

    @Test
    fun oldFieldRejectsUnsupportedAge() {
        assertFailsContaining("not a recognised age group for old field") {
            agbOldFieldClassificationScores(field, AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.UNDER_16)
        }
    }

    @Test
    fun oldIndoorRejectsUnsupportedBowstyleAndAge() {
        assertFailsContaining("not a recognised bowstyle for old indoor") {
            agbOldIndoorClassificationScores(oldIndoor, AgbBowstyle.BAREBOW, AgbGender.OPEN, AgbAge.ADULT)
        }
        assertFailsContaining("not a recognised age group for old indoor") {
            agbOldIndoorClassificationScores(oldIndoor, AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.UNDER_14)
        }
    }

    @Test
    fun coaxingMapsUnsupportedCategoriesToSupportedOnes() {
        assertEquals(AgbBowstyle.COMPOUND, coaxOldIndoorGroup(AgbBowstyle.COMPOUNDBAREBOW, AgbGender.OPEN, AgbAge.ADULT).bowstyle)
        assertEquals(AgbBowstyle.RECURVE, coaxOldIndoorGroup(AgbBowstyle.LONGBOW, AgbGender.OPEN, AgbAge.ADULT).bowstyle)
        assertEquals(AgbBowstyle.RECURVE, coaxIndoorGroup(AgbBowstyle.RECURVE, AgbGender.FEMALE, AgbAge.ADULT).bowstyle)
        assertEquals(AgbAge.ADULT, coaxOldFieldGroup(AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.UNDER_21).ageGroup)
        assertEquals(AgbAge.ADULT, coaxFieldGroup(AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.UNDER_21).ageGroup)
        assertEquals(AgbAge.UNDER_21, coaxOutdoorGroup(AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.UNDER_21).ageGroup)
    }
}
