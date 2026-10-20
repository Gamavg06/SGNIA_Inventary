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
