package com.example.inventariocarniceria.ui.screens.scale

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.scale.TcpScaleDevice
import com.example.inventariocarniceria.theme.SgniaTheme
import com.example.inventariocarniceria.ui.components.AsyncProductImage
import com.example.inventariocarniceria.ui.components.CameraBarcodeScannerDialog
import com.example.inventariocarniceria.ui.components.SgniaCerhanDualScaleCard
import com.example.inventariocarniceria.ui.components.SgniaCerhanGlobalSummaryCard
import com.example.inventariocarniceria.ui.components.SgniaCerhanGreetingHeader
import com.example.inventariocarniceria.ui.components.SgniaCerhanNumberedCard
import com.example.inventariocarniceria.ui.components.SgniaCerhanQuickAccessCard
import com.example.inventariocarniceria.ui.components.SgniaPillButton
import com.example.inventariocarniceria.ui.components.SgniaQuickActionItem
import com.example.inventariocarniceria.ui.screens.inventory.DespiecePiernaDialog
import com.example.inventariocarniceria.ui.screens.inventory.PriceAdjustmentDialog
import com.example.inventariocarniceria.ui.screens.inventory.QendraCatalogImportDialog
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndustrialScaleVisorScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scaleState by viewModel.scaleState.collectAsState()
    val scalesList by viewModel.scalesList.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val recentCaptures by viewModel.recentCaptures.collectAsState()
    val scannedBarcodeResult by viewModel.scannedBarcodeResult.collectAsState()
    val haptic = LocalHapticFeedback.current

    var searchExpanded by remember { mutableStateOf(false) }
    var isPluListOpen by remember { mutableStateOf(false) }
    var isManualWeightOpen by remember { mutableStateOf(false) }
    var isCameraScannerOpen by remember { mutableStateOf(false) }
    var manualWeightInput by remember { mutableStateOf("") }

    // Garantizamos que siempre se muestren AMBAS básculas principales (01. y 02.)
    val scale1 = scalesList.getOrNull(0) ?: TcpScaleDevice(
        id = 1,
        name = "Báscula 1 (Principal)",
        ip = "192.168.100.40",
        port = 10001
    )
    val scale2 = scalesList.getOrNull(1) ?: TcpScaleDevice(
        id = 2,
        name = "Báscula 2 (Mostrador)",
        ip = "192.168.100.50",
        port = 10001
    )

    val suggestions = remember(uiState.codeInput, allProducts) {
        val q = uiState.codeInput.trim().lowercase()
        if (q.isEmpty()) emptyList()
        else {
            allProducts.filter { prod ->
                prod.localCode.toString().contains(q) ||
                prod.code.toString().contains(q) ||
                prod.name.lowercase().contains(q) ||
                prod.synonyms.lowercase().contains(q)
            }.take(6)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SgniaTheme.colors.backgroundPrimary),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // SALUDO PRINCIPAL ESTILO CERHAN (SEGUNDA IMAGEN)
        // ==========================================
        item {
            SgniaCerhanGreetingHeader(
                title = "Buenas tardes, ${uiState.currentBranch.ifEmpty { "Panel de Tienda" }}",
                subtitle = "Desde aquí monitoreas ambas básculas en tiempo real, escaneas cajas y capturas el pesaje de mostrador."
            )
        }

        // ==========================================
        // AVISO DE CAJA ESCANEADA (SI EXISTE)
        // ==========================================
        if (scannedBarcodeResult != null) {
            item {
                val res = scannedBarcodeResult!!
                val prod = res.product
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF181715))
                        .border(1.5.dp, Color(0xFFEEDDC8), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3EFE6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Color(0xFF141312),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (prod != null) "📦 ${prod.name}" else "Código: ${res.rawCode}",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color(0xFFFAF6F0)
                                )
                                Text(
                                    text = res.explanation,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFD6CEC2)
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.clearScannedBarcodeResult() }) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Confirmar",
                                tint = Color(0xFFF3EFE6)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 01. BÁSCULA 1 EN VIVO (TARJETA PRINCIPAL 01.)
        // ==========================================
        item {
            val isScale1Active = scaleState.activeScaleId == scale1.id
            val weight1 = if (isScale1Active) scaleState.currentWeight else scale1.lastWeight
            val stable1 = if (isScale1Active) scaleState.isStable else scale1.isStable

            SgniaCerhanDualScaleCard(
                numberPrefix = "01.",
                scaleDevice = scale1,
                liveWeightKg = weight1,
                isStable = stable1,
                isActiveScale = isScale1Active,
                onSelectScale = { viewModel.selectActiveScale(scale1.id) },
                onManualWeightClick = {
                    viewModel.selectActiveScale(scale1.id)
                    manualWeightInput = if (weight1 > 0.0) weight1.toString() else ""
                    isManualWeightOpen = true
                },
                onTareClick = {
                    viewModel.selectActiveScale(scale1.id)
                    viewModel.setManualWeight(0.000)
                }
            )
        }

        // ==========================================
        // 02. BÁSCULA 2 EN VIVO (TARJETA PRINCIPAL 02.)
        // ==========================================
        item {
            val isScale2Active = scaleState.activeScaleId == scale2.id
            val weight2 = if (isScale2Active) scaleState.currentWeight else scale2.lastWeight
            val stable2 = if (isScale2Active) scaleState.isStable else scale2.isStable

            SgniaCerhanDualScaleCard(
                numberPrefix = "02.",
                scaleDevice = scale2,
                liveWeightKg = weight2,
                isStable = stable2,
                isActiveScale = isScale2Active,
                onSelectScale = { viewModel.selectActiveScale(scale2.id) },
                onManualWeightClick = {
                    viewModel.selectActiveScale(scale2.id)
                    manualWeightInput = if (weight2 > 0.0) weight2.toString() else ""
                    isManualWeightOpen = true
                },
                onTareClick = {
                    viewModel.selectActiveScale(scale2.id)
                    viewModel.setManualWeight(0.000)
                }
            )
        }

        // ==========================================
        // 03. CORTE SELECCIONADO, BUSCADOR Y CAPTURA (TARJETA 03.)
        // ==========================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF181715))
                    .border(1.dp, Color(0xFF2E2B27), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "03.",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 21.sp,
                                color = Color(0xFFD6CEC2)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Corte Activo y Captura",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color(0xFFFAF6F0)
                            )
                        }
                        Text(
                            text = "PLUs (${allProducts.size})",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFF3EFE6),
                            modifier = Modifier.clickable { isPluListOpen = true }
                        )
                    }

                    // Buscador híbrido con botón de cámara QR/Barcode integrado
                    ExposedDropdownMenuBox(
                        expanded = searchExpanded && suggestions.isNotEmpty(),
                        onExpandedChange = { searchExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.codeInput,
                            onValueChange = {
                                viewModel.onCodeInputChanged(it)
                                searchExpanded = true
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            placeholder = { Text("Buscar por PLU o nombre (ej. 24, Arrachera)...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFEEDDC8))
                            },
                            trailingIcon = {
                                IconButton(onClick = { isCameraScannerOpen = true }) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "Escanear Caja",
                                        tint = Color(0xFFEEDDC8)
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFEEDDC8),
                                unfocusedBorderColor = Color(0xFF38332D),
                                focusedTextColor = Color(0xFFFAF6F0),
                                unfocusedTextColor = Color(0xFFFAF6F0)
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = searchExpanded && suggestions.isNotEmpty(),
                            onDismissRequest = { searchExpanded = false }
                        ) {
                            suggestions.forEach { prod ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "[#${prod.localCode}] ${prod.name}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "$${String.format(Locale.US, "%.2f", prod.price)}/kg",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.onCodeInputChanged(prod.localCode.toString())
                                        searchExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Detalle del producto seleccionado + Ajuste rápido de precio
                    val product = uiState.selectedProduct
                    if (product != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF23201D))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncProductImage(
                                imageUrl = product.imageUri,
                                contentDescription = product.name,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "[PLU #${product.localCode}] ${product.name}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color(0xFFFAF6F0)
                                )
                                Text(
                                    text = "Precio: $${String.format(Locale.US, "%.2f", product.price)}/kg • Categoría: ${product.category}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFD6CEC2)
                                )
                            }
                        }

                        // Píldoras de ajuste rápido de precio (-$10, -$5, +$5, +$10)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(-10.0, -5.0, 5.0, 10.0).forEach { delta ->
                                val label = if (delta > 0) "+$${delta.toInt()}" else "-$${kotlin.math.abs(delta).toInt()}"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF23201D))
                                        .border(1.dp, Color(0xFF3E3A35), RoundedCornerShape(10.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val newP = (product.price + delta).coerceAtLeast(1.0)
                                            viewModel.updateProductPrice(product, newP)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFF3EFE6)
                                    )
                                }
                            }
                        }
                    }

                    // Botón Crema Principal para capturar pesaje
                    SgniaPillButton(
                        text = "CAPTURAR PESAJE EN INVENTARIO (${String.format(Locale.US, "%.3f", scaleState.currentWeight)} KG)",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.captureWeight()
                        },
                        enabled = uiState.selectedProduct != null,
                        modifier = Modifier.fillMaxWidth(),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Scale,
                                contentDescription = null,
                                tint = Color(0xFF141210),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
        }

        // ==========================================
        // 04. SINCRONIZACIÓN DE RED E IPS DE BÁSCULAS (TARJETA 04.)
        // ==========================================
        item {
            SgniaCerhanNumberedCard(
                numberPrefix = "04.",
                title = "Sincronización de Red TCP/IP",
                description = "Restablece IPs oficiales (.40 y .50) o importa catálogo de cortes Qendra.",
                bottomMetric = "Tráfico recibido: ${scaleState.totalBytesReceived} bytes • Toca para restablecer IPs",
                primaryIcon = Icons.Default.NetworkWifi,
                secondaryIcon = Icons.Default.FlashOn,
                showChart = false,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.resetToOfficialScaleIps()
                }
            )
        }

        // ==========================================
        // ACCESO RÁPIDO (CUADRÍCULA 2x2 BOTONES CREMA)
        // ==========================================
        item {
            SgniaCerhanQuickAccessCard(
                title = "Acceso Rápido",
                actions = listOf(
                    SgniaQuickActionItem(
                        label = "Escanear Caja",
                        icon = Icons.Default.QrCodeScanner,
                        onClick = { isCameraScannerOpen = true }
                    ),
                    SgniaQuickActionItem(
                        label = "Peso Manual",
                        icon = Icons.Default.Edit,
                        onClick = {
                            manualWeightInput = if (scaleState.currentWeight > 0.0) scaleState.currentWeight.toString() else ""
                            isManualWeightOpen = true
                        }
                    ),
                    SgniaQuickActionItem(
                        label = "Despiece",
                        icon = Icons.Default.ContentCut,
                        onClick = { viewModel.openDespiece() }
                    ),
                    SgniaQuickActionItem(
                        label = "Catálogo PLU",
                        icon = Icons.Default.FormatListNumbered,
                        onClick = { isPluListOpen = true }
                    )
                )
            )
        }

        // ==========================================
        // RESUMEN GLOBAL DE TIENDA (CON GRÁFICAS)
        // ==========================================
        item {
            SgniaCerhanGlobalSummaryCard(
                title = "Resumen Global",
                firstLabel = "Total Cortes Activos en Catálogo:",
                firstValue = "${allProducts.size}",
                secondLabel = "Total Pesajes Capturados:",
                secondValue = "${recentCaptures.size}"
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ==========================================
    // MODALES OPERATIVOS DE MOSTRADOR
    // ==========================================
    if (uiState.isPriceAdjustmentOpen) {
        PriceAdjustmentDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closePriceAdjustmentDialog() }
        )
    }

    if (uiState.isQendraImportOpen) {
        QendraCatalogImportDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeQendraImportDialog() }
        )
    }

    if (uiState.isDespieceOpen) {
        DespiecePiernaDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeDespiece() }
        )
    }

    if (isPluListOpen) {
        PluCatalogListDialog(
            products = allProducts,
            onSelectProduct = { prod ->
                viewModel.onCodeInputChanged(prod.localCode.toString())
                isPluListOpen = false
            },
            onDismiss = { isPluListOpen = false },
            onSaveProduct = { updated -> viewModel.saveProduct(updated) },
            onLinkDerivative = { child, parent -> viewModel.linkDerivative(child, parent) },
            onDeleteProduct = { viewModel.deleteProduct(it) },
            onDeleteAllProducts = { viewModel.deleteAllProducts() }
        )
    }

    if (isManualWeightOpen) {
        AlertDialog(
            onDismissRequest = { isManualWeightOpen = false },
            containerColor = Color(0xFF181715),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Scale, contentDescription = null, tint = Color(0xFFEEDDC8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fijar Pesaje de Báscula", fontWeight = FontWeight.Black, color = Color(0xFFFAF6F0))
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Introduce el peso mostrado en la báscula física:",
                        fontSize = 12.sp,
                        color = Color(0xFFB5ACA0)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF100F0E), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFEEDDC8), RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (manualWeightInput.isBlank()) "0.000 kg" else "$manualWeightInput kg",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp,
                            color = Color(0xFFF3EFE6)
                        )
                    }

                    val keypadRows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf(".", "0", "⌫")
                    )

                    keypadRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { key ->
                                Button(
                                    onClick = {
                                        when (key) {
                                            "⌫" -> if (manualWeightInput.isNotEmpty()) {
                                                manualWeightInput = manualWeightInput.dropLast(1)
                                            }
                                            "." -> if (!manualWeightInput.contains(".")) {
                                                manualWeightInput = if (manualWeightInput.isEmpty()) "0." else "$manualWeightInput."
                                            }
                                            else -> if (manualWeightInput.length < 7) {
                                                manualWeightInput += key
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF262320)
                                    )
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFF3EFE6)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("0.500", "1.000", "2.000", "20.00").forEach { preset ->
                            OutlinedButton(
                                onClick = { manualWeightInput = preset },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("${preset}kg", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEEDDC8))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = manualWeightInput.replace(",", ".").toDoubleOrNull()
                        if (parsed != null && parsed >= 0.0) {
                            viewModel.setManualWeight(parsed)
                        }
                        isManualWeightOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3EFE6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("FIJAR PESO EN PANTALLA", color = Color(0xFF141312), fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isManualWeightOpen = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar", color = Color(0xFFB5ACA0))
                }
            }
        )
    }

    if (isCameraScannerOpen) {
        CameraBarcodeScannerDialog(
            onDismiss = { isCameraScannerOpen = false },
            onBarcodeScanned = { barcode ->
                isCameraScannerOpen = false
                viewModel.processScannedBarcode(barcode)
            }
        )
    }
}
