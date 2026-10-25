package com.example.inventariocarniceria.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.inventariocarniceria.data.scale.TcpScaleDevice
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.theme.SgniaTheme
import com.example.inventariocarniceria.theme.SgniaThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.roundToInt

// =========================================================================
// 1. SELECTOR TÁCTICO DE TEMA: SÍMBOLO SOL Y LUNA
// =========================================================================
@Composable
fun SgniaThemeToggle(
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    currentMode: SgniaThemeMode = SgniaTheme.mode
) {
    val isGold = currentMode == SgniaThemeMode.OBSIDIAN_GOLD
    val haptic = LocalHapticFeedback.current

    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isGold) 0f else 360f,
        animationSpec = spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow),
        label = "themeIconRotation"
    )

    Box(
        modifier = modifier
            .size(42.dp)
            .graphicsLayer {
                shape = CircleShape
                clip = true
                rotationZ = rotation
            }
            .background(SgniaTheme.colors.surfaceGlass)
            .border(1.5.dp, SgniaTheme.colors.borderGradient, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isGold) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = "Modo Sol (Obsidian Gold)",
                tint = Color(0xFFFFB300),
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = "Modo Luna (Cyber Violet)",
                tint = Color(0xFFC084FC),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// =========================================================================
// 2. TARJETA DARK GLASSMORPHISM DE ALTO RENDIMIENTO
// =========================================================================
@Composable
fun TacticalGlassCard(
    modifier: Modifier = Modifier,
    borderBrush: Brush = SgniaTheme.colors.borderGradient,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                shadowElevation = 6.dp.toPx()
                shape = RoundedCornerShape(22.dp)
                clip = true
            }
            .background(SgniaTheme.colors.surfaceGlass)
            .border(1.dp, SgniaTheme.colors.surfaceGlassBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

// =========================================================================
// 3. TECLADO NUMÉRICO TÁCTICO CON HÁPTICA PARA USO CON GUANTES
// =========================================================================
@Composable
fun TacticalKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("DEL", "0", "OK")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    TacticalKeyButton(
                        key = key,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            when (key) {
                                "DEL" -> onDeleteClick()
                                "OK" -> onConfirmClick()
                                else -> onDigitClick(key)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TacticalKeyButton(
    key: String,
    onClick: () -> Unit
) {
    val isOk = key == "OK"
    val isDel = key == "DEL"

    Box(
        modifier = Modifier
            .size(68.dp)
            .graphicsLayer {
                shape = CircleShape
                clip = true
            }
            .background(
                if (isOk) SgniaTheme.colors.accentPrimary.copy(alpha = 0.25f)
                else if (isDel) Color.Red.copy(alpha = 0.15f)
                else SgniaTheme.colors.surfaceGlass
            )
            .border(
                1.dp,
                if (isOk) SgniaTheme.colors.accentPrimary
                else if (isDel) Color.Red.copy(alpha = 0.5f)
                else SgniaTheme.colors.surfaceGlassBorder,
                CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            isOk -> Icon(Icons.Default.Check, contentDescription = "OK", tint = SgniaTheme.colors.accentPrimary, modifier = Modifier.size(24.dp))
            isDel -> Icon(Icons.Default.Backspace, contentDescription = "Borrar", tint = Color.Red, modifier = Modifier.size(22.dp))
            else -> {
                Text(
                    text = key,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = SgniaTheme.colors.textPrimary
                )
            }
        }
    }
}

// =========================================================================
// 4. DISPLAY DE PIN HEXAGONAL/ANILLOS CON SHAKE FÍSICO DE RECHAZO
// =========================================================================
@Composable
fun TacticalPinDisplay(
    pin: String,
    maxDigits: Int = 4,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(isError) {
        if (isError) {
            // Animación física de sacudida horizontal de rechazo
            for (i in 0..3) {
                shakeOffset.animateTo(24f, spring(stiffness = 2000f))
                shakeOffset.animateTo(-24f, spring(stiffness = 2000f))
            }
            shakeOffset.animateTo(0f)
        }
    }

    Row(
        modifier = modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until maxDigits) {
            val isFilled = i < pin.length
            val ringColor = if (isError) SgniaTheme.colors.statusAlert
            else if (isFilled) SgniaTheme.colors.accentPrimary
            else SgniaTheme.colors.surfaceGlassBorder

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer {
                        shape = CircleShape
                        clip = true
                    }
                    .background(
                        if (isFilled) SgniaTheme.colors.accentPrimary.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .border(2.dp, ringColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isFilled) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(ringColor)
                    )
                }
            }
        }
    }
}

// =========================================================================
// 5. SELECTOR TÁCTICO DE BÁSCULA TCP/IP WI-FI CON LED PING
// =========================================================================
@Composable
fun ScaleSelectorHeader(
    selectedScale: Int, // 1 para Mostrador, 2 para Cortes
    onSelectScale: (Int) -> Unit,
    scale1Name: String = "BÁSCULA 1 (MOSTRADOR)",
    scale1Ip: String = "192.168.100.50",
    scale1Port: Int = 10001,
    scale1Connected: Boolean = true,
    scale2Name: String = "BÁSCULA 2 (CORTES)",
    scale2Ip: String = "192.168.100.51",
    scale2Port: Int = 10001,
    scale2Connected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ledPing")
    val ledAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ledPulse"
    )

    val led1Color = if (scale1Connected) SgniaTheme.colors.statusStable else SgniaTheme.colors.statusAlert
    val led2Color = if (scale2Connected) SgniaTheme.colors.statusStable else SgniaTheme.colors.statusAlert

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SgniaTheme.colors.surfaceGlass)
            .border(1.dp, SgniaTheme.colors.surfaceGlassBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val scale1Selected = selectedScale == 1
        val scale2Selected = selectedScale == 2

        // BÁSCULA 1
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (scale1Selected) SgniaTheme.colors.accentPrimary.copy(alpha = 0.2f)
                    else Color.Transparent
                )
                .border(
                    if (scale1Selected) 1.dp else 0.dp,
                    if (scale1Selected) SgniaTheme.colors.accentPrimary else Color.Transparent,
                    RoundedCornerShape(10.dp)
                )
                .clickable { onSelectScale(1) }
                .padding(vertical = 8.dp, horizontal = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // LED Indicador de Conexión
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer { alpha = if (scale1Connected) ledAlpha else 0.7f }
                        .background(led1Color, CircleShape)
                )
                Column {
                    Text(
                        text = scale1Name,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (scale1Selected) SgniaTheme.colors.accentPrimary else SgniaTheme.colors.textSecondary
                    )
                    Text(
                        text = "TCP $scale1Ip:$scale1Port",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = SgniaTheme.colors.textMuted
                    )
                }
            }
        }

        // BÁSCULA 2
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (scale2Selected) SgniaTheme.colors.accentPrimary.copy(alpha = 0.2f)
                    else Color.Transparent
                )
                .border(
                    if (scale2Selected) 1.dp else 0.dp,
                    if (scale2Selected) SgniaTheme.colors.accentPrimary else Color.Transparent,
                    RoundedCornerShape(10.dp)
                )
                .clickable { onSelectScale(2) }
                .padding(vertical = 8.dp, horizontal = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer { alpha = if (scale2Connected) ledAlpha else 0.7f }
                        .background(led2Color, CircleShape)
                )
                Column {
                    Text(
                        text = scale2Name,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (scale2Selected) SgniaTheme.colors.accentPrimary else SgniaTheme.colors.textSecondary
                    )
                    Text(
                        text = "TCP $scale2Ip:$scale2Port",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = SgniaTheme.colors.textMuted
                    )
                }
            }
        }
    }
}

