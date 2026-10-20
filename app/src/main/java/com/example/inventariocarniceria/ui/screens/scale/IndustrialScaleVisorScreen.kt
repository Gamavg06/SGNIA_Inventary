package com.example.inventariocarniceria.ui.screens.scale

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import com.example.inventariocarniceria.ui.components.CameraBarcodeScannerDialog
import com.example.inventariocarniceria.utils.MeatBarcodeResult
import com.example.inventariocarniceria.ui.screens.inventory.DespiecePiernaDialog
import com.example.inventariocarniceria.ui.screens.inventory.PriceAdjustmentDialog
import com.example.inventariocarniceria.ui.screens.inventory.QendraCatalogImportDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.inventariocarniceria.theme.SgniaTheme
import com.example.inventariocarniceria.ui.components.AsyncProductImage
import com.example.inventariocarniceria.ui.components.OptimizedScaleOledDisplay
import com.example.inventariocarniceria.ui.components.MultiScaleSelectorHeader
import com.example.inventariocarniceria.ui.components.ScaleSelectorHeader
import com.example.inventariocarniceria.ui.components.SgniaThemeToggle
import com.example.inventariocarniceria.ui.components.TacticalGlassCard
import com.example.inventariocarniceria.ui.components.SgniaPillButton
import com.example.inventariocarniceria.ui.components.SgniaOutlinedPillButton
import com.example.inventariocarniceria.ui.components.SgniaCategoryPillRow
import com.example.inventariocarniceria.ui.components.SgniaArtisanalProductCard
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
    val haptic = LocalHapticFeedback.current

    var selectedScaleId by remember(scaleState.activeScaleId) { mutableIntStateOf(scaleState.activeScaleId) }
    var searchExpanded by remember { mutableStateOf(false) }
    var isPluListOpen by remember { mutableStateOf(false) }
    var isManualWeightOpen by remember { mutableStateOf(false) }
    var isCameraScannerOpen by remember { mutableStateOf(false) }
    var manualWeightInput by remember { mutableStateOf("") }
    val scannedBarcodeResult by viewModel.scannedBarcodeResult.collectAsState()

    val activeDevice = scalesList.find { it.id == scaleState.activeScaleId } ?: scalesList.firstOrNull()
    val activeIp = if (activeDevice != null) "${activeDevice.ip}:${activeDevice.port}" else "192.168.100.50:10001"
    val activeScaleName = activeDevice?.name ?: scaleState.deviceName

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

    var selectedCategory by remember { mutableStateOf("Todos") }
    val categories = remember(allProducts) {
        val unique = allProducts.map { it.category.trim() }.filter { it.isNotBlank() }.distinct().sorted()
        listOf("Todos") + unique
    }
    val categoryProducts = remember(selectedCategory, allProducts) {
        if (selectedCategory == "Todos") allProducts.take(8)
        else allProducts.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SgniaTheme.colors.backgroundPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 0. ENCABEZADO DE MOSTRADOR, TEMA & SESIÓN
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SgniaTheme.colors.accentPrimary.copy(alpha = 0.2f))
                            .border(1.dp, SgniaTheme.colors.accentPrimary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = SgniaTheme.colors.accentPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = uiState.currentBranch.ifEmpty { "MOSTRADOR PRINCIPAL" },
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = SgniaTheme.colors.textPrimary
                        )
                        Text(
                            text = "TERMINAL OPERATIVO • ${uiState.currentUserRole}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = SgniaTheme.colors.accentPrimary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SgniaThemeToggle(
                        currentMode = SgniaTheme.mode,
                        onToggle = { viewModel.toggleTheme() }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = SgniaTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        // ==========================================
        // 1. SELECTOR TÁCTICO DE BÁSCULA TCP/IP WI-FI
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MultiScaleSelectorHeader(
                    scales = scalesList,
                    selectedScaleId = scaleState.activeScaleId,
                    onSelectScale = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedScaleId = it
                        viewModel.selectActiveScale(it)
                    }
                )

                // BOTONES RÁPIDOS DE CONTROL DE RED Y PESO MANUAL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.resetToOfficialScaleIps()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(38.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD549))
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFD549))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("⚡ Restablecer IPs (.40 y .50)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            manualWeightInput = if (scaleState.currentWeight > 0.0) scaleState.currentWeight.toString() else ""
                            isManualWeightOpen = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("✏️ Peso Manual", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ==========================================
        // 1.1. AVISO OPERATIVO: MODO TICKET / IMPRESIÓN
        // ==========================================
        if (scaleState.totalBytesReceived == 0L) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💡 Si tu báscula física está encendida pero marca 0 bytes, coloca producto y presiona IMPRIMIR o TOTAL en la báscula física para enviar la lectura a la app.",
                            fontSize = 11.sp,
                            color = Color(0xFF5D4037),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // ==========================================
        // 1.2. GUÍA OPERATIVA: BÁSCULA SYSTEL CUORA MAX
        // ==========================================
        val isScaleWaitingVendor = scaleState.lastRawFrame.contains("PE4")
        val scaleErrorMsg = if (isScaleWaitingVendor) {
            "💡 Con el producto en el plato y el corte seleccionado en la báscula: presiona la tecla naranja 'VENTA SIN IMPRESIÓN' (abajo a la derecha) o la tecla '+A' (Vendedor A) en la báscula física para transferir el peso a la app."
        } else scaleState.lastErrorMessage

        if (scaleErrorMsg != null && scaleState.currentWeight == 0.0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = scaleErrorMsg,
                                fontSize = 12.sp,
                                color = Color(0xFFBF360C),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    manualWeightInput = if (scaleState.currentWeight > 0.0) scaleState.currentWeight.toString() else ""
                                    isManualWeightOpen = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("✏️ Ingresar Peso Manual", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 1.3. AVISO OPERATIVO: CAJA / TICKET ESCANEADO INTELIGENTE
        // ==========================================
        if (scannedBarcodeResult != null) {
            item {
                val res = scannedBarcodeResult!!
                val prod = res.product
                Card(
                    colors = CardDefaults.cardColors(containerColor = SgniaTheme.colors.surfaceGlass),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SgniaTheme.colors.accentPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SgniaTheme.colors.accentPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = SgniaTheme.colors.accentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (prod != null) "📦 ${prod.name}" else "Código: ${res.rawCode}",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = SgniaTheme.colors.textPrimary
                                )
                                Text(
                                    text = res.explanation,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = SgniaTheme.colors.statusStable
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.clearScannedBarcodeResult() }) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Aceptar",
                                tint = SgniaTheme.colors.accentPrimary
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. VISOR INDUSTRIAL OLED DE ALTO CONTRASTE (CERO RECOMPOSICIONES)
        // ==========================================
        item {
            OptimizedScaleOledDisplay(
                weightProvider = { scaleState.currentWeight },
                isStableProvider = { scaleState.isStable },
                scaleName = activeScaleName,
                ipAddress = activeIp,
                rawFrame = scaleState.lastRawFrame,
                rawHex = scaleState.lastRawHex,
                totalBytes = scaleState.totalBytesReceived,
                communicationMode = scaleState.communicationMode,
                errorMessage = scaleState.lastErrorMessage,
                onManualWeightClick = {
                    manualWeightInput = if (scaleState.currentWeight > 0.0) scaleState.currentWeight.toString() else ""
                    isManualWeightOpen = true
                }
            )
        }

        // ==========================================
        // 3. BOTONES TÁCTILES RÁPIDOS (+/- $5, +/- $10, TARA) ESTILO PILL
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(-10.0, -5.0, 5.0, 10.0).forEach { delta ->
                    val isPositive = delta > 0
                    val label = if (isPositive) "+$${delta.toInt()}" else "-$${kotlin.math.abs(delta).toInt()}"
                    val btnColor = if (isPositive) SgniaTheme.colors.statusStable else SgniaTheme.colors.statusAlert

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(SgniaTheme.colors.surfaceGlass)
                            .border(1.5.dp, btnColor.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                uiState.selectedProduct?.let { prod ->
                                    val newP = (prod.price + delta).coerceAtLeast(1.0)
                                    viewModel.updateProductPrice(prod, newP)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = btnColor
                        )
                    }
                }

                // BOTÓN CERO / TARA (ESTILO PILL)
                Box(
                    modifier = Modifier
                        .width(84.dp)
                        .height(46.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(SgniaTheme.colors.accentPrimary.copy(alpha = 0.2f))
                        .border(1.5.dp, SgniaTheme.colors.accentPrimary, RoundedCornerShape(22.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.setManualWeight(0.000)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ">0< TARA",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = SgniaTheme.colors.accentPrimary
                    )
                }
            }
        }

        // ==========================================
        // 4. ACCIONES RÁPIDAS & CATÁLOGO PLU EN FORMA DE PÍLDORA
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // BOTONES PILL: ESCANEAR CAJA / CÓDIGO + CATÁLOGO DE PLUs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SgniaPillButton(
                        text = "📷 ESCANEAR CAJA",
                        onClick = { isCameraScannerOpen = true },
                        modifier = Modifier.weight(1.2f),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Color(0xFF141210),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )

                    SgniaOutlinedPillButton(
                        text = "PLUs (${allProducts.size})",
                        onClick = { isPluListOpen = true },
                        modifier = Modifier.weight(0.95f),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.FormatListNumbered,
                                contentDescription = null,
                                tint = SgniaTheme.colors.accentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.openDespiece() },
                        colors = ButtonDefaults.buttonColors(containerColor = SgniaTheme.colors.statusAlert),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.ContentCut, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Despiece", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = { viewModel.openPriceAdjustmentDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = SgniaTheme.colors.statusStable),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF141210), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Precios", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color(0xFF141210))
                    }

                    Button(
                        onClick = { viewModel.openQendraImportDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = SgniaTheme.colors.accentSecondary),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF141210), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Qendra", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color(0xFF141210))
                    }
                }
            }
        }

        // ==========================================
        // 4.1. CARRUSEL HORIZONTAL DE CATEGORÍAS (INSPIRACIÓN BOUTIQUE / NOIR & CREMA)
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DEPARTAMENTOS & CORTES",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = SgniaTheme.colors.accentPrimary
                    )
                    Text(
                        text = "$selectedCategory (${categoryProducts.size})",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = SgniaTheme.colors.textMuted
                    )
                }

                SgniaCategoryPillRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it }
                )

                // CARRUSEL HORIZONTAL DE CORTES RÁPIDOS
                if (categoryProducts.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(categoryProducts.take(8), key = { it.code }) { prod ->
                            val isChosen = uiState.selectedProduct?.code == prod.code
                            SgniaArtisanalProductCard(
                                name = prod.name,
                                pluCode = prod.localCode,
                                category = prod.category,
                                pricePerKg = prod.price,
                                imageUrl = prod.imageUri,
                                isSelected = isChosen,
                                modifier = Modifier.width(240.dp),
                                onClick = {
                                    viewModel.onCodeInputChanged(prod.localCode.toString())
                                },
                                onActionClick = {
                                    viewModel.onCodeInputChanged(prod.localCode.toString())
                                }
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. BUSCADOR INTELIGENTE HÍBRIDO CON MINIATURA DE IMAGEN
        // ==========================================
        item {
            TacticalGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BUSCADOR DE CORTE (PLU O NOMBRE)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = SgniaTheme.colors.accentPrimary
                        )
                        Text(
                            text = "Ver todos (${allProducts.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SgniaTheme.colors.accentSecondary,
                            modifier = Modifier.clickable { isPluListOpen = true }
                        )
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
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            placeholder = { Text("Ej. 24, Quesillo, Arrachera, Chamorro...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = SgniaTheme.colors.accentPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isCameraScannerOpen = true }) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "Escanear Código",
                                        tint = SgniaTheme.colors.accentPrimary
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SgniaTheme.colors.accentPrimary,
                                unfocusedBorderColor = SgniaTheme.colors.surfaceGlassBorder,
                                focusedTextColor = SgniaTheme.colors.textPrimary,
                                unfocusedTextColor = SgniaTheme.colors.textPrimary
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                AsyncProductImage(
                                                    imageUrl = prod.imageUri,
                                                    contentDescription = prod.name,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "[#${prod.localCode}] ${prod.name}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = prod.synonyms,
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "$${String.format(Locale.US, "%.2f", prod.price)}/kg",
                                                fontWeight = FontWeight.Black,
                                                color = SgniaTheme.colors.statusStable,
                                                fontSize = 13.sp
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
                }
            }
        }

        // ==========================================
        // 5. FICHA TÉCNICA VISUAL DEL CORTE SELECCIONADO
        // ==========================================
        item {
            val product = uiState.selectedProduct
            if (product != null) {
                TacticalGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // IMAGEN ASÍNCRONA EN ALTA DEFINICIÓN
                        AsyncProductImage(
                            imageUrl = product.imageUri,
                            contentDescription = product.name,
                            modifier = Modifier.size(80.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PLU #${product.localCode}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = SgniaTheme.colors.accentPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${product.category}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = SgniaTheme.colors.textMuted
                                )
                            }
                            Text(
                                text = product.name,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = SgniaTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Precio: $${String.format(Locale.US, "%.2f", product.price)}/kg",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SgniaTheme.colors.statusStable
                                )
                                Text(
                                    text = "Máx: ${String.format(Locale.US, "%.1f", product.maxStock)} kg",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = SgniaTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6. BOTÓN GIGANTE DE CAPTURA CON HÁPTICA (PILL SOFISTICADO)
        // ==========================================
        item {
            SgniaPillButton(
                text = "CAPTURAR PESAJE EN INVENTARIO (1-TOQUE)",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.captureWeight()
                },
                enabled = uiState.selectedProduct != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = null,
                        tint = Color(0xFF141210),
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    }

    // Modales de Mostrador
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
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Scale, contentDescription = null, tint = SgniaTheme.colors.accentPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fijar Pesaje de Báscula", fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Introduce el peso mostrado en el display azul de la báscula física:",
                        fontSize = 12.sp,
                        color = SgniaTheme.colors.textSecondary
                    )

                    // Display digital con el valor actual
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .border(1.dp, SgniaTheme.colors.accentPrimary, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (manualWeightInput.isBlank()) "0.000 kg" else "${manualWeightInput} kg",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            color = SgniaTheme.colors.oledDigitColor
                        )
                    }

                    // Teclado numérico táctil industrial (3x4)
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
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .background(
                                            if (key == "⌫") Color(0xFF334155) else SgniaTheme.colors.surfaceGlass,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(1.dp, SgniaTheme.colors.surfaceGlassBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            when (key) {
                                                "⌫" -> {
                                                    if (manualWeightInput.isNotEmpty()) {
                                                        manualWeightInput = manualWeightInput.dropLast(1)
                                                    }
                                                }
                                                "." -> {
                                                    if (!manualWeightInput.contains(".")) {
                                                        manualWeightInput = if (manualWeightInput.isEmpty()) "0." else "$manualWeightInput."
                                                    }
                                                }
                                                else -> {
                                                    if (manualWeightInput.length < 7) {
                                                        manualWeightInput += key
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = SgniaTheme.colors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Atajos rápidos directos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("1.060", "0.500", "2.000").forEach { preset ->
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    manualWeightInput = preset
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                            ) {
                                Text("${preset}k", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                manualWeightInput = ""
                            },
                            modifier = Modifier.weight(0.8f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Text("C", fontSize = 11.sp, fontWeight = FontWeight.Black)
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
                    colors = ButtonDefaults.buttonColors(containerColor = SgniaTheme.colors.accentPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("FIJAR PESO EN PANTALLA", color = Color.Black, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isManualWeightOpen = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar", color = SgniaTheme.colors.textSecondary)
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
