package com.example.inventariocarniceria.ui.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * SGNIA · Sistema de diseño "Instrumento de Precisión" (Noir & Crema).
 *
 * Fuente única de verdad para colores, tipografía, espaciado y radios.
 * Todas las pantallas rediseñadas deben consumir estos tokens; no declarar
 * colores sueltos dentro de las pantallas.
 */
object NoirColors {
    // Superficies
    val Background = Color(0xFF0F0E0D)
    val Surface = Color(0xFF181715)
    val SurfaceRaised = Color(0xFF1F1D1A)
    val SurfaceSunken = Color(0xFF121110)

    // Líneas finas
    val Hairline = Color(0xFF2A2723)
    val HairlineStrong = Color(0xFF3A352F)

    // Texto y acento crema
    val Cream = Color(0xFFEEDDC8)
    val TextPrimary = Color(0xFFF3EFE6)
    val TextSecondary = Color(0xFFA8A196)
    val TextMuted = Color(0xFF9E968A)
    val OnCream = Color(0xFF141312)
    val OnCreamSecondary = Color(0xFF4A4640)

    // Acentos funcionales (apagados, sin neón)
    val Emerald = Color(0xFF4ADE80)          // Solo peso estable / estados positivos
    val EmeraldMuted = Color(0xFF1C2A21)
    val Gold = Color(0xFFC9A96E)             // Acento mínimo
    val GoldMuted = Color(0xFF2A241A)
    val Amber = Color(0xFFD9A441)            // Pendiente / lectura
    val AmberMuted = Color(0xFF2B2416)
    val Slate = Color(0xFF9FB0C0)            // En ruta / informativo
    val SlateMuted = Color(0xFF1C2127)
    val Danger = Color(0xFFE07A6F)           // Error / inestable crítico
    val DangerMuted = Color(0xFF2C1B19)
}

object NoirSpace {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 28.dp
    val xxl = 40.dp

    /** Margen horizontal estándar de pantalla. */
    val screen = 20.dp

    /** Separación estándar entre celdas bento. */
    val gutter = 12.dp

    /** Altura mínima táctil para uso con guantes o manos húmedas. */
    val touch = 64.dp
}

object NoirRadius {
    val sm = 12.dp
    val md = 18.dp
    val lg = 24.dp
    val pill = 100.dp
}

object NoirType {
    /** Cifras dominantes (kilos, importes, % merma). */
    val metricXL = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        letterSpacing = (-2).sp,
        lineHeight = 60.sp
    )
    val metricL = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        letterSpacing = (-1).sp,
        lineHeight = 40.sp
    )
    val metricM = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        letterSpacing = (-0.5).sp,
        lineHeight = 30.sp
    )

    /** Titulares de pantalla. */
    val display = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        letterSpacing = (-0.8).sp,
        lineHeight = 38.sp
    )
    val title = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp
    )
    val body = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp
    )
    val bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )

    /** Etiquetas tipo "placa de especificación": mayúsculas con tracking. */
    val label = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.6.sp,
        lineHeight = 14.sp
    )
    val button = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 0.4.sp
    )
}
