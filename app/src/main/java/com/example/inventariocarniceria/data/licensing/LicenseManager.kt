package com.example.inventariocarniceria.data.licensing

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * MOTOR DE LICENCIAMIENTO INDUSTRIAL OFFLINE - SGNIA COMPANY
 *
 * Diseñado para operar 100% fuera de línea en entornos de carnicerías, rastros y
 * balanzas industriales sin conexión a internet.
 *
 * Características de seguridad:
 * 1. Firma Criptográfica HMAC-SHA256 vinculada al Hardware ID único de la tablet.
 * 2. Protección anti-manipulación de reloj: detecta si atrasan la hora del sistema.
 * 3. Consumo único de tokens: evita que reutilicen claves anteriores.
 * 4. Soporta duraciones:
 *    - 10 minutos (Demostración de venta en vivo)
 *    - 7 días (Prueba comercial)
 *    - 1 mes / 30 días (Renta mensual)
 *    - 1 año / 365 días (Renta anual)
 *    - De por vida (Venta definitiva / Licencia perpetua)
 */
object LicenseManager {

    // Semilla criptográfica privada para firma digital
    private const val HMAC_SECRET_SEED = "SGNIA_ENTERPRISE_KEY_2026_SECURITY_SALT_GAMAVEL_MEAT_SCALE"

    private const val PREFS_NAME = "sgnia_license_preferences"
    private const val KEY_STATE = "lic_state"
    private const val KEY_TYPE = "lic_type"
    private const val KEY_ACTIVATED_AT = "lic_activated_at"
    private const val KEY_EXPIRES_AT = "lic_expires_at"
    private const val KEY_LAST_TIMESTAMP = "lic_last_timestamp"
    private const val KEY_CONSUMED_TOKENS = "lic_consumed_tokens"
    private const val KEY_ACTIVE_KEY = "lic_active_key"

    enum class LicenseTier(val code: String, val label: String, val durationMs: Long) {
        DEMO_10M("10M", "10 Minutos (Demostración en vivo)", 10 * 60 * 1000L),
        TRIAL_7D("07D", "7 Días (Prueba comercial)", 7L * 86400 * 1000L),
        RENTAL_1M("01M", "1 Mes (Renta mensual)", 30L * 86400 * 1000L),
        RENTAL_1Y("01Y", "1 Año (Renta anual)", 365L * 86400 * 1000L),
        LIFETIME("LIFE", "De por vida (Licencia definitiva)", -1L);

        companion object {
            fun fromCode(code: String): LicenseTier? {
                return entries.find { it.code.equals(code, ignoreCase = true) }
            }
        }
    }

    enum class LicenseStatus {
        ACTIVE,
        EXPIRED,
        UNLICENSED,
        CLOCK_TAMPERED
    }