/**
 * SELECTOR MULTICANAL PARA REDES DE BÁSCULAS (DE 1 A 15+ BÁSCULAS)
 * Muestra el estado en paralelo de todas las básculas conectadas.
 */
@Composable
fun MultiScaleSelectorHeader(
    scales: List<TcpScaleDevice>,
    selectedScaleId: Int,
    onSelectScale: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ledPingMulti")
    val ledAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ledPulseMulti"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SgniaTheme.colors.surfaceGlass)
            .border(1.dp, SgniaTheme.colors.surfaceGlassBorder, RoundedCornerShape(14.dp))
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RED TCP/IP EN PARALELO (${scales.size} BÁSCULAS)",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = SgniaTheme.colors.accentPrimary
            )
            Text(
                text = "Detección automática sin cambios manuales",
                fontSize = 9.sp,
                color = SgniaTheme.colors.textMuted
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(scales) { scale ->
                val isSelected = scale.id == selectedScaleId
                val ledColor = if (scale.isConnected) SgniaTheme.colors.statusStable else SgniaTheme.colors.statusAlert

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) SgniaTheme.colors.accentPrimary.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .border(
                            if (isSelected) 1.dp else 0.dp,
                            if (isSelected) SgniaTheme.colors.accentPrimary else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectScale(scale.id) }
                        .padding(vertical = 6.dp, horizontal = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .graphicsLayer { alpha = if (scale.isConnected) ledAlpha else 0.7f }
                                .background(ledColor, CircleShape)
                        )
                        Column {
                            Text(
                                text = scale.name,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected) SgniaTheme.colors.accentPrimary else SgniaTheme.colors.textSecondary
                            )
                            Text(
                                text = "TCP ${scale.ip}:${scale.port} • Rx: ${scale.totalBytesReceived} B",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = SgniaTheme.colors.textMuted
                            )
                        }

                        // MOSTRAR PESO EN VIVO DE ESTA BÁSCULA (TRABAJO SIMULTÁNEO EN PARALELO)
                        Spacer(modifier = Modifier.width(4.dp))
                        val weightColor = if (scale.lastWeight > 0.0) SgniaTheme.colors.statusStable else SgniaTheme.colors.textMuted
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.3f", scale.lastWeight)} kg",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = weightColor
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. VISOR INDUSTRIAL OLED DE ALTO CONTRASTE (CERO RECOMPOSICIONES)
// =========================================================================
@Composable
fun OptimizedScaleOledDisplay(
    weightProvider: () -> Double,
    isStableProvider: () -> Boolean,
    scaleName: String = "Systel Cuora Max (30kg)",
    ipAddress: String = "192.168.100.40",
    rawFrame: String = "",
    rawHex: String = "",
    totalBytes: Long = 0L,
    communicationMode: String = "En línea",
    errorMessage: String? = null,
    onManualWeightClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "haloTransition")
    val pulsingHaloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    val isStable = isStableProvider()
    val haloColor = if (isStable) SgniaTheme.colors.statusStable else SgniaTheme.colors.statusUnstable

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SgniaTheme.colors.oledDisplayBg)
            .border(
                2.dp,
                if (isStable) SgniaTheme.colors.statusStable.copy(alpha = 0.8f)
                else haloColor.copy(alpha = pulsingHaloAlpha),
                RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // BARRA DE ESTADO TÁCTICA DEL SOCKET
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NetworkWifi,
                        contentDescription = null,
                        tint = SgniaTheme.colors.statusStable,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SOCKET TCP/IP: $ipAddress",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SgniaTheme.colors.textSecondary
                    )
                }

                // BADGE ESTABLE / OSCILANDO
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(haloColor.copy(alpha = 0.2f))
                        .border(1.dp, haloColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(haloColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isStable) "ESTABLE" else "PESANDO…",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = haloColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DÍGITOS GIGANTES OLED DE ALTO CONTRASTE A DISTANCIA (Click para ingreso manual)
            val currentWeight = weightProvider()
            val formattedWeight = String.format(Locale.US, "%.3f", currentWeight)

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = if (onManualWeightClick != null) Modifier.clickable { onManualWeightClick() } else Modifier
            ) {
                Text(
                    text = formattedWeight,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 54.sp,
                    letterSpacing = 3.sp,
                    color = SgniaTheme.colors.oledDigitColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "kg",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = SgniaTheme.colors.accentSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$scaleName • PRECISIÓN 3 DECIMALES (0.000 kg)",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = SgniaTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(4.dp))
            if (totalBytes > 0L) {
                Text(
                    text = "📡 Rx: $totalBytes B • $communicationMode • Trama: \"$rawFrame\"",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = SgniaTheme.colors.statusStable
                )
                if (rawHex.isNotBlank()) {
                    Text(
                        text = "Hex: $rawHex",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = SgniaTheme.colors.textMuted
                    )
                }
            } else {
                Text(
                    text = "📡 $communicationMode • 0 bytes recibidos",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = Color(0xFFFFB300)
                )
                Text(
                    text = "💡 Si la báscula está en modo ticket, coloca producto y presiona IMPRIMIR",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    color = SgniaTheme.colors.textMuted
                )
            }

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF9800).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFFF9800), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFFB74D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// =========================================================================
// 7. ODÓMETRO FINANCIERO CON NÚMEROS RODANTES
// =========================================================================
@Composable
fun TacticalOdometerCounter(
    value: Double,
    prefix: String = "$",
    suffix: String = " MXN",
    modifier: Modifier = Modifier
) {
    val formatted = String.format(Locale.US, "%,.2f", value)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = prefix,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = SgniaTheme.colors.accentPrimary
        )
        AnimatedContent(
            targetState = formatted,
            transitionSpec = {
                slideInVertically { height -> height } togetherWith slideOutVertically { height -> -height }
            },
            label = "odometerAnimation"
        ) { targetText ->
            Text(
                text = targetText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = SgniaTheme.colors.textPrimary
            )
        }
        Text(
            text = suffix,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = SgniaTheme.colors.textSecondary
        )
    }
}

