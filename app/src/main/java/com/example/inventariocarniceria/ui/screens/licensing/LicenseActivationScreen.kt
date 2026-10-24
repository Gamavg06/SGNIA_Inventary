package com.example.inventariocarniceria.ui.screens.licensing

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.licensing.LicenseManager
import com.example.inventariocarniceria.ui.design.BentoCard
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirPrimaryButton
import com.example.inventariocarniceria.ui.design.NoirRadius
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun LicenseActivationScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onActivationSuccess: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var licenseInfo by remember { mutableStateOf(LicenseManager.checkLicense(context)) }
    var keyInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        licenseInfo = LicenseManager.checkLicense(context)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NoirColors.Background)
            .padding(horizontal = NoirSpace.screen),
        verticalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
    ) {
        // ESPACIADOR SUPERIOR
        item {
            Spacer(modifier = Modifier.height(NoirSpace.md))
        }

        // CABECERA NOIR & CREMA
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(NoirRadius.sm))
                                .background(NoirColors.SurfaceRaised)
                                .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                tint = NoirColors.Cream
                            )
                        }
                    }

                    Column {
                        NoirLabel(text = "SISTEMA DE SEGURIDAD INDUSTRIAL", color = NoirColors.Cream)
                        Text(
                            text = "Licencia de Operación",
                            style = NoirType.title,
                            color = NoirColors.TextPrimary
                        )
                    }
                }

                StatusPill(
                    text = when (licenseInfo.status) {
                        LicenseManager.LicenseStatus.ACTIVE -> "ACTIVADA"
                        LicenseManager.LicenseStatus.EXPIRED -> "EXPIRADA"
                        LicenseManager.LicenseStatus.UNLICENSED -> "SIN ACTIVAR"
                        LicenseManager.LicenseStatus.CLOCK_TAMPERED -> "RELOJ ALTERADO"
                    },
                    tone = when (licenseInfo.status) {
                        LicenseManager.LicenseStatus.ACTIVE -> PillTone.Positive
                        LicenseManager.LicenseStatus.EXPIRED -> PillTone.Danger
                        LicenseManager.LicenseStatus.UNLICENSED -> PillTone.Pending
                        LicenseManager.LicenseStatus.CLOCK_TAMPERED -> PillTone.Danger
                    }
                )
            }
        }

        // TARJETA BENTO 1: IDENTIFICADOR ÚNICO DE ESTE EQUIPO
        item {
            BentoCard {
                NoirLabel(text = "IDENTIFICADOR ÚNICO DE ESTE DISPOSITIVO")
                Text(
                    text = "Proporciona este código a SGNIA COMPANY para generar tu clave de activación o renovación de renta.",
                    style = NoirType.bodySmall,
                    color = NoirColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(NoirSpace.xs))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(NoirRadius.sm))
                        .background(NoirColors.SurfaceSunken)
                        .border(1.dp, NoirColors.HairlineStrong, RoundedCornerShape(NoirRadius.sm))
                        .padding(NoirSpace.md),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = licenseInfo.hardwareId,
                        style = NoirType.metricL,
                        color = NoirColors.Cream,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(NoirSpace.xs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(licenseInfo.hardwareId))
                            Toast.makeText(context, "ID copiado al portapapeles: ${licenseInfo.hardwareId}", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(NoirRadius.sm),
                        border = BorderStroke(1.dp, NoirColors.Hairline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirColors.TextPrimary),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NoirColors.Cream, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar ID", style = NoirType.bodySmall, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val msg = "Hola Gamaliel, solicito activación para el POS Carnicería SGNIA.\nMi Hardware ID es: ${licenseInfo.hardwareId}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/522761095688?text=${Uri.encode(msg)}"))
                            runCatching { context.startActivity(intent) }.onFailure {
                                Toast.makeText(context, "No se pudo abrir WhatsApp.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(NoirRadius.sm),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoirColors.SurfaceRaised,
                            contentColor = NoirColors.Emerald
                        ),
                        border = BorderStroke(1.dp, NoirColors.Emerald.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1.3f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = NoirColors.Emerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp (+52 276 109 5688)", style = NoirType.bodySmall, fontWeight = FontWeight.Bold, color = NoirColors.Emerald)
                    }
                }
            }
        }

        // TARJETA BENTO 2: ESTADO ACTUAL DE LA LICENCIA
        item {
            BentoCard {
                NoirLabel(text = "ESTADO DEL CONTRATO Y VENCIMIENTO")

                when (licenseInfo.status) {
                    LicenseManager.LicenseStatus.ACTIVE -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NoirColors.EmeraldMuted),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NoirColors.Emerald)
                            }
                            Column {
                                Text(
                                    text = licenseInfo.tier?.label ?: "Licencia Activa",
                                    style = NoirType.title,
                                    color = NoirColors.Emerald
                                )
                                val expiryText = if (licenseInfo.expiresAt == -1L) {
                                    "Licencia Perpetua / Compra Definitiva. No requiere renovaciones."
                                } else {
                                    val dateFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(licenseInfo.expiresAt))
                                    val daysLeft = TimeUnit.MILLISECONDS.toDays(licenseInfo.remainingMillis)
                                    val hoursLeft = TimeUnit.MILLISECONDS.toHours(licenseInfo.remainingMillis) % 24
                                    val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(licenseInfo.remainingMillis) % 60
                                    if (daysLeft > 0) {
                                        "Vence el $dateFmt (Quedan $daysLeft días, $hoursLeft horas)"
                                    } else {
                                        "Vence el $dateFmt (Quedan $hoursLeft h, $minutesLeft min)"
                                    }
                                }
                                Text(
                                    text = expiryText,
                                    style = NoirType.bodySmall,
                                    color = NoirColors.TextSecondary
                                )
                            }
                        }
                    }

                    LicenseManager.LicenseStatus.EXPIRED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NoirColors.DangerMuted),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = NoirColors.Danger)
                            }
                            Column {
                                Text(
                                    text = "Periodo de Servicio Finalizado",
                                    style = NoirType.title,
                                    color = NoirColors.Danger
                                )
                                Text(
                                    text = "Tu renta o prueba ha expirado. Ingresa una nueva clave de renovación o compra para desbloquear todas las funciones.",
                                    style = NoirType.bodySmall,
                                    color = NoirColors.TextSecondary
                                )
                            }
                        }
                    }

                    LicenseManager.LicenseStatus.CLOCK_TAMPERED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NoirColors.DangerMuted),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = NoirColors.Danger)
                            }
                            Column {
                                Text(
                                    text = "Reloj del Sistema Manipulado",
                                    style = NoirType.title,
                                    color = NoirColors.Danger
                                )
                                Text(
                                    text = "Se detectó que la fecha del dispositivo fue retrocedida. Corrige la hora en Ajustes de Android para reactivar el sistema.",
                                    style = NoirType.bodySmall,
                                    color = NoirColors.TextSecondary
                                )
                            }
                        }
                    }

                    LicenseManager.LicenseStatus.UNLICENSED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NoirColors.AmberMuted),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = NoirColors.Amber)
                            }
                            Column {
                                Text(
                                    text = "Equipo Pendiente de Activación",
                                    style = NoirType.title,
                                    color = NoirColors.Amber
                                )
                                Text(
                                    text = "Ingresa tu clave de 10 min, prueba de 7 días, renta mensual o compra definitiva.",
                                    style = NoirType.bodySmall,
                                    color = NoirColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // TARJETA BENTO 3: FORMULARIO DE INGRESO DE CLAVE
        item {
            BentoCard {
                NoirLabel(text = "INGRESAR CLAVE DE ACTIVACIÓN")

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = {
                        keyInput = it.uppercase(Locale.US)
                        errorMessage = null
                        successMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Clave de Activación (ej. SGN-01M-8F3A-B9C4-E2F1)", color = NoirColors.TextSecondary) },
                    placeholder = { Text("SGN-XXXX-XXXX-XXXX-XXXX", color = NoirColors.TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null, tint = NoirColors.Cream)
                    },
                    textStyle = NoirType.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NoirColors.TextPrimary),
                    shape = RoundedCornerShape(NoirRadius.sm),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoirColors.Cream,
                        unfocusedBorderColor = NoirColors.Hairline,
                        focusedContainerColor = NoirColors.SurfaceSunken,
                        unfocusedContainerColor = NoirColors.SurfaceSunken,
                        focusedTextColor = NoirColors.TextPrimary,
                        unfocusedTextColor = NoirColors.TextPrimary
                    )
                )

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = "⚠️ $errorMessage",
                        style = NoirType.bodySmall,
                        color = NoirColors.Danger,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!successMessage.isNullOrBlank()) {
                    Text(
                        text = "✅ $successMessage",
                        style = NoirType.bodySmall,
                        color = NoirColors.Emerald,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(NoirSpace.xs))

                NoirPrimaryButton(
                    text = "ACTIVAR LICENCIA",
                    icon = Icons.Default.Security,
                    enabled = keyInput.trim().length >= 10,
                    onClick = {
                        val result = LicenseManager.activateKey(context, keyInput)
                        if (result.isSuccess) {
                            licenseInfo = result.getOrThrow()
                            successMessage = "¡Activación exitosa! ${licenseInfo.tier?.label} habilitada correctamente."
                            errorMessage = null
                            keyInput = ""
                            onActivationSuccess?.invoke()
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Error al validar la clave."
                            successMessage = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // TARJETA BENTO 4: INFORMACIÓN DE CONTACTO DIRECTO DEL DESARROLLADOR
        item {
            BentoCard {
                NoirLabel(text = "SOPORTE Y RENOVACIONES · SGNIA COMPANY")
                Text(
                    text = "Gamaliel Velazquez Galicia • Liderazgo Técnico",
                    style = NoirType.body,
                    fontWeight = FontWeight.Bold,
                    color = NoirColors.TextPrimary
                )
                Text(
                    text = "Juan Jiménez 405, Centro, 75120 Nopalucan de la Granja, Puebla, México\nWhatsApp: +52 276 109 5688 • Correo: gmlvega0025@gmail.com",
                    style = NoirType.bodySmall,
                    color = NoirColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(NoirSpace.xs))
                Text(
                    text = "🌐 sgnia.xyz • Software que Protege y Hace Crecer tu Negocio",
                    style = NoirType.label,
                    color = NoirColors.Cream
                )
            }
        }

        // ESPACIADOR INFERIOR
        item {
            Spacer(modifier = Modifier.height(NoirSpace.xl))
        }
    }
}
