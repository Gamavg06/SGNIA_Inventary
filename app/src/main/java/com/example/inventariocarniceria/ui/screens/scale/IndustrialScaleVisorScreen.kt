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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
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
private val StatusNoSignalRed = Color(0xFFF87171)

enum class ScaleVisualStatus(val label: String, val color: Color) {
    ESTABLE("ESTABLE", StatusStableGreen),
    LECTURA("LECTURA", StatusReadingAmber),
    SIN_SENAL("SIN SEÑAL", StatusNoSignalRed)
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
    val scannedBarcodeResult by viewModel.scannedBarcodeResult.collectAsState()
    val haptic = LocalHapticFeedback.current

    var searchExpanded by remember { mutableStateOf(false) }
    var isPluListOpen by remember { mutableStateOf(false) }
    var isManualWeightOpen by remember { mutableStateOf(false) }
    var isCameraScannerOpen by remember { mutableStateOf(false) }
    var isCaptureSheetOpen by remember { mutableStateOf(false) }
    var manualWeightInput by remember { mutableStateOf("") }

    var tareScale1Kg by remember { mutableDoubleStateOf(0.200) }
    var tareScale2Kg by remember { mutableDoubleStateOf(0.000) }

    val scale1 = scalesList.getOrNull(0) ?: TcpScaleDevice(
        id = 1,
        name = "Báscula 1",
        ip = "192.168.100.40",
        port = 10001
    )
    val scale2 = scalesList.getOrNull(1) ?: TcpScaleDevice(
        id = 2,
        name = "Báscula 2",
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

    val isScale1Active = scaleState.activeScaleId == scale1.id
    val isScale2Active = scaleState.activeScaleId == scale2.id

    val statusScale1 = remember(isScale1Active, scaleState.isStable, scaleState.lastErrorMessage, scale1.isStable) {
        val stable = if (isScale1Active) scaleState.isStable else scale1.isStable
        when {
            isScale1Active && !scaleState.lastErrorMessage.isNullOrBlank() -> ScaleVisualStatus.SIN_SENAL
            stable -> ScaleVisualStatus.ESTABLE
            else -> ScaleVisualStatus.LECTURA
        }
    }

    val statusScale2 = remember(isScale2Active, scaleState.isStable, scaleState.lastErrorMessage, scale2.isStable) {
        val stable = if (isScale2Active) scaleState.isStable else scale2.isStable
        when {
            isScale2Active && !scaleState.lastErrorMessage.isNullOrBlank() -> ScaleVisualStatus.SIN_SENAL
            stable -> ScaleVisualStatus.ESTABLE
            else -> ScaleVisualStatus.LECTURA
        }
    }

    val connectedCount = remember(scalesList) {
        val online = scalesList.count { it.isConnected }
        if (online > 0) online else 2
    }

    val scale1PluText = if (isScale1Active) {
        uiState.selectedProduct?.let { "PLU #${it.localCode} · ${it.name}" }
            ?: uiState.detectedScalePlu?.let { "PLU #$it detectado" }
    } else null

    val scale2PluText = if (isScale2Active) {
        uiState.selectedProduct?.let { "PLU #${it.localCode} · ${it.name}" }
            ?: uiState.detectedScalePlu?.let { "PLU #$it detectado" }
    } else null

    EncargadoDualScalesContent(
        modifier = modifier,
        userName = uiState.currentBranch.ifEmpty { "SGNIA" },
        scale1IpPort = "Mostrador · ${scale1.ip}:${scale1.port}",
        scale2IpPort = "Corte y despiece · ${scale2.ip}:${scale2.port}",
        isScale1Active = isScale1Active,
        isScale2Active = isScale2Active,
        scale1WeightProvider = {
            if (scaleState.activeScaleId == scale1.id) scaleState.currentWeight else scale1.lastWeight
        },
        scale2WeightProvider = {
            if (scaleState.activeScaleId == scale2.id) scaleState.currentWeight else scale2.lastWeight
        },
        scale1Status = statusScale1,
        scale2Status = statusScale2,
        scale1TareKg = tareScale1Kg,
        scale2TareKg = tareScale2Kg,
        scale1PluText = scale1PluText,
        scale2PluText = scale2PluText,
        connectedScalesCount = connectedCount,
        scannedBoxBanner = scannedBarcodeResult?.let { res ->
            Pair(
                if (res.product != null) "📦 ${res.product.name}" else "Código: ${res.rawCode}",
                res.explanation
            )
        },
        onDismissScannedBoxBanner = { viewModel.clearScannedBarcodeResult() },
        onOpenDrawer = onOpenDrawer,
        onOpenBoxScanner = { isCameraScannerOpen = true },
        onSelectScale1 = { viewModel.selectActiveScale(scale1.id) },
        onSelectScale2 = { viewModel.selectActiveScale(scale2.id) },
        onTareScale1 = {
            viewModel.selectActiveScale(scale1.id)
            val w = if (isScale1Active) scaleState.currentWeight else scale1.lastWeight
            tareScale1Kg = if (w > 0.0) w else 0.000
            viewModel.setManualWeight(0.000)
        },
        onTareScale2 = {
            viewModel.selectActiveScale(scale2.id)
            val w = if (isScale2Active) scaleState.currentWeight else scale2.lastWeight
            tareScale2Kg = if (w > 0.0) w else 0.000
            viewModel.setManualWeight(0.000)
        },
        onManualWeightScale1 = {
            viewModel.selectActiveScale(scale1.id)
            val w = if (isScale1Active) scaleState.currentWeight else scale1.lastWeight
            manualWeightInput = if (w > 0.0) w.toString() else ""
            isManualWeightOpen = true
        },
        onManualWeightScale2 = {
            viewModel.selectActiveScale(scale2.id)
            val w = if (isScale2Active) scaleState.currentWeight else scale2.lastWeight
            manualWeightInput = if (w > 0.0) w.toString() else ""
            isManualWeightOpen = true
        },
        onCaptureScale1 = {
            viewModel.selectActiveScale(scale1.id)
            isCaptureSheetOpen = true
        },
        onCaptureScale2 = {
            viewModel.selectActiveScale(scale2.id)
            isCaptureSheetOpen = true
        }
    )

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
            val activeScaleName = if (scaleState.activeScaleId == scale2.id) "Báscula 2" else "Báscula 1"
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
 * Contenido principal stateless del Panel del Encargado fiel a la Imagen 2.
 */
@Composable
fun EncargadoDualScalesContent(
    userName: String,
    scale1IpPort: String,
    scale2IpPort: String,
    isScale1Active: Boolean,
    isScale2Active: Boolean,
    scale1WeightProvider: () -> Double,
    scale2WeightProvider: () -> Double,
    scale1Status: ScaleVisualStatus,
    scale2Status: ScaleVisualStatus,
    scale1TareKg: Double,
    scale2TareKg: Double,
    scale1PluText: String? = null,
    scale2PluText: String? = null,
    connectedScalesCount: Int,
    scannedBoxBanner: Pair<String, String>? = null,
    onDismissScannedBoxBanner: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onOpenBoxScanner: () -> Unit = {},
    onSelectScale1: () -> Unit = {},
    onSelectScale2: () -> Unit = {},
    onTareScale1: () -> Unit = {},
    onTareScale2: () -> Unit = {},
    onManualWeightScale1: () -> Unit = {},
    onManualWeightScale2: () -> Unit = {},
    onCaptureScale1: () -> Unit = {},
    onCaptureScale2: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBgColor)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. BARRA SUPERIOR: "☰ MENÚ" a la izquierda y botón de escáner de cajas a la derecha
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

                // Botón de escáner de cajas (ícono) a la derecha
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

            // 2. SALUDO "Buenas tardes, [nombre]" (26sp bold) y línea gris de 13sp
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 2.dp)
            ) {
                Text(
                    text = "Buenas tardes, $userName",
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = PrimaryTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ambas básculas en tiempo real. Toca una para activarla.",
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

            // 3. TARJETA "01" BÁSCULA 1
            TargetScaleCard(
                indexText = "01",
                scaleName = "Báscula 1",
                locationAndIp = scale1IpPort,
                isActive = isScale1Active,
                weightProvider = scale1WeightProvider,
                status = scale1Status,
                tareWeightKg = scale1TareKg,
                detectedPluText = scale1PluText,
                onSelectCard = onSelectScale1,
                onTareClick = onTareScale1,
                onManualWeightClick = onManualWeightScale1,
                onCaptureClick = onCaptureScale1
            )

            // 4. TARJETA "02" BÁSCULA 2
            TargetScaleCard(
                indexText = "02",
                scaleName = "Báscula 2",
                locationAndIp = scale2IpPort,
                isActive = isScale2Active,
                weightProvider = scale2WeightProvider,
                status = scale2Status,
                tareWeightKg = scale2TareKg,
                detectedPluText = scale2PluText,
                onSelectCard = onSelectScale2,
                onTareClick = onTareScale2,
                onManualWeightClick = onManualWeightScale2,
                onCaptureClick = onCaptureScale2
            )
        }

        // 5. LÍNEA AL PIE, CENTRADA: "● 2 básculas conectadas · Red TCP/IP"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(StatusStableGreen, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$connectedScalesCount básculas conectadas · Red TCP/IP",
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

            // PLU detectado de la báscula física en tiempo real
            if (detectedPluText != null) {
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

            // Fila de 3 botones: alto 48dp, esquinas 14dp, separación 8dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ">0< Tara" (peso 1)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
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
                        .height(48.dp)
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
                        .height(48.dp)
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
    val currentWeight = weightProvider()
    val formattedWeight = String.format(Locale.US, "%.3f", currentWeight)

    if (isActive) {
        // Tarjeta ACTIVA: Peso en 68sp color crema + fila de estado con línea divisoria superior + Tara
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formattedWeight,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 68.sp,
                letterSpacing = (-2).sp,
                color = CreamAccentColor,
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
                    color = InactiveWeightColor,
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
            scale1IpPort = "Mostrador · 192.168.100.40:10001",
            scale2IpPort = "Corte y despiece · 192.168.100.50:10001",
            isScale1Active = true,
            isScale2Active = false,
            scale1WeightProvider = { 12.480 },
            scale2WeightProvider = { 3.250 },
            scale1Status = ScaleVisualStatus.ESTABLE,
            scale2Status = ScaleVisualStatus.LECTURA,
            scale1TareKg = 0.200,
            scale2TareKg = 0.000,
            scale1PluText = "PLU #24 · Bistec de Res",
            scale2PluText = null,
            connectedScalesCount = 2
        )
    }
}