// =========================================================================
// 8. BOTÓN VIP CON SHADER DE BRILLO CONTINUO EN ÁNGULO DE 45°
// =========================================================================
@Composable
fun ShimmerVipButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmerTransition")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = SgniaTheme.colors.shimmerColors

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(52.dp)
            .graphicsLayer {
                shape = RoundedCornerShape(14.dp)
                clip = true
            }
            .drawBehind {
                val brush = Brush.linearGradient(
                    colors = listOf(
                        shimmerColors[0].copy(alpha = 0.8f),
                        shimmerColors[1],
                        shimmerColors[2].copy(alpha = 0.8f)
                    ),
                    start = Offset(shimmerTranslate, 0f),
                    end = Offset(shimmerTranslate + 200f, size.height)
                )
                drawRect(brush = brush)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            icon?.invoke()
            if (icon != null) Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = Color.Black
            )
        }
    }
}

// =========================================================================
// 9. IMAGEN ASÍNCRONA DE PRODUCTO CON SHIMMER PLACEHOLDER Y CACHÉ
// =========================================================================
private val inMemoryBitmapCache = mutableMapOf<String, android.graphics.Bitmap>()

@Composable
fun AsyncProductImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(imageUrl) { mutableStateOf(imageUrl?.let { inMemoryBitmapCache[it] }) }
    var isLoading by remember(imageUrl) { mutableStateOf(bitmap == null && !imageUrl.isNullOrEmpty()) }

    LaunchedEffect(imageUrl) {
        if (!imageUrl.isNullOrEmpty() && bitmap == null) {
            isLoading = true
            withContext(Dispatchers.IO) {
                try {
                    val url = URL(imageUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 3000
                    connection.readTimeout = 3000
                    connection.doInput = true
                    connection.connect()
                    val input = connection.inputStream
                    val decoded = BitmapFactory.decodeStream(input)
                    if (decoded != null) {
                        inMemoryBitmapCache[imageUrl] = decoded
                        bitmap = decoded
                    }
                } catch (_: Exception) {
                    // Fallback a placeholder si falla conexión
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SgniaTheme.colors.surfaceGlass)
            .border(1.dp, SgniaTheme.colors.surfaceGlassBorder, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Icono o Shimmer si está cargando
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = SgniaTheme.colors.accentSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// =========================================================================
// 10. BOTONES PILL DE ALTO IMPACTO (ESTILO PLANTILLA NOIR & CREMA)
// =========================================================================
@Composable
fun SgniaPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    containerColor: Color = SgniaTheme.colors.accentPrimary,
    contentColor: Color = Color(0xFF141210)
) {
    val haptic = LocalHapticFeedback.current
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.35f),
            disabledContentColor = contentColor.copy(alpha = 0.35f)
        ),
        modifier = modifier.height(52.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.invoke()
            if (icon != null) Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun SgniaOutlinedPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    borderColor: Color = SgniaTheme.colors.accentPrimary,
    textColor: Color = SgniaTheme.colors.accentPrimary
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SgniaTheme.colors.surfaceGlass)
            .border(1.5.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.invoke()
            if (icon != null) Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = textColor
            )
        }
    }
}

