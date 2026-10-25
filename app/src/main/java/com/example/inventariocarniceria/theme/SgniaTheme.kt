package com.example.inventariocarniceria.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * MODOS DE TEMA EJECUTIVOS DE ÉLITE
 */
enum class SgniaThemeMode {
    NOIR_CREMA    // Sistema Activo Oficial: Negro Mate & Crema Marfil (Noir & Crema)
}

/**
 * PALETA SEMÁNTICA INDUSTRIAL (@Immutable para erradicar recomposiciones)
 */
@Immutable
data class SgniaColors(
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val surfaceGlass: Color,
    val surfaceGlassBorder: Color,
    val borderGradient: Brush,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val accentGlow: Color,
    val statusStable: Color,
    val statusUnstable: Color,
    val statusAlert: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val oledDisplayBg: Color,
    val oledDigitColor: Color,
    val oledGlow: Color,
    val shimmerColors: List<Color>
)

// --- VALORES TEMA PRINCIPAL: NOIR & CREMA MARFIL (PLANTILLA ELEGANTE) ---
val NoirCremaColors = SgniaColors(
    backgroundPrimary = Color(0xFF0F0E0D),    // Negro mate cálido profundo
    backgroundSecondary = Color(0xFF171614),  // Carbón suave para barras y navegación
    surfaceGlass = Color(0xFF211E1B),         // Superficie de tarjetas café/carbón oscuro
    surfaceGlassBorder = Color(0xFF423B32),   // Borde crema sutil
    borderGradient = Brush.linearGradient(
        listOf(Color(0xFFEEDDC8), Color(0x66EEDDC8), Color(0xFF9E8B75))
    ),
    accentPrimary = Color(0xFFEEDDC8),        // Crema Marfil principal (como en la plantilla)
    accentSecondary = Color(0xFFD6C2A8),      // Crema Latte cálido
    accentGlow = Color(0x33EEDDC8),
    statusStable = Color(0xFF26A69A),         // Esmeralda suave para peso estable
    statusUnstable = Color(0xFFFFB74D),       // Ámbar suave para peso inestable
    statusAlert = Color(0xFFE57373),          // Carmesí suave para alertas
    textPrimary = Color(0xFFFAF6F0),          // Blanco Crema de alta legibilidad
    textSecondary = Color(0xFFC7BBAA),        // Crema medio para subtítulos
    textMuted = Color(0xFF887D70),            // Crema apagado para leyendas
    oledDisplayBg = Color(0xFF0A0908),        // Pantalla negra absoluta para el visor
    oledDigitColor = Color(0xFFFFE082),       // Dígitos en ámbar crema de alto contraste
    oledGlow = Color(0x33FFE082),
    shimmerColors = listOf(
        Color(0xFFEEDDC8),
        Color(0xFFFFF9F0),
        Color(0xFFD6C2A8)
    )
)

// --- VALORES TEMA A: OBSIDIAN & LIQUID GOLD TITANIUM ---
val LocalSgniaColors = staticCompositionLocalOf { NoirCremaColors }
val LocalSgniaThemeMode = staticCompositionLocalOf { SgniaThemeMode.NOIR_CREMA }

/**
 * TIPOGRAFÍA INDUSTRIAL MILITAR / PORSCHE DESIGN
 */
object SgniaTypography {
    val displayOled = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 52.sp,
        letterSpacing = 2.sp
    )
    val displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 26.sp,
        letterSpacing = 0.5.sp
    )
    val titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        letterSpacing = 0.3.sp
    )
    val labelTactical = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 1.sp
    )
    val body = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
    val monospaceValues = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
    )
}

/**
 * THEME PROVIDER REACTIVO CON INTERPOLACIÓN FLUIDA (animateColorAsState)
 */
@Composable
fun SgniaTheme(
    themeMode: SgniaThemeMode = SgniaThemeMode.NOIR_CREMA,
    content: @Composable () -> Unit
) {
    val targetColors = NoirCremaColors

    val animSpec = tween<Color>(durationMillis = 350)

    val animatedBgPrimary = animateColorAsState(targetColors.backgroundPrimary, animSpec, label = "bgPrimary")
    val animatedBgSecondary = animateColorAsState(targetColors.backgroundSecondary, animSpec, label = "bgSecondary")
    val animatedSurfaceGlass = animateColorAsState(targetColors.surfaceGlass, animSpec, label = "surfaceGlass")
    val animatedBorder = animateColorAsState(targetColors.surfaceGlassBorder, animSpec, label = "border")
    val animatedAccentPrimary = animateColorAsState(targetColors.accentPrimary, animSpec, label = "accentPrimary")
    val animatedAccentSecondary = animateColorAsState(targetColors.accentSecondary, animSpec, label = "accentSecondary")
    val animatedAccentGlow = animateColorAsState(targetColors.accentGlow, animSpec, label = "accentGlow")
    val animatedStatusStable = animateColorAsState(targetColors.statusStable, animSpec, label = "statusStable")
    val animatedStatusAlert = animateColorAsState(targetColors.statusAlert, animSpec, label = "statusAlert")
    val animatedTextPrimary = animateColorAsState(targetColors.textPrimary, animSpec, label = "textPrimary")
    val animatedTextSecondary = animateColorAsState(targetColors.textSecondary, animSpec, label = "textSecondary")
    val animatedOledDigit = animateColorAsState(targetColors.oledDigitColor, animSpec, label = "oledDigit")

    val animatedColors = SgniaColors(
        backgroundPrimary = animatedBgPrimary.value,
        backgroundSecondary = animatedBgSecondary.value,
        surfaceGlass = animatedSurfaceGlass.value,
        surfaceGlassBorder = animatedBorder.value,
        borderGradient = targetColors.borderGradient,
        accentPrimary = animatedAccentPrimary.value,
        accentSecondary = animatedAccentSecondary.value,
        accentGlow = animatedAccentGlow.value,
        statusStable = animatedStatusStable.value,
        statusUnstable = targetColors.statusUnstable,
        statusAlert = animatedStatusAlert.value,
        textPrimary = animatedTextPrimary.value,
        textSecondary = animatedTextSecondary.value,
        textMuted = targetColors.textMuted,
        oledDisplayBg = targetColors.oledDisplayBg,
        oledDigitColor = animatedOledDigit.value,
        oledGlow = targetColors.oledGlow,
        shimmerColors = targetColors.shimmerColors
    )

    val materialColors = darkColorScheme(
        primary = animatedAccentPrimary.value,
        secondary = animatedAccentSecondary.value,
        background = animatedBgPrimary.value,
        surface = animatedBgSecondary.value,
        onPrimary = Color.Black,
        onBackground = animatedTextPrimary.value,
        onSurface = animatedTextPrimary.value
    )

    CompositionLocalProvider(
        LocalSgniaColors provides animatedColors,
        LocalSgniaThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}

object SgniaTheme {
    val colors: SgniaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSgniaColors.current

    val typography: SgniaTypography
        get() = SgniaTypography

    val mode: SgniaThemeMode
        @Composable
        @ReadOnlyComposable
        get() = LocalSgniaThemeMode.current
}
