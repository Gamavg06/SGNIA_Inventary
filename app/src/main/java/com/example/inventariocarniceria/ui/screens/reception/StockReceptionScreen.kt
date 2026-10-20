package com.example.inventariocarniceria.ui.screens.reception

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.StockEntryEntity
import com.example.inventariocarniceria.ui.components.CameraBarcodeScannerDialog
import com.example.inventariocarniceria.ui.components.SgniaCerhanGlobalSummaryCard
import com.example.inventariocarniceria.ui.components.SgniaCerhanGreetingHeader
import com.example.inventariocarniceria.ui.components.SgniaCerhanNumberedCard
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

    var manualKilosInput by remember { mutableStateOf("") }
    var supplierNotesInput by remember { mutableStateOf("") }
    var isBarcodeScannerOpen by remember { mutableStateOf(false) }

    val matteBg = Color(0xFF0F0E0D)
    val cardBg = Color(0xFF181715)
    val cardBorder = Color(0xFF282623)
    val creamPrimary = Color(0xFFF3EFE6)
    val creamWarm = Color(0xFFEEDDC8)
    val textMuted = Color(0xFF8C857B)
    val darkText = Color(0xFF141312)

    val totalReceivedKilos = recentEntries.sumOf { it.kilosReceived }

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
                userName = "Recepción Mercancía",
                subtitle = "Ingreso de Cajas y Proveedores • ${uiState.currentBranch.ifBlank { "Sucursal" }}"
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
                    title = "Báscula Activa",
                    subtitle = String.format(Locale.US, "%.3f kg (%s)", scaleState.currentWeight, scaleState.deviceName.take(12)),
                    icon = Icons.Default.Scale,
                    showWaveChart = true
                )
                SgniaCerhanNumberedCard(
                    modifier = Modifier.weight(1f),
                    number = "02.",
                    title = "Ingresos Hoy",
                    subtitle = String.format(Locale.US, "%.2f kg (%d lotes)", totalReceivedKilos, recentEntries.size),
                    icon = Icons.Default.LocalShipping,
                    showWaveChart = false
                )
            }
        }

        // ==========================================================
        // TARJETA 03. ESCÁNER Y BUSCADOR DE CÓDIGO DE RECEPCIÓN
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
                        Text("Código o Ticket de Caja", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = creamWarm)
                    }
                    Button(
                        onClick = { isBarcodeScannerOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = creamPrimary, contentColor = darkText),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Escanear Caja", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = uiState.codeInput,
                    onValueChange = { viewModel.onCodeInputChanged(it) },
                    label = { Text("Código PLU o Nombre del Corte") },
                    placeholder = { Text("Ej. 24 o Manitas de Puerco") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = creamPrimary,
                        unfocusedBorderColor = cardBorder,
                        focusedTextColor = creamPrimary,
                        unfocusedTextColor = creamPrimary,
                        focusedLabelColor = creamWarm,
                        unfocusedLabelColor = textMuted
                    )
                )
            }
        }

        // ==========================================================
        // TARJETA 04. CAPTURA DE STOCK O ALTA AL VUELO
        // ==========================================================
        item {
            val product = uiState.selectedProduct
            if (product != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(cardBg)
                        .border(1.5.dp, creamWarm.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = creamPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "#${product.localCode} • ${product.name.uppercase()}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = creamPrimary
                            )
                        }
                        Text(
                            text = String.format(Locale.US, "$%.2f/kg", product.price),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = creamWarm
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
                            label = { Text("Kilos a Recibir (kg)") },
                            placeholder = { Text("Báscula: $formattedWeight kg") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = creamPrimary,
                                unfocusedBorderColor = cardBorder,
                                focusedTextColor = creamPrimary,
                                unfocusedTextColor = creamPrimary,
                                focusedLabelColor = creamWarm,
                                unfocusedLabelColor = textMuted
                            )
                        )

                        OutlinedButton(
                            onClick = { manualKilosInput = String.format(Locale.US, "%.3f", scaleState.currentWeight) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = creamPrimary)
                        ) {
                            Text("Usar Báscula", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = supplierNotesInput,
                        onValueChange = { supplierNotesInput = it },
                        label = { Text("Notas de Proveedor / Lote") },
                        placeholder = { Text("Ej. Proveedor Central #402") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = creamPrimary,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = creamPrimary,
                            unfocusedTextColor = creamPrimary,
                            focusedLabelColor = creamWarm,
                            unfocusedLabelColor = textMuted
                        )
                    )

                    Button(
                        onClick = {
                            viewModel.registerStockEntry(effectiveKilos, supplierNotesInput.ifBlank { null })
                            manualKilosInput = ""
                            supplierNotesInput = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = creamPrimary, contentColor = darkText)
                    ) {
                        Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REGISTRAR ENTRADA DE STOCK",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (uiState.isProductNotFound) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(cardBg)
                        .border(1.dp, creamWarm, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = creamWarm)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Código #${uiState.codeInput} no existe en catálogo",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = creamPrimary
                        )
                    }
                    Text(
                        text = "¿Deseas dar de alta este corte nuevo inmediatamente para registrar su entrada?",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = textMuted
                    )
                    Button(
                        onClick = { viewModel.openQuickCreateDialog() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = creamPrimary, contentColor = darkText)
                    ) {
                        Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear Producto Al Vuelo", fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // ==========================================================
        // RESUMEN GLOBAL DE ENTRADAS Y BITÁCORA
        // ==========================================================
        item {
            SgniaCerhanGlobalSummaryCard(
                metric1Label = "Kilos Recibidos",
                metric1Value = String.format(Locale.US, "%.2f kg", totalReceivedKilos),
                metric1Sub = "Acumulado de entregas recientes",
                metric2Label = "Registros",
                metric2Value = "${recentEntries.size} Entradas",
                metric2Sub = "Bitácora de proveedores",
                barValues = listOf(0.45f, 0.78f, 0.62f, 0.90f, 0.55f, 0.85f, 0.70f, 0.95f)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ÚLTIMAS ENTRADAS RECIBIDAS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = creamPrimary
                )

                if (recentEntries.isEmpty()) {
                    Text(
                        text = "Aún no hay entradas registradas en esta sucursal.",
                        fontSize = 11.sp,
                        color = textMuted
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
}

@Composable
fun StockEntryRow(entry: StockEntryEntity) {
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(entry.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121110))
            .border(1.dp, Color(0xFF282623), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "#${entry.productCode} • ${entry.productName}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFFF3EFE6)
            )
            Text(
                text = "$dateStr • ${entry.supplierNotes ?: "Recepción SGNIA"}",
                fontSize = 10.sp,
                color = Color(0xFF8C857B)
            )
        }

        val kilosStr = String.format(Locale.US, "%.3f", entry.kilosReceived)
        Text(
            text = "+$kilosStr kg",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = Color(0xFFEEDDC8)
        )
    }
}
