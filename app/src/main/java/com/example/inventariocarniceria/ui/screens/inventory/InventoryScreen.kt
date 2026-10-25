package com.example.inventariocarniceria.ui.screens.inventory

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.R
import com.example.inventariocarniceria.data.local.InventoryCaptureEntity
import com.example.inventariocarniceria.data.local.ProductEntity
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.AlertDialog
import com.example.inventariocarniceria.ui.components.MultiScaleSelectorHeader
import com.example.inventariocarniceria.ui.components.SgniaThemeToggle
import com.example.inventariocarniceria.ui.screens.scale.PluCatalogListDialog
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scaleState by viewModel.scaleState.collectAsState()
    val scalesList by viewModel.scalesList.collectAsState()
    val recentCaptures by viewModel.recentCaptures.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()

    var isChangeCodeOpen by remember { mutableStateOf(false) }
    var searchExpanded by remember { mutableStateOf(false) }
    var isPluListOpen by remember { mutableStateOf(false) }
    var isManualWeightOpen by remember { mutableStateOf(false) }
    var manualWeightInput by remember { mutableStateOf("") }

    val greenSuccess = Color(0xFF2E7D32)
    val darkRed = Color(0xFFB71C1C)
    val cardBg = MaterialTheme.colorScheme.surfaceVariant

    // Hybrid Search Suggestions Filter (by code, localCode, name, or synonyms)
    val suggestions = remember(uiState.codeInput, allProducts) {
        val query = uiState.codeInput.trim().lowercase()
        if (query.isEmpty()) emptyList()
        else {
            allProducts.filter { prod ->
                prod.localCode.toString().contains(query) ||
                prod.code.toString().contains(query) ||
                prod.name.lowercase().contains(query) ||
                prod.synonyms.lowercase().contains(query)
            }.take(6)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. HEADER & BRANDING ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo Carnicería Sgnia",
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CARNICERÍA SGNIA",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "TOMA DE INVENTARIO Y PESAJE",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    SgniaThemeToggle(
                        onToggle = { viewModel.toggleTheme() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SELECTOR MULTICANAL DE BÁSCULAS (TRABAJO EN PARALELO)
                MultiScaleSelectorHeader(
                    scales = scalesList,
                    selectedScaleId = scaleState.activeScaleId,
                    onSelectScale = { viewModel.selectActiveScale(it) }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // BARRA RÁPIDA DE ESTADO Y RESETEO DE IPS OFICIALES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (scaleState.isConnected)
                            "Enlace: ${scaleState.deviceName} (${scaleState.communicationMode})"
                        else
                            "Báscula Wi-Fi Desconectada",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (scaleState.isConnected) greenSuccess else darkRed
                    )

                    TextButton(
                        onClick = { viewModel.resetToOfficialScaleIps() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFD84315))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("⚡ Restablecer IPs (.40 y .50)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD84315))
                    }
                }

                if (scaleState.totalBytesReceived == 0L) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "💡 Si no marca peso, coloca producto y pulsa IMPRIMIR o TOTAL en la báscula física.",
                                fontSize = 11.sp,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // BOTONES DE ACCIÓN RÁPIDA DE MOSTRADOR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.openDespiece() },
                        colors = ButtonDefaults.buttonColors(containerColor = darkRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Despiece", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.openPriceAdjustmentDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = greenSuccess),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Precios", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.openQendraImportDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Qendra", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // --- 2. HYBRID SMART SEARCH FIELD (CODE / NAME / SYNONYMS) WITH AUTOCOMPLETE ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BUSCADOR INTELIGENTE HÍBRIDO",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(
                            onClick = { isPluListOpen = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.FormatListNumbered,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ver todos (${allProducts.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

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
                            label = { Text("Código (ej. 24, 60) o Nombre/Sinónimo (ej. hebra, jamón, chamorro)") },
                            placeholder = { Text("Ej. 24 o Oaxaca") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = searchExpanded && suggestions.isNotEmpty(),
                            onDismissRequest = { searchExpanded = false }
                        ) {
                            suggestions.forEach { prod ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "[#${prod.localCode}] ${prod.name} ($${prod.price}/kg)",
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (prod.synonyms.isNotBlank()) {
                                                Text(
                                                    text = "Alias: ${prod.synonyms}",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { isPluListOpen = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4AF37),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(
                            Icons.Default.FormatListNumbered,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📋 CATÁLOGO COMPLETO DE PLUs (${allProducts.size} PRODUCTOS)",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // --- 3. GIANT WEIGHING MODULE ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LECTURA EN TIEMPO REAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray,
                            letterSpacing = 1.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (scaleState.isStable) Color(0xFF1B5E20) else Color(0xFFE65100))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (scaleState.isStable) "ESTABLE" else "LECTURA...",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val formattedWeight = String.format(Locale.US, "%.3f", scaleState.currentWeight)
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable {
                            manualWeightInput = if (scaleState.currentWeight > 0.0) scaleState.currentWeight.toString() else ""
                            isManualWeightOpen = true
                        }
                    ) {
                        Text(
                            text = formattedWeight,
                            fontSize = 58.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00E676)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "kg",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (scaleState.totalBytesReceived > 0L)
                            "📡 Rx: ${scaleState.totalBytesReceived} B • ${scaleState.communicationMode} • Trama: \"${scaleState.lastRawFrame}\""
                        else
                            "📡 ${scaleState.communicationMode} • 0 bytes recibidos",
                        fontSize = 10.sp,
                        color = if (scaleState.totalBytesReceived > 0L) greenSuccess else Color(0xFFE65100)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                manualWeightInput = if (scaleState.currentWeight > 0.0) scaleState.currentWeight.toString() else ""
                                isManualWeightOpen = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text("✏️ Peso Manual", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setManualWeight(0.000) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text(">0< TARA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.captureWeight() },
                        enabled = uiState.selectedProduct != null && (scaleState.isConnected || scaleState.currentWeight > 0.0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = greenSuccess,
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Scale, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CAPTURAR PESO",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- 4. SELECTED PRODUCT CARD + REASSIGN LOCAL CODE BUTTON ---
        item {
            val product = uiState.selectedProduct
            val currentWeight = scaleState.currentWeight

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (product != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (product != null) {
                        val shortage = (product.maxStock - currentWeight).coerceAtLeast(0.0)
                        val formattedShortage = String.format(Locale.US, "%.3f", shortage)
                        val formattedMax = String.format(Locale.US, "%.3f", product.maxStock)
                        val formattedCurrent = String.format(Locale.US, "%.3f", currentWeight)
                        val formattedPrice = String.format(Locale.US, "$%.2f", product.price)

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
                                    text = "${product.localCode} - ${product.name.uppercase()}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = "$formattedPrice / kg",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = greenSuccess
                            )
                        }

                        if (product.synonyms.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Sinónimos: ${product.synonyms}", fontSize = 11.sp, color = Color.Gray)
                        }

                        // Badge de presentación de empaque
                        if (product.orderUnitType != "KG" || product.parentProductCode != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val badgeText = when (product.orderUnitType) {
                                "BARRA" -> "🥖 Llega por Barra (~${product.weightPerBoxKg} kg) • En exist: ${product.currentStockUnits.toInt()} barras"
                                "PAQUETE" -> "📦 Llega por Paquete (~${product.weightPerBoxKg} kg) • En exist: ${product.currentStockUnits.toInt()} pqtes"
                                "BOLSA" -> "🛍️ Llega por Bolsa (~${product.weightPerBoxKg} kg) / Caja ${product.unitsPerBox} b • En exist: ${product.currentStockUnits.toInt()} bolsas"
                                "CAJA" -> "📦 Llega por Caja (~${product.weightPerBoxKg} kg) • En exist: ${product.currentStockUnits.toInt()} cajas"
                                "PIEZA" -> "🥩 Por Pieza • En exist: ${product.currentStockUnits.toInt()} pzas"
                                else -> null
                            }
                            val badgeColor = when (product.orderUnitType) {
                                "BARRA" -> Color(0xFFD84315)
                                "PAQUETE" -> Color(0xFF0277BD)
                                "BOLSA" -> greenSuccess
                                else -> Color(0xFFE65100)
                            }
                            if (badgeText != null) {
                                Text(
                                    text = badgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }
                            if (product.parentProductCode != null) {
                                Text(
                                    text = "🧬 Derivado de materia prima #${product.parentProductCode}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6A1B9A)
                                )
                            }
                        }

                        // Guía rápida para mostrador: Venta suelta por importe ($15, $20, $30, etc.)
                        if (product.price > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "💵 Equivalencias Mostrador (Venta Suelta por Importe):",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val p15Kg = String.format(Locale.US, "%.3f kg", 15.0 / product.price)
                                        val p20Kg = String.format(Locale.US, "%.3f kg", 20.0 / product.price)
                                        val p30Kg = String.format(Locale.US, "%.3f kg", 30.0 / product.price)
                                        val p50Kg = String.format(Locale.US, "%.3f kg", 50.0 / product.price)
                                        Text("$15 = $p15Kg", fontSize = 10.sp, color = greenSuccess, fontWeight = FontWeight.Bold)
                                        Text("$20 = $p20Kg", fontSize = 10.sp, color = greenSuccess, fontWeight = FontWeight.Bold)
                                        Text("$30 = $p30Kg", fontSize = 10.sp, color = greenSuccess, fontWeight = FontWeight.Bold)
                                        Text("$50 = $p50Kg", fontSize = 10.sp, color = greenSuccess, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Existencia Actual", style = MaterialTheme.typography.labelMedium, color = greenSuccess, fontWeight = FontWeight.Bold)
                                Text("${formattedCurrent} kg", fontWeight = FontWeight.Black, color = greenSuccess, fontSize = 17.sp)
                            }
                            Column {
                                Text("Stock Máximo", style = MaterialTheme.typography.labelMedium)
                                Text("${formattedMax} kg", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Faltante (Pedido)", style = MaterialTheme.typography.labelMedium, color = darkRed)
                                Text(
                                    text = "${formattedShortage} kg",
                                    fontWeight = FontWeight.Bold,
                                    color = if (shortage > 0) darkRed else greenSuccess
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { isChangeCodeOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("✏️ Cambiar mi código local", fontWeight = FontWeight.Bold)
                        }

                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isProductNotFound)
                                    "Producto no encontrado. Escribe otro nombre o código."
                                else
                                    "Ingresa o busca un código de producto arriba",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // --- 5. RECENT CAPTURES HISTORY ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "HISTORIAL RECIENTE (ÚLTIMAS 3 CAPTURAS)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (recentCaptures.isEmpty()) {
                    Text(
                        text = "Aún no hay pesajes capturados hoy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                } else {
                    recentCaptures.forEach { capture ->
                        RecentCaptureItem(capture)
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }

    // Modal Change Local Code Dialog
    if (isChangeCodeOpen && uiState.selectedProduct != null) {
        ChangeLocalCodeDialog(
            product = uiState.selectedProduct!!,
            allProducts = allProducts,
            onDismiss = { isChangeCodeOpen = false },
            onConfirmChangeCode = { product, newLocalCode ->
                viewModel.updateProductLocalCode(product, newLocalCode)
                isChangeCodeOpen = false
            }
        )
    }

    // Modal Price Adjustment Dialog
    if (uiState.isPriceAdjustmentOpen) {
        PriceAdjustmentDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closePriceAdjustmentDialog() }
        )
    }

    // Modal Qendra Catalog Import Dialog
    if (uiState.isQendraImportOpen) {
        QendraCatalogImportDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeQendraImportDialog() }
        )
    }

    // Modal Despiece y Rendimiento de Pierna Dialog
    if (uiState.isDespieceOpen) {
        DespiecePiernaDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeDespiece() }
        )
    }

    // Modal Catálogo Completo de PLUs y Derivadas
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
            title = { Text("Ingresar Peso Manual", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Escribe el peso mostrado en la báscula física si no se transmitió automáticamente:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = manualWeightInput,
                        onValueChange = { manualWeightInput = it },
                        label = { Text("Peso en Kilogramos (ej. 1.350)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.5, 1.0, 2.0, 5.0).forEach { delta ->
                            OutlinedButton(
                                onClick = {
                                    val current = manualWeightInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    manualWeightInput = String.format(Locale.US, "%.3f", current + delta)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+${delta.toInt()}k", fontSize = 11.sp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = greenSuccess)
                ) {
                    Text("Fijar Peso", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isManualWeightOpen = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun RecentCaptureItem(capture: InventoryCaptureEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${capture.productCode}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = capture.productName,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        val weightFormatted = String.format(Locale.US, "%.3f kg", capture.capturedWeight)
        Text(
            text = weightFormatted,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
    }
}
