package com.example.inventariocarniceria.ui.screens.scale

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.scale.TcpScaleDevice
import com.example.inventariocarniceria.ui.components.AsyncProductImage
import com.example.inventariocarniceria.ui.components.CameraBarcodeScannerDialog
import com.example.inventariocarniceria.ui.components.SgniaPillButton
import com.example.inventariocarniceria.ui.screens.inventory.DespiecePiernaDialog
import com.example.inventariocarniceria.ui.screens.inventory.PriceAdjustmentDialog
import com.example.inventariocarniceria.ui.screens.inventory.QendraCatalogImportDialog
import com.example.inventariocarniceria.ui.screens.settings.ScaleNetworkConfigDialog
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import java.util.Locale

// Paleta exacta del Diseño Objetivo (Imagen 2)
private val ScreenBgColor = Color(0xFF0F0E0D)
private val CardBgColor = Color(0xFF181715)
private val NormalBorderColor = Color(0xFF2A2723)
private val ButtonBorderColor = Color(0xFF3A352F)
private val CreamAccentColor = Color(0xFFEEDDC8)
private val PrimaryTextColor = Color(0xFFF3EFE6)
private val SecondaryTextColor = Color(0xFFA8A196)
private val InactiveWeightColor = Color(0xFFC9BFB0)
private val DarkInkColor = Color(0xFF141312)

private val StatusStableGreen = Color(0xFF4ADE80)
private val StatusReadingAmber = Color(0xFFFBBF24)
private val StatusNoSignalRed = Color(0xFFE07A6F)