// =========================================================================
// 11. CARRUSEL HORIZONTAL DE CATEGORÍAS EN FORMA DE PÍLDORA (CHIPS)
// =========================================================================
@Composable
fun SgniaCategoryPillRow(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { cat ->
            val isSelected = cat.equals(selectedCategory, ignoreCase = true)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) SgniaTheme.colors.accentPrimary
                        else SgniaTheme.colors.surfaceGlass
                    )
                    .border(
                        1.dp,
                        if (isSelected) SgniaTheme.colors.accentPrimary else SgniaTheme.colors.surfaceGlassBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelectCategory(cat) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cat,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = if (isSelected) Color(0xFF141210) else SgniaTheme.colors.textSecondary
                )
            }
        }
    }
}

// =========================================================================
// 12. TARJETA ARTESANAL DE CORTE/PRODUCTO (PLANTILLA SOFISTICADA)
// =========================================================================
@Composable
fun SgniaArtisanalProductCard(
    name: String,
    pluCode: Int,
    category: String,
    pricePerKg: Double,
    imageUrl: String? = null,
    stockKg: Double? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onActionClick: (() -> Unit)? = null,
    actionLabel: String = "Pesar",
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) SgniaTheme.colors.accentPrimary.copy(alpha = 0.15f)
                else SgniaTheme.colors.surfaceGlass
            )
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) SgniaTheme.colors.accentPrimary else SgniaTheme.colors.surfaceGlassBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Miniatura redondeada estilo de la plantilla
            AsyncProductImage(
                imageUrl = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            // Información central
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SgniaTheme.colors.accentPrimary.copy(alpha = 0.22f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "#$pluCode",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = SgniaTheme.colors.accentPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category,
                        fontSize = 10.sp,
                        color = SgniaTheme.colors.textMuted
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = name,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SgniaTheme.colors.textPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$${String.format(java.util.Locale.US, "%.2f", pricePerKg)} /kg",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = SgniaTheme.colors.oledDigitColor
                    )
                    if (stockKg != null) {
                        Text(
                            text = "• Disp: ${String.format(java.util.Locale.US, "%.1f", stockKg)}kg",
                            fontSize = 10.sp,
                            color = SgniaTheme.colors.textMuted
                        )
                    }
                }
            }

            // Botón de acción pill lateral
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) SgniaTheme.colors.accentPrimary
                        else SgniaTheme.colors.accentPrimary.copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        SgniaTheme.colors.accentPrimary,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (onActionClick != null) onActionClick() else onClick()
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionLabel,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = if (isSelected) Color(0xFF141210) else SgniaTheme.colors.accentPrimary
                )
            }
        }
    }
}

