package com.example.inventariocarniceria.data.scale

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScaleFrameParserTest {

    @Test
    fun `buildCuoraWeightRequest builds exact 3-byte frame IP + 0x01 + XOR CRC`() {
        // Balanza ID = 1 (ej. 192.168.100.50): 0x01, 0x01, 0x00
        assertArrayEquals(
            byteArrayOf(0x01, 0x01, 0x00),
            ScaleFrameParser.buildCuoraWeightRequest(1)
        )
        // Balanza ID = 2 (ej. 192.168.100.40): 0x02, 0x01, 0x03
        assertArrayEquals(
            byteArrayOf(0x02, 0x01, 0x03),
            ScaleFrameParser.buildCuoraWeightRequest(2)
        )
        // Balanza ID = 20: 0x14, 0x01, 0x15
        assertArrayEquals(
            byteArrayOf(20, 0x01, 21),
            ScaleFrameParser.buildCuoraWeightRequest(20)
        )
    }

    @Test
    fun `parseCuoraBinary decodes official 16-byte Cuora Max Function 1 weight frame`() {
        // ID=2, Función=1, Peso Neto="002000" (2.000 kg), Condición='e' (Estable), Tara="000000", CRC=0x54
        val payload = "002000e000000".toByteArray(Charsets.US_ASCII)
        val packet = ByteArray(16)
        packet[0] = 0x02
        packet[1] = 0x01
        System.arraycopy(payload, 0, packet, 2, 13)
        var crc = 0
        for (i in 0 until 15) crc = crc xor (packet[i].toInt() and 0xFF)
        packet[15] = crc.toByte()

        val result = ScaleFrameParser.parseCuoraBinary(packet, packet.size)
        assertNotNull(result)
        assertEquals(2, result!!.scaleAddress)
        assertEquals(1, result.functionCode)
        assertNotNull(result.parsedWeight)
        assertEquals(2.000, result.parsedWeight!!.weight, 0.0001)
        assertTrue(result.parsedWeight!!.isStable)
        assertNull(result.parsedWeight!!.errorCode)
    }

    @Test
    fun `parseCuoraBinary decodes unstable weight on Scale ID 1`() {
        // ID=1, Función=1, Peso Neto="001250" (1.250 kg), Condición='i' (Inestable), Tara="000000"
        val payload = "001250i000000".toByteArray(Charsets.US_ASCII)
        val packet = ByteArray(16)
        packet[0] = 0x01
        packet[1] = 0x01
        System.arraycopy(payload, 0, packet, 2, 13)
        var crc = 0
        for (i in 0 until 15) crc = crc xor (packet[i].toInt() and 0xFF)
        packet[15] = crc.toByte()

        val result = ScaleFrameParser.parseCuoraBinary(packet, packet.size)
        assertNotNull(result)
        assertEquals(1, result!!.scaleAddress)
        assertEquals(1.250, result.parsedWeight!!.weight, 0.0001)
        assertFalse(result.parsedWeight!!.isStable)
    }

    @Test
    fun `parseCuoraBinary decodes error frame 02 50 45 34 23 and extracts scale address 2`() {
        val errorPacket = byteArrayOf(0x02, 0x50, 0x45, 0x34, 0x23)
        val result = ScaleFrameParser.parseCuoraBinary(errorPacket, errorPacket.size)
        assertNotNull(result)
        assertEquals(2, result!!.scaleAddress)
        assertEquals(0x50, result.functionCode)
        assertEquals("E4", result.protocolErrorCode)
        assertNull(result.parsedWeight)
    }

    @Test
    fun `parseCuoraBinary decodes ACK frame 02 57 41 43 4B 1C and extracts scale address 2`() {
        val ackPacket = byteArrayOf(0x02, 0x57, 0x41, 0x43, 0x4B, 0x1C)
        val result = ScaleFrameParser.parseCuoraBinary(ackPacket, ackPacket.size)
        assertNotNull(result)
        assertEquals(2, result!!.scaleAddress)
        assertTrue(result.isAck)
    }

    @Test
    fun `ASCII parse extracts net weight from 002000e000000 instead of tare`() {
        val parsed = ScaleFrameParser.parse("002000e000000")
        assertNotNull(parsed)
        assertEquals(2.000, parsed!!.weight, 0.0001)
        assertTrue(parsed.isStable)
        assertNull(parsed.errorCode)
    }

    @Test
    fun `ASCII parse handles decimal and error frames accurately`() {
        val dec = ScaleFrameParser.parse("PE1.060#")
        assertNotNull(dec)
        assertEquals(1.060, dec!!.weight, 0.0001)
        assertNull(dec.errorCode)

        val err = ScaleFrameParser.parse("PE4#")
        assertNotNull(err)
        assertEquals(0.0, err!!.weight, 0.0001)
        assertEquals("PE4", err.errorCode)
    }

    @Test
    fun `parseCuoraPluListResponse decodes Function 31 PLU list for both Cuora Max 6-digit and Cuora 2 4-digit`() {
        val rawMax = ScaleFrameParser.buildCuoraCommand(1, 31, "N000001V0N000024V1N000105V2F")
        val listMax = ScaleFrameParser.parseCuoraPluListResponse(rawMax)
        assertEquals(3, listMax.size)
        assertEquals(1 to true, listMax[0])
        assertEquals(24 to true, listMax[1])
        assertEquals(105 to true, listMax[2])

        val rawCuora2 = ScaleFrameParser.buildCuoraCommand(2, 31, "N0001V0N0024V1F")
        val listCuora2 = ScaleFrameParser.parseCuoraPluListResponse(rawCuora2)
        assertEquals(2, listCuora2.size)
        assertEquals(1 to false, listCuora2[0])
        assertEquals(24 to false, listCuora2[1])
    }

    @Test
    fun `parseCuoraSinglePluResponse decodes Function 62 V7_0 V6_2 and Function 3 V4_0 accurately`() {
        val sectors = mapOf(1 to "RES", 2 to "CERDO", 5 to "LACTEOS")

        // 1. Prueba con trama oficial de Form1.vb (Función 3 - Cuora V4.0):
        val vbData = "00012Activia Bebible Fr00050000050000001050000U000000SSOtros datos"
        val vbPacket = ScaleFrameParser.buildCuoraCommand(1, 3, vbData)
        val parsedVb = ScaleFrameParser.parseCuoraSinglePluResponse(vbPacket, vbPacket.size, priceDecimals = 2, sectorsMap = sectors)
        assertNotNull(parsedVb)
        assertEquals(1, parsedVb!!.code)
        assertEquals("Activia Bebible Fr", parsedVb.name)
        assertEquals(5.00, parsedVb.price, 0.001)
        assertEquals("LACTEOS", parsedVb.section)

        // 2. Prueba con trama oficial Función 62 (Cuora Max V6.2):
        // [6 NroPLU: 000024][1 Gestión: 0][18 Nombre: MANITAS DE PUERCO ][6 Precio1: 007500][6 Precio2: 007500][6 CodPLU: 000024][2 Sector: 02][4 Vto: 0000][1 Tipo: P]
        val v62Data = "000024" + "0" + "MANITAS DE PUERCO " + "007500" + "007500" + "000024" + "02" + "0000" + "P" + "0000"
        val v62Packet = ScaleFrameParser.buildCuoraCommand(1, 62, v62Data)
        val parsedV62 = ScaleFrameParser.parseCuoraSinglePluResponse(v62Packet, v62Packet.size, priceDecimals = 2, sectorsMap = sectors)
        assertNotNull(parsedV62)
        assertEquals(24, parsedV62!!.code)
        assertEquals("MANITAS DE PUERCO", parsedV62.name)
        assertEquals(75.00, parsedV62.price, 0.001)
        assertEquals("CERDO", parsedV62.section)
    }

    @Test
    fun `ASCII parse extracts PLU and weight from Cuora Function 72 and ticket frames`() {
        val func72 = "000000123^23/10/26^14:30:00^01^000024^02^0000^01250^00000^000000009375F"
        val res72 = ScaleFrameParser.parse(func72)
        assertNotNull(res72)
        assertEquals(24, res72!!.pluCode)
        assertEquals(1.250, res72.weight, 0.0001)

        val ticket = "01;24;1.060;75.00#"
        val resTicket = ScaleFrameParser.parse(ticket)
        assertNotNull(resTicket)
        assertEquals(24, resTicket!!.pluCode)
        assertEquals(1.060, resTicket.weight, 0.0001)
    }
}
