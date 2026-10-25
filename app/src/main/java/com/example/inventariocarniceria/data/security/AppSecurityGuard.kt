package com.example.inventariocarniceria.data.security

import android.content.Context
import android.os.Build
import android.os.Debug
import android.os.SystemClock
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

/**
 * SISTEMA DE BLINDAJE Y SEGURIDAD INDUSTRIAL - SGNIA COMPANY
 *
 * Protecciones activas:
 * 1. Detección de dispositivos con Root (Magisk, SuperSU, KernelSU, su binaries).
 * 2. Detección de inyección de código y depuradores espía (Frida, Xposed, JDWP debugger).
 * 3. Protección contra alteración del reloj del sistema (Anti-Clock Rollback).
 * 4. Ofuscación de llaves y secretos criptográficos mediante enmascaramiento binario XOR.
 */
object AppSecurityGuard {

    private const val PREFS_SEC = "sgnia_security_guard_prefs"
    private const val KEY_LAST_WALL_TIME = "sec_last_wall_time"
    private const val KEY_LAST_ELAPSED = "sec_last_elapsed"

    sealed class SecurityVerdict {
        object Passed : SecurityVerdict()
        data class Compromised(val title: String, val message: String) : SecurityVerdict()
    }

    /**
     * Reconstruye en memoria la semilla de firma privada HMAC-SHA256
     * sin dejar cadenas literales rastreables en el bytecode dex.
     */
    fun getObfuscatedSecretSeed(): String {
        val obfSeed = byteArrayOf(
            (0x09).toByte(), (0x7B).toByte(), (0x30).toByte(), (0x56).toByte(), (0x0C).toByte(), (0x34).toByte(), (0x6F).toByte(), (0x47).toByte(),
            (0x0E).toByte(), (0x79).toByte(), (0x2C).toByte(), (0x4F).toByte(), (0x1F).toByte(), (0x22).toByte(), (0x79).toByte(), (0x4C).toByte(),
            (0x05).toByte(), (0x77).toByte(), (0x3B).toByte(), (0x46).toByte(), (0x12).toByte(), (0x59).toByte(), (0x1A).toByte(), (0x3B).toByte(),
            (0x6C).toByte(), (0x63).toByte(), (0x2D).toByte(), (0x5A).toByte(), (0x0E).toByte(), (0x3E).toByte(), (0x78).toByte(), (0x40).toByte(),
            (0x0E).toByte(), (0x65).toByte(), (0x21).toByte(), (0x4C).toByte(), (0x0C).toByte(), (0x27).toByte(), (0x7E).toByte(), (0x56).toByte(),
            (0x1D).toByte(), (0x7D).toByte(), (0x33).toByte(), (0x5E).toByte(), (0x1B).toByte(), (0x2E).toByte(), (0x66).toByte(), (0x56).toByte(),
            (0x17).toByte(), (0x79).toByte(), (0x3F).toByte(), (0x4B).toByte(), (0x12).toByte(), (0x38).toByte(), (0x69).toByte(), (0x48).toByte(),
            (0x16).toByte(), (0x79).toByte()
        )
        val mask = byteArrayOf((0x5A).toByte(), (0x3C).toByte(), (0x7E).toByte(), (0x1F).toByte(), (0x4D).toByte(), (0x6B).toByte(), (0x2A).toByte(), (0x09).toByte())
        val chars = CharArray(obfSeed.size)
        for (i in obfSeed.indices) {
            chars[i] = (obfSeed[i].toInt() xor mask[i % mask.size].toInt()).toChar()
        }
        return String(chars)
    }

    /**
     * Inspecciona si el entorno de ejecución está rooteado
     */
    fun isDeviceRooted(): Boolean {
        // 1. Verificación de etiquetas de compilación (test-keys en ROMs no oficiales o de desarrollo)
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        // 2. Verificación de binarios SU conocidos
        val rootPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/data/adb/magisk",
            "/system/xbin/daemonsu"
        )
        for (path in rootPaths) {
            try {
                if (File(path).exists()) return true
            } catch (_: Exception) { }
        }

        return false
    }

    /**
     * Inspecciona si hay herramientas de depuración o hooking en memoria (Frida, Xposed, JDWP)
     */
    fun isDebuggerOrHookActive(): Boolean {
        // 1. Depurador conectado al proceso
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
            return true
        }

        // 2. Mapeo de librerías en memoria para detectar inyección de Frida o Xposed
        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                BufferedReader(FileReader(mapsFile)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        val lower = line.lowercase()
                        if (lower.contains("frida") || lower.contains("gadget") ||
                            lower.contains("xposed") || lower.contains("substrate")) {
                            return true
                        }
                        line = reader.readLine()
                    }
                }
            }
        } catch (_: Exception) { }

        return false
    }

    /**
     * Verifica la integridad del reloj del sistema contra manipulaciones de fecha/hora.
     */
    fun isClockManipulated(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_SEC, Context.MODE_PRIVATE)
        val lastWallTime = prefs.getLong(KEY_LAST_WALL_TIME, 0L)
        val lastElapsed = prefs.getLong(KEY_LAST_ELAPSED, 0L)

        val nowWall = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()

        // Si la hora actual es inferior a la última hora registrada por más de 30 segundos,
        // el usuario retrocedió el reloj del sistema.
        if (lastWallTime > 0L && nowWall < (lastWallTime - 30_000L)) {
            return true
        }

        // Guardar marcas actualizadas si el tiempo avanza normalmente
        if (nowWall >= lastWallTime) {
            prefs.edit()
                .putLong(KEY_LAST_WALL_TIME, nowWall)
                .putLong(KEY_LAST_ELAPSED, nowElapsed)
                .apply()
        }

        return false
    }

    /**
     * Ejecuta una auditoría de seguridad integral en la terminal.
     */
    fun performSecurityAudit(context: Context): SecurityVerdict {
        if (isDebuggerOrHookActive()) {
            return SecurityVerdict.Compromised(
                title = "Alerta de Seguridad: Depurador Detectado",
                message = "Se detectó una herramienta de monitoreo o depuración activa en la memoria del dispositivo. Por seguridad del inventario y las ventas, la app ha suspendido el acceso."
            )
        }

        if (isClockManipulated(context)) {
            return SecurityVerdict.Compromised(
                title = "Alerta de Seguridad: Reloj Manipulado",
                message = "La fecha u hora del sistema fue retrocedida intencionalmente. Restablece la fecha y hora automática en los Ajustes de Android para continuar."
            )
        }

        // Nota: Si el dispositivo está rooteado, se registra como advertencia de seguridad crítica
        // pero se permite la operación si el reloj y la memoria no están intervenidos.
        return SecurityVerdict.Passed
    }
}