// =========================================================================
// 13. PLANTILLA EJECUTIVA CERHAN (NEGRO MATE & CREMA MARFIL)
// =========================================================================

data class SgniaSidebarItem(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badge: String? = null
)

data class SgniaQuickActionItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

/**
 * Menú Lateral Izquierdo (Sidebar) idéntico a la plantilla CERHAN en Negro Mate y Crema.
 */
@Composable
fun SgniaCerhanSidebar(
    brandTitle: String = "SGNIA",
    subtitle: String = "POS & INVENTARIO",
    items: List<SgniaSidebarItem>,
    selectedItemId: String,
    onSelectItem: (String) -> Unit,
    onLogout: () -> Unit,
    supportText: String = "Red TCP/IP Activa",
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = modifier
            .width(265.dp)
            .fillMaxSize()
            .background(Color(0xFF141312))
            .border(width = 1.dp, color = Color(0xFF2B2824))
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cabecera con Logo SGNIA + Nombre
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3EFE6))
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.inventariocarniceria.R.drawable.logo),
                        contentDescription = "Logo SGNIA",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = brandTitle,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFFF3EFE6)
                    )
                    Text(
                        text = subtitle,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp,
                        color = Color(0xFFA89F91)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lista vertical de pestañas laterales con pestaña crema a la derecha
            items.forEach { item ->
                val isSelected = item.id == selectedItemId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectItem(item.id)
                        }
                        .background(
                            if (isSelected) Color(0xFF23201D) else Color.Transparent
                        )
                        .padding(start = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) Color(0xFFF3EFE6) else Color(0xFF9E9589),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = item.label,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isSelected) Color(0xFFF3EFE6) else Color(0xFFB5ACA0)
                        )
                        if (item.badge != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF3EFE6).copy(alpha = 0.18f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.badge,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFFF3EFE6)
                                )
                            }
                        }
                    }

                    // Indicador crema en el borde derecho (idéntico a la imagen 1 de CERHAN)
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(30.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                                .background(Color(0xFFF3EFE6))
                        )
                    }
                }
            }
        }

        // Pie de menú lateral: Estado y Cerrar Sesión (Logout)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NetworkWifi,
                    contentDescription = null,
                    tint = Color(0xFF9E9589),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = supportText,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = Color(0xFF9E9589)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF23201D))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLogout()
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Cerrar Sesión",
                    tint = Color(0xFFF3EFE6),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Cerrar Sesión",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFFF3EFE6)
                )
            }
        }
    }
}

