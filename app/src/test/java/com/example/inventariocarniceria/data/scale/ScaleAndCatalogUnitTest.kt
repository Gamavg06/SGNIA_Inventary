package com.example.inventariocarniceria.data.scale

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScaleAndCatalogUnitTest {

    // ==========================================
    // 1. PRUEBAS DE TRAMAS DE PESO INDUSTRIAL
    // ==========================================

    @Test
    fun testExplicitDecimalWeights() {
        // Formato estándar con kg
        val res1 = ScaleFrameParser.parse("+001.250kg")
        assertNotNull(res1)
        assertEquals(1.250, res1!!.weight, 0.0001)
        assertTrue(res1.isStable)

        // Formato con punto y espacios
        val res2 = ScaleFrameParser.parse("ST,GS,  12.350 kg")
        assertNotNull(res2)
        assertEquals(12.350, res2!!.weight, 0.0001)
        assertTrue(res2.isStable)

        // Cero exacto
        val res3 = ScaleFrameParser.parse("0.000")
        assertNotNull(res3)
        assertEquals(0.000, res3!!.weight, 0.0001)

        // Formato con coma decimal (común en Latinoamérica / Argentina / Systel)
        val res4 = ScaleFrameParser.parse("+02,450kg")
        assertNotNull(res4)
        assertEquals(2.450, res4!!.weight, 0.0001)
    }

    @Test
    fun testWeightInGrams() {
        // Gramos explícitos
        val res1 = ScaleFrameParser.parse("1250g")
        assertNotNull(res1)
        assertEquals(1.250, res1!!.weight, 0.0001)

        val res2 = ScaleFrameParser.parse("500gr")
        assertNotNull(res2)
        assertEquals(0.500, res2!!.weight, 0.0001)

        val res3 = ScaleFrameParser.parse("0g")
        assertNotNull(res3)
        assertEquals(0.000, res3!!.weight, 0.0001)
    }

    @Test
    fun testImpliedDecimalWeightsSystelESP() {
        // Systel Cuora continuo / Toledo 5-6 dígitos sin punto decimal (implied 3 decimals)
        // 001250 -> 1.250 kg
        val res1 = ScaleFrameParser.parse("001250")
        assertNotNull(res1)
        assertEquals(1.250, res1!!.weight, 0.0001)

        // 000000 -> 0.000 kg
        val res2 = ScaleFrameParser.parse("000000")
        assertNotNull(res2)
        assertEquals(0.000, res2!!.weight, 0.0001)

        // 015400 -> 15.400 kg
        val res3 = ScaleFrameParser.parse("015400")
        assertNotNull(res3)
        assertEquals(15.400, res3!!.weight, 0.0001)

        // Con prefijo de estado WN001250
        val res4 = ScaleFrameParser.parse("WN001250")
        assertNotNull(res4)
        assertEquals(1.250, res4!!.weight, 0.0001)
    }

    @Test
    fun testSignedIntegerWeights() {
        // Formato entero con signo +1250 -> 1.250 kg
        val res1 = ScaleFrameParser.parse("+1250")
        assertNotNull(res1)
        assertEquals(1.250, res1!!.weight, 0.0001)
    }

    @Test
    fun testStabilityDetection() {
        // Estable
        val stable = ScaleFrameParser.parse("ST,GS,001.500kg")
        assertNotNull(stable)
        assertTrue(stable!!.isStable)

        // Inestable con US
        val unstableUS = ScaleFrameParser.parse("US,GS,001.500kg")
        assertNotNull(unstableUS)
        assertFalse(unstableUS!!.isStable)

        // Inestable con IN o MOV
        val unstableIN = ScaleFrameParser.parse("IN,001.500kg")
        assertNotNull(unstableIN)
        assertFalse(unstableIN!!.isStable)

        val unstableMOV = ScaleFrameParser.parse("MOV 001.500")
        assertNotNull(unstableMOV)
        assertFalse(unstableMOV!!.isStable)
    }

    @Test
    fun testPluExtractionInWeightFrame() {
        // Trama con PLU
        val res1 = ScaleFrameParser.parse("PLU: 20  PESO: 1.450kg")
        assertNotNull(res1)
        assertEquals(20, res1!!.pluCode)
        assertEquals(1.450, res1.weight, 0.0001)

        // Trama con COD
        val res2 = ScaleFrameParser.parse("COD: 31  001250")
        assertNotNull(res2)
        assertEquals(31, res2!!.pluCode)
        assertEquals(1.250, res2.weight, 0.0001)

        // Trama delimitada por punto y coma (PLU;PESO)
        val res3 = ScaleFrameParser.parse("10; 2.300")
        assertNotNull(res3)
        assertEquals(10, res3!!.pluCode)
        assertEquals(2.300, res3.weight, 0.0001)

        // Trama completa de ticket con precio total (el precio no debe confundir el peso)
        val res4 = ScaleFrameParser.parse("PLU: 24   PESO: 1.250kg   TOTAL: $150.00")
        assertNotNull(res4)
        assertEquals(24, res4!!.pluCode)
        assertEquals(1.250, res4.weight, 0.0001)
    }

    @Test
    fun testSystelCuoraErrorCodes() {
        // PE4# -> Peso menor al mínimo legal (< 100g)
        val pe4 = ScaleFrameParser.parse("PE4#")
        assertNotNull(pe4)
        assertEquals("PE4", pe4!!.errorCode)
        assertEquals(0.000, pe4.weight, 0.0001)
        assertTrue(pe4.errorMessage?.contains("mínimo", ignoreCase = true) == true)

        // PE4 sin hash
        val pe4NoHash = ScaleFrameParser.parse("PE4")
        assertNotNull(pe4NoHash)
        assertEquals("PE4", pe4NoHash!!.errorCode)

        // PE1# -> Inestable
        val pe1 = ScaleFrameParser.parse("PE1#")
        assertNotNull(pe1)
        assertEquals("PE1", pe1!!.errorCode)
        assertFalse(pe1.isStable)

        // PE2# -> Cero
        val pe2 = ScaleFrameParser.parse("PE2#")
        assertNotNull(pe2)
        assertEquals("PE2", pe2!!.errorCode)

        // PE3# -> Negativo
        val pe3 = ScaleFrameParser.parse("PE3#")
        assertNotNull(pe3)
        assertEquals("PE3", pe3!!.errorCode)

        // PE5# -> Sobrecarga
        val pe5 = ScaleFrameParser.parse("PE5#")
        assertNotNull(pe5)
        assertEquals("PE5", pe5!!.errorCode)
    }

    @Test
    fun testSystelCuoraWeightFrames() {
        // P000.500# -> 0.500 kg
        val p1 = ScaleFrameParser.parse("P000.500#")
        assertNotNull(p1)
        assertEquals(0.500, p1!!.weight, 0.0001)
        assertTrue(p1.isStable)

        // PE000.500# -> 0.500 kg Estable
        val pe1 = ScaleFrameParser.parse("PE000.500#")
        assertNotNull(pe1)
        assertEquals(0.500, pe1!!.weight, 0.0001)
        assertTrue(pe1.isStable)

        // PI001.250# -> 1.250 kg Inestable
        val pi1 = ScaleFrameParser.parse("PI001.250#")
        assertNotNull(pi1)
        assertEquals(1.250, pi1!!.weight, 0.0001)
        assertFalse(pi1.isStable)

        // P 1.250# con espacio
        val p2 = ScaleFrameParser.parse("P 1.250#")
        assertNotNull(p2)
        assertEquals(1.250, p2!!.weight, 0.0001)

        // PE+02.450#
        val p3 = ScaleFrameParser.parse("PE+02.450#")
        assertNotNull(p3)
        assertEquals(2.450, p3!!.weight, 0.0001)

        // Formato con decimales implícitos: P000500# -> 0.500 kg
        val imp1 = ScaleFrameParser.parse("P000500#")
        assertNotNull(imp1)
        assertEquals(0.500, imp1!!.weight, 0.0001)

        // Formato con decimales implícitos: PE001250# -> 1.250 kg
        val imp2 = ScaleFrameParser.parse("PE001250#")
        assertNotNull(imp2)
        assertEquals(1.250, imp2!!.weight, 0.0001)

        // Caso crítico detectado en prueba real: 1.060 kg de leche con prefijo PE
        val milk1 = ScaleFrameParser.parse("PE1.060#")
        assertNotNull(milk1)
        assertEquals(1.060, milk1!!.weight, 0.0001)
        assertNull(milk1.errorCode)
        assertTrue(milk1.isStable)

        val milk2 = ScaleFrameParser.parse("PE 1.060#")
        assertNotNull(milk2)
        assertEquals(1.060, milk2!!.weight, 0.0001)
        assertNull(milk2.errorCode)

        // Caso donde el peso empieza con 4 (ej: 4.500 kg) no debe confundirse con PE4
        val meat4 = ScaleFrameParser.parse("PE4.500#")
        assertNotNull(meat4)
        assertEquals(4.500, meat4!!.weight, 0.0001)
        assertNull(meat4.errorCode)

        // Formato ticket con punto y coma: 01;24;1.060;75.00#
        val ticket = ScaleFrameParser.parse("01;24;1.060;75.00#")
        assertNotNull(ticket)
        assertEquals(1.060, ticket!!.weight, 0.0001)
    }

    @Test
    fun testInvalidOrNoiseFrames() {
        assertNull(ScaleFrameParser.parse(""))
        assertNull(ScaleFrameParser.parse("   "))
        assertNull(ScaleFrameParser.parse("@@#$$%^^&"))
        assertNull(ScaleFrameParser.parse("ERROR_COMM"))
        assertNull(ScaleFrameParser.parse("WACK"))
        assertNull(ScaleFrameParser.parse("ACK"))
    }

    // ==========================================
    // 2. PRUEBAS DE IMPORTACIÓN DE CATÁLOGO QENDRA
    // ==========================================

    @Test
    fun testQendraCatalogParserSemicolon() {
        val input = """
            10;PIERNA CON HUESO;98.50;CERDO
            20;PIERNA SIN HUESO;115.00;CERDO
            30;BISTEC DE RES;185.00;RES
        """.trimIndent()

        val list = ScaleCatalogParser.parse(input)
        assertEquals(3, list.size)
        assertEquals(10, list[0].code)
        assertEquals("PIERNA CON HUESO", list[0].name)
        assertEquals(98.50, list[0].price, 0.001)
        assertEquals("CERDO", list[0].section)
    }

    @Test
    fun testQendraCatalogParserCsvAndQuotes() {
        val input = """
            "1","BISTEC SELECTO","195.50","RES"
            "2","COSTILLA CARGADA","130.00","RES"
        """.trimIndent()

        val list = ScaleCatalogParser.parse(input)
        assertEquals(2, list.size)
        assertEquals(1, list[0].code)
        assertEquals("BISTEC SELECTO", list[0].name)
        assertEquals(195.50, list[0].price, 0.001)
        assertEquals("RES", list[0].section)
    }

    @Test
    fun testQendraCatalogParserTabsAndSymbols() {
        val input = """
            # Encabezado que debe ignorarse
            // Otro comentario
            15	CHULETA AHUMADA	$110,00	CERDO
            25	QUESO MANCHEGO	$140.50	LACTEOS
        """.trimIndent()

        val list = ScaleCatalogParser.parse(input)
        assertEquals(2, list.size)
        assertEquals(15, list[0].code)
        assertEquals("CHULETA AHUMADA", list[0].name)
        assertEquals(110.00, list[0].price, 0.001)
        assertEquals("CERDO", list[0].section)

        assertEquals(25, list[1].code)
        assertEquals(140.50, list[1].price, 0.001)
    }

    @Test
    fun testQendraCatalogParserCorruptLinesIgnored() {
        val input = """
            CODIGO;NOMBRE;PRECIO;CATEGORIA
            ;;;
            999;SOLO DOS COLUMNAS
            invalido;PRODUCTO MALO;100.00;GENERAL
            12;PRODUCTO BUENO;50.00;ABARROTES
        """.trimIndent()

        val list = ScaleCatalogParser.parse(input)
        assertEquals(1, list.size)
        assertEquals(12, list[0].code)
        assertEquals("PRODUCTO BUENO", list[0].name)
        assertEquals(50.00, list[0].price, 0.001)
    }

    @Test
    fun testParseSupabaseCsvExportWithHardwareIdAndDbIdColumns() {
        val input = """
            id,owner_hw_id,code,name,price,category,created_at
            4,SGN-4F18-75FA,1,MENUDO DE RES,110.00,Res,2026-10-26 01:06:43.805859+00
            5,SGN-4F18-75FA,2,R. ESPINAZO,30.00,Res,2026-10-26 01:06:43.805859+00
            6,SGN-4F18-75FA,22,CUERO S/G,36.00,Otros,2026-10-26 01:06:43.805859+00
        """.trimIndent()

        val list = ScaleCatalogParser.parse(input)
        assertEquals(3, list.size)
        assertEquals(1, list[0].code)
        assertEquals("MENUDO DE RES", list[0].name)
        assertEquals(110.00, list[0].price, 0.001)
        assertEquals("Res", list[0].section)

        assertEquals(22, list[2].code)
        assertEquals("CUERO S/G", list[2].name)
        assertEquals(36.00, list[2].price, 0.001)
    }

    // ==========================================
    // 3. PRUEBAS DE CÁLCULO DE PEDIDOS Y CAJAS
    // ==========================================

    @Test
    fun testOrderUnitsCalculationForBoxesAndPackagedItems() {
        // 1. Menudo: Caja de 27.24 kg, faltante de 54.48 kg -> 2 cajas
        val boxWeight = 27.24
        val shortage = 54.48
        val calculatedBoxes = kotlin.math.max(1.0, kotlin.math.ceil(shortage / boxWeight))
        assertEquals(2.0, calculatedBoxes, 0.001)

        // 2. Pierna: Caja de 32 kg, faltante de 40 kg -> 2 cajas (ceil)
        val piernaWeight = 32.0
        val piernaShortage = 40.0
        val piernaBoxes = kotlin.math.max(1.0, kotlin.math.ceil(piernaShortage / piernaWeight))
        assertEquals(2.0, piernaBoxes, 0.001)

        // 3. Granel KG: Faltante de 15.5 kg -> unidades = 15.5
        val kgShortage = 15.5
        assertEquals(15.5, kgShortage, 0.001)
    }

    @Test
    fun testNonDerivedOrdersFilterIncludesKgAndBoxes() {
        data class MockProduct(val code: Int, val parentProductCode: Int?, val orderUnitType: String)
        data class MockOrder(val productCode: Int, val shortage: Double)

        val products = listOf(
            MockProduct(10, null, "CAJA"),      // Menudo (Base)
            MockProduct(20, null, "KG"),        // Bistec a granel (Base)
            MockProduct(21, 20, "KG"),          // Bistec picado (Derivado del 20)
            MockProduct(30, null, "BARRA"),     // Jamón (Base)
            MockProduct(40, null, "PIEZA")      // Costillar (Base)
        ).associateBy { it.code }

        val orders = listOf(
            MockOrder(10, 54.48),
            MockOrder(20, 25.0),
            MockOrder(21, 5.0),   // Derivado, debe filtrarse
            MockOrder(30, 7.0),
            MockOrder(40, 1.0)
        )

        val filtered = orders.filter { order ->
            val prod = products[order.productCode]
            prod == null || prod.parentProductCode == null
        }

        // Deben pasar Menudo (10), Bistec base (20), Jamón (30), Costillar (40), excluyendo sólo el derivado (21)
        assertEquals(4, filtered.size)
        assertTrue(filtered.any { it.productCode == 10 })
        assertTrue(filtered.any { it.productCode == 20 }) // KG preservado
        assertFalse(filtered.any { it.productCode == 21 }) // Derivado omitido
        assertTrue(filtered.any { it.productCode == 30 })
        assertTrue(filtered.any { it.productCode == 40 })
    }

    @Test
    fun testWhatsAppExporterNeverOutputsZeroBoxes() {
        val captureWithUnits = com.example.inventariocarniceria.data.local.InventoryCaptureEntity(
            productCode = 10,
            productName = "Menudo",
            capturedWeight = 0.0,
            maxStock = 54.48,
            shortage = 54.48,
            orderedUnits = 2.0,
            unitType = "CAJA"
        )
        val msg = com.example.inventariocarniceria.utils.WhatsAppExporter.generateFormattedMessage(listOf(captureWithUnits))
        assertTrue(msg.contains("2 CAJA(S)"))
        assertFalse(msg.contains("0 CAJA(S)"))

        val captureWithoutUnits = com.example.inventariocarniceria.data.local.InventoryCaptureEntity(
            productCode = 20,
            productName = "Bistec Granel",
            capturedWeight = 5.0,
            maxStock = 20.0,
            shortage = 15.0,
            orderedUnits = 0.0,
            unitType = "KG"
        )
        val msgKg = com.example.inventariocarniceria.utils.WhatsAppExporter.generateFormattedMessage(listOf(captureWithoutUnits))
        assertTrue(msgKg.contains("15.000 kg"))
        assertFalse(msgKg.contains("0 CAJA(S)"))
    }
}
