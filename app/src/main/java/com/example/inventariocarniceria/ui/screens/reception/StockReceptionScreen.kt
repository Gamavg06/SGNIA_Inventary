package com.example.inventariocarniceria.ui.screens.reception

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.R
import com.example.inventariocarniceria.data.local.StockEntryEntity
import com.example.inventariocarniceria.ui.components.SgniaThemeToggle
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

    val greenSuccess = Color(0xFF2E7D32)
    val darkRed = Color(0xFFB71C1C)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. HEADER & SYSTEL CUORA MAX BADGE ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo Carnicería Sgnia",
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RECEPCIÓN DE MERCANCÍA",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Ingreso Total de Stock / Llegada de Proveedores",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    SgniaThemeToggle(
                        onToggle = { viewModel.toggleTheme() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (scaleState.isConnected) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                        .border(
                            1.dp,
                            if (scaleState.isConnected) greenSuccess else darkRed,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (scaleState.isConnected) greenSuccess else darkRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (scaleState.isConnected)
                                "BÁSCULA WI-FI TCP/IP: CONECTADA (${scaleState.deviceName})"
                            else
                                "BÁSCULA WI-FI TCP/IP: DESCONECTADA",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (scaleState.isConnected) greenSuccess else darkRed
                        )
                    }
                }
            }
        }

        // --- 2. CODE INPUT (1-107) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BUSCAR O INGRESAR CÓDIGO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.codeInput,
                        onValueChange = { viewModel.onCodeInputChanged(it) },
                        label = { Text("CÓDIGO DE PRODUCTO") },
                        placeholder = { Text("Ej. 24") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                }
            }
        }

        // --- 3. PRODUCT STATUS OR QUICK CREATE AL VUELO ---
        item {
            val product = uiState.selectedProduct
            if (product != null) {
                // Product Found Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = greenSuccess
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${product.code} - ${product.name.uppercase()}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "$%.2f/kg", product.price),
                                fontWeight = FontWeight.Bold,
                                color = greenSuccess
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Kilos Input (Scale weight or manual override)
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
                                placeholder = { Text("Peso báscula: $formattedWeight kg") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Button(
                                onClick = { manualKilosInput = String.format(Locale.US, "%.3f", scaleState.currentWeight) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                            ) {
                                Text("Usar Báscula")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = supplierNotesInput,
                            onValueChange = { supplierNotesInput = it },
                            label = { Text("Notas de Proveedor / N° Lote") },
                            placeholder = { Text("Ej. Camión Reg. #402") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.registerStockEntry(effectiveKilos, supplierNotesInput.ifBlank { null })
                                manualKilosInput = ""
                                supplierNotesInput = ""
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REGISTRAR ENTRADA DE STOCK",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            } else if (uiState.isProductNotFound) {
                // Product Not Found -> Quick Create Al Vuelo
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Código #${uiState.codeInput} no existe en catálogo",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFE65100)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "¿Deseas dar de alta este corte nuevo inmediatamente para continuar la recepción de mercancía?",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.openQuickCreateDialog() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("✨ Crear Producto Al Vuelo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 4. RECENT STOCK INFLOW ENTRIES TABLE ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ÚLTIMAS ENTRADAS RECIBIDAS (PROVEEDORES)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (recentEntries.isEmpty()) {
                    Text(
                        text = "Aún no hay entradas registradas en esta sucursal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                } else {
                    recentEntries.forEach { entry ->
                        StockEntryRow(entry)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }

    // Modal Quick Create Al Vuelo
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

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "#${entry.productCode} ${entry.productName}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "$dateStr • ${entry.supplierNotes ?: "Recepción SGNIA"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            val kilosStr = String.format(Locale.US, "%.3f", entry.kilosReceived)
            Text(
                text = "+$kilosStr kg",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF2E7D32)
            )
        }
    }
}
