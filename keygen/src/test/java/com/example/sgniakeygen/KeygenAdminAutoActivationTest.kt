package com.example.sgniakeygen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class KeygenAdminAutoActivationTest {

    @Test
    fun testGenerateKeyForHardwareId_ValidFormats() {
        val hwId1 = "SGN-8F2A-3C91"
        val keyRental1M = generateKeyForHardwareId(hwId1, KeygenTier.RENTAL_1M)
        assertNotNull("La clave no debe ser nula para hardware ID válido", keyRental1M)
        assertTrue("Debe empezar con SGN-01M-", keyRental1M!!.startsWith("SGN-01M-"))
        assertEquals("Formato SGN-01M-XXXX-XXXX-XXXX tiene 22 caracteres", 22, keyRental1M.length)

        val keyLifetime = generateKeyForHardwareId(hwId1, KeygenTier.LIFETIME)
        assertNotNull(keyLifetime)
        assertTrue("Debe empezar con SGN-LIFE-", keyLifetime!!.startsWith("SGN-LIFE-"))
        assertEquals("Formato SGN-LIFE-XXXX-XXXX-XXXX tiene 23 caracteres", 23, keyLifetime.length)
    }

    @Test
    fun testGenerateKeyForHardwareId_Sanitization() {
        // Formatos con minúsculas, espacios y sin prefijo SGN
        val hwRaw1 = "sgn 8f2a 3c91"
        val hwRaw2 = "8f2a3c91"
        val hwRaw3 = "SGN-8F2A-3C91"

        val key1 = generateKeyForHardwareId(hwRaw1, KeygenTier.RENTAL_1M)
        val key2 = generateKeyForHardwareId(hwRaw2, KeygenTier.RENTAL_1M)
        val key3 = generateKeyForHardwareId(hwRaw3, KeygenTier.RENTAL_1M)

        assertNotNull(key1)
        assertNotNull(key2)
        assertNotNull(key3)
        // Todos deben tener tokens y estructura de clave válidas
        assertTrue(key1!!.startsWith("SGN-01M-"))
        assertTrue(key2!!.startsWith("SGN-01M-"))
        assertTrue(key3!!.startsWith("SGN-01M-"))
    }

    @Test
    fun testGenerateKeyForHardwareId_InvalidHardwareIdReturnsNull() {
        // Demasiado corto (< 8 caracteres alfanuméricos tras remover SGN y guiones)
        assertNull(generateKeyForHardwareId("SGN-123", KeygenTier.RENTAL_1M))
        assertNull(generateKeyForHardwareId("", KeygenTier.RENTAL_1M))
        assertNull(generateKeyForHardwareId("   ", KeygenTier.RENTAL_1M))
        assertNull(generateKeyForHardwareId("SGN-ABC", KeygenTier.LIFETIME))
    }

    @Test
    fun testHmacIntegrityAcrossEcosystem() {
        val cleanHw = "8F2A3C91"
        val tierCode = "01M"
        val token = "A1B2"
        val payload = "$cleanHw:$tierCode:$token"

        val expectedHmacSeed = "SGNIA_ENTERPRISE_KEY_2026_SECURITY_SALT_GAMAVEL_MEAT_SCALE"
        val hmac1 = calculateHmacSha256(payload, expectedHmacSeed).take(8).uppercase(Locale.US)
        val hmac2 = calculateHmacSha256(payload, expectedHmacSeed).take(8).uppercase(Locale.US)

        assertEquals("HMAC debe ser determinista para idéntica entrada y semilla", hmac1, hmac2)
        assertEquals("Debe tener 8 caracteres hexadecimales", 8, hmac1.length)

        // Dos hardwares distintos jamás deben compartir firma
        val diffPayload = "99BBCCDD:$tierCode:$token"
        val diffHmac = calculateHmacSha256(diffPayload, expectedHmacSeed).take(8).uppercase(Locale.US)
        assertFalse("Hardwares distintos producen HMACs totalmente distintos", hmac1 == diffHmac)
    }

    @Test
    fun testActivateAndGenerateAllKeysForOwner_MultiBranchSimultaneousGeneration() {
        val branches = listOf(
            BranchItem(
                id = "b1",
                name = "Sucursal Matriz Centro",
                hardwareId = "SGN-1111-AAAA",
                generatedKey = "",
                status = "PENDIENTE"
            ),
            BranchItem(
                id = "b2",
                name = "Sucursal Norte Mercado",
                hardwareId = "SGN-2222-BBBB",
                generatedKey = "",
                status = "PENDIENTE"
            ),
            BranchItem(
                id = "b3",
                name = "Sucursal Sur Express",
                hardwareId = "SGN-3333-CCCC",
                generatedKey = "",
                status = "PENDIENTE"
            )
        )

        val owner = OwnerRecord(
            id = "owner-gamaliel-001",
            ownerName = "Gamaliel Velázquez",
            companyName = "Carnicerías La Bendición",
            phoneWhatsApp = "2761095688",
            email = "contacto@labendicion.com",
            membershipTier = KeygenTier.RENTAL_1Y,
            contractStatus = "ACTIVO",
            ownerHardwareId = "SGN-9999-ZZZZ",
            ownerLicenseKey = "",
            ownerStatus = "PENDIENTE",
            branches = branches,
            pricePerBranch = 6000.0,
            totalAmount = 18000.0,
            paymentStatus = "NO_PAGADO"
        )

        // Simular el cambio a PAGADO automático
        val activated = activateAndGenerateAllKeysForOwner(owner)

        // Verificaciones del Dueño
        assertEquals("PAGADO", activated.paymentStatus)
        assertEquals("ACTIVO", activated.contractStatus)
        assertTrue("La clave del dueño debe generarse", activated.ownerLicenseKey.isNotBlank())
        assertTrue("La clave del dueño debe iniciar con SGN-01Y-", activated.ownerLicenseKey.startsWith("SGN-01Y-"))
        assertEquals("ACTIVADA (01Y)", activated.ownerStatus)

        // Verificaciones de todas las Sucursales
        assertEquals(3, activated.branches.size)
        activated.branches.forEachIndexed { _, branch ->
            assertTrue("Sucursal ${branch.name} debe tener clave generada", branch.generatedKey.isNotBlank())
            assertTrue("Sucursal ${branch.name} debe empezar con SGN-01Y-", branch.generatedKey.startsWith("SGN-01Y-"))
            assertEquals("ACTIVADA (01Y)", branch.status)
        }
    }

    @Test
    fun testActivateAndGenerateAllKeysForOwner_BranchesWithEmptyHardwareIdHandledGracefully() {
        val branches = listOf(
            BranchItem(
                id = "b1",
                name = "Sucursal Con Dispositivo",
                hardwareId = "SGN-AAAA-1111",
                generatedKey = "",
                status = "PENDIENTE"
            ),
            BranchItem(
                id = "b2",
                name = "Sucursal Nueva Sin Dispositivo Aún",
                hardwareId = "", // Todavía no tienen la báscula/POS
                generatedKey = "",
                status = "PENDIENTE"
            )
        )

        val owner = OwnerRecord(
            id = "owner-mixed",
            ownerName = "Cliente Mixto",
            companyName = "Comercializadora",
            phoneWhatsApp = "5551234567",
            email = "mixto@empresa.com",
            membershipTier = KeygenTier.LIFETIME,
            ownerHardwareId = "SGN-XXXX-9999",
            branches = branches,
            paymentStatus = "NO_PAGADO"
        )

        val activated = activateAndGenerateAllKeysForOwner(owner)

        assertEquals("PAGADO", activated.paymentStatus)
        assertTrue("Dueño con HW debe tener clave", activated.ownerLicenseKey.startsWith("SGN-LIFE-"))

        // Sucursal 1 (con HW) tiene clave
        val b1 = activated.branches[0]
        assertTrue("Sucursal 1 debe tener clave", b1.generatedKey.startsWith("SGN-LIFE-"))
        assertEquals("ACTIVADA (LIFE)", b1.status)

        // Sucursal 2 (sin HW) no rompe ni corrompe el registro
        val b2 = activated.branches[1]
        assertTrue("Sucursal 2 sin HW permanece con clave vacía", b2.generatedKey.isEmpty())
        assertEquals("PENDIENTE", b2.status)
    }

    @Test
    fun testPaymentTogglePreservesKeysWhenUnmarked() {
        val owner = OwnerRecord(
            id = "owner-toggle",
            ownerName = "Cliente Toggle",
            companyName = "Carnes del Valle",
            phoneWhatsApp = "2761000000",
            email = "valle@carnes.com",
            ownerHardwareId = "SGN-7777-8888",
            membershipTier = KeygenTier.RENTAL_1M,
            branches = listOf(
                BranchItem(id = "b1", name = "Sucursal 1", hardwareId = "SGN-9999-1111")
            ),
            paymentStatus = "NO_PAGADO"
        )

        // 1. Marcar como pagado genera claves
        val paid = activateAndGenerateAllKeysForOwner(owner)
        val generatedOwnerKey = paid.ownerLicenseKey
        val generatedBranchKey = paid.branches[0].generatedKey
        assertTrue(generatedOwnerKey.isNotBlank())
        assertTrue(generatedBranchKey.isNotBlank())
        assertEquals("PAGADO", paid.paymentStatus)

        // 2. Si se cambia temporalmente a PENDIENTE, las claves no se destruyen
        val unpaid = paid.copy(paymentStatus = "NO_PAGADO")
        assertEquals("NO_PAGADO", unpaid.paymentStatus)
        assertEquals(generatedOwnerKey, unpaid.ownerLicenseKey)
        assertEquals(generatedBranchKey, unpaid.branches[0].generatedKey)
    }
}
