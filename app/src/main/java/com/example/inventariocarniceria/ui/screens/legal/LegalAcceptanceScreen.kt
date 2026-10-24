package com.example.inventariocarniceria.ui.screens.legal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.R
import com.example.inventariocarniceria.data.preferences.LegalPreferences
import com.example.inventariocarniceria.ui.design.BentoCard
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirPrimaryButton
import com.example.inventariocarniceria.ui.design.NoirRadius
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PANTALLA 1: ACEPTACIÓN INICIAL (Solo se muestra en la primera apertura tras instalar).
 *
 * DECISIONES DE DISEÑO:
 * 1. Tarjetas Bento táctiles para Términos y Aviso de Privacidad con metadatos en píldoras que permiten lectura previa antes de consentir.
 * 2. Casilla de verificación de 48dp de alto táctil mínimo para evitar pulsaciones accidentales o frustración con guantes en mostrador.
 * 3. Botón de acción dominante con retroalimentación visual reactiva (atenuado si no hay check, crema luminoso al confirmar).
 */
@Composable
fun LegalAcceptanceScreen(
    onAcceptanceComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val legalPrefs = remember { LegalPreferences(context) }

    var isChecked by remember { mutableStateOf(false) }
    var readingDocument by remember { mutableStateOf<DocumentType?>(null) }

    // Si el usuario presiona una tarjeta, se abre el lector modal/subpantalla
    if (readingDocument != null) {
        DocumentReaderScreen(
            documentType = readingDocument!!,
            onBack = { readingDocument = null }
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NoirColors.Background)
            .padding(horizontal = NoirSpace.md, vertical = NoirSpace.lg),
        verticalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
    ) {
        // ENCABEZADO Y LOGO SGNIA
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(NoirRadius.md))
                        .background(NoirColors.SurfaceRaised)
                        .border(1.5.dp, NoirColors.HairlineStrong, RoundedCornerShape(NoirRadius.md)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sglogo),
                        contentDescription = "SGNIA Logo",
                        modifier = Modifier.size(56.dp)
                    )
                }

                NoirLabel("SGNIA CARNICERÍA · BALANZA POS", color = NoirColors.Gold)

                Text(
                    text = "Antes de comenzar",
                    style = NoirType.display.copy(fontSize = 26.sp),
                    color = NoirColors.TextPrimary
                )

                Text(
                    text = "Para garantizar la seguridad de tus datos y la validez operativa del sistema, por favor revisa y acepta los términos de uso.",
                    style = NoirType.bodySmall,
                    color = NoirColors.TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        // TARJETA TOCABLE 1: TÉRMINOS Y CONDICIONES
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                onClick = { readingDocument = DocumentType.TERMS },
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(NoirRadius.sm))
                                .background(NoirColors.SurfaceRaised),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = NoirColors.Cream,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Términos y Condiciones",
                                style = NoirType.title.copy(fontSize = 14.sp),
                                color = NoirColors.TextPrimary
                            )
                            Text(
                                text = "Licencia de uso, hardware de pesaje y deslinde",
                                style = NoirType.bodySmall.copy(fontSize = 11.sp),
                                color = NoirColors.TextMuted
                            )
                        }
                    }

                    StatusPill(text = "v1.0.0 · 2026", tone = PillTone.Gold, showDot = false)
                }
            }
        }

        // TARJETA TOCABLE 2: AVISO DE PRIVACIDAD
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                onClick = { readingDocument = DocumentType.PRIVACY },
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(NoirRadius.sm))
                                .background(NoirColors.SurfaceRaised),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = null,
                                tint = NoirColors.Cream,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Aviso de Privacidad",
                                style = NoirType.title.copy(fontSize = 14.sp),
                                color = NoirColors.TextPrimary
                            )
                            Text(
                                text = "Protección de datos locales y derechos ARCO",
                                style = NoirType.bodySmall.copy(fontSize = 11.sp),
                                color = NoirColors.TextMuted
                            )
                        }
                    }

                    StatusPill(text = "v1.0.0 · 2026", tone = PillTone.Gold, showDot = false)
                }
            }
        }

        // CASILLA GRANDE (>= 48DP)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(NoirRadius.md))
                    .background(NoirColors.Surface)
                    .border(1.dp, if (isChecked) NoirColors.Cream else NoirColors.Hairline, RoundedCornerShape(NoirRadius.md))
                    .clickable { isChecked = !isChecked }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox estilizado
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isChecked) NoirColors.Cream else NoirColors.SurfaceRaised)
                        .border(1.5.dp, if (isChecked) NoirColors.Cream else NoirColors.HairlineStrong, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isChecked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Aceptado",
                            tint = NoirColors.OnCream,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "He leído y acepto los Términos y el Aviso de Privacidad",
                    style = NoirType.bodySmall.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                    color = if (isChecked) NoirColors.TextPrimary else NoirColors.TextSecondary
                )
            }
        }

        // BOTÓN CONTINUAR
        item {
            Spacer(Modifier.height(8.dp))

            NoirPrimaryButton(
                text = "CONTINUAR",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                enabled = isChecked,
                height = 56.dp,
                onClick = {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val currentDate = dateFormat.format(Date())

                    scope.launch {
                        legalPrefs.saveAcceptance(
                            version = "v1.0.0",
                            date = currentDate,
                            role = "INITIAL_OWNER_STORE"
                        )
                        onAcceptanceComplete()
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0E0D)
@Composable
fun LegalAcceptanceScreenPreview() {
    LegalAcceptanceScreen(onAcceptanceComplete = {})
}
