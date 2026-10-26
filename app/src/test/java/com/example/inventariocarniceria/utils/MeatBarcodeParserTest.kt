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

    @Test
    fun parse_directBoxWeightBarcode_extractsExactDecimalWeight() {
        // Caso real de caja de empacadora (Kekén / Porcícola Mexicano): código de barras "22.30"
        val barcode = "22.30"
        val result = MeatBarcodeParser.parse(barcode, testProducts)

        assertTrue(result.isWeightExtractedFromBarcode)
        assertEquals(22.30, result.weightKg, 0.001)

        // Caso con coma decimal "22,30"
        val commaResult = MeatBarcodeParser.parse("22,30", testProducts)
        assertTrue(commaResult.isWeightExtractedFromBarcode)
        assertEquals(22.30, commaResult.weightKg, 0.001)

        // Caso con unidad "22.30 kg"
        val kgResult = MeatBarcodeParser.parse("22.30 kg", testProducts)
        assertTrue(kgResult.isWeightExtractedFromBarcode)
        assertEquals(22.30, kgResult.weightKg, 0.001)
    }

    @Test
    fun parse_fourDigitWeightBoxBarcode_extractsCalculatedWeight() {
        // Código de barras de 4 dígitos "2230" cuando no coincide con un producto
        val barcode = "2230"
        val result = MeatBarcodeParser.parse(barcode, testProducts)

        assertTrue(result.isWeightExtractedFromBarcode)
        assertEquals(22.30, result.weightKg, 0.001)
    }

    @Test
    fun parse_kekenIndustrialPipeBarcode_extractsWeightFolioAndLote() {
        val cabezaProd = ProductEntity(
            code = 23,
            localCode = 23,
            name = "Cabeza Congelado",
            synonyms = "cabeza, 124154A1"
        )
        val productsWithCabeza = testProducts + cabezaProd
        val barcode = "01H1180201658|P124154A1|L0027567916|D04/10/2026|Q  22.30 |W0R00"
        val result = MeatBarcodeParser.parse(barcode, productsWithCabeza)

        assertTrue(result.isWeightExtractedFromBarcode)
        assertEquals(22.30, result.weightKg, 0.001)
        assertEquals("1180201658", result.boxFolio)
        assertEquals("0027567916", result.lotNumber)
        assertNotNull(result.product)
        assertEquals(23, result.product?.localCode)
    }
}
