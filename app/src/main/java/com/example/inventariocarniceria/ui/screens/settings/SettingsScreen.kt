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
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.inventariocarniceria.theme.SgniaTheme
import com.example.inventariocarniceria.ui.components.SgniaThemeToggle
import com.example.inventariocarniceria.ui.components.TacticalGlassCard
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

@Composable
fun SettingsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val scalesList by viewModel.scalesList.collectAsState()

    var isScaleConfigDialogOpen by remember { mutableStateOf(false) }
    var showResetAllConfirm by remember { mutableStateOf(false) }

    val accentGreen = Color(0xFF2E7D32)
    val purplePrimary = Color(0xFF6A1B9A)
    val alertRed = Color(0xFFC62828)

    val onlineCount = scalesList.count { it.isConnected }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SgniaTheme.colors.backgroundPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // HEADER Y TEMA (SOL Y LUNA)
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AJUSTES DEL SISTEMA",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = SgniaTheme.colors.textPrimary
                    )
                    Text(
                        text = "Configuración TCP/IP de Básculas y parámetros de tienda",
                        style = MaterialTheme.typography.bodySmall,
                        color = SgniaTheme.colors.textSecondary
                    )
                }
                SgniaThemeToggle(
                    currentMode = themeMode,
                    onToggle = { viewModel.toggleTheme() }
                )
            }
        }

        // ==========================================
        // BOTÓN SUBVENTANA: CONFIGURAR BÁSCULAS EN RED
        // ==========================================
        item {
            TacticalGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(purplePrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .border(1.dp, purplePrimary, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Scale, contentDescription = null, tint = purplePrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "RED DE BÁSCULAS TCP/IP",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = SgniaTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Sucursales con 2, 5 (Grajales) o hasta 15+ básculas",
                                    fontSize = 11.sp,
                                    color = SgniaTheme.colors.textSecondary
                                )
                            }
                        }

                        // Badge de estado general
                        Surface(
                            color = if (onlineCount > 0) accentGreen.copy(alpha = 0.15f) else alertRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (onlineCount > 0) accentGreen else alertRed, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "$onlineCount/${scalesList.size} EN LÍNEA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (onlineCount > 0) accentGreen else alertRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "⚡ Conexión simultánea activa: Todas las básculas transmiten en paralelo sin desconectarse ni tener que cambiar manualmente de equipo.",
                        fontSize = 11.sp,
                        color = SgniaTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🛡️ Servicio en segundo plano activo (WakeLock): Sigue funcionando con la app cerrada o pantalla apagada.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentGreen
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón principal solicitado por el usuario para abrir la subventana
                    Button(
                        onClick = { isScaleConfigDialogOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = purplePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONFIGURAR BÁSCULAS (${scalesList.size} REGISTRADAS)",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ==========================================
        // MODO FÍSICO HARDWARE TCP/IP
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Modo Físico Hardware TCP/IP",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Lectura directa por socket TCP/IP conectada a básculas Systel Cuora en la red local. Cero simulación.",
                                style = MaterialTheme.typography.bodySmall,
                                color = accentGreen
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Modo Físico Activo",
                            tint = accentGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // MODO DUEÑO: INICIAR EN BLANCO (VACIAR TODO)
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB71C1C).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFB71C1C).copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFB71C1C),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "INICIAR EN BLANCO (MODO DUEÑO)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFFB71C1C)
                            )
                            Text(
                                text = "Limpia productos de prueba, sucursales e inversiones para arrancar desde cero.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SgniaTheme.colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showResetAllConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VACIAR BASE DE DATOS (MODO DUEÑO)",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ==========================================
        // CERRAR SESIÓN
        // ==========================================
        item {
            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = SgniaTheme.colors.statusAlert),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CERRAR SESIÓN DE TIENDA",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // Diálogo de confirmación para Modo Dueño (Vaciar todo)
    if (showResetAllConfirm) {
        AlertDialog(
            onDismissRequest = { showResetAllConfirm = false },
            title = {
                Text(
                    text = "⚠️ ¿Iniciar Todo el Sistema en Blanco?",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFB71C1C)
                )
            },
            text = {
                Text(
                    text = "Esta acción eliminará de forma permanente los datos de prueba:\n\n" +
                            "• Catálogo de productos y cortes derivados de prueba\n" +
                            "• Sucursales e inversiones registradas\n" +
                            "• Historial de pedidos y capturas\n\n" +
                            "El sistema quedará 100% en blanco para que tú como Dueño crees tu sucursal oficial, configures tus básculas y extraigas o des de alta tus códigos oficiales.",
                    color = SgniaTheme.colors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllDataForFreshStart()
                        showResetAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) {
                    Text("SÍ, VACIAR TODO", color = Color.White, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetAllConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal Subventana de Configuración de Básculas
    if (isScaleConfigDialogOpen) {
        ScaleNetworkConfigDialog(
            viewModel = viewModel,
            onDismiss = { isScaleConfigDialogOpen = false }
        )
    }
}
