package com.example.inventariocarniceria.data.scale

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap

data class ScaleState(
    val isConnected: Boolean = false,
    val deviceName: String = "Báscula Wi-Fi TCP/IP",
    val currentWeight: Double = 0.000,
    val isStable: Boolean = true,
    val activeScaleId: Int = 1,
    val lastPluCode: Int? = null,
    val lastRawFrame: String = "",
    val lastRawHex: String = "",
    val totalBytesReceived: Long = 0L,
    val communicationMode: String = "Iniciando...",
    val lastErrorCode: String? = null,
    val lastErrorMessage: String? = null
)

data class TcpScaleDevice(
    val id: Int,
    val name: String,
    val ip: String,
    val port: Int = 10001,
    val brand: String = "Systel",
    val model: String = "Cuora Max",
    val protocol: String = "AUTO",
    val isConnected: Boolean = false,
    val statusDescription: String = "Desconectado",
    val latencyMs: Long? = null,
    val lastWeight: Double = 0.000,
    val isStable: Boolean = true,
    val lastPluCode: Int? = null,
    val lastRawFrame: String = "",
    val lastRawHex: String = "",
    val totalBytesReceived: Long = 0L,
    val communicationMode: String = "Desconectado",
    val lastErrorCode: String? = null,
    val lastErrorMessage: String? = null
)

data class ParsedPluPreview(
    val code: Int,
    val name: String,
    val price: Double,
    val section: String
)

data class ParsedScaleWeight(
    val weight: Double,
    val isStable: Boolean,
    val pluCode: Int? = null,
    val cleanFrame: String = "",
    val errorCode: String? = null,
    val errorMessage: String? = null
)

data class CuoraBinaryParseResult(
    val scaleAddress: Int,
    val functionCode: Int,
    val parsedWeight: ParsedScaleWeight?,
    val isAck: Boolean = false,
    val protocolErrorCode: String? = null
)

object ScaleFrameParser {
    // 1. Detección estricta de Códigos de Error / Advertencia Systel Cuora (ej: "PE4#", "PE4", "PE1", "ERR4")
    // NOTA: Debe ser EXACTAMENTE el código sin decimales posteriores para no confundir con "PE1.060#" (Peso Estable 1.060 kg)
    private val systelErrorRegex = Regex("""^(?:PE|ERR|ERROR)[;:\s=]*([1-9])#?$""", RegexOption.IGNORE_CASE)

    // 2. Formato Oficial Systel Cuora Max / Cuora 2 / RS232 (Función 1 y Protocolos 1/2):
    // 6 números (o '-' y 5 números, o 5 números) en gramos sin coma + 'e' (Estable) / 'i' (Inestable) + 6 números de Tara opcionales
    // Ejemplo oficial manual Cuora Max V7.0 Pág. 4: "000250e000000" (250g estable), "002000e000000" (2.000kg estable), "-00250i000000"
    private val systelCuoraGramsStabilityRegex = Regex("""(?<!\d)([+-]?\s*\d{5,6})([eiEI])(\d{0,6})""")

    // 3. Prefijo Systel Cuora P / PE / PI con peso decimal (ej: "P001.250#", "PE000.500#", "PE1.060#", "PE 1.060#", "P 1.250kg", "PE+02.450#")
    private val systelPrefixDecimalRegex = Regex("""(?:PE|PI|P)[;:\s=]*([+-]?\s*\d{1,4}[.,]\d{1,3})\s*(?:kg|#)?""", RegexOption.IGNORE_CASE)

    // 4. Prefijo Systel Cuora P / PE / PI con 5-6 dígitos enteros (3 decimales implícitos) (ej: "P000500#", "PE001250#")
    private val systelPrefixImpliedRegex = Regex("""(?:PE|PI|P)[;:\s=]*([+-]?\s*\d{5,6})#?""", RegexOption.IGNORE_CASE)

    // 5. Etiquetado explícito de peso (ej: "PESO: 1.250kg", "NET: 2.300")
    private val labeledWeightRegex = Regex("""(?:PESO|NETO|NET|WT|W|PES)[:;\s=]+([+-]?\s*\d{1,4}[.,]\d{1,3})\s*(?:kg)?""", RegexOption.IGNORE_CASE)

    // 6. Decimal con unidad kg explícita (ej: "+001.250kg", "ST,GS, 12.350 kg")
    private val kgExplicitRegex = Regex("""([+-]?\s*\d{1,4}[.,]\d{1,3})\s*(?:kg|kilo)\b""", RegexOption.IGNORE_CASE)

    // 7. Decimal simple con o sin signo o '#' al final (ej: "1.250", "0.000", "02,450", "001.250#")
    private val plainDecimalRegex = Regex("""(?<!\d)([+-]?\s*\d{1,4}[.,]\d{1,3})(?!\d)""")

    // 8. Gramos explícitos (ej: "1250g", "500gr")
    private val gramsRegex = Regex("""([+-]?\s*\d{1,6})\s*g(?:r)?\b""", RegexOption.IGNORE_CASE)

    // 9. Entero de 5-6 dígitos con 3 decimales implícitos (Systel Cuora / Toledo) (ej: "001250", "WN001250", "000500#")
    private val impliedDecimalRegex = Regex("""(?:WN|ST|US|GS|[+-])?\s*(\d{5,6})(?:#|\b)""")

    // 10. Entero con signo de 4-5 dígitos (ej: "+1250" -> 1.250 kg)
    private val signedIntRegex = Regex("""([+-])\s*(\d{4,5})\b""")

    // 11. PLU / Código de artículo (ej. "PLU:24", "PLU 24", "COD:24", "ART: 24", "P0024")
    private val pluRegex = Regex("""(?:PLU|COD|ART|ITEM)[:;\s=]*#?(\d{1,5})\b""", RegexOption.IGNORE_CASE)
    private val pluPrefixSinglePRegex = Regex("""^P0*([1-9]\d{0,4})#?$""", RegexOption.IGNORE_CASE)

    /**
     * Construye cualquier trama oficial del protocolo Systel Cuora Max V7.0 / V6.2 / Cuora 2 V4.0:
     * [Dirección (1 byte: 0..99)] + [Función (1 byte)] + [Cadena de datos ASCII] + [Verificación XOR (1 byte)]
     */
    fun buildCuoraCommand(scaleAddress: Int, functionCode: Int, asciiPayload: String = ""): ByteArray {
        val addr = scaleAddress.coerceIn(0, 99).toByte()
        val func = (functionCode and 0xFF).toByte()
        val payloadBytes = asciiPayload.toByteArray(Charsets.ISO_8859_1)
        val frame = ByteArray(2 + payloadBytes.size + 1)
        frame[0] = addr
        frame[1] = func
        System.arraycopy(payloadBytes, 0, frame, 2, payloadBytes.size)
        var crc = (addr.toInt() and 0xFF) xor (func.toInt() and 0xFF)
        for (b in payloadBytes) {
            crc = crc xor (b.toInt() and 0xFF)
        }
        frame[frame.size - 1] = crc.toByte()
        return frame
    }

    /**
     * Construye la trama oficial de 3 bytes para Solicitud de Peso, Estabilidad y Tara (Función 1)
     * según el "Protocolo de comunicación CUORA MAX V7.0 / V6.2 / CUORA 2 V4.0" (Pág. 3 y 4):
     * [Dirección (1 byte: 0..99)] + [Función (1 byte: 0x01)] + [Verificación XOR (1 byte: Dirección xor Función)]
     */
    fun buildCuoraWeightRequest(scaleAddress: Int): ByteArray = buildCuoraCommand(scaleAddress, 1)

    /**
     * Construye la trama oficial de 3 bytes para Solicitud de Firma Digital (Función 2 = 0x02)
     */
    fun buildCuoraDigitalSignatureRequest(scaleAddress: Int): ByteArray = buildCuoraCommand(scaleAddress, 2)

    /**
     * Construye la trama oficial de 3 bytes para Envío de Lista de Sectores (Función 11 = 0x0B)
     */
    fun buildCuoraSectorsListRequest(scaleAddress: Int): ByteArray = buildCuoraCommand(scaleAddress, 11)

    /**
     * Construye la trama oficial de 3 bytes para Solicitud de Estado / PING (Función 23 = 0x17)
     */
    fun buildCuoraPingRequest(scaleAddress: Int): ByteArray = buildCuoraCommand(scaleAddress, 23)

    /**
     * Construye la trama oficial de 3 bytes para Solicitud de Listado de Todos los PLU Cargados (Función 31 = 0x1F)
     */
    fun buildCuoraPluListRequest(scaleAddress: Int): ByteArray = buildCuoraCommand(scaleAddress, 31)

    /**
     * Construye la trama oficial para Solicitud de un PLU Extendido desde la balanza a la PC (Función 62 = 0x3E)
     * Formato de datos: 6 números ASCII (000001 a 999999)
     */
    fun buildCuoraExtendedPluRequest(scaleAddress: Int, pluNumber: Int): ByteArray {
        val param = String.format(java.util.Locale.US, "%06d", pluNumber.coerceIn(1, 999999))
        return buildCuoraCommand(scaleAddress, 62, param)
    }

    /**
     * Construye la trama oficial para Solicitud de un PLU estándar desde la balanza a la PC (Función 3 = 0x03)
     * Usado en Cuora 2 V4.0 (4 dígitos: 0001..4000) o Cuora Max legado (6 dígitos)
     */
    fun buildCuoraLegacyPluRequest(scaleAddress: Int, pluNumber: Int, sixDigits: Boolean = false): ByteArray {
        val param = if (sixDigits) {
            String.format(java.util.Locale.US, "%06d", pluNumber.coerceIn(1, 999999))
        } else {
            String.format(java.util.Locale.US, "%04d", pluNumber.coerceIn(1, 9999))
        }
        return buildCuoraCommand(scaleAddress, 3, param)
    }