enum class ScaleVisualStatus(val label: String, val color: Color) {
    ESTABLE("ESTABLE", StatusStableGreen),
    LECTURA("LECTURA", StatusReadingAmber),
    SIN_CONEXION("SIN CONEXIÓN", StatusNoSignalRed)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndustrialScaleVisorScreen(
    viewModel: InventoryViewModel,
    onOpenDrawer: () -> Unit = {},
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
    var isCaptureSheetOpen by remember { mutableStateOf(false) }
    var isScaleNetworkConfigOpen by remember { mutableStateOf(false) }
    var manualWeightInput by remember { mutableStateOf("") }

    var scaleTareMap by remember {
        mutableStateOf<Map<Int, Double>>(
            mapOf(1 to 0.200, 2 to 0.000)
        )
    }

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

    val activePluText = uiState.selectedProduct?.let { "PLU #${it.localCode} · ${it.name}" }
        ?: uiState.detectedScalePlu?.let { "PLU #$it detectado" }

    val connectedCount = remember(scalesList) {
        val online = scalesList.count { it.isConnected }
        if (online > 0) online else scalesList.size
    }

    EncargadoDualScalesContent(
        modifier = modifier,
        userName = uiState.currentBranch.ifEmpty { "SGNIA" },
        scalesList = scalesList,
        activeScaleId = scaleState.activeScaleId,
        currentWeight = scaleState.currentWeight,
        isScaleStable = scaleState.isStable,
        scaleErrorMessage = scaleState.lastErrorMessage,
        scaleTareMap = scaleTareMap,
        detectedPluText = activePluText,
        connectedScalesCount = connectedCount,
        recentCaptures = recentCaptures,
        allProducts = allProducts,
        activeSelectedProduct = uiState.selectedProduct,
        scannedBoxBanner = scannedBarcodeResult?.let { res ->
            Pair(
                if (res.product != null) "📦 ${res.product.name}" else "Código: ${res.rawCode}",
                res.explanation
            )
        },
        onDismissScannedBoxBanner = { viewModel.clearScannedBarcodeResult() },
        onOpenDrawer = onOpenDrawer,
        onOpenBoxScanner = { isCameraScannerOpen = true },
        onOpenScaleConfig = { isScaleNetworkConfigOpen = true },
        onSelectScale = { scaleId -> viewModel.selectActiveScale(scaleId) },
        onTareScale = { scaleId ->
            viewModel.selectActiveScale(scaleId)
            val active = scaleState.activeScaleId == scaleId
            val w = if (active) scaleState.currentWeight else (scalesList.find { it.id == scaleId }?.lastWeight ?: 0.0)
            scaleTareMap = scaleTareMap + (scaleId to (if (w > 0.0) w else 0.000))
            viewModel.setManualWeight(0.000)
        },
        onManualWeightScale = { scaleId ->
            viewModel.selectActiveScale(scaleId)
            val active = scaleState.activeScaleId == scaleId
            val w = if (active) scaleState.currentWeight else (scalesList.find { it.id == scaleId }?.lastWeight ?: 0.0)
            manualWeightInput = if (w > 0.0) w.toString() else ""
            isManualWeightOpen = true
        },
        onCaptureScale = { scaleId ->
            viewModel.selectActiveScale(scaleId)
            isCaptureSheetOpen = true
        }
    )

    if (isScaleNetworkConfigOpen) {
        ScaleNetworkConfigDialog(
            viewModel = viewModel,
            onDismiss = { isScaleNetworkConfigOpen = false }
        )
    }

    // =========================================================================
    // MODAL BOTTOM SHEET: CORTE ACTIVO Y CAPTURA (Al tocar "Capturar")
    // =========================================================================
    if (isCaptureSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { isCaptureSheetOpen = false },
            sheetState = sheetState,
            containerColor = CardBgColor,
            contentColor = PrimaryTextColor,
            dragHandle = {
                androidx.compose.material3.BottomSheetDefaults.DragHandle(
                    color = ButtonBorderColor
                )
            }
        ) {
            val activeScaleName = scalesList.find { it.id == scaleState.activeScaleId }?.name ?: "Báscula ${scaleState.activeScaleId}"
            val activeWeight = scaleState.currentWeight
            val selectedProduct = uiState.selectedProduct
            val totalImporte = (selectedProduct?.price ?: 0.0) * activeWeight

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cabecera del panel de captura
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Corte Activo y Captura",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryTextColor
                        )
                        Text(
                            text = "$activeScaleName · Peso activo: ${String.format(Locale.US, "%.3f", activeWeight)} kg",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = SecondaryTextColor
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(CreamAccentColor.copy(alpha = 0.15f))
                                .border(1.dp, CreamAccentColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .clickable {
                                    isCaptureSheetOpen = false
                                    viewModel.openQendraImportDialog()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = CreamAccentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Importar PLUs",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = CreamAccentColor
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF23201D))
                                .border(1.dp, ButtonBorderColor, RoundedCornerShape(10.dp))
                                .clickable { isPluListOpen = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FormatListNumbered,
                                    contentDescription = null,
                                    tint = CreamAccentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Catálogo (${allProducts.size})",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = PrimaryTextColor
                                )
                            }
                        }
                    }
                }

                // Buscador de corte/PLU con botón de escáner de cámara (QR/código de barras)
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
                            Icon(Icons.Default.Search, contentDescription = null, tint = CreamAccentColor)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isCameraScannerOpen = true }) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Escanear Caja / QR",
                                    tint = CreamAccentColor
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CreamAccentColor,
                            unfocusedBorderColor = ButtonBorderColor,
                            focusedTextColor = PrimaryTextColor,
                            unfocusedTextColor = PrimaryTextColor
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = searchExpanded && suggestions.isNotEmpty(),
                        onDismissRequest = { searchExpanded = false },
                        modifier = Modifier
                            .background(CardBgColor)
                            .border(1.dp, ButtonBorderColor, RoundedCornerShape(12.dp))
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
                                            fontSize = 13.sp,
                                            color = PrimaryTextColor
                                        )
                                        Text(
                                            text = "$${String.format(Locale.US, "%.2f", prod.price)}/kg",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = StatusStableGreen
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.onCodeInputChanged(prod.localCode.toString())
                                    searchExpanded = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(
                                    textColor = PrimaryTextColor
                                )
                            )
                        }
                    }
                }

                // Botón dedicado Escanear Caja dentro del panel de captura
                OutlinedButton(
                    onClick = { isCameraScannerOpen = true },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ButtonBorderColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTextColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = CreamAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Escanear Caja / Código de Barras o QR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Detalle del corte seleccionado + Botones -$10/-$5/+$5/+$10 + Importe Total
                if (selectedProduct != null) {
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
                            imageUrl = selectedProduct.imageUri,
                            contentDescription = selectedProduct.name,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "[PLU #${selectedProduct.localCode}] ${selectedProduct.name}",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = PrimaryTextColor
                            )
                            Text(
                                text = "Precio: $${String.format(Locale.US, "%.2f", selectedProduct.price)}/kg • ${selectedProduct.category}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = SecondaryTextColor
                            )
                        }
                    }

                    // Botones rápidos de precio (-$10, -$5, +$5, +$10)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(-10.0, -5.0, 5.0, 10.0).forEach { delta ->
                            val label = if (delta > 0) "+$${delta.toInt()}" else "-$${kotlin.math.abs(delta).toInt()}"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF23201D))
                                    .border(1.dp, ButtonBorderColor, RoundedCornerShape(10.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        val newP = (selectedProduct.price + delta).coerceAtLeast(1.0)
                                        viewModel.updateProductPrice(selectedProduct, newP)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PrimaryTextColor
                                )
                            }
                        }
                    }

                    // Fila de Importe Total
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background( ScreenBgColor)
                            .border(1.dp, NormalBorderColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "IMPORTE TOTAL",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SecondaryTextColor
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%,.2f", totalImporte)} MXN",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = CreamAccentColor
                        )
                    }
                }

                // Botón principal "Capturar pesaje en inventario"
                SgniaPillButton(
                    text = "Capturar pesaje en inventario (${String.format(Locale.US, "%.3f", activeWeight)} kg)",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.captureWeight()
                        isCaptureSheetOpen = false
                    },
                    enabled = selectedProduct != null,
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = DarkInkColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
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
            onDeleteAllProducts = { viewModel.deleteAllProducts() },
            onImportQendra = {
                isPluListOpen = false
                viewModel.openQendraImportDialog()
            }
        )
    }

    if (isManualWeightOpen) {
        AlertDialog(
            onDismissRequest = { isManualWeightOpen = false },
            containerColor = CardBgColor,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Scale, contentDescription = null, tint = CreamAccentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fijar Pesaje de Báscula", fontWeight = FontWeight.Black, color = PrimaryTextColor)
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
                        color = SecondaryTextColor
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ScreenBgColor, RoundedCornerShape(12.dp))
                            .border(1.dp, CreamAccentColor, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (manualWeightInput.isBlank()) "0.000 kg" else "$manualWeightInput kg",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp,
                            color = PrimaryTextColor
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
                                        color = PrimaryTextColor
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
                                Text("${preset}kg", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CreamAccentColor)
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
                    colors = ButtonDefaults.buttonColors(containerColor = CreamAccentColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("FIJAR PESO EN PANTALLA", color = DarkInkColor, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isManualWeightOpen = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar", color = SecondaryTextColor)
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

/**
 * Contenido principal dinámico del Panel del Encargado (soporta 1, 2, 4, 8, 10+ básculas).
 */
@Composable
fun EncargadoDualScalesContent(
    userName: String,
    scalesList: List<TcpScaleDevice>,
    activeScaleId: Int,
    currentWeight: Double,
    isScaleStable: Boolean,
    scaleErrorMessage: String?,
    scaleTareMap: Map<Int, Double>,
    detectedPluText: String? = null,
    connectedScalesCount: Int,
    recentCaptures: List<com.example.inventariocarniceria.data.local.InventoryCaptureEntity> = emptyList(),
    allProducts: List<com.example.inventariocarniceria.data.local.ProductEntity> = emptyList(),
    activeSelectedProduct: com.example.inventariocarniceria.data.local.ProductEntity? = null,
    scannedBoxBanner: Pair<String, String>? = null,
    onDismissScannedBoxBanner: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onOpenBoxScanner: () -> Unit = {},
    onOpenScaleConfig: () -> Unit = {},
    onSelectScale: (Int) -> Unit = {},
    onTareScale: (Int) -> Unit = {},
    onManualWeightScale: (Int) -> Unit = {},
    onCaptureScale: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBgColor)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. BARRA SUPERIOR: "☰ MENÚ", BOTÓN RÁPIDO "＋ BÁSCULAS" Y ESCÁNER DE CAJAS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBgColor)
                        .border(1.dp, NormalBorderColor, RoundedCornerShape(14.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenDrawer()
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "☰",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTextColor
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MENÚ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp,
                            color = PrimaryTextColor
                        )
                    }
                }

                // Botón central de acceso directo al Gestor de Básculas
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBgColor)
                        .border(1.dp, ButtonBorderColor, RoundedCornerShape(14.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenScaleConfig()
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = CreamAccentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "BÁSCULAS (${scalesList.size})",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CreamAccentColor
                        )
                    }
                }

                // Botón de escáner de cajas a la derecha
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBgColor)
                        .border(1.dp, NormalBorderColor, RoundedCornerShape(14.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenBoxScanner()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    ScannerViewfinderIcon(
                        color = PrimaryTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. SALUDO CON NÚMERO DINÁMICO DE BÁSCULAS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Buenas tardes, $userName",
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = PrimaryTextColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (scalesList.size == 1) "1 báscula conectada en tiempo real. Toca para activarla."
                    else "Red de ${scalesList.size} básculas en tiempo real. Toca una para activarla.",
                    fontSize = 13.sp,
                    color = SecondaryTextColor
                )
            }

            // Aviso emergente si se acaba de escanear una caja
            if (scannedBoxBanner != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBgColor)
                        .border(1.dp, CreamAccentColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scannedBoxBanner.first,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryTextColor
                        )
                        Text(
                            text = scannedBoxBanner.second,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = SecondaryTextColor
                        )
                    }
                    IconButton(onClick = onDismissScannedBoxBanner) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Confirmar",
                            tint = CreamAccentColor
                        )
                    }
                }
            }

            // 3. LISTA DINÁMICA DE TODAS LAS BÁSCULAS REGISTRADAS (1, 2, 3, 5, 10 BÁSCULAS)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(scalesList, key = { _, s -> s.id }) { index, scale ->
                    val isActive = activeScaleId == scale.id
                    val isConnected = if (isActive) (connectedScalesCount > 0 && scaleErrorMessage.isNullOrBlank()) else scale.isConnected

                    val status = if (!isConnected || (isActive && !scaleErrorMessage.isNullOrBlank())) {
                        ScaleVisualStatus.SIN_CONEXION
                    } else if (if (isActive) isScaleStable else scale.isStable) {
                        ScaleVisualStatus.ESTABLE
                    } else {
                        ScaleVisualStatus.LECTURA
                    }

                    val tareKg = scaleTareMap[scale.id] ?: 0.000
                    val locationAndIp = "${scale.brand} ${scale.model} · ${scale.ip}:${scale.port}"

                    // Resolución de producto PLU en tiempo real para esta báscula
                    val scalePlu = if (isActive) (activeSelectedProduct?.localCode ?: scale.lastPluCode) else scale.lastPluCode
                    val resolvedProd = if (isActive && activeSelectedProduct != null) {
                        activeSelectedProduct
                    } else if (scalePlu != null) {
                        allProducts.find { it.localCode == scalePlu || it.code == scalePlu }
                    } else null

                    val detectedProdName = resolvedProd?.name ?: if (scalePlu == 1) "Menudo de Res" else if (scalePlu != null) "PLU #$scalePlu" else null
                    val detectedPluNum = scalePlu ?: resolvedProd?.localCode
                    val detectedPrice = resolvedProd?.price ?: if (scalePlu == 1) 120.00 else null

                    TargetScaleCard(
                        indexText = String.format(Locale.US, "%02d", index + 1),
                        scaleName = scale.name,
                        locationAndIp = locationAndIp,
                        isActive = isActive,
                        weightProvider = {
                            if (activeScaleId == scale.id) currentWeight else scale.lastWeight
                        },
                        status = status,
                        tareWeightKg = tareKg,
                        detectedProductName = detectedProdName,
                        detectedPluCode = detectedPluNum,
                        detectedPricePerKg = detectedPrice,
                        detectedPluText = if (isActive) detectedPluText else null,
                        onSelectCard = { onSelectScale(scale.id) },
                        onTareClick = { onTareScale(scale.id) },
                        onManualWeightClick = { onManualWeightScale(scale.id) },
                        onCaptureClick = { onCaptureScale(scale.id) }
                    )
                }

                // Aprovechamiento de espacio cuando hay 1 o 2 básculas
                if (scalesList.size <= 2 && recentCaptures.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(CardBgColor)
                                .border(1.dp, NormalBorderColor, RoundedCornerShape(18.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ÚLTIMOS PESAJES CAPTURADOS",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = CreamAccentColor
                                    )
                                    Text(
                                        text = "${recentCaptures.size} en historial",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = SecondaryTextColor
                                    )
                                }
                                recentCaptures.take(3).forEach { cap ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cap.productName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = PrimaryTextColor,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.3f", cap.capturedWeight)} kg",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = CreamAccentColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. LÍNEA AL PIE, CENTRADA Y CLICKABLE PARA GESTIONAR LA RED
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onOpenScaleConfig() }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (connectedScalesCount > 0) StatusStableGreen else Color(0xFFE07A6F), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$connectedScalesCount de ${scalesList.size} básculas conectadas · Red TCP/IP ⚙️",
                fontSize = 13.sp,
                color = SecondaryTextColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Tarjeta de Báscula fiel a la Imagen 2 (Activa / Inactiva).
 */
@Composable
private fun TargetScaleCard(
    indexText: String,
    scaleName: String,
    locationAndIp: String,
    isActive: Boolean,
    weightProvider: () -> Double,
    status: ScaleVisualStatus,
    tareWeightKg: Double,
    detectedProductName: String? = null,
    detectedPluCode: Int? = null,
    detectedPricePerKg: Double? = null,
    detectedPluText: String? = null,
    onSelectCard: () -> Unit,
    onTareClick: () -> Unit,
    onManualWeightClick: () -> Unit,
    onCaptureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardBgColor)
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) CreamAccentColor else NormalBorderColor,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSelectCard()
            }
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Fila superior: índice "01" (monoespaciada 20sp gris), nombre "Báscula 1" (18sp bold), píldora a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = indexText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp,
                        color = SecondaryTextColor
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = scaleName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PrimaryTextColor
                    )
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(CreamAccentColor)
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ACTIVA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            color = DarkInkColor
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .border(1.dp, ButtonBorderColor, RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TOCAR PARA USAR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            color = SecondaryTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Ubicación + IP:puerto en monoespaciada 12sp gris
            Text(
                text = locationAndIp,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = SecondaryTextColor
            )

            // PLU y Producto detectado de la báscula física en tiempo real (ej. 1 kg de Menudo de Res código PLU 1)
            if (detectedProductName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val w = weightProvider()
                val price = detectedPricePerKg ?: 0.0
                val totalImp = if (price > 0 && w > 0) w * price else null

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF221F1B))
                        .border(1.dp, CreamAccentColor.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🥩 $detectedProductName",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = CreamAccentColor,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "PLU #${detectedPluCode ?: 1}" + if (price > 0) " · $${String.format(Locale.US, "%.2f", price)}/kg" else "",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = SecondaryTextColor
                            )
                        }

                        if (totalImp != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${String.format(Locale.US, "%,.2f", totalImp)}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color(0xFFC9A96E)
                                )
                                Text(
                                    text = "TOTAL VENTA",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = SecondaryTextColor
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2A2723))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "PLU ACTIVO",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp,
                                    color = SecondaryTextColor
                                )
                            }
                        }
                    }
                }
            } else if (detectedPluText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF221F1B))
                        .border(1.dp, CreamAccentColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🥩 $detectedPluText",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CreamAccentColor,
                        maxLines = 1
                    )
                    Text(
                        text = "PLU BÁSCULA",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        color = SecondaryTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lectura diferida de peso para evitar recomposiciones innecesarias
            LiveScaleWeightAndStatusSection(
                isActive = isActive,
                weightProvider = weightProvider,
                status = status,
                tareWeightKg = tareWeightKg
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Fila de 3 botones: alto 64dp (NoirSpace.touch para manos húmedas/guantes), esquinas 14dp, separación 8dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ">0< Tara" (peso 1)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Transparent)
                        .border(1.dp, ButtonBorderColor, RoundedCornerShape(14.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTareClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ">0< Tara",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryTextColor
                    )
                }

                // "Peso" (peso 1)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Transparent)
                        .border(1.dp, ButtonBorderColor, RoundedCornerShape(14.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onManualWeightClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Peso",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryTextColor
                    )
                }

                // "Capturar" (peso 1.5)
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isActive) CreamAccentColor else NormalBorderColor)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCaptureClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Capturar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isActive) DarkInkColor else PrimaryTextColor
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveScaleWeightAndStatusSection(
    isActive: Boolean,
    weightProvider: () -> Double,
    status: ScaleVisualStatus,
    tareWeightKg: Double
) {
    val isDisconnected = status == ScaleVisualStatus.SIN_CONEXION
    val currentWeight = weightProvider()
    val formattedWeight = if (isDisconnected) "--.---" else String.format(Locale.US, "%.3f", currentWeight)

    if (isActive) {
        // Tarjeta ACTIVA: Peso en 68sp color crema (o rojo si desconectada) + fila de estado con línea divisoria superior + Tara
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formattedWeight,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 68.sp,
                letterSpacing = (-2).sp,
                color = if (isDisconnected) StatusNoSignalRed else CreamAccentColor,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "kg",
                fontSize = 22.sp,
                color = SecondaryTextColor,
                modifier = Modifier.alignByBaseline()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 1.dp, color = NormalBorderColor)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScaleStatusIndicator(status = status)
            Text(
                text = "Tara ${String.format(Locale.US, "%.3f", tareWeightKg)} kg",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = SecondaryTextColor
            )
        }
    } else {
        // Tarjeta INACTIVA: Peso en 48sp #C9BFB0; estado en la misma línea alineado a la derecha; se omite fila de Tara
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row {
                Text(
                    text = formattedWeight,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp,
                    letterSpacing = (-1.5).sp,
                    color = if (isDisconnected) StatusNoSignalRed else InactiveWeightColor,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "kg",
                    fontSize = 22.sp,
                    color = SecondaryTextColor,
                    modifier = Modifier.alignByBaseline()
                )
            }

            Box(modifier = Modifier.padding(bottom = 8.dp)) {
                ScaleStatusIndicator(status = status)
            }
        }
    }
}