    data class LicenseInfo(
        val status: LicenseStatus,
        val tier: LicenseTier?,
        val hardwareId: String,
        val activatedAt: Long,
        val expiresAt: Long,
        val remainingMillis: Long,
        val rawKey: String
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Obtiene el identificador de hardware único formateado como SGN-XXXX-XXXX
     */
    fun getHardwareId(context: Context): String {
        val raw = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (_: Exception) { null }

        val seed = if (raw.isNullOrBlank() || raw == "9774d56d682e549c") {
            // En emuladores o dispositivos sin ANDROID_ID, derivamos del hardware de Android
            "${Build.MANUFACTURER}_${Build.MODEL}_${Build.BOARD}_SGNIA"
        } else {
            raw
        }

        val hash = sha256Hex(seed).uppercase(Locale.US).take(8)
        val part1 = hash.substring(0, 4)
        val part2 = hash.substring(4, 8)
        return "SGN-$part1-$part2"
    }

    /**
     * Generador de clave de activación criptográfica (utilizado por el Keygen)
     */
    fun generateActivationKey(targetHardwareId: String, tier: LicenseTier, tokenHex: String = generateRandomToken()): String {
        val cleanHw = normalizeId(targetHardwareId)
        val cleanToken = tokenHex.trim().uppercase(Locale.US).take(4).padStart(4, '0')
        val payload = "$cleanHw:${tier.code}:$cleanToken"
        val hmac = calculateHmacSha256(payload, HMAC_SECRET_SEED).take(8).uppercase(Locale.US)
        val hmacPart1 = hmac.substring(0, 4)
        val hmacPart2 = hmac.substring(4, 8)
        return "SGN-${tier.code}-$cleanToken-$hmacPart1-$hmacPart2"
    }

    /**
     * Valida y activa una clave de activación introducida por el usuario
     */
    fun activateKey(context: Context, keyInput: String): Result<LicenseInfo> {
        val hardwareId = getHardwareId(context)
        val cleanHw = normalizeId(hardwareId)

        val cleanKey = keyInput.trim().uppercase(Locale.US).replace(" ", "").replace("-", "")
        if (!cleanKey.startsWith("SGN") || cleanKey.length < 15) {
            return Result.failure(IllegalArgumentException("Formato de clave inválido. Debe iniciar con 'SGN-'."))
        }

        // Remueve prefijo SGN
        val withoutPrefix = cleanKey.removePrefix("SGN")

        // 1. Manejo de Clave de Revocación / Kill-Switch emitida por SGNIA Keygen Admin
        if (withoutPrefix.startsWith("KILL") || withoutPrefix.startsWith("REVOKE")) {
            val restKill = if (withoutPrefix.startsWith("KILL")) withoutPrefix.removePrefix("KILL") else withoutPrefix.removePrefix("REVOKE")
            if (restKill.length >= 12) {
                val token = restKill.take(4)
                val signatureProvided = restKill.drop(4).take(8)
                val payload = "$cleanHw:KILL:$token"
                val expectedSignature = calculateHmacSha256(payload, HMAC_SECRET_SEED).take(8).uppercase(Locale.US)
                if (signatureProvided.equals(expectedSignature, ignoreCase = true)) {
                    val prefs = getPrefs(context)
                    prefs.edit()
                        .putString(KEY_STATE, LicenseStatus.EXPIRED.name)
                        .putLong(KEY_EXPIRES_AT, 0L)
                        .putString(KEY_ACTIVE_KEY, "REVOCADA")
                        .apply()
                    return Result.failure(SecurityException("🚫 ACCESO REVOCADO: Esta terminal ha sido dada de baja del sistema por SGNIA COMPANY."))
                } else {
                    return Result.failure(SecurityException("Clave de revocación no válida para este dispositivo."))
                }
            }
        }

        // Extrae componentes:
        // Si el código es 'LIFE' (4 caracteres), o '10M'/'07D'/'01M'/'01Y' (3 caracteres)
        val tierCode: String
        val rest: String
        if (withoutPrefix.startsWith("LIFE")) {
            tierCode = "LIFE"
            rest = withoutPrefix.removePrefix("LIFE")
        } else {
            tierCode = withoutPrefix.take(3)
            rest = withoutPrefix.drop(3)
        }

        val tier = LicenseTier.fromCode(tierCode)
            ?: return Result.failure(IllegalArgumentException("Tipo de licencia desconocido ($tierCode)."))

        if (rest.length < 12) {
            return Result.failure(IllegalArgumentException("La clave no tiene la longitud requerida."))
        }

        val token = rest.take(4)
        val signatureProvided = rest.drop(4).take(8)

        // Verificar si este token ya fue consumido en este dispositivo (para evitar doble uso de clave)
        val prefs = getPrefs(context)
        val consumedTokens = prefs.getStringSet(KEY_CONSUMED_TOKENS, emptySet()) ?: emptySet()
        if (consumedTokens.contains(token)) {
            return Result.failure(IllegalStateException("Esta clave ya fue utilizada anteriormente en este equipo."))
        }

        // Calcular firma esperada
        val payload = "$cleanHw:${tier.code}:$token"
        val expectedSignature = calculateHmacSha256(payload, HMAC_SECRET_SEED).take(8).uppercase(Locale.US)

        if (!signatureProvided.equals(expectedSignature, ignoreCase = true)) {
            return Result.failure(SecurityException("Clave de activación no válida para este dispositivo ($hardwareId)."))
        }

        // Clave válida: proceder a registrar la activación
        val now = System.currentTimeMillis()
        val expiresAt = if (tier == LicenseTier.LIFETIME) {
            -1L
        } else {
            now + tier.durationMs
        }

        val updatedConsumed = consumedTokens.toMutableSet().apply { add(token) }

        prefs.edit()
            .putString(KEY_STATE, LicenseStatus.ACTIVE.name)
            .putString(KEY_TYPE, tier.code)
            .putLong(KEY_ACTIVATED_AT, now)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putLong(KEY_LAST_TIMESTAMP, now)
            .putStringSet(KEY_CONSUMED_TOKENS, updatedConsumed)
            .putString(KEY_ACTIVE_KEY, keyInput.trim())
            .apply()

        val info = LicenseInfo(
            status = LicenseStatus.ACTIVE,
            tier = tier,
            hardwareId = hardwareId,
            activatedAt = now,
            expiresAt = expiresAt,
            remainingMillis = if (expiresAt == -1L) Long.MAX_VALUE else (expiresAt - now),
            rawKey = keyInput.trim()
        )

        return Result.success(info)
    }

    /**
     * Inspecciona el estado actual de la licencia en tiempo real
     */
    fun checkLicense(context: Context): LicenseInfo {
        val prefs = getPrefs(context)
        val hardwareId = getHardwareId(context)

        val stateStr = prefs.getString(KEY_STATE, null)
        val tierCode = prefs.getString(KEY_TYPE, null)
        val activatedAt = prefs.getLong(KEY_ACTIVATED_AT, 0L)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val lastTimestamp = prefs.getLong(KEY_LAST_TIMESTAMP, 0L)
        val activeKey = prefs.getString(KEY_ACTIVE_KEY, "") ?: ""

        val tier = tierCode?.let { LicenseTier.fromCode(it) }

        if (stateStr == null || tier == null) {
            return LicenseInfo(
                status = LicenseStatus.UNLICENSED,
                tier = null,
                hardwareId = hardwareId,
                activatedAt = 0L,
                expiresAt = 0L,
                remainingMillis = 0L,
                rawKey = ""
            )
        }

        val now = System.currentTimeMillis()

        // 1. Detección de manipulación de reloj (rollback superior a 60 segundos)
        if (now < lastTimestamp - 60_000L) {
            return LicenseInfo(
                status = LicenseStatus.CLOCK_TAMPERED,
                tier = tier,
                hardwareId = hardwareId,
                activatedAt = activatedAt,
                expiresAt = expiresAt,
                remainingMillis = 0L,
                rawKey = activeKey
            )
        }

        // Actualizar último timestamp conocido
        if (now > lastTimestamp) {
            prefs.edit().putLong(KEY_LAST_TIMESTAMP, now).apply()
        }

        // 2. Licencia Perpetua (De por vida)
        if (expiresAt == -1L) {
            return LicenseInfo(
                status = LicenseStatus.ACTIVE,
                tier = tier,
                hardwareId = hardwareId,
                activatedAt = activatedAt,
                expiresAt = -1L,
                remainingMillis = Long.MAX_VALUE,
                rawKey = activeKey
            )
        }

        // 3. Licencia temporal (10m, 7d, 1m, 1y)
        val remaining = expiresAt - now
        return if (remaining <= 0) {
            prefs.edit().putString(KEY_STATE, LicenseStatus.EXPIRED.name).apply()
            LicenseInfo(
                status = LicenseStatus.EXPIRED,
                tier = tier,
                hardwareId = hardwareId,
                activatedAt = activatedAt,
                expiresAt = expiresAt,
                remainingMillis = 0L,
                rawKey = activeKey
            )
        } else {
            LicenseInfo(
                status = LicenseStatus.ACTIVE,
                tier = tier,
                hardwareId = hardwareId,
                activatedAt = activatedAt,
                expiresAt = expiresAt,
                remainingMillis = remaining,
                rawKey = activeKey
            )
        }
    }

    private fun normalizeId(hwId: String): String {
        return hwId.trim().uppercase(Locale.US).replace("SGN", "").replace("-", "").replace(" ", "")
    }

    private fun generateRandomToken(): String {
        val chars = "0123456789ABCDEF"
        return (1..4).map { chars.random() }.joinToString("")
    }

    private fun calculateHmacSha256(data: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        val bytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02X".format(it) }
    }

    private fun sha256Hex(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(text.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02X".format(it) }
    }
}