/**
 * Barra superior estilo CERHAN con botón de Menú Lateral (al costado), barra de búsqueda y controles.
 */
@Composable
fun SgniaCerhanTopBar(
    onOpenDrawer: () -> Unit,
    searchPlaceholder: String = "Buscar corte, código o caja...",
    onSearchClick: (() -> Unit)? = null,
    onToggleTheme: (() -> Unit)? = null,
    rightActionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onRightActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón para abrir el menú desplegable del costado
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1A1816))
                    .border(1.dp, Color(0xFFEEDDC8).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenDrawer()
                    }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "☰",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF3EFE6)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MENÚ",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp,
                        color = Color(0xFFF3EFE6)
                    )
                }
            }
        }

        // Píldora de búsqueda rápida + Iconos derechos
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (onSearchClick != null) {
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1F1D1A))
                        .border(1.dp, Color(0xFF38332D), RoundedCornerShape(20.dp))
                        .clickable { onSearchClick() }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔍",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = searchPlaceholder,
                            fontSize = 12.sp,
                            color = Color(0xFFA89F91),
                            maxLines = 1
                        )
                    }
                }
            }

            if (rightActionIcon != null && onRightActionClick != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F1D1A))
                        .border(1.dp, Color(0xFF38332D), CircleShape)
                        .clickable { onRightActionClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = rightActionIcon,
                        contentDescription = null,
                        tint = Color(0xFFF3EFE6),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (onToggleTheme != null) {
                SgniaThemeToggle(
                    currentMode = SgniaTheme.mode,
                    onToggle = onToggleTheme
                )
            }
        }
    }
}

