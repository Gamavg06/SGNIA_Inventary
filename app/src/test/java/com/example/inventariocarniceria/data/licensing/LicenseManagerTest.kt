package com.example.inventariocarniceria.data.licensing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LicenseManagerTest {

    @Test
    fun testGenerateAndFormatKeysForAllTiers() {
        val targetHwId = "SGN-8F2A-3C91"

        // 1. 10 Minutos (Demo)
        val key10m = LicenseManager.generateActivationKey(targetHwId, LicenseManager.LicenseTier.DEMO_10M, "A1B2")
        assertTrue("Debe empezar con SGN-10M-", key10m.startsWith("SGN-10M-A1B2-"))
        assertEquals("Debe tener formato SGN-10M-XXXX-XXXX-XXXX (22 caracteres)", 22, key10m.length)

        // 2. 7 Días (Prueba)
        val key7d = LicenseManager.generateActivationKey(targetHwId, LicenseManager.LicenseTier.TRIAL_7D, "B2C3")
        assertTrue("Debe empezar con SGN-07D-", key7d.startsWith("SGN-07D-B2C3-"))
        assertEquals(22, key7d.length)

        // 3. 1 Mes (Renta mensual)
        val key1m = LicenseManager.generateActivationKey(targetHwId, LicenseManager.LicenseTier.RENTAL_1M, "C3D4")
        assertTrue("Debe empezar con SGN-01M-", key1m.startsWith("SGN-01M-C3D4-"))
        assertEquals(22, key1m.length)

        // 4. 1 Año (Renta anual)
        val key1y = LicenseManager.generateActivationKey(targetHwId, LicenseManager.LicenseTier.RENTAL_1Y, "D4E5")
        assertTrue("Debe empezar con SGN-01Y-", key1y.startsWith("SGN-01Y-D4E5-"))
        assertEquals(22, key1y.length)

        // 5. De por vida (Perpetua)
        val keyLife = LicenseManager.generateActivationKey(targetHwId, LicenseManager.LicenseTier.LIFETIME, "E5F6")
        assertTrue("Debe empezar con SGN-LIFE-", keyLife.startsWith("SGN-LIFE-E5F6-"))
        assertEquals(23, keyLife.length)
    }

    @Test
    fun testDeterministicSignatureWithSameInput() {
        val hwId = "SGN-B71A-992F"
        val tier = LicenseManager.LicenseTier.RENTAL_1M
        val token = "99AA"

        val key1 = LicenseManager.generateActivationKey(hwId, tier, token)
        val key2 = LicenseManager.generateActivationKey(hwId, tier, token)

        assertEquals("Mismo HW y token deben producir exactamente la misma firma", key1, key2)
    }

    @Test
    fun testDifferentHardwareProducesDifferentKeys() {
        val hw1 = "SGN-1111-2222"
        val hw2 = "SGN-3333-4444"
        val tier = LicenseManager.LicenseTier.LIFETIME
        val token = "F0F0"

        val key1 = LicenseManager.generateActivationKey(hw1, tier, token)
        val key2 = LicenseManager.generateActivationKey(hw2, tier, token)

        assertTrue("Distintos dispositivos deben tener firmas criptográficas diferentes", key1 != key2)
    }

    @Test
    fun testTierParsingFromCode() {
        assertEquals(LicenseManager.LicenseTier.DEMO_10M, LicenseManager.LicenseTier.fromCode("10M"))
        assertEquals(LicenseManager.LicenseTier.TRIAL_7D, LicenseManager.LicenseTier.fromCode("07D"))
        assertEquals(LicenseManager.LicenseTier.RENTAL_1M, LicenseManager.LicenseTier.fromCode("01M"))
        assertEquals(LicenseManager.LicenseTier.RENTAL_1Y, LicenseManager.LicenseTier.fromCode("01Y"))
        assertEquals(LicenseManager.LicenseTier.LIFETIME, LicenseManager.LicenseTier.fromCode("LIFE"))
    }
}