    /**
     * Extrae la cantidad de decimales de precio ('0' o '2') de la respuesta a Función 2 (Firma Digital)
     * Formato respuesta: [IP][0x02] FxxxxCxxxxxxSxxxxPxxxxxAxxxD[0|2] [CRC]
     */
    fun parseCuoraDigitalSignatureDecimals(bytes: ByteArray, length: Int = bytes.size): Int? {
        if (length < 6) return null
        val ascii = String(bytes, 0, length, Charsets.ISO_8859_1)
        val match = Regex("""F\d{4}C\d{6}S\d{4}P\d{5}A\d{3}D(\d)""").find(ascii)
            ?: Regex("""D([02])""").find(ascii)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    /**
     * Decodifica la respuesta de Función 11 (Lista de sectores/secciones separadas por ';'):
     * Formato respuesta: [IP][11] [01NOMBRE SECTOR     ;02OTRO SECTOR       ;] [CRC]
     */
    fun parseCuoraSectorsResponse(bytes: ByteArray, length: Int = bytes.size): Map<Int, String> {
        if (length < 4) return emptyMap()
        var start = 0
        var end = length
        if ((bytes[0].toInt() and 0xFF) in 1..99 && (bytes[1].toInt() and 0xFF) == 11) {
            start = 2
            end = (length - 1).coerceAtLeast(start)
        }
        val ascii = String(bytes, start, end - start, Charsets.ISO_8859_1)
        val map = mutableMapOf<Int, String>()
        ascii.split(";").forEach { token ->
            val clean = token.filter { it in ' '..'~' }
            if (clean.length >= 3) {
                val secId = clean.substring(0, 2).toIntOrNull()
                val secName = clean.substring(2).trim()
                if (secId != null && secId in 1..99 && secName.isNotBlank()) {
                    map[secId] = secName
                }
            }
        }
        return map
    }

    /**
     * Decodifica la respuesta de Función 31 (Listado de todos los PLUs cargados en memoria):
     * Formato respuesta oficial Systel: [IP][31] [6 dígitos Nro PLU][1 char Versión]... 'F' [CRC] (Cuora Max)
     * o [4 dígitos Nro PLU][1 char Versión]... 'F' [CRC] (Cuora 2)
     * Retorna lista de pares: (Número de PLU, esDe6Digitos)
     */
    fun parseCuoraPluListResponse(bytes: ByteArray, length: Int = bytes.size): List<Pair<Int, Boolean>> {
        if (length < 3) return emptyList()
        val ascii = String(bytes, 0, length, Charsets.ISO_8859_1)
        val seen = mutableSetOf<Int>()
        val result = mutableListOf<Pair<Int, Boolean>>()

        // Formato 1: Soporte con prefijo 'N' y 'V' (usado en emuladores o especificaciones documentadas)
        val regexNV = Regex("""N(\d{4,6})V\d""")
        regexNV.findAll(ascii).forEach { m ->
            val rawNum = m.groupValues[1]
            val pluNum = rawNum.toIntOrNull()
            if (pluNum != null && pluNum > 0 && seen.add(pluNum)) {
                result.add(pluNum to (rawNum.length == 6))
            }
        }
        if (result.isNotEmpty()) return result

        // Formato 2: Protocolo oficial en el cable Systel Cuora Max / Cuora 2
        // Trama: [Dirección][31][Payload...] 'F' [CRC]
        var startIdx = 0
        for (i in 0 until (length - 1)) {
            val addr = bytes[i].toInt() and 0xFF
            val fn = bytes[i + 1].toInt() and 0xFF
            if (addr in 0..99 && fn == 31) {
                startIdx = i + 2
                break
            }
        }

        val fIndex = ascii.lastIndexOf('F')
        val endIdx = if (fIndex in startIdx until length) fIndex else (length - 1).coerceAtLeast(startIdx)
        val payload = if (endIdx > startIdx) ascii.substring(startIdx, endIdx) else ""

        // En Cuora Max V7.0/V6.2: Bloques continuos de 7 bytes: 6 dígitos de número + 1 byte versión
        if (payload.length >= 7) {
            var i = 0
            var valid7Count = 0
            while (i + 7 <= payload.length) {
                val digits = payload.substring(i, i + 6)
                if (digits.all { it.isDigit() }) valid7Count++
                i += 7
            }
            if (valid7Count > 0) {
                i = 0
                while (i + 7 <= payload.length) {
                    val digits = payload.substring(i, i + 6)
                    val plu = digits.toIntOrNull()
                    if (plu != null && plu > 0 && seen.add(plu)) {
                        result.add(plu to true)
                    }
                    i += 7
                }
            }
        }

        // En Cuora 2 V4.0: Bloques continuos de 5 bytes: 4 dígitos de número + 1 byte versión
        if (result.isEmpty() && payload.length >= 5) {
            var i = 0
            while (i + 5 <= payload.length) {
                val digits = payload.substring(i, i + 4)
                val plu = digits.toIntOrNull()
                if (plu != null && plu > 0 && seen.add(plu)) {
                    result.add(plu to false)
                }
                i += 5
            }
        }

        // Formato 3: Fallback general por secuencias de números continuos
        if (result.isEmpty()) {
            val digitSeqRegex = Regex("""\b(\d{1,6})\b""")
            digitSeqRegex.findAll(payload).forEach { m ->
                val num = m.groupValues[1].toIntOrNull()
                if (num != null && num in 1..999999 && seen.add(num)) {
                    result.add(num to (m.groupValues[1].length > 4))
                }
            }
        }

        return result
    }

    /**
     * Decodifica la respuesta de Función 62 (PLU Extendido Cuora Max V7.0 / V6.2) o Función 3 (PLU Cuora 2 V4.0):
     * - Función 62 (V7.0): [6 dígitos Nro PLU][1 char Gestión][18 chars Nombre][10x6 dígitos Precios/Rangos][6 dígitos Código PLU][2 dígitos Sector][4 dígitos Vto][1 char Tipo Venta: P/U/E/C]...
     * - Función 62 (V6.2): [6 dígitos Nro PLU][1 char Gestión][18 chars Nombre][2x6 dígitos Precios][6 dígitos Código PLU][2 dígitos Sector][4 dígitos Vto][1 char Tipo Venta: P/U/E/C]...
     * - Función 3  (V4.0): [4 dígitos Nro PLU][1 char Versión][18 chars Nombre][2x6 dígitos Precios][5 dígitos Código PLU][2 dígitos Sección][4 dígitos Vto][1 char Tipo Venta: P/U]...
     */
    fun parseCuoraSinglePluResponse(
        bytes: ByteArray,
        length: Int = bytes.size,
        priceDecimals: Int? = null,
        sectorsMap: Map<Int, String> = emptyMap()
    ): ParsedPluPreview? {
        if (length < 25) return null

        // Localizar el inicio del contenido ASCII (omitir dirección y función 62 o 3 si vienen al frente)
        var start = 0
        for (i in 0 until (length - 1)) {
            val addr = bytes[i].toInt() and 0xFF
            val fn = bytes[i + 1].toInt() and 0xFF
            if (addr in 0..99 && (fn == 62 || fn == 3)) {
                start = i + 2
                break
            }
        }
        val end = (length - 1).coerceAtLeast(start)
        val ascii = String(bytes, start, end - start, Charsets.ISO_8859_1).trimStart { it < ' ' }

        // 1. Intentar formato Función 62 (6 dígitos de Nro PLU al inicio)
        val match62 = Regex("""(\d{6})([0-9A-Za-z])(.{18})(\d{6})""").find(ascii)
        if (match62 != null) {
            val pluNum = match62.groupValues[1].toIntOrNull() ?: return null
            val rawName = match62.groupValues[3].replace(Regex("[\\x00-\\x1F]"), " ").trim()
            val name = rawName.ifBlank { "PLU $pluNum" }
            val rawPrice1 = match62.groupValues[4].toLongOrNull() ?: 0L

            var pluCode = pluNum
            var sectorId = 1

            val matchStart = match62.range.first
            val relativeAscii = ascii.substring(matchStart)

            if (relativeAscii.length >= 98 && relativeAscii[97] in "PUECpuec") {
                // Cuora Max V7.0 (5 listas de precio + 5 rangos = 60 bytes desde offset 25 hasta 85)
                pluCode = relativeAscii.substring(85, 91).toIntOrNull()?.takeIf { it > 0 } ?: pluNum
                sectorId = relativeAscii.substring(91, 93).toIntOrNull()?.takeIf { it > 0 } ?: 1
            } else if (relativeAscii.length >= 50 && relativeAscii[49] in "PUECpuec") {
                // Cuora Max V6.2 (2 listas de precio = 12 bytes desde offset 25 hasta 37)
                pluCode = relativeAscii.substring(37, 43).toIntOrNull()?.takeIf { it > 0 } ?: pluNum
                sectorId = relativeAscii.substring(43, 45).toIntOrNull()?.takeIf { it > 0 } ?: 1
            }

            val price = when (priceDecimals) {
                2 -> rawPrice1 / 100.0
                0 -> rawPrice1.toDouble()
                else -> if (rawPrice1 >= 1000L) rawPrice1 / 100.0 else rawPrice1.toDouble()
            }
            val section = sectorsMap[sectorId]?.takeIf { it.isNotBlank() } ?: "General"
            return ParsedPluPreview(
                code = pluCode,
                name = name,
                price = price,
                section = section
            )
        }

        // 2. Intentar formato Función 3 (4 dígitos de Nro PLU al inicio - Cuora 2 V4.0)
        val match3 = Regex("""(\d{4})([0-9A-Za-z])(.{18})(\d{6})""").find(ascii)
        if (match3 != null) {
            val pluNum = match3.groupValues[1].toIntOrNull() ?: return null
            val rawName = match3.groupValues[3].replace(Regex("[\\x00-\\x1F]"), " ").trim()
            val name = rawName.ifBlank { "PLU $pluNum" }
            val rawPrice1 = match3.groupValues[4].toLongOrNull() ?: 0L

            val matchStart = match3.range.first
            val relativeAscii = ascii.substring(matchStart)

            val pluCode = if (relativeAscii.length >= 40) {
                relativeAscii.substring(35, 40).toIntOrNull()?.takeIf { it > 0 } ?: pluNum
            } else pluNum

            val sectorId = if (relativeAscii.length >= 42) {
                relativeAscii.substring(40, 42).toIntOrNull()?.takeIf { it > 0 } ?: 1
            } else 1

            val price = when (priceDecimals) {
                2 -> rawPrice1 / 100.0
                0 -> rawPrice1.toDouble()
                else -> if (rawPrice1 >= 1000L) rawPrice1 / 100.0 else rawPrice1.toDouble()
            }
            val section = sectorsMap[sectorId]?.takeIf { it.isNotBlank() } ?: "General"
            return ParsedPluPreview(
                code = pluCode,
                name = name,
                price = price,
                section = section
            )
        }

        return null
    }

    /**
     * Decodifica tramas binarias direccionadas del protocolo oficial Systel Cuora Max:
     * Estructura: [Dirección (1..99)] + [Función] + [Datos ASCII] + [CRC XOR (1 byte)]
     * - Respuesta Función 1 (16 bytes): [IP] [0x01] [Peso Neto 6 chars] ['e'/'i'] [Tara 6 chars] [CRC]
     * - Respuesta Error (5 bytes): [IP] [Función] ['E'] ['1'..'9'] [CRC]
     * - Respuesta ACK (6 bytes): [IP] [Función] ['A']['C']['K'] [CRC]
     */
    fun parseCuoraBinary(bytes: ByteArray, length: Int = bytes.size): CuoraBinaryParseResult? {
        if (length < 5) return null

        // 1. Buscar respuesta de Función 1 (Solicitud de peso, estabilidad y tara: IP + 0x01 + Datos + CRC)
        for (i in 0..(length - 9)) {
            val addr = bytes[i].toInt() and 0xFF
            val func = bytes[i + 1].toInt() and 0xFF
            if (addr in 1..99 && func == 1) {
                // Extraer ventana ASCII después de [IP, 0x01] (sin incluir el byte final de CRC si hay 16 bytes)
                val maxAsciiLen = (length - i - 3).coerceIn(7, 13)
                val asciiCandidate = String(bytes, i + 2, maxAsciiLen, Charsets.US_ASCII)
                val match = systelCuoraGramsStabilityRegex.find(asciiCandidate)
                if (match != null) {
                    val netGramsStr = match.groupValues[1].replace(" ", "")
                    val stabilityStr = match.groupValues[2].lowercase()
                    val tareGramsStr = match.groupValues[3]
                    val netGrams = netGramsStr.toDoubleOrNull()
                    if (netGrams != null) {
                        val isStable = (stabilityStr == "e")
                        val weightKg = if (netGrams >= 0.0) {
                            kotlin.math.round(netGrams) / 1000.0
                        } else {
                            0.0
                        }
                        val cleanDisplay = "${netGramsStr}${stabilityStr}${tareGramsStr}"
                        val negError = if (netGrams < 0.0) {
                            "PE3" to "⚠️ Peso negativo (${netGrams.toInt()}g). Presiona CERO o TARA en la báscula"
                        } else {
                            null to null
                        }
                        return CuoraBinaryParseResult(
                            scaleAddress = addr,
                            functionCode = 1,
                            parsedWeight = ParsedScaleWeight(
                                weight = weightKg,
                                isStable = isStable,
                                pluCode = null,
                                cleanFrame = cleanDisplay,
                                errorCode = negError.first,
                                errorMessage = negError.second
                            )
                        )
                    }
                }
            }
        }

        // 2. Buscar trama de Error oficial Cuora Max: [IP (1..99)] [Función] ['E'] ['1'..'9'] [CRC XOR]
        for (i in 0..(length - 5)) {
            val addr = bytes[i].toInt() and 0xFF
            val func = bytes[i + 1].toInt() and 0xFF
            val eChar = bytes[i + 2].toInt().toChar()
            val numChar = bytes[i + 3].toInt().toChar()
            val crc = bytes[i + 4]
            val expectedCrc = (bytes[i].toInt() xor bytes[i + 1].toInt() xor bytes[i + 2].toInt() xor bytes[i + 3].toInt()).toByte()
            if (addr in 1..99 && eChar == 'E' && numChar in '1'..'9' && crc == expectedCrc) {
                return CuoraBinaryParseResult(
                    scaleAddress = addr,
                    functionCode = func,
                    parsedWeight = null,
                    isAck = false,
                    protocolErrorCode = "E$numChar"
                )
            }
        }

        // 3. Buscar trama ACK oficial Cuora Max: [IP (1..99)] [Función] ['A']['C']['K'] [CRC XOR]
        if (length >= 6) {
            for (i in 0..(length - 6)) {
                val addr = bytes[i].toInt() and 0xFF
                val func = bytes[i + 1].toInt() and 0xFF
                if (addr in 1..99 &&
                    bytes[i + 2] == 'A'.code.toByte() &&
                    bytes[i + 3] == 'C'.code.toByte() &&
                    bytes[i + 4] == 'K'.code.toByte()
                ) {
                    return CuoraBinaryParseResult(
                        scaleAddress = addr,
                        functionCode = func,
                        parsedWeight = null,
                        isAck = true,
                        protocolErrorCode = null
                    )
                }
            }
        }

        return null
    }

    fun parse(rawFrame: String): ParsedScaleWeight? {
        val clean = rawFrame.filter { it in ' '..'~' }.trim()
        if (clean.isEmpty()) return null

        // 1. Detección de estabilidad general:
        val isUnstable = clean.contains("US", ignoreCase = true) ||
                         clean.contains("IN", ignoreCase = true) ||
                         clean.contains("PI", ignoreCase = true) ||
                         clean.contains("MOV", ignoreCase = true)
        var isStable = !isUnstable

        // 2. Extracción de PLU tolerante (Transmisión de ticket, Función 72 Cuora Max o selección en teclado de báscula)
        var pluCode = pluRegex.find(clean)?.groupValues?.get(1)?.toIntOrNull()
        if (pluCode == null) {
            pluCode = pluPrefixSinglePRegex.find(clean)?.groupValues?.get(1)?.toIntOrNull()
        }
        var cuoraTicketWeightKg: Double? = null
        if (clean.contains("^")) {
            // Formato Oficial Cuora Max Función 72: TicketId^Fecha^Hora^Vendedor^PLU^Sector^Unidades^Gramos^Drenado^ImporteF
            val caretParts = clean.trimEnd('F', 'f').split("^")
            if (caretParts.size >= 8) {
                val ticketPlu = caretParts[4].trim().toIntOrNull()
                val ticketGrams = caretParts[7].trim().toDoubleOrNull()
                if (ticketPlu != null && ticketPlu > 0) {
                    pluCode = ticketPlu
                }
                if (ticketGrams != null && ticketGrams > 0.0) {
                    cuoraTicketWeightKg = ticketGrams / 1000.0
                }
            }
        }
        if (pluCode == null && clean.contains(";")) {
            val parts = clean.replace("#", "").split(";").map { it.trim() }
            if (parts.size >= 2) {
                // Formato estándar ticket báscula: [Estación 1 o 2];[PLU];[Peso];[Precio] ej. "01;24;1.060;75.00"
                val p0 = parts[0].toIntOrNull()
                val p1 = parts[1].toIntOrNull()
                if (parts.size >= 3 && p0 in 1..9 && p1 != null && p1 in 1..9999 && !parts[1].contains(".") && !parts[1].contains(",")) {
                    pluCode = p1
                } else if (p0 != null && p0 in 1..9999 && !parts[0].contains(".") && !parts[0].contains(",")) {
                    pluCode = p0
                } else if (p1 != null && p1 in 1..9999 && !parts[1].contains(".") && !parts[1].contains(",")) {
                    pluCode = p1
                }
            }
        }

        // 3. Extracción de Peso tolerante (PRIORIDAD MÁXIMA ANTES DE ERRORES)
        var parsedWeight: Double? = cuoraTicketWeightKg

        // Prioridad 1A: Formato Oficial Systel Cuora Max / Cuora 2 en gramos + 'e'/'i' + tara (ej: "002000e000000", "000250i000000", "02000e00")
        if (!clean.contains(".") && !clean.contains(",")) {
            val cuoraMatch = systelCuoraGramsStabilityRegex.find(clean)
            if (cuoraMatch != null) {
                val grams = cuoraMatch.groupValues[1].replace(" ", "").toDoubleOrNull()
                val cond = cuoraMatch.groupValues[2].lowercase()
                if (grams != null) {
                    isStable = (cond == "e")
                    parsedWeight = if (grams >= 0.0) grams / 1000.0 else 0.0
                }
            }
        }

        // Prioridad 1B: Prefijo Systel Cuora P / PE / PI con decimal (ej: "P001.250#", "PE000.500#", "PE1.060#", "PE 1.060#", "PE01.060#")
        if (parsedWeight == null) {
            val systelDecMatch = systelPrefixDecimalRegex.find(clean)
            if (systelDecMatch != null) {
                val numStr = systelDecMatch.groupValues[1].replace(" ", "").replace(",", ".")
                parsedWeight = numStr.toDoubleOrNull()
            }
        }

        // Prioridad 2: Ticket delimitado por punto y coma (ej: "01;24;1.060;75.00#", "24;1.060;75.00")
        if (parsedWeight == null && clean.contains(";")) {
            val tokens = clean.replace("#", "").split(";").map { it.trim() }
            for (token in tokens) {
                if (token.contains(".") || token.contains(",")) {
                    val candidate = token.replace(",", ".").toDoubleOrNull()
                    if (candidate != null && candidate in 0.001..150.0) {
                        parsedWeight = candidate
                        break
                    }
                }
            }
        }

        // Prioridad 3: Etiquetado explícito de peso (ej: "PESO: 1.250kg", "NET: 2.300")
        if (parsedWeight == null) {
            val labeledMatch = labeledWeightRegex.find(clean)
            if (labeledMatch != null) {
                val numStr = labeledMatch.groupValues[1].replace(" ", "").replace(",", ".")
                parsedWeight = numStr.toDoubleOrNull()
            }
        }

        // Prioridad 4: Decimal con unidad kg explícita (ej: "+001.250kg", "ST,GS, 12.350 kg")
        if (parsedWeight == null) {
            val kgMatch = kgExplicitRegex.find(clean)
            if (kgMatch != null) {
                val numStr = kgMatch.groupValues[1].replace(" ", "").replace(",", ".")
                parsedWeight = numStr.toDoubleOrNull()
            }
        }

        // Prioridad 5: Decimal simple (ej: "1.250", "0.000", "02,450", "001.250#")
        if (parsedWeight == null) {
            val plainMatch = plainDecimalRegex.find(clean)
            if (plainMatch != null) {
                val numStr = plainMatch.groupValues[1].replace(" ", "").replace(",", ".")
                parsedWeight = numStr.toDoubleOrNull()
            }
        }

        // Prioridad 6: Prefijo Systel Cuora con decimales implícitos (ej: "P000500#", "PE001250#")
        if (parsedWeight == null) {
            val systelImpMatch = systelPrefixImpliedRegex.find(clean)
            if (systelImpMatch != null) {
                val numImp = systelImpMatch.groupValues[1].replace(" ", "").replace("+", "").toDoubleOrNull()
                if (numImp != null) {
                    parsedWeight = numImp / 1000.0
                }
            }
        }

        // Prioridad 7: Gramos (ej: "1250g", "500gr")
        if (parsedWeight == null) {
            val gMatch = gramsRegex.find(clean)
            if (gMatch != null) {
                val numGrams = gMatch.groupValues[1].replace(" ", "").toDoubleOrNull()
                if (numGrams != null) {
                    parsedWeight = numGrams / 1000.0
                }
            }
        }

        // Prioridad 8: Entero de 5-6 dígitos con 3 decimales implícitos (Systel Cuora / Toledo)
        if (parsedWeight == null) {
            val impMatch = impliedDecimalRegex.find(clean)
            if (impMatch != null) {
                val numImp = impMatch.groupValues[1].toDoubleOrNull()
                if (numImp != null) {
                    parsedWeight = numImp / 1000.0
                }
            }
        }

        // Prioridad 9: Entero con signo de 4-5 dígitos (ej: "+1250" -> 1.250 kg)
        if (parsedWeight == null) {
            val sMatch = signedIntRegex.find(clean)
            if (sMatch != null) {
                val sign = if (sMatch.groupValues[1] == "-") -1.0 else 1.0
                val rawVal = sMatch.groupValues[2].toDoubleOrNull()
                if (rawVal != null) {
                    parsedWeight = (rawVal / 1000.0) * sign
                }
            }
        }

        // Si se extrajo un peso válido, retornarlo directamente sin buscar errores
        if (parsedWeight != null) {
            val rounded = kotlin.math.max(0.0, kotlin.math.round(parsedWeight * 1000) / 1000.0)
            return ParsedScaleWeight(
                weight = rounded,
                isStable = isStable,
                pluCode = pluCode,
                cleanFrame = clean,
                errorCode = null,
                errorMessage = null
            )
        }

        // 4. Detección estricta de Códigos de Error / Advertencia Systel Cuora (ej: "PE4#", "PE4")
        // SOLO si NO contiene punto o coma decimal
        if (!clean.contains(".") && !clean.contains(",")) {
            val errMatch = systelErrorRegex.find(clean)
            if (errMatch != null) {
                val code = errMatch.groupValues[1].toIntOrNull() ?: 4
                val (errCode, errMsg) = when (code) {
                    1 -> "PE1" to "⚠️ Peso inestable / Movimiento en el plato"
                    2 -> "PE2" to "Báscula en cero (Plato vacío)"
                    3 -> "PE3" to "⚠️ Peso negativo (Presiona CERO o TARA en la báscula)"
                    4 -> "PE4" to "⚠️ Función no soportada o peso menor al mínimo (Min 0.1 kg)"
                    5 -> "PE5" to "⚠️ Sobrecarga en la báscula (> 30 kg)"
                    else -> "PE$code" to "⚠️ Mensaje de advertencia Systel PE$code"
                }
                return ParsedScaleWeight(
                    weight = 0.0,
                    isStable = (code != 1),
                    pluCode = pluCode,
                    cleanFrame = clean,
                    errorCode = errCode,
                    errorMessage = errMsg
                )
            }
        }

        return null
    }
}

object ScaleCatalogParser {
    /**
     * Analizador inteligente y tolerante de catálogos de PLUs para básculas Systel Cuora Max / Qendra / Itasa / Excel / CSV.
     * Soporta:
     * - Formatos con o sin encabezados (PLU, Código, Nombre, Descripción, Precio, Sección/Depto)
     * - Delimitadores: ';', ',', '\t', '|'
     * - Exportaciones oficiales de Qendra (ej. "Numero;Descripcion;Precio;Seccion...")
     * - Formatos simples tipo "1;Bistec de Res;185.00;Res" o "24, Manitas de Puerco, $75.00"
     */
    fun parse(rawText: String): List<ParsedPluPreview> {
        val list = mutableListOf<ParsedPluPreview>()
        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && !it.startsWith("//") }
        if (lines.isEmpty()) return emptyList()