/**
 * Encabezado de saludo estilo CERHAN ("Buenas tardes, Administrador Principal")
 */
@Composable
fun SgniaCerhanGreetingHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = 23.sp,
            color = SgniaTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = subtitle,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = SgniaTheme.colors.textSecondary
        )
    }
}

@Composable
fun SgniaCerhanGreetingHeader(
    userName: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    greetingPrefix: String = "Buenas tardes,"
) {
    SgniaCerhanGreetingHeader(
        title = "$greetingPrefix $userName",
        subtitle = subtitle,
        modifier = modifier
    )
}

/**
 * Tarjeta Numerada Horizontal (01., 02., 03., 04.) con iconos y gráfica integrada estilo CERHAN.
 */
@Composable
fun SgniaCerhanNumberedCard(
    numberPrefix: String,
    title: String,
    description: String,
    bottomMetric: String,
    primaryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    secondaryIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    showChart: Boolean = true,
    isHighlighted: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF181715))
            .border(
                width = if (isHighlighted) 1.8.dp else 1.dp,
                color = if (isHighlighted) Color(0xFFEEDDC8) else Color(0xFF2E2B27),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna izquierda: 01. Título, descripción y métrica inferior
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = numberPrefix,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 21.sp,
                        color = Color(0xFFD6CEC2)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFFFAF6F0)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = Color(0xFFB5ACA0),
                    lineHeight = 16.sp
                )
                if (bottomMetric.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = bottomMetric,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFF3EFE6)
                    )
                }
            }

            // Columna derecha: Iconos superiores y mini gráfica vectorial inferior
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = primaryIcon,
                        contentDescription = null,
                        tint = Color(0xFFEEDDC8),
                        modifier = Modifier.size(26.dp)
                    )
                    if (secondaryIcon != null) {
                        Icon(
                            imageVector = secondaryIcon,
                            contentDescription = null,
                            tint = Color(0xFFD6CEC2),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (showChart) {
                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .width(72.dp)
                            .height(24.dp)
                    ) {
                        val barWidth = size.width / 9f
                        val heights = listOf(0.35f, 0.55f, 0.45f, 0.75f, 0.9f)
                        heights.forEachIndexed { idx, ratio ->
                            val barH = size.height * ratio
                            val x = idx * (barWidth * 1.8f) + 6f
                            drawRect(
                                color = Color(0xFF8A8175).copy(alpha = 0.55f),
                                topLeft = Offset(x, size.height - barH),
                                size = androidx.compose.ui.geometry.Size(barWidth, barH)
                            )
                        }
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, size.height * 0.75f)
                            cubicTo(
                                size.width * 0.3f, size.height * 0.2f,
                                size.width * 0.6f, size.height * 0.8f,
                                size.width, size.height * 0.15f
                            )
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFFEEDDC8),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SgniaCerhanNumberedCard(
    number: String,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    showWaveChart: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    SgniaCerhanNumberedCard(
        numberPrefix = number,
        title = title,
        description = subtitle,
        bottomMetric = "",
        primaryIcon = icon,
        showChart = showWaveChart,
        onClick = onClick,
        modifier = modifier
    )
}

/**
 * Tarjeta Especial de Báscula en Vivo (01. Báscula 1 / 02. Báscula 2) con el estilo exacto de la plantilla CERHAN.
 */
@Composable
fun SgniaCerhanDualScaleCard(
    numberPrefix: String,
    scaleDevice: TcpScaleDevice,
    liveWeightKg: Double,
    isStable: Boolean,
    isActiveScale: Boolean,
    onSelectScale: () -> Unit,
    onManualWeightClick: () -> Unit,
    onTareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val formattedWeight = remember(liveWeightKg) {
        String.format(Locale.US, "%.3f", liveWeightKg)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF181715))
            .border(
                width = if (isActiveScale) 2.dp else 1.dp,
                color = if (isActiveScale) Color(0xFFEEDDC8) else Color(0xFF2E2B27),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSelectScale()
            }
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = numberPrefix,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal,
                            fontSize = 21.sp,
                            color = Color(0xFFD6CEC2)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = scaleDevice.name,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFFFAF6F0)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isActiveScale) Color(0xFFF3EFE6)
                                    else Color(0xFF2B2824)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isActiveScale) "ACTIVA" else "TOCAR PARA USAR",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                color = if (isActiveScale) Color(0xFF141312) else Color(0xFFB5ACA0)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Red TCP/IP: ${scaleDevice.ip}:${scaleDevice.port} • ${if (scaleDevice.isConnected) "Conectada en vivo" else "Esperando lectura / Ticket"}",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = Color(0xFFB5ACA0)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Scale,
                    contentDescription = null,
                    tint = Color(0xFFEEDDC8),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lectura de Peso Digital + Gráfica y Controles Rápidos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formattedWeight,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        color = if (isActiveScale) Color(0xFFF3EFE6) else Color(0xFFC7BBAA)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "kg",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFD6CEC2)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isStable) "● ESTABLE" else "◌ MOVIMIENTO",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (isStable) Color(0xFFD6CEC2) else Color(0xFFFFB74D)
                    )
                }

                // Botones crema compactos de acción rápida en la báscula
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2B2824))
                            .border(1.dp, Color(0xFF4A443D), RoundedCornerShape(10.dp))
                            .clickable { onTareClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = ">0< Tara",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFF3EFE6)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF3EFE6))
                            .clickable { onManualWeightClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✏️ Peso",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color(0xFF141312)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta "Acceso Rápido" con cuadrícula 2x2 de botones Crema con texto Negro (Idéntica a la plantilla CERHAN).
 */
@Composable
fun SgniaCerhanQuickAccessCard(
    title: String = "Acceso Rápido",
    actions: List<SgniaQuickActionItem>,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF181715))
            .border(1.dp, Color(0xFF2E2B27), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFFFAF6F0)
            )

            val chunked = actions.chunked(2)
            chunked.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { action ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3EFE6))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    action.onClick()
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.label,
                                    tint = Color(0xFF141312),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = action.label,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF141312),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta "Resumen Global" con números grandes y gráficas vectoriales de líneas y barras (Idéntica a la plantilla CERHAN).
 */
