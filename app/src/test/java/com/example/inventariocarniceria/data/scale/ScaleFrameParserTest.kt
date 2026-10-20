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
}