        val firstLine = lines.first()
        val delimiter = when {
            firstLine.contains(";") -> ";"
            firstLine.contains("|") -> "|"
            firstLine.contains("\t") -> "\t"
            firstLine.contains(",") -> ","
            else -> ";"
        }

        var headerMap: Map<String, Int>? = null
        val firstTokens = firstLine.split(delimiter).map { it.trim().trim('"', '\'').lowercase() }
        val hasHeader = firstTokens.any {
            it.contains("plu") || it.contains("cod") || it.contains("art") ||
            it.contains("nom") || it.contains("desc") || it.contains("prec") || it.contains("item")
        }

        var startIdx = 0
        if (hasHeader) {
            val map = mutableMapOf<String, Int>()
            firstTokens.forEachIndexed { idx, h ->
                when {
                    (h.contains("plu") || h.contains("cod") || h == "id" || h == "num" || h == "numero") && !map.containsKey("code") -> map["code"] = idx
                    (h.contains("nom") || h.contains("desc") || h.contains("art") || h.contains("prod") || h.contains("detalle")) && !map.containsKey("name") -> map["name"] = idx
                    (h.contains("prec") || h.contains("price") || h.contains("cost") || h.contains("pvp") || h.contains("importe")) && !map.containsKey("price") -> map["price"] = idx
                    (h.contains("sec") || h.contains("dep") || h.contains("fam") || h.contains("cat") || h.contains("grupo")) && !map.containsKey("sec") -> map["sec"] = idx
                }
            }
            if (map.containsKey("code") && map.containsKey("name")) {
                headerMap = map
                startIdx = 1
            }
        }