@Composable
fun SgniaCerhanGlobalSummaryCard(
    title: String = "Resumen Global",
    firstLabel: String,
    firstValue: String,
    secondLabel: String,
    secondValue: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF181715))
            .border(1.dp, Color(0xFF2E2B27), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFFFAF6F0)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Métrica 1
            Text(
                text = firstLabel,
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                color = Color(0xFFB5ACA0)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = firstValue,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = Color(0xFFFAF6F0)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF221F1B))
                        .border(1.dp, Color(0xFF38332D), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "HARDWARE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFFEEDDC8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF2E2B27))
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Métrica 2
            Text(
                text = secondLabel,
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                color = Color(0xFFB5ACA0)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = secondValue,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = Color(0xFFFAF6F0)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF221F1B))
                        .border(1.dp, Color(0xFF38332D), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "EN LÍNEA",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF4ADE80)
                    )
                }
            }
        }
    }
}

@Composable
fun SgniaCerhanGlobalSummaryCard(
    metric1Label: String,
    metric1Value: String,
    metric1Sub: String = "",
    metric2Label: String,
    metric2Value: String,
    metric2Sub: String = "",
    barValues: List<Float> = emptyList(),
    modifier: Modifier = Modifier
) {
    SgniaCerhanGlobalSummaryCard(
        title = "Resumen Global",
        firstLabel = if (metric1Sub.isNotBlank()) "$metric1Label ($metric1Sub)" else metric1Label,
        firstValue = metric1Value,
        secondLabel = if (metric2Sub.isNotBlank()) "$metric2Label ($metric2Sub)" else metric2Label,
        secondValue = metric2Value,
        modifier = modifier
    )
}
