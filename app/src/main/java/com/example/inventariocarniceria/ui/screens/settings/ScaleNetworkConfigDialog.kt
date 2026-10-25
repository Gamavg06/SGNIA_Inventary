package com.example.inventariocarniceria.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inventariocarniceria.data.scale.ScaleBrandCatalog
import com.example.inventariocarniceria.data.scale.ScaleBrandModel
import com.example.inventariocarniceria.data.scale.TcpScaleDevice
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import com.example.inventariocarniceria.ui.design.NoirColors
import java.util.Locale

// Paleta Oficial Bento Grid Noir & Crema
private val NoirBg = NoirColors.Background
private val NoirSurface = NoirColors.Surface
private val NoirSurfaceRaised = NoirColors.SurfaceRaised
private val NoirSurfaceSunken = NoirColors.SurfaceSunken
private val NoirHairline = NoirColors.Hairline
private val NoirHairlineStrong = NoirColors.HairlineStrong
private val NoirCream = NoirColors.Cream
private val NoirOnCream = NoirColors.OnCream
private val NoirTextPrimary = NoirColors.TextPrimary
private val NoirTextSecondary = NoirColors.TextSecondary
private val NoirEmerald = NoirColors.Emerald
private val NoirAmber = NoirColors.Amber
private val NoirDanger = NoirColors.Danger