        for (i in startIdx until lines.size) {
            val line = lines[i]
            val lineDelim = when {
                line.contains(";") -> ";"
                line.contains("|") -> "|"
                line.contains("\t") -> "\t"
                line.contains(",") -> ","
                else -> delimiter
            }
            val tokens = line.split(lineDelim).map { it.trim().trim('"', '\'') }
            if (tokens.size < 3) continue

            var code: Int? = null
            var name: String? = null
            var price: Double = 0.0
            var sec: String = "General"

            if (headerMap != null) {
                val codeIdx = headerMap["code"] ?: 0
                val nameIdx = headerMap["name"] ?: 1
                val priceIdx = headerMap["price"]
                val secIdx = headerMap["sec"]

                code = tokens.getOrNull(codeIdx)?.toIntOrNull()
                name = tokens.getOrNull(nameIdx)
                val priceStr = priceIdx?.let { tokens.getOrNull(it) }?.replace("$", "")?.replace(" ", "")?.replace(",", ".")
                price = priceStr?.toDoubleOrNull() ?: 0.0
                sec = secIdx?.let { tokens.getOrNull(it) }?.takeIf { it.isNotBlank() } ?: "General"
            } else {
                // Heurística automática sin encabezado
                // 1. Encontrar el índice del código numérico (entero entre 1 y 999999)
                val codeIndex = tokens.indexOfFirst { token ->
                    val num = token.toIntOrNull()
                    num != null && num in 1..999999
                }
                if (codeIndex != -1) {
                    code = tokens[codeIndex].toIntOrNull()
                    val otherTokens = tokens.filterIndexed { index, _ -> index != codeIndex }

                    // 2. Encontrar el precio (número con punto/coma o signo $)
                    val priceIndex = otherTokens.indexOfFirst { t ->
                        val cleanNum = t.replace("$", "").replace(" ", "").replace(",", ".")
                        cleanNum.toDoubleOrNull() != null && (t.contains(".") || t.contains(",") || t.contains("$"))
                    }

                    if (priceIndex != -1) {
                        price = otherTokens[priceIndex].replace("$", "").replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
                        val nameTokens = otherTokens.filterIndexed { idx, _ -> idx != priceIndex }
                        name = nameTokens.firstOrNull { it.isNotBlank() }
                        sec = nameTokens.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "General"
                    } else {
                        // Si no hay decimal explícito pero hay al menos 2 tokens restantes
                        name = otherTokens.firstOrNull { it.isNotBlank() }
                        val possiblePrice = otherTokens.getOrNull(1)?.replace("$", "")?.replace(" ", "")?.replace(",", ".")?.toDoubleOrNull()
                        if (possiblePrice != null) {
                            price = possiblePrice
                            sec = otherTokens.getOrNull(2)?.takeIf { it.isNotBlank() } ?: "General"
                        } else {
                            sec = otherTokens.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "General"
                        }
                    }
                }
            }

            if (code != null && !name.isNullOrBlank()) {
                list.add(ParsedPluPreview(code, name, price, sec))
            }
        }
        return list
    }
}

