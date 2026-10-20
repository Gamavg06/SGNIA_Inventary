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
import com.example.inventariocarniceria.ui.components.SgniaCerhanGlobalSummaryCard
import com.example.inventariocarniceria.ui.components.SgniaCerhanGreetingHeader
import com.example.inventariocarniceria.ui.components.SgniaCerhanNumberedCard
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

@Composable
fun SettingsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val scalesList by viewModel.scalesList.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var isScaleConfigDialogOpen by remember { mutableStateOf(false) }
    var showResetAllConfirm by remember { mutableStateOf(false) }

    val matteBg = Color(0xFF0F0E0D)
    val cardBg = Color(0xFF181715)
    val cardBorder = Color(0xFF282623)
    val creamPrimary = Color(0xFFF3EFE6)
    val creamWarm = Color(0xFFEEDDC8)
    val textMuted = Color(0xFF8C857B)
    val darkText = Color(0xFF141312)

    val onlineCount = scalesList.count { it.isConnected }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(matteBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ==========================================================
        // SALUDO EJECUTIVO ESTILO CERHAN
        // ==========================================================
        item {
            SgniaCerhanGreetingHeader(
                userName = "Ajustes del Sistema",
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
                SgniaCerhanNumberedCard(
                    modifier = Modifier.weight(1f),
                    number = "01.",
                    title = "Básculas en Red",
                    subtitle = "$onlineCount/${scalesList.size} equipos en línea",
                    icon = Icons.Default.Scale,
                    showWaveChart = true
                )
                SgniaCerhanNumberedCard(
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
        // TARJETA 03. CONFIGURAR BÁSCULAS EN RED
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
                        Text("Red de Básculas TCP/IP", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = creamWarm)
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
                                .background(if (onlineCount > 0) creamPrimary else textMuted, CircleShape)
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
                    text = "Conexión simultánea activa: Báscula 1 y Báscula 2 transmiten en paralelo sin desconectarse, con servicio WakeLock en segundo plano.",
                    fontSize = 11.sp,
                    color = textMuted,
                    lineHeight = 15.sp
                )

                Button(
                    onClick = { isScaleConfigDialogOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = creamPrimary, contentColor = darkText),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONFIGURAR BÁSCULAS (${scalesList.size} REGISTRADAS)",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
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
                        Text("Mantenimiento y Reinicio Limpio", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = creamWarm)
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = creamWarm,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "Limpia productos de prueba, sucursales e inversiones si necesitas inicializar el sistema desde cero.",
                    fontSize = 11.sp,
                    color = textMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showResetAllConfirm = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = creamWarm),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vaciar Base", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = creamPrimary, contentColor = darkText),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cerrar Sesión", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        }

        // ==========================================================
        // RESUMEN GLOBAL DE RED
        // ==========================================================
        item {
            SgniaCerhanGlobalSummaryCard(
                metric1Label = "Básculas Configuradas",
                metric1Value = "${scalesList.size} Equipos",
                metric1Sub = "Puertos TCP/IP listos",
                metric2Label = "Estado de Enlace",
                metric2Value = "$onlineCount Activas",
                metric2Sub = "Monitoreo continuo",
                barValues = listOf(0.60f, 0.85f, 0.72f, 0.94f, 0.68f, 0.90f, 0.82f, 0.98f)
            )
        }
    }

    if (showResetAllConfirm) {
        AlertDialog(
            onDismissRequest = { showResetAllConfirm = false },
            containerColor = cardBg,
            title = {
                Text(
                    text = "¿Iniciar Todo el Sistema en Blanco?",
                    fontWeight = FontWeight.Black,
                    color = creamPrimary
                )
            },
            text = {
                Text(
                    text = "Esta acción eliminará los datos actuales de catálogo, sucursales e historial para arrancar desde cero.",
                    color = textMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllDataForFreshStart()
                        showResetAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = creamPrimary, contentColor = darkText)
                ) {
                    Text("SÍ, VACIAR TODO", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetAllConfirm = false }) {
                    Text("Cancelar", color = creamWarm)
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
}
