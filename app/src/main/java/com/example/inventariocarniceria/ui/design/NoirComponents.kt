package com.example.inventariocarniceria.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =====================================================================
// ETIQUETA "PLACA DE ESPECIFICACIÓN"
// =====================================================================

@Composable
fun NoirLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = NoirColors.TextSecondary
) {
    Text(
        text = text.uppercase(),
        style = NoirType.label,
        color = color,
        modifier = modifier
    )
}

// =====================================================================
// TARJETA BENTO
// =====================================================================

/**
 * Celda bento: superficie plana con borde fino de 1dp, sin sombras.
 * [hero] = true pinta la celda en crema (máximo una por pantalla).
 */
@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    hero: Boolean = false,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(NoirSpace.lg),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(NoirSpace.sm),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(NoirRadius.lg)
    val bg = if (hero) NoirColors.Cream else NoirColors.Surface
    val borderColor = when {
        hero -> NoirColors.Cream
        selected -> NoirColors.Cream
        else -> NoirColors.Hairline
    }
    val borderWidth = if (selected && !hero) 1.5.dp else 1.dp

    Column(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(borderWidth, borderColor, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        verticalArrangement = verticalArrangement,
        content = content
    )
}

// =====================================================================
// PÍLDORA DE ESTADO
// =====================================================================

enum class PillTone { Neutral, Positive, Pending, Transit, Warning, Danger, Gold }

@Composable
fun StatusPill(
    text: String,
    tone: PillTone = PillTone.Neutral,
    modifier: Modifier = Modifier,
    showDot: Boolean = true
) {
    val (fg, bg) = when (tone) {
        PillTone.Neutral -> NoirColors.TextSecondary to NoirColors.SurfaceRaised
        PillTone.Positive -> NoirColors.Emerald to NoirColors.EmeraldMuted
        PillTone.Pending -> NoirColors.Amber to NoirColors.AmberMuted
        PillTone.Transit -> NoirColors.Slate to NoirColors.SlateMuted
        PillTone.Warning -> NoirColors.Amber to NoirColors.AmberMuted
        PillTone.Danger -> NoirColors.Danger to NoirColors.DangerMuted
        PillTone.Gold -> NoirColors.Gold to NoirColors.GoldMuted
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(NoirRadius.pill))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text.uppercase(),
            style = NoirType.label.copy(fontSize = 10.sp, letterSpacing = 1.2.sp),
            color = fg
        )
    }
}

// =====================================================================
// MÉTRICA GRANDE
// =====================================================================

/**
 * Cifra dominante con unidad y etiqueta. Pensada para leerse en 2 segundos.
 */
@Composable
fun BigMetric(
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    label: String? = null,
    style: TextStyle = NoirType.metricL,
    valueColor: Color = NoirColors.TextPrimary,
    labelColor: Color = NoirColors.TextSecondary
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label != null) NoirLabel(label, color = labelColor)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, style = style, color = valueColor, maxLines = 1)
            if (unit != null) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = unit,
                    fontFamily = FontFamily.Monospace,
                    fontSize = (style.fontSize.value * 0.38f).coerceAtLeast(12f).sp,
                    fontWeight = FontWeight.Medium,
                    color = labelColor,
                    modifier = Modifier.padding(bottom = (style.fontSize.value * 0.12f).dp)
                )
            }
        }
    }
}

// =====================================================================
// BOTONES
// =====================================================================

@Composable
fun NoirPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    height: Dp = NoirSpace.touch
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(NoirRadius.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = NoirColors.Cream,
            contentColor = NoirColors.OnCream,
            disabledContainerColor = NoirColors.SurfaceRaised,
            disabledContentColor = NoirColors.TextMuted
        ),
        contentPadding = PaddingValues(horizontal = NoirSpace.lg),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = NoirType.button)
    }
}

@Composable
fun NoirSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    height: Dp = NoirSpace.touch
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(NoirRadius.md),
        border = BorderStroke(1.dp, NoirColors.HairlineStrong),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = NoirColors.TextPrimary,
            disabledContentColor = NoirColors.TextMuted
        ),
        contentPadding = PaddingValues(horizontal = NoirSpace.md),
        modifier = modifier.height(height)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = NoirType.button.copy(fontSize = 15.sp))
    }
}

// =====================================================================
// SELECTOR SEGMENTADO
// =====================================================================

@Composable
fun NoirSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(NoirRadius.sm))
            .background(NoirColors.SurfaceSunken)
            .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSel = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSel) NoirColors.Cream else Color.Transparent)
                    .clickable { onSelect(index) }
                    .heightIn(min = 36.dp)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.uppercase(),
                    style = NoirType.label.copy(fontSize = 11.sp, letterSpacing = 1.2.sp),
                    color = if (isSel) NoirColors.OnCream else NoirColors.TextSecondary
                )
            }
        }
    }
}

// =====================================================================
// PIN: INDICADOR Y TECLADO TÁCTIL GRANDE
// =====================================================================

@Composable
fun NoirPinDots(
    length: Int,
    maxDigits: Int = 4,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxDigits) { i ->
            val filled = i < length
            val color = when {
                isError -> NoirColors.Danger
                filled -> NoirColors.Cream
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NoirColors.SurfaceSunken)
                    .border(
                        1.dp,
                        if (isError) NoirColors.Danger else if (filled) NoirColors.HairlineStrong else NoirColors.Hairline,
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

/**
 * Teclado numérico de teclas grandes (68dp) para uso con guantes.
 * Distribución: 1-9 · borrar / 0 / confirmar.
 */
@Composable
fun NoirKeypad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    keyHeight: Dp = 68.dp
) {
    val haptic = LocalHapticFeedback.current
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("DEL", "0", "OK")
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key ->
                    val isOk = key == "OK"
                    val shape = RoundedCornerShape(NoirRadius.md)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(keyHeight)
                            .clip(shape)
                            .background(if (isOk) NoirColors.Cream else NoirColors.SurfaceRaised)
                            .border(1.dp, if (isOk) NoirColors.Cream else NoirColors.Hairline, shape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                when (key) {
                                    "DEL" -> onDelete()
                                    "OK" -> onConfirm()
                                    else -> onDigit(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (key) {
                            "DEL" -> Icon(
                                Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Borrar",
                                tint = NoirColors.TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            "OK" -> Icon(
                                Icons.Default.Check,
                                contentDescription = "Confirmar",
                                tint = NoirColors.OnCream,
                                modifier = Modifier.size(28.dp)
                            )
                            else -> Text(
                                text = key,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                fontSize = 28.sp,
                                color = NoirColors.TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
// PREVIEWS DEL SISTEMA DE COMPONENTES
// =====================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0F0E0D, widthDp = 380)
@Composable
private fun NoirComponentsPreview() {
    Column(
        modifier = Modifier
            .background(NoirColors.Background)
            .padding(NoirSpace.screen),
        verticalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPill("Completado", PillTone.Positive)
            StatusPill("Pendiente", PillTone.Pending)
            StatusPill("En ruta", PillTone.Transit)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(NoirSpace.gutter)) {
            BentoCard(modifier = Modifier.weight(1f)) {
                BigMetric(value = "12.480", unit = "kg", label = "Volumen")
            }
            BentoCard(modifier = Modifier.weight(1f), hero = true) {
                BigMetric(
                    value = "8.4",
                    unit = "%",
                    label = "Merma",
                    valueColor = NoirColors.OnCream,
                    labelColor = NoirColors.OnCreamSecondary
                )
            }
        }
        NoirPrimaryButton(text = "Continuar", onClick = {})
    }
}