class TcpScaleManager(
    val context: Context? = null
) {
    companion object {
        @Volatile
        private var INSTANCE: TcpScaleManager? = null

        fun getInstance(context: Context?): TcpScaleManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TcpScaleManager(context?.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences("sgnia_scales_config", Context.MODE_PRIVATE)

    // Lista de todas las básculas configuradas en red (soporta de 1 a 15+ básculas en paralelo)
    private val _scalesList = MutableStateFlow<List<TcpScaleDevice>>(emptyList())
    val scalesList: StateFlow<List<TcpScaleDevice>> = _scalesList.asStateFlow()

    // Estado general del visor (muestra el pesaje de la última báscula activa en la que se colocó producto)
    private val _scaleState = MutableStateFlow(
        ScaleState(
            isConnected = false,
            deviceName = "Red de Básculas TCP/IP",
            currentWeight = 0.000,
            isStable = true,
            activeScaleId = 1
        )
    )
    val scaleState: StateFlow<ScaleState> = _scaleState.asStateFlow()

    // Compatibilidad retroactiva para vistas existentes que observan scale1 y scale2
    private val _scale1Config = MutableStateFlow(
        TcpScaleDevice(1, "Báscula 1 (SGNIA1)", "192.168.100.40", 10001)
    )
    val scale1Config: StateFlow<TcpScaleDevice> = _scale1Config.asStateFlow()

    private val _scale2Config = MutableStateFlow(
        TcpScaleDevice(2, "Báscula 2", "192.168.100.50", 10001)
    )
    val scale2Config: StateFlow<TcpScaleDevice> = _scale2Config.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val scaleJobs = ConcurrentHashMap<Int, Job>()
    private val scaleSockets = ConcurrentHashMap<Int, Socket>()
    private var isManualWeightActive = false

    fun stopScaleWorker(scaleId: Int) {
        scaleJobs[scaleId]?.cancel()
        scaleJobs.remove(scaleId)
        scaleSockets[scaleId]?.let { sock ->
            try { sock.close() } catch (_: Exception) {}
        }
        scaleSockets.remove(scaleId)
    }

    init {
        loadScalesFromPrefs()
        startAllScaleListeners()
    }

    // --- CARGA Y PERSISTENCIA DE BÁSCULAS (JSON EN SHARED PREFERENCES) ---
    private fun loadScalesFromPrefs() {
        val jsonString = prefs?.getString("scales_json_list", null)
        val loaded = mutableListOf<TcpScaleDevice>()

        if (!jsonString.isNullOrBlank()) {
            try {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    loaded.add(
                        TcpScaleDevice(
                            id = obj.getInt("id"),
                            name = obj.getString("name"),
                            ip = obj.getString("ip"),
                            port = obj.optInt("port", 10001),
                            brand = obj.optString("brand", "Systel"),
                            model = obj.optString("model", "Cuora Max"),
                            protocol = obj.optString("protocol", "AUTO"),
                            isConnected = false,
                            statusDescription = "Iniciando..."
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Auto-migración si existen configuraciones viejas o vacías (.50/.51 en lugar de .40/.50)
        val needsMigration = loaded.isEmpty() || (
            loaded.size >= 2 && (
                (loaded[0].ip == "192.168.100.50" && loaded[1].ip == "192.168.100.51") ||
                (loaded[0].ip == "192.168.100.51") ||
                (loaded[0].ip == "192.168.100.50" && loaded.none { it.ip == "192.168.100.40" })
            )
        )

        if (needsMigration) {
            loaded.clear()
            loaded.add(TcpScaleDevice(1, "Báscula 1 (SGNIA1)", "192.168.100.40", 10001))
            loaded.add(TcpScaleDevice(2, "Báscula 2", "192.168.100.50", 10001))
        }

        _scalesList.value = loaded
        syncLegacyConfigs()
        saveScalesToPrefs()
    }

    fun resetToOfficialScaleIps() {
        val resetList = listOf(
            TcpScaleDevice(1, "Báscula 1 (SGNIA1)", "192.168.100.40", 10001),
            TcpScaleDevice(2, "Báscula 2", "192.168.100.50", 10001)
        )
        _scalesList.value = resetList
        saveScalesToPrefs()
        startAllScaleListeners()
    }

    private fun saveScalesToPrefs() {
        try {
            val jsonArray = JSONArray()
            _scalesList.value.forEach { scale ->
                val obj = JSONObject().apply {
                    put("id", scale.id)
                    put("name", scale.name)
                    put("ip", scale.ip)
                    put("port", scale.port)
                    put("brand", scale.brand)
                    put("model", scale.model)
                    put("protocol", scale.protocol)
                }
                jsonArray.put(obj)
            }
            prefs?.edit()?.putString("scales_json_list", jsonArray.toString())?.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        syncLegacyConfigs()
    }

    private fun syncLegacyConfigs() {
        val list = _scalesList.value
        val first = list.getOrNull(0) ?: TcpScaleDevice(1, "Báscula 1 (SGNIA1)", "192.168.100.40", 10001)
        val second = list.getOrNull(1) ?: TcpScaleDevice(2, "Báscula 2", "192.168.100.50", 10001)
        _scale1Config.value = first
        _scale2Config.value = second
    }

    // --- ADMINISTRACIÓN DINÁMICA DE BÁSCULAS (+) ---
    fun addScale(
        name: String,
        ip: String,
        port: Int,
        brand: String = "Systel",
        model: String = "Cuora Max",
        protocol: String = "AUTO"
    ): TcpScaleDevice {
        val cleanName = name.trim().ifEmpty { "Báscula ${_scalesList.value.size + 1}" }
        val cleanIp = ip.trim().ifEmpty { "192.168.100.50" }
        val cleanPort = if (port in 1..65535) port else 10001
        val nextId = (_scalesList.value.maxOfOrNull { it.id } ?: 0) + 1

        val newScale = TcpScaleDevice(
            id = nextId,
            name = cleanName,
            ip = cleanIp,
            port = cleanPort,
            brand = brand.trim().ifEmpty { "Systel" },
            model = model.trim().ifEmpty { "Cuora Max" },
            protocol = protocol.trim().ifEmpty { "AUTO" },
            isConnected = false,
            statusDescription = "Conectando..."
        )

        _scalesList.value = _scalesList.value + newScale
        saveScalesToPrefs()
        startScaleWorker(newScale.id)
        return newScale
    }

    fun updateScale(scale: TcpScaleDevice) {
        val updatedList = _scalesList.value.map {
            if (it.id == scale.id) scale.copy(
                name = scale.name.trim().ifEmpty { "Báscula ${scale.id}" },
                ip = scale.ip.trim(),
                port = if (scale.port in 1..65535) scale.port else 10001,
                brand = scale.brand.trim().ifEmpty { "Systel" },
                model = scale.model.trim().ifEmpty { "Cuora Max" },
                protocol = scale.protocol.trim().ifEmpty { "AUTO" }
            ) else it
        }
        _scalesList.value = updatedList
        saveScalesToPrefs()
        startScaleWorker(scale.id)
    }

    fun deleteScale(scaleId: Int) {
        stopScaleWorker(scaleId)
        _scalesList.value = _scalesList.value.filter { it.id != scaleId }
        saveScalesToPrefs()
    }

    fun updateScaleConfigs(scale1Ip: String, scale1Port: Int, scale2Ip: String, scale2Port: Int) {
        val currentList = _scalesList.value.toMutableList()
        if (currentList.isNotEmpty()) {
            currentList[0] = currentList[0].copy(ip = scale1Ip.trim(), port = scale1Port)
        }
        if (currentList.size > 1) {
            currentList[1] = currentList[1].copy(ip = scale2Ip.trim(), port = scale2Port)
        }
        _scalesList.value = currentList
        saveScalesToPrefs()
        startAllScaleListeners()
    }

    // --- SELECCIONAR BÁSCULA ACTIVA ---
    fun selectActiveScale(scaleId: Int) {
        val target = _scalesList.value.find { it.id == scaleId }
        _scaleState.value = _scaleState.value.copy(
            activeScaleId = scaleId,
            deviceName = target?.name ?: "Báscula $scaleId",
            currentWeight = target?.lastWeight ?: 0.000,
            isStable = target?.isStable ?: true,
            lastPluCode = target?.lastPluCode ?: _scaleState.value.lastPluCode,
            lastRawFrame = target?.lastRawFrame ?: "",
            lastRawHex = target?.lastRawHex ?: "",
            totalBytesReceived = target?.totalBytesReceived ?: 0L,
            communicationMode = target?.communicationMode ?: "Desconectado",
            lastErrorCode = target?.lastErrorCode,
            lastErrorMessage = target?.lastErrorMessage
        )
    }

    // --- ESCUCHA SIMULTÁNEA EN PARALELO DE TODAS LAS BÁSCULAS ---
    fun startAllScaleListeners() {
        val activeIds = _scalesList.value.map { it.id }.toSet()
        scaleJobs.keys.toList().forEach { id ->
            if (id !in activeIds) {
                stopScaleWorker(id)
            }
        }

        _scalesList.value.forEach { scale ->
            if (scaleJobs[scale.id]?.isActive != true) {
                startScaleWorker(scale.id)
            }
        }
    }

    private fun buildProbeCommandsForScale(scale: TcpScaleDevice): List<Pair<String, ByteArray>> {
        val commands = mutableListOf<Pair<String, ByteArray>>()
        val addedAddresses = mutableSetOf<Int>()

        fun addCuoraAddress(addr: Int) {
            if (addr in 0..99 && addedAddresses.add(addr)) {
                commands.add("CUORA MAX ID:$addr" to ScaleFrameParser.buildCuoraWeightRequest(addr))
            }
        }

        val isTorrey = scale.brand.equals("Torrey", ignoreCase = true) || scale.protocol.contains("TORREY", ignoreCase = true)
        val isRhino = scale.brand.equals("Rhino", ignoreCase = true) || scale.protocol.contains("RHINO", ignoreCase = true)
        val isToledo = scale.brand.contains("Toledo", ignoreCase = true) || scale.protocol.contains("TOLEDO", ignoreCase = true)

        if (isTorrey) {
            commands.add("Torrey 'W' (0x57)" to byteArrayOf(0x57))
            commands.add("Torrey 'W\\r\\n'" to "W\r\n".toByteArray(Charsets.US_ASCII))
            commands.add("Torrey 'P\\r\\n'" to "P\r\n".toByteArray(Charsets.US_ASCII))
            commands.add("Torrey ENQ (0x05)" to byteArrayOf(0x05))
        } else if (isToledo) {
            commands.add("Toledo MT-SICS 'S\\r\\n'" to "S\r\n".toByteArray(Charsets.US_ASCII))
            commands.add("Toledo 'SI\\r\\n'" to "SI\r\n".toByteArray(Charsets.US_ASCII))
            commands.add("Toledo 'P\\r\\n'" to "P\r\n".toByteArray(Charsets.US_ASCII))
        } else if (isRhino) {
            commands.add("Rhino 'P\\r\\n'" to "P\r\n".toByteArray(Charsets.US_ASCII))
            commands.add("Rhino 'W\\r\\n'" to "W\r\n".toByteArray(Charsets.US_ASCII))
        }

        // 1. Dirección Cuora Max previamente descubierta y guardada para esta IP (conexión instantánea)
        val cachedAddr = prefs?.getInt("cuora_addr_${scale.ip}", -1) ?: -1
        if (cachedAddr in 1..99) {
            addCuoraAddress(cachedAddr)
        }

        // 2. Direcciones prioritarias Cuora Max (1, 2, ID de lista, último octeto de IP ej. 40/50, dígito de octeto ej. 4/5, 3..15, 20)
        addCuoraAddress(1)
        addCuoraAddress(2)
        addCuoraAddress(scale.id)

        val lastOctet = scale.ip.substringAfterLast(".").trim().toIntOrNull()
        if (lastOctet != null) {
            if (lastOctet in 1..99) addCuoraAddress(lastOctet)
            if ((lastOctet / 10) in 1..9) addCuoraAddress(lastOctet / 10)
            if ((lastOctet % 10) in 1..9) addCuoraAddress(lastOctet % 10)
        }

        for (priorityId in listOf(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 20, 0)) {
            addCuoraAddress(priorityId)
        }

        // 3. Comandos oficiales Systel RS-232 (Protocolos 0 a 4 de Soft VB Systel) y universales
        commands.add("Systel P1 (0x07 0x07)" to byteArrayOf(0x07, 0x07))
        commands.add("Systel P2 (0x07)" to byteArrayOf(0x07))
        commands.add("Systel P0 (ENQ 0x05)" to byteArrayOf(0x05))
        commands.add("Systel P3 (P)" to "P".toByteArray(Charsets.US_ASCII))
        commands.add("Systel P4 (W)" to "W".toByteArray(Charsets.US_ASCII))
        commands.add("P\\r\\n" to "P\r\n".toByteArray(Charsets.US_ASCII))
        commands.add("W\\r\\n" to "W\r\n".toByteArray(Charsets.US_ASCII))

        // 4. Barrido completo de todas las demás direcciones posibles Cuora Max (1..99)
        for (addr in 1..99) {
            addCuoraAddress(addr)
        }

        return commands
    }

    private fun startScaleWorker(scaleId: Int) {
        stopScaleWorker(scaleId)
        scaleJobs[scaleId] = coroutineScope.launch {
            while (isActive) {
                val scale = _scalesList.value.find { it.id == scaleId } ?: break
                val probeCommands = buildProbeCommandsForScale(scale)
                var socket: Socket? = null

                try {
                    val sock = Socket()
                    sock.tcpNoDelay = true
                    sock.connect(InetSocketAddress(scale.ip, scale.port), 2500)
                    // Timeout de lectura ágil (200ms) para barrido rápido de IDs y lectura en tiempo real
                    sock.soTimeout = 200
                    socket = sock
                    scaleSockets[scaleId] = sock

                    updateScaleDeviceStatus(scaleId, true, "En línea (TCP)", null)
                    updateScaleDeviceMode(scaleId, " Enlazando protocolo Cuora Max...")

                    val inputStream = sock.getInputStream()
                    val outputStream = sock.getOutputStream()

                    val buffer = ByteArray(512)
                    val frameAccumulator = java.io.ByteArrayOutputStream()
                    val binaryAccumulator = java.io.ByteArrayOutputStream()
                    var totalBytes = 0L

                    // ==============================================================
                    // FASE 1: ESCUCHA PASIVA RÁPIDA (400ms) PARA FLUJO CONTINUO
                    // ==============================================================
                    var isContinuousStream = false
                    val passiveListenStart = System.currentTimeMillis()

                    while (isActive && sock.isConnected && !sock.isClosed && (System.currentTimeMillis() - passiveListenStart < 400)) {
                        try {
                            val bytesRead = inputStream.read(buffer)
                            if (bytesRead == -1) break
                            if (bytesRead > 0) {
                                totalBytes += bytesRead
                                isContinuousStream = true
                                updateScaleDeviceMode(scaleId, "CONTINUO (PASIVO)")
                                processIncomingBytes(buffer, bytesRead, frameAccumulator, binaryAccumulator, scaleId, scale.ip, totalBytes)
                                break
                            }
                        } catch (_: SocketTimeoutException) {
                            // Continúa a fase de sondeo activo tras 400ms
                        }
                    }

                    // ==============================================================
                    // FASE 2: BUCLE DE RECEPCIÓN Y SONDEO OFICIAL CUORA MAX (FUNCIÓN 1)
                    // ==============================================================
                    var activeProbeIndex = 0
                    var lockedCommand: Pair<String, ByteArray>? = null
                    var lastPollTime = 0L
                    var quietTimeouts = 0

                    while (isActive && sock.isConnected && !sock.isClosed) {
                        val now = System.currentTimeMillis()
                        val pollIntervalMs = if (lockedCommand != null) 250L else 200L

                        // Si NO es modo pasivo y toca enviar solicitud de peso:
                        if (!isContinuousStream && (now - lastPollTime >= pollIntervalMs)) {
                            lastPollTime = now
                            try {
                                val currentCmd = if (lockedCommand != null) {
                                    lockedCommand!!
                                } else {
                                    val candidate = probeCommands[activeProbeIndex]
                                    activeProbeIndex = (activeProbeIndex + 1) % probeCommands.size
                                    updateScaleDeviceMode(scaleId, "Buscando [${candidate.first}]")
                                    candidate
                                }

                                outputStream.write(currentCmd.second)
                                outputStream.flush()
                            } catch (e: Exception) {
                                break // Conexión cerrada o error de I/O
                            }
                        }

                        // LECTURA DE RESPUESTA DEL SOCKET
                        try {
                            val bytesRead = inputStream.read(buffer)
                            if (bytesRead == -1) break

                            if (bytesRead > 0) {
                                totalBytes += bytesRead
                                quietTimeouts = 0

                                val hexString = buffer.take(bytesRead.coerceAtMost(16)).joinToString(" ") { String.format("%02X", it) }
                                updateScaleDeviceTelemetry(scaleId, totalBytes, hexString)

                                // 1. Intentar decodificación binaria directa protocolo oficial Systel Cuora Max
                                binaryAccumulator.write(buffer, 0, bytesRead)
                                val accBytes = binaryAccumulator.toByteArray()
                                val cuoraResult = ScaleFrameParser.parseCuoraBinary(accBytes, accBytes.size)

                                if (cuoraResult != null) {
                                    binaryAccumulator.reset()
                                    frameAccumulator.reset()

                                    val discoveredAddr = cuoraResult.scaleAddress
                                    // Guardar dirección descubierta para esta IP y fijar Función 1 (Solicitud de peso)
                                    prefs?.edit()?.putInt("cuora_addr_${scale.ip}", discoveredAddr)?.apply()
                                    val officialWeightCmd = ScaleFrameParser.buildCuoraWeightRequest(discoveredAddr)
                                    lockedCommand = "CUORA MAX [ID:$discoveredAddr]" to officialWeightCmd
                                    updateScaleDeviceMode(scaleId, "CUORA MAX [ID:$discoveredAddr]")

                                    if (cuoraResult.parsedWeight != null) {
                                        applyParsedWeight(cuoraResult.parsedWeight, scaleId)
                                    } else if (cuoraResult.functionCode != 1) {
                                        // Si respondió ACK o Error a otra función, pedir de inmediato Función 1 (Peso)
                                        try {
                                            outputStream.write(officialWeightCmd)
                                            outputStream.flush()
                                        } catch (_: Exception) {}
                                    }
                                } else {
                                    // Evitar crecimiento indefinido del acumulador binario si no es trama Cuora
                                    if (binaryAccumulator.size() > 64) {
                                        binaryAccumulator.reset()
                                    }

                                    // 2. Si la báscula respondió ACK (0x06), enviar ENQ (0x05)
                                    val hasAckByte = (0 until bytesRead).any { buffer[it] == 0x06.toByte() }
                                    if (hasAckByte) {
                                        try {
                                            outputStream.write(byteArrayOf(0x05))
                                            outputStream.flush()
                                        } catch (_: Exception) {}
                                    }

                                    val parsedResult = processIncomingBytes(
                                        buffer,
                                        bytesRead,
                                        frameAccumulator,
                                        binaryAccumulator,
                                        scaleId,
                                        scale.ip,
                                        totalBytes
                                    )

                                    // Fijar comando si devolvió una trama de peso válida (incluso 0.000 kg en plato vacío)
                                    if (parsedResult != null && parsedResult.errorCode == null && lockedCommand == null) {
                                        val prevIndex = (activeProbeIndex - 1 + probeCommands.size) % probeCommands.size
                                        val locked = probeCommands[prevIndex]
                                        lockedCommand = locked
                                        updateScaleDeviceMode(scaleId, "ENLACE FIJADO [${locked.first}]")
                                    }
                                }
                            }
                        } catch (e: SocketTimeoutException) {
                            quietTimeouts++
                            // Si tras un timeout quedó una trama pendiente en el acumulador (fin de ráfaga por silencio >= 5ms)
                            if (binaryAccumulator.size() >= 5) {
                                val accBytes = binaryAccumulator.toByteArray()
                                val cuoraResult = ScaleFrameParser.parseCuoraBinary(accBytes, accBytes.size)
                                if (cuoraResult != null) {
                                    val discoveredAddr = cuoraResult.scaleAddress
                                    prefs?.edit()?.putInt("cuora_addr_${scale.ip}", discoveredAddr)?.apply()
                                    lockedCommand = "CUORA MAX [ID:$discoveredAddr]" to ScaleFrameParser.buildCuoraWeightRequest(discoveredAddr)
                                    updateScaleDeviceMode(scaleId, "CUORA MAX [ID:$discoveredAddr]")
                                    if (cuoraResult.parsedWeight != null) {
                                        applyParsedWeight(cuoraResult.parsedWeight, scaleId)
                                    }
                                }
                                binaryAccumulator.reset()
                            }

                            if (frameAccumulator.size() >= 3) {
                                val rawFrame = frameAccumulator.toString("US-ASCII")
                                val p = parseWeightFrame(rawFrame, scaleId)
                                if (p != null && p.errorCode == null && lockedCommand == null) {
                                    val prevIndex = (activeProbeIndex - 1 + probeCommands.size) % probeCommands.size
                                    val locked = probeCommands[prevIndex]
                                    lockedCommand = locked
                                    updateScaleDeviceMode(scaleId, "ENLACE FIJADO [${locked.first}]")
                                }
                                frameAccumulator.reset()
                            }

                            // Si pasan 16 timeouts (~3.5 segundos) sin respuesta en un comando fijado, desbloquear para re-escanear
                            if (lockedCommand != null && quietTimeouts >= 16) {
                                lockedCommand = null
                                quietTimeouts = 0
                            }
                        }
                    }
                } catch (e: ConnectException) {
                    updateScaleDeviceStatus(
                        scaleId,
                        false,
                        "Rechazado (ECONNREFUSED). Cierra Qendra en la PC si está abierto (Lantronix es mono-cliente)",
                        null
                    )
                    updateScaleDeviceMode(scaleId, "ECONNREFUSED (Ocupado por PC)")
                } catch (e: SocketTimeoutException) {
                    updateScaleDeviceStatus(scaleId, false, "Tiempo agotado (Báscula apagada o fuera de red)", null)
                    updateScaleDeviceMode(scaleId, "Timeout")
                } catch (e: Exception) {
                    updateScaleDeviceStatus(scaleId, false, "Sin conexión: ${e.localizedMessage ?: "Desconectada"}", null)
                    updateScaleDeviceMode(scaleId, "Error de red")
                } finally {
                    scaleSockets.remove(scaleId)
                    try { socket?.close() } catch (_: Exception) {}
                }

                delay(2000)
            }
        }
    }

    private fun processIncomingBytes(
        buffer: ByteArray,
        bytesRead: Int,
        frameAccumulator: java.io.ByteArrayOutputStream,
        binaryAccumulator: java.io.ByteArrayOutputStream? = null,
        scaleId: Int,
        scaleIp: String = "",
        totalBytes: Long
    ): ParsedScaleWeight? {
        // 1. Verificar primero si el buffer contiene una trama binaria direccionada Cuora Max
        val cuoraDirect = ScaleFrameParser.parseCuoraBinary(buffer, bytesRead)
        if (cuoraDirect != null) {
            if (scaleIp.isNotBlank()) {
                prefs?.edit()?.putInt("cuora_addr_$scaleIp", cuoraDirect.scaleAddress)?.apply()
            }
            binaryAccumulator?.reset()
            frameAccumulator.reset()
            if (cuoraDirect.parsedWeight != null) {
                return applyParsedWeight(cuoraDirect.parsedWeight, scaleId)
            }
            return null
        }

        var lastParsed: ParsedScaleWeight? = null

        // 2. Procesar bytes ASCII / RS-232 buscando delimitadores
        for (i in 0 until bytesRead) {
            val b = buffer[i]
            val isDelimiter = b == 0x03.toByte() || b == 0x0A.toByte() ||
                              b == 0x0D.toByte() || b == 0x00.toByte() ||
                              b == 0x04.toByte() || b == '#'.code.toByte() ||
                              b == 0x1C.toByte() || b == 0x1D.toByte() ||
                              b == 0x1E.toByte() || b == 0x1F.toByte()

            if (isDelimiter) {
                if (b == '#'.code.toByte()) {
                    frameAccumulator.write(b.toInt())
                }
                if (frameAccumulator.size() > 0) {
                    val rawFrame = frameAccumulator.toString("US-ASCII")
                    val p = parseWeightFrame(rawFrame, scaleId)
                    if (p != null) lastParsed = p
                    frameAccumulator.reset()
                }
            } else if (b == 0x02.toByte()) {
                // STX (Start of Text): procesar acumulado previo si existe
                if (frameAccumulator.size() >= 3) {
                    val rawFrame = frameAccumulator.toString("US-ASCII")
                    val p = parseWeightFrame(rawFrame, scaleId)
                    if (p != null) lastParsed = p
                }
                frameAccumulator.reset()
            } else if (b in 32..126 || b == 9.toByte()) {
                frameAccumulator.write(b.toInt())
                // Si la trama alcanza formato Systel de 13 bytes ("002000e000000") o >= 16 bytes sin delimitador
                if (frameAccumulator.size() >= 13) {
                    val currentStr = frameAccumulator.toString("US-ASCII")
                    if (currentStr.length >= 18 || currentStr.contains('e', ignoreCase = true) || currentStr.contains('i', ignoreCase = true)) {
                        val p = parseWeightFrame(currentStr, scaleId)
                        if (p != null) {
                            lastParsed = p
                            frameAccumulator.reset()
                        }
                    }
                }
            }
        }
        return lastParsed
    }

    private fun updateScaleDeviceStatus(scaleId: Int, isConnected: Boolean, status: String, latency: Long?) {
        _scalesList.value = _scalesList.value.map { dev ->
            if (dev.id == scaleId) {
                dev.copy(isConnected = isConnected, statusDescription = status, latencyMs = latency)
            } else dev
        }
        syncLegacyConfigs()

        val anyConnected = _scalesList.value.any { it.isConnected }
        _scaleState.value = _scaleState.value.copy(isConnected = anyConnected)
    }

    private fun updateScaleDeviceTelemetry(scaleId: Int, totalBytes: Long, hexString: String) {
        _scalesList.value = _scalesList.value.map { dev ->
            if (dev.id == scaleId) dev.copy(
                totalBytesReceived = totalBytes,
                lastRawHex = hexString
            ) else dev
        }
        syncLegacyConfigs()
        if (scaleId == _scaleState.value.activeScaleId) {
            _scaleState.value = _scaleState.value.copy(
                totalBytesReceived = totalBytes,
                lastRawHex = hexString
            )
        }
    }

    private fun updateScaleDeviceMode(scaleId: Int, mode: String) {
        _scalesList.value = _scalesList.value.map { dev ->
            if (dev.id == scaleId) dev.copy(communicationMode = mode) else dev
        }
        syncLegacyConfigs()
        if (scaleId == _scaleState.value.activeScaleId) {
            _scaleState.value = _scaleState.value.copy(communicationMode = mode)
        }
    }

    private fun updateScaleDeviceRawFrame(scaleId: Int, raw: String) {
        _scalesList.value = _scalesList.value.map { dev ->
            if (dev.id == scaleId) dev.copy(lastRawFrame = raw) else dev
        }
        syncLegacyConfigs()
        if (scaleId == _scaleState.value.activeScaleId) {
            _scaleState.value = _scaleState.value.copy(lastRawFrame = raw)
        }
    }

    private fun updateScaleDeviceWeight(
        scaleId: Int,
        weight: Double,
        isStable: Boolean,
        pluCode: Int?,
        raw: String = "",
        errorCode: String? = null,
        errorMessage: String? = null
    ) {
        _scalesList.value = _scalesList.value.map { dev ->
            if (dev.id == scaleId) dev.copy(
                lastWeight = weight,
                isStable = isStable,
                lastPluCode = pluCode ?: dev.lastPluCode,
                lastRawFrame = if (raw.isNotBlank()) raw else dev.lastRawFrame,
                lastErrorCode = errorCode,
                lastErrorMessage = errorMessage
            ) else dev
        }
        syncLegacyConfigs()
    }

    private fun applyParsedWeight(parsed: ParsedScaleWeight, sourceScaleId: Int): ParsedScaleWeight {
        updateScaleDeviceRawFrame(sourceScaleId, parsed.cleanFrame)
        val sourceScale = _scalesList.value.find { it.id == sourceScaleId }
        val scaleName = sourceScale?.name ?: "Báscula $sourceScaleId"

        // 1. Actualizar el estado de ESTA báscula en la lista (siempre en paralelo sin interferencia)
        updateScaleDeviceWeight(
            sourceScaleId,
            parsed.weight,
            parsed.isStable,
            parsed.pluCode,
            parsed.cleanFrame,
            parsed.errorCode,
            parsed.errorMessage
        )

        // 2. Gestionar el visor principal:
        val isCurrentlySelected = sourceScaleId == _scaleState.value.activeScaleId
        val isCurrentIdle = _scaleState.value.currentWeight <= 0.010
        val hasNewWeight = parsed.weight > 0.020

        // Si es la báscula seleccionada, o si la seleccionada está vacía (<= 10g) y en la otra colocan producto (> 20g): enfoque automático
        if (isCurrentlySelected || (isCurrentIdle && hasNewWeight)) {
            val finalWeight = if (isManualWeightActive && parsed.weight == 0.0) {
                _scaleState.value.currentWeight
            } else {
                if (parsed.weight > 0.020) isManualWeightActive = false
                parsed.weight
            }

            _scaleState.value = _scaleState.value.copy(
                currentWeight = finalWeight,
                isStable = parsed.isStable,
                isConnected = true,
                deviceName = scaleName,
                activeScaleId = sourceScaleId,
                lastPluCode = parsed.pluCode ?: _scaleState.value.lastPluCode,
                lastRawFrame = parsed.cleanFrame,
                totalBytesReceived = sourceScale?.totalBytesReceived ?: _scaleState.value.totalBytesReceived,
                lastRawHex = sourceScale?.lastRawHex ?: _scaleState.value.lastRawHex,
                communicationMode = sourceScale?.communicationMode ?: _scaleState.value.communicationMode,
                lastErrorCode = parsed.errorCode,
                lastErrorMessage = parsed.errorMessage
            )
        }
        return parsed
    }

    // --- PROCESADOR DE TRAMA DE PESO INDUSTRIAL TOLERANTE (SYSTEL / TOLEDO / DIBAL) ---
    internal fun parseWeightFrame(rawFrame: String, sourceScaleId: Int): ParsedScaleWeight? {
        val parsed = ScaleFrameParser.parse(rawFrame)
        if (parsed != null) {
            return applyParsedWeight(parsed, sourceScaleId)
        } else {
            val clean = rawFrame.filter { it in ' '..'~' }.trim()
            if (clean.isNotEmpty()) {
                updateScaleDeviceRawFrame(sourceScaleId, clean)
                // Si la trama no es un error explícito (ej: "WACK", "ACK", eco, etc.), limpiar mensaje de error viejo en pantalla
                if (sourceScaleId == _scaleState.value.activeScaleId && _scaleState.value.lastErrorCode != null) {
                    _scaleState.value = _scaleState.value.copy(
                        lastErrorCode = null,
                        lastErrorMessage = null
                    )
                }
            }
            return null
        }
    }

    // --- VERIFICAR CONEXIÓN (PING) MONO-CONEXIÓN INTELIGENTE ---
    suspend fun pingScale(scaleId: Int = 0, ip: String, port: Int, timeoutMs: Int = 2000): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        // 1. Si la báscula ya está conectada y recibiendo datos en el worker activo, evitar crear un 2do socket
        val targetScale = _scalesList.value.find { (scaleId > 0 && it.id == scaleId) || (it.ip == ip && it.port == port) }
        if (targetScale != null && targetScale.isConnected) {
            return@withContext Pair(
                true,
                "Báscula en línea y enlazada (Canal activo, peso actual: ${targetScale.lastWeight} kg, Rx: ${targetScale.totalBytesReceived} B)"
            )
        }

        // 2. Si no está conectada, pausar temporalmente el reintento de esa báscula para que no colisione
        val targetId = targetScale?.id ?: scaleId
        if (targetId > 0) {
            scaleJobs[targetId]?.cancel()
        }

        val startTime = System.currentTimeMillis()
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, port), timeoutMs)
            val elapsed = System.currentTimeMillis() - startTime
            socket.close()

            // Reactivar worker
            if (targetId > 0) {
                startScaleWorker(targetId)
            }

            Pair(true, "Conectado exitosamente (${elapsed}ms)")
        } catch (e: ConnectException) {
            if (targetId > 0) startScaleWorker(targetId)
            Pair(
                false,
                "Conexión rechazada (ECONNREFUSED). El módulo Lantronix de la báscula es mono-cliente. Cierra el programa Qendra en la PC si está abierto."
            )
        } catch (e: SocketTimeoutException) {
            if (targetId > 0) startScaleWorker(targetId)
            Pair(false, "Tiempo agotado (${timeoutMs}ms). Verifica que la báscula esté encendida y en la red Wi-Fi.")
        } catch (e: Exception) {
            if (targetId > 0) startScaleWorker(targetId)
            Pair(false, "Error de red: ${e.localizedMessage ?: "Inalcanzable"}")
        }
    }

    // --- EXTRACCIÓN DIRECTA DE PLUs DESDE LA MEMORIA DE LA BÁSCULA VÍA TCP/IP (FUNCIONES 2, 11, 31, 62 Y 3) ---
    suspend fun extractPlusFromScale(scaleId: Int): Pair<List<ParsedPluPreview>, String> = withContext(Dispatchers.IO) {
        val target = _scalesList.value.find { it.id == scaleId } ?: _scale1Config.value

        // 1. Pausar y cerrar forzosamente el socket en segundo plano de esta báscula (Lantronix es mono-cliente)
        stopScaleWorker(target.id)
        delay(400) // Permitir al módulo Lantronix reiniciar y liberar el socket TCP

        var socket: Socket? = null
        try {
            val sock = Socket()
            sock.tcpNoDelay = true
            sock.connect(InetSocketAddress(target.ip, target.port), 3000)
            sock.soTimeout = 800
            socket = sock

            val input = sock.getInputStream()
            val output = sock.getOutputStream()
            val readBuf = ByteArray(2048)

            fun sendAndReadBurst(cmd: ByteArray, waitUntilF: Boolean = false, maxWaitMs: Long = 1800L): ByteArray {
                // Limpiar bytes residuales del buffer de entrada
                try {
                    while (input.available() > 0) {
                        input.read(readBuf)
                    }
                } catch (_: Exception) {}

                output.write(cmd)
                output.flush()

                val out = java.io.ByteArrayOutputStream()
                val start = System.currentTimeMillis()
                var timeoutsAfterData = 0

                while (System.currentTimeMillis() - start < maxWaitMs) {
                    try {
                        val n = input.read(readBuf)
                        if (n <= 0) break
                        out.write(readBuf, 0, n)
                        timeoutsAfterData = 0

                        val currentBytes = out.toByteArray()
                        if (waitUntilF) {
                            val str = String(currentBytes, Charsets.ISO_8859_1)
                            // En Función 31 la trama termina con 'F' + 1 byte CRC
                            val fIndex = str.lastIndexOf('F')
                            if (fIndex >= 2 && currentBytes.size >= fIndex + 2) {
                                break
                            }
                        }
                    } catch (_: SocketTimeoutException) {
                        if (out.size() > 0) {
                            timeoutsAfterData++
                            if (!waitUntilF || timeoutsAfterData >= 2) {
                                break
                            }
                        }
                    }
                }
                return out.toByteArray()
            }

            // 2. Descubrir o verificar la Dirección (ID 1..99) de la báscula Cuora Max
            var discoveredAddr = prefs?.getInt("cuora_addr_${target.ip}", -1) ?: -1
            val candidates = mutableListOf<Int>()
            if (discoveredAddr in 1..99) candidates.add(discoveredAddr)
            candidates.addAll(listOf(1, 2, target.id, 0))
            val lastOctet = target.ip.substringAfterLast(".").trim().toIntOrNull()
            if (lastOctet != null) {
                if (lastOctet in 1..99) candidates.add(lastOctet)
                if ((lastOctet / 10) in 1..9) candidates.add(lastOctet / 10)
                if ((lastOctet % 10) in 1..9) candidates.add(lastOctet % 10)
            }
            for (id in 3..20) candidates.add(id)

            var activeAddr = -1
            for (cand in candidates.distinct()) {
                val probeResp = sendAndReadBurst(ScaleFrameParser.buildCuoraWeightRequest(cand), maxWaitMs = 350L)
                if (probeResp.size >= 5) {
                    val respAddr = probeResp[0].toInt() and 0xFF
                    if (respAddr in 0..99) {
                        activeAddr = respAddr
                        prefs?.edit()?.putInt("cuora_addr_${target.ip}", activeAddr)?.apply()
                        break
                    }
                }
            }

            if (activeAddr !in 0..99) {
                activeAddr = if (discoveredAddr in 0..99) discoveredAddr else target.id.coerceIn(1, 99)
            }

            // 3. Consultar Firma Digital (Función 2) para conocer cantidad de decimales de precio (0 o 2)
            val sigBytes = sendAndReadBurst(ScaleFrameParser.buildCuoraDigitalSignatureRequest(activeAddr), maxWaitMs = 600L)
            val priceDecimals = ScaleFrameParser.parseCuoraDigitalSignatureDecimals(sigBytes)

            // 4. Consultar Lista de Sectores / Departamentos (Función 11)
            val secBytes = sendAndReadBurst(ScaleFrameParser.buildCuoraSectorsListRequest(activeAddr), maxWaitMs = 800L)
            val sectorsMap = ScaleFrameParser.parseCuoraSectorsResponse(secBytes)

            // 5. Consultar Listado de Todos los PLUs Cargados en Memoria (Función 31)
            var pluListBytes = sendAndReadBurst(
                ScaleFrameParser.buildCuoraPluListRequest(activeAddr),
                waitUntilF = true,
                maxWaitMs = 2500L
            )
            var pluEntries = ScaleFrameParser.parseCuoraPluListResponse(pluListBytes)
            if (pluEntries.isEmpty()) {
                // Segundo intento con mayor margen de espera
                pluListBytes = sendAndReadBurst(
                    ScaleFrameParser.buildCuoraPluListRequest(activeAddr),
                    waitUntilF = true,
                    maxWaitMs = 3500L
                )
                pluEntries = ScaleFrameParser.parseCuoraPluListResponse(pluListBytes)
            }

            val extractedList = mutableListOf<ParsedPluPreview>()
            var forceLegacyFunc3 = false

            // 6. Si Función 31 devolvió la lista de PLUs, descargar el detalle de cada uno
            if (pluEntries.isNotEmpty()) {
                for ((pluNum, isSixDigits) in pluEntries) {
                    val useFunc3 = forceLegacyFunc3 || !isSixDigits
                    val req = if (!useFunc3) {
                        ScaleFrameParser.buildCuoraExtendedPluRequest(activeAddr, pluNum)
                    } else {
                        ScaleFrameParser.buildCuoraLegacyPluRequest(activeAddr, pluNum, sixDigits = false)
                    }

                    var resp = sendAndReadBurst(req, maxWaitMs = 500L)
                    val binCheck = ScaleFrameParser.parseCuoraBinary(resp, resp.size)
                    if (!useFunc3 && binCheck?.protocolErrorCode == "E4") {
                        // Si el firmware no soporta Función 62 (E4), conmutar automáticamente a Función 3
                        forceLegacyFunc3 = true
                        resp = sendAndReadBurst(
                            ScaleFrameParser.buildCuoraLegacyPluRequest(activeAddr, pluNum, sixDigits = false),
                            maxWaitMs = 500L
                        )
                    }

                    val parsedPlu = ScaleFrameParser.parseCuoraSinglePluResponse(
                        bytes = resp,
                        length = resp.size,
                        priceDecimals = priceDecimals,
                        sectorsMap = sectorsMap
                    )
                    if (parsedPlu != null) {
                        extractedList.add(parsedPlu)
                    }
                }
            } else {
                // FALLBACK AUTOMÁTICO: Si Función 31 no fue soportada por la versión de firmware de la báscula,
                // barrer directamente los códigos PLU comunes (1 al 60) mediante Función 62 / Función 3
                var consecutiveMisses = 0
                for (candPlu in 1..60) {
                    val req = if (!forceLegacyFunc3) {
                        ScaleFrameParser.buildCuoraExtendedPluRequest(activeAddr, candPlu)
                    } else {
                        ScaleFrameParser.buildCuoraLegacyPluRequest(activeAddr, candPlu, sixDigits = false)
                    }

                    var resp = sendAndReadBurst(req, maxWaitMs = 350L)
                    val binCheck = ScaleFrameParser.parseCuoraBinary(resp, resp.size)
                    if (!forceLegacyFunc3 && binCheck?.protocolErrorCode == "E4") {
                        forceLegacyFunc3 = true
                        resp = sendAndReadBurst(
                            ScaleFrameParser.buildCuoraLegacyPluRequest(activeAddr, candPlu, sixDigits = false),
                            maxWaitMs = 350L
                        )
                    }

                    val parsed = ScaleFrameParser.parseCuoraSinglePluResponse(
                        bytes = resp,
                        length = resp.size,
                        priceDecimals = priceDecimals,
                        sectorsMap = sectorsMap
                    )
                    if (parsed != null && parsed.name.isNotBlank()) {
                        extractedList.add(parsed)
                        consecutiveMisses = 0
                    } else {
                        if (extractedList.isNotEmpty()) {
                            consecutiveMisses++
                            if (consecutiveMisses >= 8) break // Detener tras 8 códigos consecutivos vacíos después del último producto
                        }
                    }
                }
            }

            if (extractedList.isNotEmpty()) {
                Pair(
                    extractedList,
                    "✅ Se extrajeron ${extractedList.size} PLUs directamente de la memoria de ${target.name} (${target.ip}, ID:$activeAddr). Revisa la lista y pulsa 'Guardar Catálogo'."
                )
            } else if (pluEntries.isNotEmpty()) {
                val fallbackList = pluEntries.map { (num, _) ->
                    ParsedPluPreview(code = num, name = "PLU #$num", price = 0.0, section = "General")
                }
                Pair(
                    fallbackList,
                    "⚠️ Se detectaron ${fallbackList.size} códigos PLU en ${target.name} (ID:$activeAddr). Revisa la vista previa antes de guardar."
                )
            } else {
                Pair(
                    emptyList(),
                    "Enlace TCP activo con ${target.name} (${target.ip}, ID:$activeAddr), pero la báscula reportó 0 PLUs cargados en su memoria interna. También puedes subir tu archivo de Qendra (.txt/.csv)."
                )
            }
        } catch (e: ConnectException) {
            Pair(
                emptyList(),
                "Conexión rechazada por ${target.name} (${target.ip}). Cierra Qendra en la PC si está abierto (el módulo Lantronix es mono-cliente)."
            )
        } catch (e: SocketTimeoutException) {
            Pair(
                emptyList(),
                "Tiempo agotado al conectar con ${target.name} (${target.ip}). Verifica que la báscula esté encendida y conectada al Wi-Fi."
            )
        } catch (e: Exception) {
            Pair(
                emptyList(),
                "Error al extraer PLUs de ${target.name}: ${e.localizedMessage ?: "Fallo de red"}"
            )
        } finally {
            try { socket?.close() } catch (_: Exception) {}
            // Reactivar inmediatamente el hilo de lectura de peso en vivo de esta báscula
            startScaleWorker(target.id)
        }
    }

    fun setManualWeight(weight: Double) {
        val rounded = kotlin.math.round(weight * 1000) / 1000.0
        isManualWeightActive = (rounded > 0.0)
        _scaleState.value = _scaleState.value.copy(
            currentWeight = rounded,
            isStable = true
        )
    }

    fun clearManualWeight() {
        isManualWeightActive = false
        _scaleState.value = _scaleState.value.copy(
            currentWeight = 0.000,
            isStable = true
        )
    }
}
