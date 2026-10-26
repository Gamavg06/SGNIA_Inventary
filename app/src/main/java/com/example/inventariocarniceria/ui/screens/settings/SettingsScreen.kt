package com.example.inventariocarniceria.ui.screens.settings

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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.ui.components.SgniaGlobalSummaryCard
import com.example.inventariocarniceria.ui.components.SgniaGreetingHeader
import com.example.inventariocarniceria.ui.components.SgniaNumberedCard
import com.example.inventariocarniceria.ui.screens.inventory.QendraCatalogImportDialog
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import androidx.compose.ui.text.font.FontFamily
import com.example.inventariocarniceria.ui.design.NoirColors
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NetworkCheck

@Composable
fun SettingsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier,
    onOpenAbout: () -> Unit = {},
    onOpenLicensing: () -> Unit = {}
) {
    val scalesList by viewModel.scalesList.collectAsState()
    val scaleState by viewModel.scaleState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var isScaleConfigDialogOpen by remember { mutableStateOf(false) }
    var showResetStep1 by remember { mutableStateOf(false) }
    var showResetStep2 by remember { mutableStateOf(false) }

    val matteBg = NoirColors.Background
    val cardBg = NoirColors.Surface
    val cardBorder = NoirColors.Hairline
    val creamPrimary = NoirColors.TextPrimary
    val creamWarm = NoirColors.Cream
    val textMuted = NoirColors.TextMuted
    val darkText = NoirColors.OnCream

    val onlineCount = scalesList.count { it.isConnected }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(matteBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ==========================================================
        // ENCABEZADO SGNIA
        // ==========================================================
        item {
            SgniaGreetingHeader(
                title = "Ajustes del Sistema",
                subtitle = "Configuración TCP/IP de Básculas y Red Local • ${uiState.currentBranch.ifBlank { "Sucursal" }}"
            )
        }

        // ==========================================================
        // TARJETAS NUMERADAS 01. Y 02.
        // ==========================================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SgniaNumberedCard(
                    modifier = Modifier.weight(1f),
                    number = "01.",
                    title = "Básculas en Red",
                    subtitle = "$onlineCount/${scalesList.size} equipos en línea",
                    icon = Icons.Default.Scale,
                    showWaveChart = true
                )
                SgniaNumberedCard(
                    modifier = Modifier.weight(1f),
                    number = "02.",
                    title = "Socket TCP/IP",
                    subtitle = "Hardware físico directo",
                    icon = Icons.Default.Router,
                    showWaveChart = false
                )
            }
        }

        // ==========================================================
        // TARJETA 03. RED DE BÁSCULAS (SINCRONIZACIÓN TCP/IP)
        // ==========================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("03.", fontSize = 18.sp, fontWeight = FontWeight.Black, color = creamPrimary)
                        Text("Red de Básculas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = creamWarm)
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF121110))
                            .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(if (onlineCount > 0) Color(0xFF4ADE80) else textMuted, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$onlineCount/${scalesList.size} EN LÍNEA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = creamPrimary
                        )
                    }
                }

                Text(
                    text = "Sincronización de Red TCP/IP: configura IP/puerto, realiza pruebas de Ping, monitorea tráfico recibido o restablece IPs oficiales (.40 y .50).",
                    fontSize = 13.sp,
                    color = textMuted,
                    lineHeight = 18.sp
                )

                // Telemetría de Tráfico Recibido
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF121110))
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tráfico recibido: ${scaleState.totalBytesReceived} bytes",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = creamPrimary
                    )
                    Text(
                        text = scaleState.communicationMode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = creamWarm
                    )
                }

                Button(
                    onClick = { isScaleConfigDialogOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = creamPrimary, contentColor = darkText),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PING Y EDITAR IP / PUERTO (${scalesList.size} BÁSCULAS)",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.resetToOfficialScaleIps() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = creamWarm),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restablecer IPs", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.openQendraImportDialog() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = creamPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Catálogo Qendra", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // ==========================================================
        // TARJETA: LICENCIA INDUSTRIAL & ACTIVACIÓN
        // ==========================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("LICENCIA INDUSTRIAL", fontSize = 16.sp, fontWeight = FontWeight.Black, color = creamPrimary)
                        Text("Contratos de uso, demos y compras", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = creamWarm)
                    }
                }

                Text(
                    text = "Control criptográfico offline para licencias de 10 min (demo), 7 días (prueba), 1 mes/año (renta) o definitiva de por vida.",
                    fontSize = 13.sp,
                    color = textMuted
                )

                Button(
                    onClick = onOpenLicensing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23201D), contentColor = creamWarm),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("GESTIONAR LICENCIA Y CLAVES", fontWeight = FontWeight.Black, fontSize = 13.sp, color = creamWarm)
                }
            }
        }

        // ==========================================================
        // TARJETA: ACERCA DE SGNIA COMPANY & SOPORTE
        // ==========================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("SGNIA COMPANY", fontSize = 16.sp, fontWeight = FontWeight.Black, color = creamPrimary)
                    Text("Software & Soporte Técnico • v1.0.0", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = creamWarm)
                }

                Text(
                    text = "Soporte técnico directo vía WhatsApp (+52 276 109 5688), contacto del desarrollador y arquitectura offline.",
                    fontSize = 13.sp,
                    color = textMuted
                )

                Button(
                    onClick = onOpenAbout,
                    colors = ButtonDefaults.buttonColors(containerColor = creamWarm, contentColor = darkText),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("VER INFORMACIÓN DE SGNIA & SOPORTE", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }

        // ==========================================================
        // TARJETA 04. MANTENIMIENTO Y REINICIO DE BASE DE DATOS
        // ==========================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("04.", fontSize = 18.sp, fontWeight = FontWeight.Black, color = creamPrimary)
                        Text("Sesión de Terminal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = creamWarm)
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = creamWarm,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "Cierra la sesión actual de la terminal para alternar a otro usuario o bloquear la estación de pesaje.",
                    fontSize = 12.sp,
                    color = textMuted
                )

                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = creamPrimary, contentColor = darkText),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }

        // ==========================================================
        // RESUMEN GLOBAL DE RED
        // ==========================================================
        item {
            SgniaGlobalSummaryCard(
                metric1Label = "Básculas Configuradas",
                metric1Value = "${scalesList.size} Equipos",
                metric1Sub = "Puertos TCP/IP listos",
                metric2Label = "Estado de Enlace",
                metric2Value = "$onlineCount Activas",
                metric2Sub = "Monitoreo continuo"
            )
        }

        // ==========================================================
        // ZONA DE PELIGRO: VACIAR BASE DE DATOS
        // ==========================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, NoirColors.Danger.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ZONA DE PELIGRO",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            color = NoirColors.Danger
                        )
                        Text(
                            text = "Reinicio Limpio del Sistema",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = creamWarm
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = NoirColors.Danger,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "Elimina permanentemente productos, sucursales e historial para arrancar una instalación limpia.",
                    fontSize = 13.sp,
                    color = textMuted
                )

                OutlinedButton(
                    onClick = { showResetStep1 = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirColors.Danger),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NoirColors.Danger.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vaciar Base de Datos", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    if (showResetStep1) {
        AlertDialog(
            onDismissRequest = { showResetStep1 = false },
            containerColor = cardBg,
            title = {
                Text(
                    text = "¿Vaciar Base de Datos?",
                    fontWeight = FontWeight.Black,
                    color = NoirColors.Danger
                )
            },
            text = {
                Text(
                    text = "Esta acción eliminará todos los registros locales: catálogo de productos, inventarios capturados, configuración de sucursales e historial. ¿Desea continuar a la confirmación de seguridad?",
                    color = textMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetStep1 = false
                        showResetStep2 = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirColors.Danger, contentColor = Color.White)
                ) {
                    Text("CONTINUAR A CONFIRMACIÓN", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetStep1 = false }) {
                    Text("Cancelar", color = creamWarm)
                }
            }
        )
    }

    if (showResetStep2) {
        AlertDialog(
            onDismissRequest = { showResetStep2 = false },
            containerColor = cardBg,
            title = {
                Text(
                    text = "⚠️ CONFIRMACIÓN DEFINITIVA",
                    fontWeight = FontWeight.Black,
                    color = NoirColors.Danger
                )
            },
            text = {
                Text(
                    text = "Esta acción es irreversible y no se puede deshacer. Se borrarán permanentemente todos los datos de la terminal.",
                    color = textMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllDataForFreshStart()
                        showResetStep2 = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirColors.Danger, contentColor = Color.White)
                ) {
                    Text("SÍ, BORRAR TODO DEFINITIVAMENTE", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetStep2 = false }) {
                    Text("Abortar", color = creamWarm)
                }
            }
        )
    }

    if (isScaleConfigDialogOpen) {
        ScaleNetworkConfigDialog(
            viewModel = viewModel,
            onDismiss = { isScaleConfigDialogOpen = false }
        )
    }

    if (uiState.isQendraImportOpen) {
        QendraCatalogImportDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeQendraImportDialog() }
        )
    }
}