@Composable
private fun ScaleStatusIndicator(status: ScaleVisualStatus) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(status.color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = status.label,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.2.sp,
            color = status.color
        )
    }
}

/**
 * Ícono de escáner de cajas (esquinas de visor + línea central) idéntico al botón superior derecho de la Imagen 2.
 */
@Composable
private fun ScannerViewfinderIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val w = size.width
        val h = size.height
        val cornerLen = w * 0.28f

        // Esquina superior izquierda
        drawLine(color, Offset(0f, cornerLen), Offset(0f, 0f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(0f, 0f), Offset(cornerLen, 0f), strokeWidth = stroke, cap = StrokeCap.Round)

        // Esquina superior derecha
        drawLine(color, Offset(w - cornerLen, 0f), Offset(w, 0f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w, 0f), Offset(w, cornerLen), strokeWidth = stroke, cap = StrokeCap.Round)

        // Esquina inferior izquierda
        drawLine(color, Offset(0f, h - cornerLen), Offset(0f, h), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(0f, h), Offset(cornerLen, h), strokeWidth = stroke, cap = StrokeCap.Round)

        // Esquina inferior derecha
        drawLine(color, Offset(w - cornerLen, h), Offset(w, h), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w, h - cornerLen), Offset(w, h), strokeWidth = stroke, cap = StrokeCap.Round)

        // Línea horizontal central de escaneo
        drawLine(
            color = color,
            start = Offset(w * 0.18f, h * 0.5f),
            end = Offset(w * 0.82f, h * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0E0D, widthDp = 390, heightDp = 844)
@Composable
fun IndustrialScaleVisorScreenPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBgColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        EncargadoDualScalesContent(
            userName = "SGNIA",
            scalesList = listOf(
                TcpScaleDevice(1, "Báscula 1", "192.168.100.40", 10001, "Torrey", "L-EQ", lastWeight = 12.480, isConnected = true),
                TcpScaleDevice(2, "Báscula 2", "192.168.100.50", 10001, "Systel", "Cuora Max", lastWeight = 3.250, isConnected = true)
            ),
            activeScaleId = 1,
            currentWeight = 12.480,
            isScaleStable = true,
            scaleErrorMessage = null,
            scaleTareMap = mapOf(1 to 0.200, 2 to 0.000),
            detectedPluText = "PLU #24 · Bistec de Res",
            connectedScalesCount = 2
        )
    }
}
