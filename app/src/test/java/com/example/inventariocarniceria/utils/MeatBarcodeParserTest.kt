package com.example.inventariocarniceria.utils

import com.example.inventariocarniceria.data.local.ProductEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeatBarcodeParserTest {

    private val testProducts = listOf(
        ProductEntity(
            code = 45,
            localCode = 45,
            name = "Manitas de Puerco",
            weightPerBoxKg = 20.0,
            orderUnitType = "CAJA",
            synonyms = "manitas, puerco, 7501020304050, MP45"
        ),
        ProductEntity(
            code = 12,
            localCode = 12,
            name = "Arrachera Marinada",
            weightPerBoxKg = 15.5,
            orderUnitType = "CAJA",
            synonyms = "arrachera, suave"
        ),
        ProductEntity(
            code = 101,
            localCode = 101,
            name = "Costilla de Cerdo",
            weightPerBoxKg = 0.0,
            orderUnitType = "KG"
        )
    )

    @Test
    fun parse_boxWithLast4DigitsWeight_returnsProductAndExactWeight() {
        // Ejemplo: caja de Manitas de Puerco con código que termina en 2000 (20.00 kg)
        // El prefijo es "MP45" o "45"
        val barcode = "MP452000"
        val result = MeatBarcodeParser.parse(barcode, testProducts)

        assertNotNull(result.product)
        assertEquals(45, result.product?.localCode)
        assertEquals("Manitas de Puerco", result.product?.name)
        assertEquals(20.00, result.weightKg, 0.001)
        assertTrue(result.isWeightExtractedFromBarcode)
    }

    @Test
    fun parse_boxWithArbitraryWeightLast4Digits_returnsCalculatedWeight() {
        // Caja de Manitas con peso variable 21.45 kg -> termina en 2145
        val barcode = "MP452145"
        val result = MeatBarcodeParser.parse(barcode, testProducts)

        assertNotNull(result.product)
        assertEquals("Manitas de Puerco", result.product?.name)
        assertEquals(21.45, result.weightKg, 0.001)
    }

    @Test
    fun parse_boxWithoutWeight_usesCatalogStandardWeight() {
        // Caso: la caja solo trae el código de barras del producto (7501020304050)
        // No trae peso en los últimos dígitos, pero el catálogo sabe que la caja de manitas es de 20.0 kg
        val barcode = "7501020304050"
        val result = MeatBarcodeParser.parse(barcode, testProducts)

        assertNotNull(result.product)
        assertEquals("Manitas de Puerco", result.product?.name)
        assertEquals(20.0, result.weightKg, 0.001)
        assertEquals("CAJA", result.unitType)
    }

    @Test
    fun parse_systelScaleTicket_extractsPluAndWeight() {
        // Formato Systel: 20 + 00012 (PLU 12) + 02350 (2.350 kg) + 0 (check)
        val barcode = "2000012023500"
        val result = MeatBarcodeParser.parse(barcode, testProducts)

        assertNotNull(result.product)
        assertEquals("Arrachera Marinada", result.product?.name)
        assertEquals(2.350, result.weightKg, 0.001)
        assertTrue(result.isWeightExtractedFromBarcode)
    }

    @Test
    fun parse_qrStructured_extractsCorrectly() {
        val qrCode = "PLU:45;PESO:20.0"
        val result = MeatBarcodeParser.parse(qrCode, testProducts)

        assertNotNull(result.product)
        assertEquals("Manitas de Puerco", result.product?.name)
        assertEquals(20.0, result.weightKg, 0.001)
    }
}
