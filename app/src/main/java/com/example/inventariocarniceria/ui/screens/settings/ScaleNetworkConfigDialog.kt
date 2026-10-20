package com.example.inventariocarniceria.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inventariocarniceria.data.scale.TcpScaleDevice
import com.example.inventariocarniceria.theme.SgniaTheme
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import java.util.Locale

/**
 * SUBVENTANA FLOTANTE: CONFIGURACIÓN DE BÁSCULAS EN RED
 * Permite gestionar de 1 a 15+ básculas por sucursal con escucha simultánea.
 */
@Composable
fun ScaleNetworkConfigDialog(
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    val scalesList by viewModel.scalesList.collectAsState()

    var isAddOrEditDialogOpen by remember { mutableStateOf(false) }
    var editingScale by remember { mutableStateOf<TcpScaleDevice?>(null) }
    var scaleToDelete by remember { mutableStateOf<TcpScaleDevice?>(null) }

    var pingingScaleId by remember { mutableStateOf<Int?>(null) }
    var pingResultMap by remember { mutableStateOf<Map<Int, Pair<Boolean, String>>>(emptyMap()) }

    val accentGreen = Color(0xFF2E7D32)
    val alertRed = Color(0xFFB71C1C)
    val purplePrimary = Color(0xFF6A1B9A)
    val amberWarning = Color(0xFFE65100)

    val onlineCount = scalesList.count { it.isConnected }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // --- 1. CABECERA DE LA SUBVENTANA ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = purplePrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Scale,
                                    contentDescription = null,
                                    tint = purplePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CONFIGURAR BÁSCULAS EN RED",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                text = "Multiconexión simultánea activa • Soporta 1 a 15+ básculas",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar subventana")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- 2. BANNER DE ESTADO SIMULTÁNEO Y SEGUNDO PLANO ---
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (onlineCount > 0) accentGreen.copy(alpha = 0.10f) else amberWarning.copy(alpha = 0.10f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (onlineCount > 0) accentGreen.copy(alpha = 0.3f) else amberWarning.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (onlineCount > 0) accentGreen else amberWarning,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$onlineCount de ${scalesList.size} BÁSCULAS EN LÍNEA",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = if (onlineCount > 0) accentGreen else amberWarning
                                )
                            }

                            Surface(
                                color = accentGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "PARALELO ACTIVO",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = accentGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚡ Conexión simultánea: Todas las básculas transmiten en paralelo sin necesidad de cambiar manualmente de equipo.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = accentGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Servicio segundo plano activo (WakeLock): Sigue funcionando con la app cerrada o pantalla apagada.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- 3. BOTÓN [+] AGREGAR BÁSCULA & RESTABLECER IPS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            editingScale = null
                            isAddOrEditDialogOpen = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = purplePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ AGREGAR BÁSCULA",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.resetToOfficialScaleIps()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "⚡ RESET (.40/.50)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = amberWarning
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- 4. LISTA DINÁMICA DE BÁSCULAS CONFIGURADAS ---
                if (scalesList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay básculas registradas. Pulsa '+ AGREGAR BÁSCULA' para añadir tu primera báscula.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(scalesList) { scale ->
                            val isPinging = pingingScaleId == scale.id
                            val pingResult = pingResultMap[scale.id]

                            ScaleConfigItemCard(
                                scale = scale,
                                isPinging = isPinging,
                                pingResult = pingResult,
                                onPing = {
                                    pingingScaleId = scale.id
                                    viewModel.pingScale(scale.id, scale.ip, scale.port) { ok, msg ->
                                        pingingScaleId = null
                                        pingResultMap = pingResultMap + (scale.id to Pair(ok, msg))
                                    }
                                },
                                onEdit = {
                                    editingScale = scale
                                    isAddOrEditDialogOpen = true
                                },
                                onDelete = {
                                    scaleToDelete = scale
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- 5. PIE DE VENTANA: BOTÓN LISTO ---
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = accentGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("GUARDAR Y APLICAR CAMBIOS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal Sub-diálogo: Agregar / Editar Báscula
    if (isAddOrEditDialogOpen) {
        AddEditScaleDialog(
            existingScale = editingScale,
            onDismiss = {
                isAddOrEditDialogOpen = false
                editingScale = null
            },
            onTestPing = { ip, port, onResult ->
                viewModel.pingScale(editingScale?.id ?: 0, ip, port, onResult)
            },
            onSave = { name, ip, port ->
                if (editingScale != null) {
                    viewModel.updateScale(
                        editingScale!!.copy(name = name, ip = ip, port = port)
                    )
                } else {
                    viewModel.addScale(name, ip, port)
                }
                isAddOrEditDialogOpen = false
                editingScale = null
            }
        )
    }

    // Diálogo de Confirmación para Eliminar Báscula
    scaleToDelete?.let { targetScale ->
        AlertDialog(
            onDismissRequest = { scaleToDelete = null },
            title = { Text("Eliminar Báscula de la Red", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Deseas desconectar y eliminar '${targetScale.name}' (${targetScale.ip}:${targetScale.port}) de la red local?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteScale(targetScale.id)
                        scaleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = alertRed)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { scaleToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * TARJETA DE CADA BÁSCULA CONFIGURADA
 */
@Composable
fun ScaleConfigItemCard(
    scale: TcpScaleDevice,
    isPinging: Boolean,
    pingResult: Pair<Boolean, String>?,
    onPing: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accentGreen = Color(0xFF2E7D32)
    val alertRed = Color(0xFFB71C1C)
    val purplePrimary = Color(0xFF6A1B9A)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (scale.isConnected) accentGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Fila 1: Nombre, Badge de Estado y Botones de Acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // LED de estado
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (scale.isConnected) accentGreen else alertRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = scale.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge Estado
                    Surface(
                        color = if (scale.isConnected) accentGreen.copy(alpha = 0.15f) else alertRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (scale.isConnected) "EN LÍNEA" else "DESCONECTADA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (scale.isConnected) accentGreen else alertRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Botón Editar
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = purplePrimary, modifier = Modifier.size(18.dp))
                    }

                    // Botón Eliminar
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = alertRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Fila 2: IP, Puerto y Último Peso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TCP ${scale.ip}:${scale.port}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (scale.lastWeight > 0.0) {
                    Text(
                        text = "Último peso: ${String.format(Locale.US, "%.3f", scale.lastWeight)} kg",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = accentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Telemetría de bytes y modo
            Text(
                text = "📡 Modo: ${scale.communicationMode} • Rx: ${scale.totalBytesReceived} B",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = if (scale.totalBytesReceived > 0L) accentGreen else Color(0xFFE65100)
            )
            if (scale.lastRawFrame.isNotBlank()) {
                Text(
                    text = "Trama: \"${scale.lastRawFrame}\"",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Fila 3: Botón de Ping en vivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPing,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    if (isPinging) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verificando...", fontSize = 11.sp)
                    } else {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verificar Ping", fontSize = 11.sp)
                    }
                }

                if (pingResult != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pingResult.second,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (pingResult.first) accentGreen else alertRed,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * SUB-DIÁLOGO: AGREGAR O EDITAR BÁSCULA (+)
 */
@Composable
fun AddEditScaleDialog(
    existingScale: TcpScaleDevice? = null,
    onDismiss: () -> Unit,
    onTestPing: (ip: String, port: Int, onResult: (Boolean, String) -> Unit) -> Unit,
    onSave: (name: String, ip: String, port: Int) -> Unit
) {
    val isEditing = existingScale != null

    var nameInput by remember { mutableStateOf(existingScale?.name ?: "") }
    var ipInput by remember { mutableStateOf(existingScale?.ip ?: "192.168.100.50") }
    var portInput by remember { mutableStateOf(existingScale?.port?.toString() ?: "10001") }

    var isTestingPing by remember { mutableStateOf(false) }
    var testPingResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val accentGreen = Color(0xFF2E7D32)
    val alertRed = Color(0xFFB71C1C)
    val purplePrimary = Color(0xFF6A1B9A)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint = purplePrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "EDITAR BÁSCULA" else "AGREGAR NUEVA BÁSCULA",
                    fontWeight = FontWeight.Black
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nombre de la Báscula") },
                    placeholder = { Text("Ej. Báscula 3 Grajales Recibo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ipInput,
                        onValueChange = { ipInput = it },
                        label = { Text("Dirección IP") },
                        placeholder = { Text("192.168.100.52") },
                        singleLine = true,
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = portInput,
                        onValueChange = { portInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Puerto") },
                        placeholder = { Text("10001") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Botón de prueba en vivo
                OutlinedButton(
                    onClick = {
                        val port = portInput.toIntOrNull() ?: 10001
                        isTestingPing = true
                        testPingResult = null
                        onTestPing(ipInput.trim(), port) { ok, msg ->
                            isTestingPing = false
                            testPingResult = Pair(ok, msg)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTestingPing) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verificando Conexión...")
                    } else {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Probar Conexión (Ping)")
                    }
                }

                testPingResult?.let { res ->
                    Surface(
                        color = if (res.first) accentGreen.copy(alpha = 0.12f) else alertRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = res.second,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (res.first) accentGreen else alertRed,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val port = portInput.toIntOrNull() ?: 10001
                    if (nameInput.isNotBlank() && ipInput.isNotBlank()) {
                        onSave(nameInput.trim(), ipInput.trim(), port)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentGreen)
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Agregar Báscula")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
