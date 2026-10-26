package com.example.inventariocarniceria.ui.screens.reception

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.inventariocarniceria.utils.MeatBarcodeParser
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.StockEntryEntity
import com.example.inventariocarniceria.ui.components.CameraBarcodeScannerDialog
import com.example.inventariocarniceria.ui.design.BentoCard
import com.example.inventariocarniceria.ui.design.BigMetric
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirPrimaryButton
import com.example.inventariocarniceria.ui.design.NoirRadius
import com.example.inventariocarniceria.ui.design.NoirSecondaryButton
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill
import com.example.inventariocarniceria.ui.screens.inventory.DespiecePiernaDialog
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockReceptionScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scaleState by viewModel.scaleState.collectAsState()
    val recentEntries by viewModel.recentStockEntries.collectAsState()
    val scannedBarcodeResult by viewModel.scannedBarcodeResult.collectAsState()

    var manualKilosInput by remember { mutableStateOf("") }
    var supplierNotesInput by remember { mutableStateOf("") }
    var isBarcodeScannerOpen by remember { mutableStateOf(false) }

    // Relleno automático inmediato del peso al escanear código de barras de caja (ej. 22.30 kg)
    androidx.compose.runtime.LaunchedEffect(scannedBarcodeResult) {
        scannedBarcodeResult?.let { res ->
            if (res.weightKg > 0.0) {
                manualKilosInput = String.format(Locale.US, "%.3f", res.weightKg)
            }
        }
    }

    val totalReceivedKilos = recentEntries.sumOf { it.kilosReceived }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NoirColors.Background)
            .padding(horizontal = NoirSpace.md, vertical = NoirSpace.sm),
        verticalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
    ) {
        // ENCABEZADO
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    NoirLabel("RECEPCIÓN DE MERCANCÍA · ${uiState.currentBranch.uppercase()}", color = NoirColors.Gold)
                    Text(
                        text = "Entradas & Despiece",
                        style = NoirType.display.copy(fontSize = 24.sp),
                        color = NoirColors.TextPrimary
                    )
                    Text(
                        text = "Ingreso de canales, cajas por código de barras y pesaje en báscula",
                        style = NoirType.bodySmall,
                        color = NoirColors.TextMuted
                    )
                }
            }
        }

        // BENTO HERO
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
            ) {
                val isScaleConnected = scaleState.isConnected && scaleState.lastErrorMessage.isNullOrBlank()
                BentoCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(NoirSpace.md)
                ) {
                    BigMetric(
                        value = if (isScaleConnected) String.format(Locale.US, "%.3f", scaleState.currentWeight) else "--.---",
                        label = "BÁSCULA ACTIVA",
                        unit = "KG",
                        style = NoirType.metricL,
                        valueColor = when {
                            !isScaleConnected -> NoirColors.Danger
                            scaleState.isStable -> NoirColors.Emerald
                            else -> NoirColors.Cream
                        }
                    )
                    StatusPill(
                        text = when {
                            !isScaleConnected -> "SIN CONEXIÓN"
                            scaleState.isStable -> "ESTABLE"
                            else -> "LECTURA"
                        },
                        tone = when {
                            !isScaleConnected -> PillTone.Danger
                            scaleState.isStable -> PillTone.Positive
                            else -> PillTone.Pending
                        }
                    )
                }

                BentoCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(NoirSpace.md)
                ) {
                    BigMetric(
                        value = String.format(Locale.US, "%.1f", totalReceivedKilos),
                        label = "INGRESOS HOY",
                        unit = "KG",
                        style = NoirType.metricL,
                        valueColor = NoirColors.TextPrimary
                    )
                    StatusPill("${recentEntries.size} LOTES", tone = PillTone.Neutral)
                }
            }
        }

        // BOTONES RÁPIDOS Y BÚSQUEDA DE CORTE
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(NoirSpace.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                ) {
                    NoirPrimaryButton(
                        text = "Escanear Caja",
                        icon = Icons.Default.QrCodeScanner,
                        onClick = { isBarcodeScannerOpen = true },
                        modifier = Modifier.weight(1f)
                    )

                    NoirSecondaryButton(
                        text = "Despiece",
                        icon = Icons.Default.ContentCut,
                        onClick = { viewModel.openDespiece() },
                        modifier = Modifier.weight(0.9f)
                    )
                }

                OutlinedTextField(
                    value = uiState.codeInput,
                    onValueChange = { viewModel.onCodeInputChanged(it) },
                    label = { Text("Código PLU o Nombre del Corte", color = NoirColors.TextSecondary) },
                    placeholder = { Text("Ej. 24 o Pierna de Cerdo", color = NoirColors.TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(NoirRadius.md),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoirColors.Cream,
                        unfocusedBorderColor = NoirColors.Hairline,
                        focusedTextColor = NoirColors.TextPrimary,
                        unfocusedTextColor = NoirColors.TextPrimary,
                        focusedContainerColor = NoirColors.Surface,
                        unfocusedContainerColor = NoirColors.Surface
                    )
                )
            }
        }

        // PANEL DE CAPTURA DEL PRODUCTO DETECTADO
        item {
            val product = uiState.selectedProduct
            if (product != null) {
                BentoCard(
                    contentPadding = PaddingValues(NoirSpace.lg),
                    verticalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NoirColors.Emerald, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PLU #${product.localCode} · ${product.name.uppercase()}",
                                style = NoirType.title.copy(fontSize = 15.sp),
                                color = NoirColors.TextPrimary
                            )
                        }
                        Text(
                            text = String.format(Locale.US, "$%.2f/kg", product.price),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NoirColors.Cream
                        )
                    }

                    val effectiveKilos = manualKilosInput.toDoubleOrNull() ?: scaleState.currentWeight
                    val formattedWeight = String.format(Locale.US, "%.3f", effectiveKilos)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualKilosInput,
                            onValueChange = { manualKilosInput = it },
                            label = { Text("Kilos a Recibir (kg)", color = NoirColors.TextSecondary) },
                            placeholder = { Text("Báscula: $formattedWeight kg", color = NoirColors.TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            trailingIcon = {
                                androidx.compose.material3.IconButton(onClick = { isBarcodeScannerOpen = true }) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "Escanear peso de caja",
                                        tint = NoirColors.Cream
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(NoirRadius.sm),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NoirColors.Cream,
                                unfocusedBorderColor = NoirColors.Hairline,
                                focusedTextColor = NoirColors.TextPrimary,
                                unfocusedTextColor = NoirColors.TextPrimary,
                                focusedContainerColor = NoirColors.SurfaceSunken,
                                unfocusedContainerColor = NoirColors.SurfaceSunken
                            )
                        )

                        NoirSecondaryButton(
                            text = "Báscula",
                            icon = Icons.Default.Scale,
                            onClick = { manualKilosInput = String.format(Locale.US, "%.3f", scaleState.currentWeight) },
                            modifier = Modifier.width(130.dp)
                        )
                    }

                    OutlinedTextField(
                        value = supplierNotesInput,
                        onValueChange = { supplierNotesInput = it },
                        label = { Text("Notas de Proveedor / Lote", color = NoirColors.TextSecondary) },
                        placeholder = { Text("Ej. Rastro Central Lote #402", color = NoirColors.TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(NoirRadius.sm),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoirColors.Cream,
                            unfocusedBorderColor = NoirColors.Hairline,
                            focusedTextColor = NoirColors.TextPrimary,
                            unfocusedTextColor = NoirColors.TextPrimary
                        )
                    )

                    NoirPrimaryButton(
                        text = "REGISTRAR ENTRADA DE STOCK",
                        icon = Icons.Default.LocalShipping,
                        onClick = {
                            viewModel.registerStockEntry(effectiveKilos, supplierNotesInput.ifBlank { null })
                            manualKilosInput = ""
                            supplierNotesInput = ""
                        }
                    )
                }
            } else if (uiState.isProductNotFound) {
                BentoCard(
                    contentPadding = PaddingValues(NoirSpace.lg),
                    verticalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = NoirColors.Amber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Código #${uiState.codeInput} no existe en catálogo",
                            style = NoirType.title.copy(fontSize = 14.sp),
                            color = NoirColors.TextPrimary
                        )
                    }

                    Text(
                        text = "¿Deseas dar de alta este corte nuevo inmediatamente para registrar su entrada?",
                        style = NoirType.bodySmall,
                        color = NoirColors.TextMuted
                    )

                    NoirPrimaryButton(
                        text = "Crear Producto Al Vuelo",
                        icon = Icons.Default.AddBusiness,
                        onClick = { viewModel.openQuickCreateDialog() }
                    )
                }
            }
        }

        // BITÁCORA DE ENTRADAS RECIENTES
        item {
            BentoCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(NoirSpace.sm)
            ) {
                NoirLabel("ÚLTIMAS ENTRADAS RECIBIDAS", color = NoirColors.Cream)

                if (recentEntries.isEmpty()) {
                    Text(
                        text = "Aún no hay entradas registradas en esta sucursal.",
                        style = NoirType.bodySmall,
                        color = NoirColors.TextMuted
                    )
                } else {
                    recentEntries.forEach { entry ->
                        StockEntryRow(entry)
                    }
                }
            }
        }
    }

    if (isBarcodeScannerOpen) {
        CameraBarcodeScannerDialog(
            onDismiss = { isBarcodeScannerOpen = false },
            onBarcodeScanned = { barcode ->
                isBarcodeScannerOpen = false
                val products = viewModel.allProducts.value
                val result = MeatBarcodeParser.parse(barcode, products)

                // 1. Relleno automático inmediato del pesaje si el código contiene peso (ej. "22.30" de la caja)
                if (result.weightKg > 0.0) {
                    manualKilosInput = String.format(Locale.US, "%.3f", result.weightKg)
                }

                // 2. Si identificó producto y no está seleccionado, seleccionarlo
                if (result.product != null) {
                    viewModel.onCodeInputChanged(result.product.localCode.toString())
                }

                // 3. Si el código corresponde a un folio/lote de caja (ej. 1180201658 de Kekén)
                if (barcode.length in 8..15 && barcode.all { it.isDigit() }) {
                    supplierNotesInput = "Folio Caja: $barcode"
                }

                viewModel.processScannedBarcode(barcode)
            }
        )
    }

    if (uiState.isQuickCreateOpen) {
        QuickProductCreateDialog(
            suggestedCode = uiState.quickCreateCode,
            initialWeightFromScale = scaleState.currentWeight,
            onDismiss = { viewModel.closeQuickCreateDialog() },
            onSaveAlVuelo = { product, initialKilos, supplierNotes ->
                viewModel.saveProductAlVuelo(product, initialKilos, supplierNotes)
            }
        )
    }

    if (uiState.isDespieceOpen) {
        DespiecePiernaDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeDespiece() }
        )
    }
}

@Composable
fun StockEntryRow(entry: StockEntryEntity) {
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(entry.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NoirRadius.sm))
            .background(NoirColors.SurfaceSunken)
            .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "PLU #${entry.productCode} · ${entry.productName}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = NoirColors.TextPrimary
            )
            Text(
                text = "$dateStr • ${entry.supplierNotes ?: "Recepción SGNIA"}",
                fontSize = 11.sp,
                color = NoirColors.TextMuted
            )
        }

        val kilosStr = String.format(Locale.US, "%.3f", entry.kilosReceived)
        Text(
            text = "+$kilosStr kg",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = NoirColors.Cream
        )
    }
}
