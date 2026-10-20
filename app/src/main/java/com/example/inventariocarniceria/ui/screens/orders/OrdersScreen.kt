package com.example.inventariocarniceria.ui.screens.orders

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.InventoryCaptureEntity
import com.example.inventariocarniceria.data.local.ProductEntity
import com.example.inventariocarniceria.ui.components.SgniaThemeToggle
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import com.example.inventariocarniceria.utils.PdfExporter
import com.example.inventariocarniceria.utils.WhatsAppExporter
import java.util.Locale

@Composable
fun OrdersScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val pendingOrders by viewModel.pendingOrders.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val recentCaptures by viewModel.recentCaptures.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isManualOrderOpen by remember { mutableStateOf(false) }
    var selectedOrderProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("Todas") }

    val darkRed = Color(0xFFB71C1C)
    val whatsAppGreen = Color(0xFF25D366)
    val greenSuccess = Color(0xFF2E7D32)
    val orangeCajas = Color(0xFFE65100)

    val productMap = remember(allProducts) {
        allProducts.associateBy { it.code }
    }

    // Filtrar pedidos: mostrar todos los productos base/no derivados (Cajas, Barras, Paquetes, Piezas o Granel KG)
    // Se excluyen únicamente los derivados internos (bistec, molida) que se descuentan de su producto padre
    val nonDerivedOrders = remember(pendingOrders, productMap) {
        pendingOrders.filter { order ->
            val prod = productMap[order.productCode]
            val isNotDerived = prod?.parentProductCode == null
            isNotDerived
        }
    }

    // Todos los productos no derivados que pueden pedirse a proveedores o rastro
    val orderableProducts = remember(allProducts) {
        allProducts.filter { it.parentProductCode == null }
    }

    // Filtrar por departamento / categoría seleccionada
    val displayOrders = remember(nonDerivedOrders, selectedCategoryFilter, productMap) {
        if (selectedCategoryFilter == "Todas") {
            nonDerivedOrders
        } else {
            nonDerivedOrders.filter { order ->
                val prod = productMap[order.productCode]
                val cat = prod?.category.orEmpty()
                when (selectedCategoryFilter) {
                    "Carnicería" -> cat in listOf("Res", "Cerdo", "Pollo", "Carnicería", "Carnes", "")
                    "Lácteos y Quesos" -> cat in listOf("Quesos", "Lácteos", "Lacteos")
                    "Jamones y Embutidos" -> cat in listOf("Jamones", "Salchichas", "Embutidos")
                    "Cremería y Abarrotes" -> cat in listOf("Abarrotes", "Cremería", "Cremeria", "Otros")
                    else -> true
                }
            }
        }
    }

    val totalShortageKg = nonDerivedOrders.sumOf { it.shortage }
    val totalBoxesCount = remember(nonDerivedOrders, productMap) {
        nonDerivedOrders.sumOf { order ->
            val prod = productMap[order.productCode]
            val unitType = if (order.unitType.isNotBlank() && order.unitType != "KG") order.unitType else (prod?.orderUnitType ?: "KG")
            val boxWeight = prod?.weightPerBoxKg ?: 0.0
            if (unitType in listOf("CAJA", "BARRA", "PAQUETE", "BOLSA", "BOTE")) {
                if (order.orderedUnits > 0) order.orderedUnits else if (boxWeight > 0) kotlin.math.max(1.0, kotlin.math.ceil(order.shortage / boxWeight)) else 0.0
            } else 0.0
        }.toInt()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PEDIDO DE SURTIDO",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = "Toca cualquier producto para ajustar cajas o unidades a pedir",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                SgniaThemeToggle(
                    onToggle = { viewModel.toggleTheme() }
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = {
                    selectedOrderProduct = null
                    isManualOrderOpen = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar Producto",
                        tint = greenSuccess
                    )
                }
                if (nonDerivedOrders.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearAllCaptures() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Limpiar Lista",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // BOTÓN PRINCIPAL PARA AGREGAR PRODUCTO
        OutlinedButton(
            onClick = {
                selectedOrderProduct = null
                isManualOrderOpen = true
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = greenSuccess)
            Spacer(modifier = Modifier.width(6.dp))
            Text("➕ Agregar Producto / Cajas al Pedido", fontWeight = FontWeight.Bold, color = greenSuccess)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // RESUMEN OPERATIVO (SIN PRECIOS DE VENTA)
        if (nonDerivedOrders.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL A PEDIR", style = MaterialTheme.typography.labelSmall)
                        val boxesExtra = if (totalBoxesCount > 0) " • $totalBoxesCount ${if (totalBoxesCount == 1) "Caja" else "Cajas"}" else ""
                        Text(
                            text = String.format(Locale.US, "%.2f kg", totalShortageKg) + boxesExtra,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("PRODUCTOS EN LISTA", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${nonDerivedOrders.size} en pedido",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // CHIPS DE DEPARTAMENTOS / CATEGORÍAS (LÁCTEOS, CREMERÍA, ETC.)
        val orderCategories = listOf("Todas", "Carnicería", "Lácteos y Quesos", "Jamones y Embutidos", "Cremería y Abarrotes")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(orderCategories) { cat ->
                val isSelected = cat == selectedCategoryFilter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategoryFilter = cat },
                    label = {
                        val count = if (cat == "Todas") nonDerivedOrders.size else {
                            nonDerivedOrders.count { order ->
                                val p = productMap[order.productCode]
                                val c = p?.category.orEmpty()
                                when (cat) {
                                    "Carnicería" -> c in listOf("Res", "Cerdo", "Pollo", "Carnicería", "Carnes", "")
                                    "Lácteos y Quesos" -> c in listOf("Quesos", "Lácteos", "Lacteos")
                                    "Jamones y Embutidos" -> c in listOf("Jamones", "Salchichas", "Embutidos")
                                    "Cremería y Abarrotes" -> c in listOf("Abarrotes", "Cremería", "Cremeria", "Otros")
                                    else -> true
                                }
                            }
                        }
                        Text("$cat ($count)", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = greenSuccess,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ENCABEZADO DE TABLA (SIN PRECIO DE VENTA)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CÓD", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("PRODUCTO", modifier = Modifier.weight(1.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("EXISTENCIA", modifier = Modifier.weight(1.1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                Text("A PEDIR", modifier = Modifier.weight(1.4f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End, color = greenSuccess)
                Spacer(modifier = Modifier.width(68.dp)) // Espacio para botones de editar y borrar
            }
        }

        if (displayOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📦", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (nonDerivedOrders.isEmpty()) "No hay pedidos pendientes" else "Sin pedidos en esta categoría",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (nonDerivedOrders.isEmpty()) "Toca 'Agregar Producto / Cajas' para crear un pedido." else "Selecciona 'Todas' para ver todos los pedidos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(displayOrders) { order ->
                    val product = productMap[order.productCode]
                    OrderItemRow(
                        order = order,
                        product = product,
                        onItemClick = {
                            selectedOrderProduct = product
                            isManualOrderOpen = true
                        },
                        onDeleteClick = {
                            viewModel.deleteOrderItem(order.productCode)
                        }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // BOTONES DE ACCIÓN: WHATSAPP Y PDF DE PEDIDO
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { WhatsAppExporter.shareToWhatsApp(context, nonDerivedOrders) },
                    enabled = nonDerivedOrders.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = whatsAppGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        PdfExporter.generateAndShareVipInventoryPdf(
                            context = context,
                            orders = nonDerivedOrders,
                            allProducts = allProducts,
                            branchName = uiState.currentBranch,
                            managerName = "Encargado (${uiState.currentBranch})"
                        )
                    },
                    enabled = nonDerivedOrders.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = darkRed)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF Pedido", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // BOTÓN DE CORTE DE VENTAS
            Button(
                onClick = {
                    val salesSource = if (recentCaptures.isNotEmpty()) recentCaptures else nonDerivedOrders
                    PdfExporter.generateAndShareVipSalesReport(
                        context = context,
                        captures = salesSource,
                        allProducts = allProducts,
                        branchName = uiState.currentBranch,
                        managerName = "Encargado (${uiState.currentBranch})"
                    )
                },
                enabled = nonDerivedOrders.isNotEmpty() || recentCaptures.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = greenSuccess)
            ) {
                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GENERAR CORTE DE VENTAS (PDF)", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
            }
        }
    }

    // Modal Manual Order Dialog
    if (isManualOrderOpen) {
        ManualOrderDialog(
            allProducts = orderableProducts,
            initialProduct = selectedOrderProduct,
            onDismiss = { isManualOrderOpen = false },
            onAddManualOrder = { product, shortageKg, orderedUnits, unitType ->
                viewModel.addManualOrder(product, shortageKg, orderedUnits, unitType)
                isManualOrderOpen = false
            },
            onDeleteOrder = { product ->
                viewModel.deleteOrderItem(product.code)
                isManualOrderOpen = false
            }
        )
    }
}

@Composable
fun OrderItemRow(
    order: InventoryCaptureEntity,
    product: ProductEntity?,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val greenSuccess = Color(0xFF2E7D32)
    val orangeCajas = Color(0xFFE65100)

    val effectiveUnitType = if (order.unitType.isNotBlank() && order.unitType != "KG") order.unitType else (product?.orderUnitType ?: "KG")
    val boxWeight = product?.weightPerBoxKg ?: 0.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onItemClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val codeFormatted = String.format(Locale.US, "%03d", order.productCode)

        // CÓDIGO
        Text(codeFormatted, modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold, fontSize = 13.sp)

        // PRODUCTO
        Column(modifier = Modifier.weight(1.8f)) {
            Text(order.productName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            val packNote = when (effectiveUnitType) {
                "BARRA" -> "Llega: Barra ~${boxWeight} kg"
                "PAQUETE" -> "Llega: Paquete ~${boxWeight} kg"
                "BOLSA" -> "Llega: Bolsa ~${boxWeight} kg (${product?.unitsPerBox ?: 1} b/caja)"
                "CAJA" -> "Llega: Caja ~${boxWeight} kg"
                "BOTE" -> "Llega: Bote ~${boxWeight} kg"
                "PIEZA" -> "Llega: Por Pieza"
                else -> null
            }
            if (packNote != null) {
                Text(
                    text = packNote,
                    fontSize = 10.5.sp,
                    color = when (effectiveUnitType) {
                        "BARRA" -> Color(0xFFD84315)
                        "PAQUETE" -> Color(0xFF0277BD)
                        "BOLSA" -> greenSuccess
                        "BOTE" -> Color(0xFF6A1B9A)
                        "PIEZA" -> Color(0xFF00838F)
                        else -> orangeCajas
                    },
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // EXISTENCIA (EN UNIDADES / BARRAS / PAQUETES / CAJAS / BOLSAS O KG)
        val stockUnits = if (product != null && product.currentStockUnits > 0) {
            product.currentStockUnits
        } else if (boxWeight > 0 && order.capturedWeight > 0) {
            order.capturedWeight / boxWeight
        } else {
            0.0
        }

        val stockDisplay = when (effectiveUnitType) {
            "CAJA" -> {
                val boxes = stockUnits.toInt()
                val kgStr = String.format(Locale.US, "%.1f", order.capturedWeight)
                if (boxes > 0) "$boxes cjas\n($kgStr kg)" else "$kgStr kg"
            }
            "BARRA" -> {
                val bars = stockUnits.toInt()
                val kgStr = String.format(Locale.US, "%.1f", order.capturedWeight)
                if (bars > 0) "$bars barras\n($kgStr kg)" else "$kgStr kg"
            }
            "PAQUETE" -> {
                val pqtes = stockUnits.toInt()
                val kgStr = String.format(Locale.US, "%.1f", order.capturedWeight)
                if (pqtes > 0) "$pqtes pqtes\n($kgStr kg)" else "$kgStr kg"
            }
            "BOLSA" -> {
                val bolsas = stockUnits.toInt()
                val kgStr = String.format(Locale.US, "%.1f", order.capturedWeight)
                if (bolsas > 0) "$bolsas bolsas\n($kgStr kg)" else "$kgStr kg"
            }
            "BOTE" -> {
                val botes = stockUnits.toInt()
                val kgStr = String.format(Locale.US, "%.1f", order.capturedWeight)
                if (botes > 0) "$botes botes\n($kgStr kg)" else "$kgStr kg"
            }
            "PIEZA" -> {
                val pzas = if (stockUnits > 0) stockUnits.toInt() else order.capturedWeight.toInt()
                "$pzas pzas"
            }
            else -> String.format(Locale.US, "%.2f kg", order.capturedWeight)
        }
        Text(
            text = stockDisplay,
            modifier = Modifier.weight(1.1f),
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            color = Color.DarkGray
        )

        // A PEDIR (EN BARRAS / PAQUETES / BOLSAS / CAJAS O KG)
        val kgFormatted = String.format(Locale.US, "%.2f", order.shortage)
        val unitsToOrder = if (order.orderedUnits > 0) {
            order.orderedUnits
        } else if (boxWeight > 0 && order.shortage > 0) {
            kotlin.math.max(1.0, kotlin.math.ceil(order.shortage / boxWeight))
        } else if (effectiveUnitType == "PIEZA" && order.shortage > 0) {
            kotlin.math.max(1.0, kotlin.math.ceil(order.shortage))
        } else {
            0.0
        }

        val toOrderDisplay = when (effectiveUnitType) {
            "CAJA" -> "${unitsToOrder.toInt()} CAJA(S)\n(~$kgFormatted kg)"
            "BARRA" -> "${unitsToOrder.toInt()} BARRA(S)\n(~$kgFormatted kg)"
            "PAQUETE" -> "${unitsToOrder.toInt()} PQTE(S)\n(~$kgFormatted kg)"
            "BOLSA" -> "${unitsToOrder.toInt()} BOLSA(S)\n(~$kgFormatted kg)"
            "BOTE" -> "${unitsToOrder.toInt()} BOTE(S)\n(~$kgFormatted kg)"
            "PIEZA" -> "${unitsToOrder.toInt()} PZA(S)\n(~$kgFormatted kg)"
            else -> String.format(Locale.US, "%.3f kg", order.shortage)
        }
        val orderTextColor = when (effectiveUnitType) {
            "BARRA" -> Color(0xFFD84315)
            "PAQUETE" -> Color(0xFF0277BD)
            "CAJA" -> orangeCajas
            "BOTE" -> Color(0xFF6A1B9A)
            "BOLSA" -> greenSuccess
            "PIEZA" -> Color(0xFF00838F)
            else -> greenSuccess
        }
        Text(
            text = toOrderDisplay,
            modifier = Modifier.weight(1.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            color = orderTextColor
        )

        // Botones de acción: Editar y Eliminar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onItemClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar Cantidad",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp)
                )
            }
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar del Pedido",
                    tint = Color.Gray,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