/**
 * SUBVENTANA FLOTANTE: CONFIGURACIÓN DE BÁSCULAS EN RED
 * Soporta de 1 a 15+ básculas con catálogo inteligente de marcas/modelos y escucha paralela.
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

    val onlineCount = scalesList.count { it.isConnected }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NoirSurface),
            border = BorderStroke(1.dp, NoirHairline),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NoirSurfaceRaised)
                                .border(1.dp, NoirHairline, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Scale,
                                contentDescription = null,
                                tint = NoirCream,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "RED INDUSTRIAL DE BÁSCULAS",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NoirCream
                            )
                            Text(
                                text = "Gestor Multimarca TCP/IP",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = NoirTextPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NoirSurfaceRaised)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = NoirTextSecondary)
                    }
                }

                HorizontalDivider(color = NoirHairline, modifier = Modifier.padding(vertical = 10.dp))

                // --- 2. BANNER DE ESTADO SIMULTÁNEO ---
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (onlineCount > 0) NoirEmerald.copy(alpha = 0.12f) else NoirAmber.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (onlineCount > 0) NoirEmerald.copy(alpha = 0.35f) else NoirAmber.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (onlineCount > 0) NoirEmerald else NoirAmber, CircleShape)
                            )
                            Text(
                                text = "$onlineCount de ${scalesList.size} BÁSCULAS ACTIVAS EN RED",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (onlineCount > 0) NoirEmerald else NoirAmber
                            )
                        }

                        Text(
                            text = "Escucha Concurrente",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NoirTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- 3. BARRA DE ACCIÓN: AGREGAR BÁSCULA & RESET ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            editingScale = null
                            isAddOrEditDialogOpen = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NoirCream, contentColor = NoirOnCream),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = NoirOnCream, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "＋ Agregar Báscula Extra",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetToOfficialScaleIps() },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, NoirAmber.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirAmber),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = "⚡ Reset Predeterminado (.40/.50)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- 4. LISTA DINÁMICA DE BÁSCULAS CONFIGURADAS ---
                if (scalesList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(NoirSurfaceSunken)
                            .border(1.dp, NoirHairline, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Scale, contentDescription = null, tint = NoirTextSecondary, modifier = Modifier.size(36.dp))
                            Text(
                                text = "No hay básculas registradas",
                                fontWeight = FontWeight.Bold,
                                color = NoirTextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Pulsa '+ Agregar Báscula Extra' para conectar tu báscula Torrey, Systel, Rhino o genérica.",
                                fontSize = 13.sp,
                                color = NoirTextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = NoirCream, contentColor = NoirOnCream),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("GUARDAR Y APLICAR EN PANTALLA", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }
    }

    // Modal Sub-diálogo: Agregar / Editar Báscula con Buscador de Marcas y Modelos
    if (isAddOrEditDialogOpen) {
        AddEditScaleDialog(
            existingScale = editingScale,
            existingCount = scalesList.size,
            onDismiss = {
                isAddOrEditDialogOpen = false
                editingScale = null
            },
            onTestPing = { ip, port, onResult ->
                viewModel.pingScale(editingScale?.id ?: 0, ip, port, onResult)
            },
            onSave = { name, ip, port, brand, model, protocol ->
                if (editingScale != null) {
                    viewModel.updateScale(
                        editingScale!!.copy(
                            name = name,
                            ip = ip,
                            port = port,
                            brand = brand,
                            model = model,
                            protocol = protocol
                        )
                    )
                } else {
                    viewModel.addScale(
                        name = name,
                        ip = ip,
                        port = port,
                        brand = brand,
                        model = model,
                        protocol = protocol
                    )
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
            containerColor = NoirSurface,
            title = {
                Text("Eliminar Báscula de la Red", fontWeight = FontWeight.Bold, color = NoirTextPrimary)
            },
            text = {
                Text(
                    "¿Deseas desconectar y eliminar '${targetScale.name}' (${targetScale.brand} ${targetScale.model} • ${targetScale.ip}:${targetScale.port}) de la red local?",
                    color = NoirTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteScale(targetScale.id)
                        scaleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirDanger, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { scaleToDelete = null }) {
                    Text("Cancelar", color = NoirTextSecondary)
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
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = NoirSurfaceRaised),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (scale.isConnected) NoirEmerald.copy(alpha = 0.5f) else NoirHairline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fila 1: Nombre, Marca/Modelo y Botones de edición
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = scale.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = NoirTextPrimary
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NoirSurfaceSunken)
                                .border(1.dp, NoirCream.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${scale.brand} ${scale.model}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NoirCream
                            )
                        }
                    }

                    Text(
                        text = "IP: ${scale.ip} • Puerto: ${scale.port} • Protocolo: ${scale.protocol}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = NoirTextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = NoirCream, modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = NoirDanger, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Fila 2: Telemetría y estado de lectura
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (scale.isConnected) NoirEmerald else NoirDanger, CircleShape)
                    )
                    Text(
                        text = if (scale.isConnected) "EN LÍNEA" else "SIN CONEXIÓN",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (scale.isConnected) NoirEmerald else NoirDanger
                    )
                }

                Text(
                    text = if (scale.isConnected) "Último peso: ${String.format(Locale.US, "%.3f", scale.lastWeight)} kg" else "Último peso: --.---",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (scale.isConnected) NoirEmerald else NoirDanger
                )
            }

            // Fila 3: Botón de Ping
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPing,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NoirHairlineStrong),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirCream),
                    modifier = Modifier.height(34.dp)
                ) {
                    if (isPinging) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = NoirCream)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Probando...", fontSize = 11.sp)
                    } else {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(14.dp), tint = NoirCream)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verificar Ping", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (pingResult != null) {
                    Text(
                        text = pingResult.second,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (pingResult.first) NoirEmerald else NoirDanger,
                        maxLines = 1
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = NoirSurface,
            title = {
                Text("¿Eliminar báscula?", fontWeight = FontWeight.Bold, color = NoirTextPrimary)
            },
            text = {
                Text(
                    text = "Se eliminará la configuración de '${scale.name}' (${scale.ip}:${scale.port}).",
                    color = NoirTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirDanger, contentColor = Color.White)
                ) {
                    Text("ELIMINAR", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = NoirCream)
                }
            }
        )
    }
}

/**
 * SUB-DIÁLOGO: AGREGAR O EDITAR BÁSCULA CON BUSCADOR DE MARCA Y MODELO
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditScaleDialog(
    existingScale: TcpScaleDevice? = null,
    existingCount: Int = 0,
    onDismiss: () -> Unit,
    onTestPing: (ip: String, port: Int, onResult: (Boolean, String) -> Unit) -> Unit,
    onSave: (name: String, ip: String, port: Int, brand: String, model: String, protocol: String) -> Unit
) {
    val isEditing = existingScale != null

    var selectedBrand by remember { mutableStateOf(existingScale?.brand ?: "Systel") }
    var selectedModel by remember { mutableStateOf(existingScale?.model ?: "Cuora Max") }
    var selectedProtocol by remember { mutableStateOf(existingScale?.protocol ?: "SYSTEL_CUORA") }

    var nameInput by remember {
        mutableStateOf(
            existingScale?.name ?: "Báscula ${existingCount + 1} ($selectedBrand $selectedModel)"
        )
    }
    var ipInput by remember { mutableStateOf(existingScale?.ip ?: "192.168.100.52") }
    var portInput by remember { mutableStateOf(existingScale?.port?.toString() ?: "10001") }

    var searchQuery by remember { mutableStateOf("") }
    var selectedBrandFilter by remember { mutableStateOf("TODAS") }

    var isTestingPing by remember { mutableStateOf(false) }
    var testPingResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    // Filtrar catálogo por búsqueda y marca seleccionada
    val filteredCatalog = remember(searchQuery, selectedBrandFilter) {
        ScaleBrandCatalog.CATALOG.filter { item ->
            val matchBrand = selectedBrandFilter == "TODAS" || item.brand.equals(selectedBrandFilter, ignoreCase = true)
            val matchQuery = searchQuery.isBlank() ||
                    item.brand.contains(searchQuery, ignoreCase = true) ||
                    item.model.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            matchBrand && matchQuery
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NoirSurface),
            border = BorderStroke(1.dp, NoirCream.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditing) "EDITAR BÁSCULA INDUSTRIAL" else "AGREGAR BÁSCULA ADICIONAL",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NoirCream
                        )
                        Text(
                            text = if (isEditing) "Modificar Parámetros de Red" else "Vincular Nueva Báscula a la Red",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = NoirTextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = NoirTextSecondary)
                    }
                }

                HorizontalDivider(color = NoirHairline)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // SECCIÓN 1: BUSCADOR DE MARCA Y MODELO
                    item {
                        Text(
                            text = "1. SELECCIONA LA MARCA Y MODELO DE LA BÁSCULA",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = NoirCream
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Buscar modelo (ej. Torrey L-EQ, Cuora, Rhino, Toledo...)", color = NoirTextSecondary) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NoirCream) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = outlinedNoirColors()
                        )
                    }

                    // Chips de marcas rápidas
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ScaleBrandCatalog.BRANDS.forEach { brandName ->
                                val isSelected = selectedBrandFilter == brandName
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NoirCream else NoirSurfaceRaised)
                                        .border(1.dp, if (isSelected) NoirCream else NoirHairline, RoundedCornerShape(8.dp))
                                        .clickable { selectedBrandFilter = brandName }
                                        .heightIn(min = 48.dp)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = brandName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) NoirOnCream else NoirTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Lista de modelos disponibles
                    items(filteredCatalog) { modelInfo ->
                        val isPicked = selectedBrand == modelInfo.brand && selectedModel == modelInfo.model
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPicked) NoirSurfaceSunken else NoirSurfaceRaised
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                if (isPicked) 1.5.dp else 1.dp,
                                if (isPicked) NoirCream else NoirHairline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedBrand = modelInfo.brand
                                    selectedModel = modelInfo.model
                                    selectedProtocol = modelInfo.protocol
                                    portInput = modelInfo.defaultPort.toString()
                                    if (nameInput.isBlank() || nameInput.startsWith("Báscula")) {
                                        nameInput = "Báscula ${existingCount + 1} (${modelInfo.brand} ${modelInfo.model})"
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "${modelInfo.brand} ${modelInfo.model}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            color = if (isPicked) NoirCream else NoirTextPrimary
                                        )
                                        if (modelInfo.popular) {
                                            Text(
                                                text = "POPULAR",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NoirEmerald
                                            )
                                        }
                                    }
                                    Text(
                                        text = modelInfo.description,
                                        fontSize = 11.sp,
                                        color = NoirTextSecondary
                                    )
                                    Text(
                                        text = "Puerto Sugerido: ${modelInfo.defaultPort} • Protocolo: ${modelInfo.protocol}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = NoirAmber
                                    )
                                }

                                if (isPicked) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Seleccionado",
                                        tint = NoirEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // SECCIÓN 2: DATOS DE RED Y NOMBRE
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2. PARÁMETROS DE CONEXIÓN TCP/IP",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = NoirCream
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Nombre Identificador de la Báscula", color = NoirTextSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = outlinedNoirColors()
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = ipInput,
                                onValueChange = { ipInput = it },
                                label = { Text("Dirección IP (ej. 192.168.100.52)", color = NoirTextSecondary) },
                                singleLine = true,
                                modifier = Modifier.weight(2f),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NoirTextPrimary),
                                colors = outlinedNoirColors()
                            )

                            OutlinedTextField(
                                value = portInput,
                                onValueChange = { portInput = it.filter { c -> c.isDigit() } },
                                label = { Text("Puerto", color = NoirTextSecondary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NoirTextPrimary),
                                colors = outlinedNoirColors()
                            )
                        }
                    }

                    item {
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
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, NoirHairlineStrong),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirCream),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            if (isTestingPing) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = NoirCream)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Comprobando Socket TCP/IP...")
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp), tint = NoirCream)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Probar Conexión (Ping a Báscula)", fontWeight = FontWeight.Bold)
                            }
                        }

                        testPingResult?.let { res ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (res.first) NoirEmerald.copy(alpha = 0.15f) else NoirDanger.copy(alpha = 0.15f))
                                    .border(1.dp, if (res.first) NoirEmerald else NoirDanger, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = res.second,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.first) NoirEmerald else NoirDanger
                                )
                            }
                        }
                    }
                }

                // Botones Cancelar y Guardar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, NoirHairline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirTextSecondary),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val port = portInput.toIntOrNull() ?: 10001
                            if (nameInput.isNotBlank() && ipInput.isNotBlank()) {
                                onSave(
                                    nameInput.trim(),
                                    ipInput.trim(),
                                    port,
                                    selectedBrand,
                                    selectedModel,
                                    selectedProtocol
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NoirCream, contentColor = NoirOnCream),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f).height(46.dp)
                    ) {
                        Text(
                            text = if (isEditing) "💾 Guardar Cambios" else "＋ Agregar Báscula",
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun outlinedNoirColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NoirCream,
    unfocusedBorderColor = NoirHairline,
    focusedContainerColor = NoirSurfaceSunken,
    unfocusedContainerColor = NoirSurfaceSunken,
    focusedTextColor = NoirTextPrimary,
    unfocusedTextColor = NoirTextPrimary
)
