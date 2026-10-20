package com.example.inventariocarniceria.ui.screens.orders

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.InventoryCaptureEntity
import com.example.inventariocarniceria.data.local.ProductEntity
import com.example.inventariocarniceria.ui.components.SgniaCerhanGlobalSummaryCard
import com.example.inventariocarniceria.ui.components.SgniaCerhanGreetingHeader
import com.example.inventariocarniceria.ui.components.SgniaCerhanNumberedCard
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

    val matteBg = Color(0xFF0F0E0D)
    val cardBg = Color(0xFF181715)
    val cardBorder = Color(0xFF282623)
    val creamPrimary = Color(0xFFF3EFE6)
    val creamWarm = Color(0xFFEEDDC8)
    val textMuted = Color(0xFF8C857B)
    val darkText = Color(0xFF141312)

    val productMap = remember(allProducts) {
        allProducts.associateBy { it.code }
    }

    val nonDerivedOrders = remember(pendingOrders, productMap) {
        pendingOrders.filter { order ->
            val prod = productMap[order.productCode]
            prod?.parentProductCode == null
        }
    }

    val orderableProducts = remember(allProducts) {
        allProducts.filter { it.parentProductCode == null }
    }

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
                userName = "Pedidos de Surtido",
                subtitle = "Requisición de Cajas y Cortes Faltantes • ${uiState.currentBranch.ifBlank { "Sucursal" }}"
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
                    title = "Total a Pedir",
                    subtitle = String.format(Locale.US, "%.2f kg • %d Cajas", totalShortageKg, totalBoxesCount),
                    icon = Icons.Default.ShoppingCart,
                    showWaveChart = true
                )
                SgniaCerhanNumberedCard(
                    modifier = Modifier.weight(1f),
                    number = "02.",
                    title = "En Requisición",
                    subtitle = "${nonDerivedOrders.size} cortes por surtir",
                    icon = Icons.Default.Inventory2,
                    showWaveChart = false
                )
            }
        }

        // ==========================================================
        // BOTÓN PRINCIPAL AGREGAR PRODUCTO + CHIPS DE FILTRO
        // ==========================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            selectedOrderProduct = null
                            isManualOrderOpen = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = creamPrimary, contentColor = darkText),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Agregar Corte / Caja al Pedido", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }

                    if (nonDerivedOrders.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.clearAllCaptures() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = creamWarm),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Limpiar", modifier = Modifier.size(16.dp))
                        }
                    }
                }

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
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = creamPrimary,
                                selectedLabelColor = darkText,
                                containerColor = Color(0xFF121110),
                                labelColor = textMuted
                            )
                        )
                    }
                }
            }
        }

        // ==========================================================
        // TARJETA 03. TABLA DE REQUISICIÓN DE PEDIDO
        // ==========================================================
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF121110))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CÓD", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Black, fontSize = 11.sp, color = creamWarm)
                    Text("PRODUCTO", modifier = Modifier.weight(1.8f), fontWeight = FontWeight.Black, fontSize = 11.sp, color = creamWarm)
                    Text("EXISTENCIA", modifier = Modifier.weight(1.1f), fontWeight = FontWeight.Black, fontSize = 11.sp, textAlign = TextAlign.End, color = textMuted)
                    Text("A PEDIR", modifier = Modifier.weight(1.4f), fontWeight = FontWeight.Black, fontSize = 11.sp, textAlign = TextAlign.End, color = creamPrimary)
                    Spacer(modifier = Modifier.width(64.dp))
                }

                if (displayOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (nonDerivedOrders.isEmpty()) "No hay pedidos pendientes" else "Sin pedidos en esta categoría",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = creamPrimary
                            )
                            Text(
                                text = "Toca 'Agregar Corte / Caja al Pedido' arriba para capturar faltantes.",
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }
                    }
                } else {
                    displayOrders.forEach { order ->
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
                        HorizontalDivider(color = cardBorder)
                    }
                }
            }
        }

        // ==========================================================
        // TARJETA 04. EXPORTACIÓN EJECUTIVA (WHATSAPP / PDF)
        // ==========================================================
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
                    text = "04. Exportar Pedido y Corte",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = creamPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { WhatsAppExporter.shareToWhatsApp(context, nonDerivedOrders) },
                        enabled = nonDerivedOrders.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = creamPrimary,
                            contentColor = darkText,
                            disabledContainerColor = Color(0xFF23211E),
                            disabledContentColor = textMuted
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontWeight = FontWeight.Black, fontSize = 12.sp)
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
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = creamWarm,
                            contentColor = darkText,
                            disabledContainerColor = Color(0xFF23211E),
                            disabledContentColor = textMuted
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF Pedido", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }

                OutlinedButton(
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
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = creamPrimary)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GENERAR CORTE DE VENTAS (PDF)", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }

        // ==========================================================
        // RESUMEN GLOBAL DE SURTIDO
        // ==========================================================
        item {
            SgniaCerhanGlobalSummaryCard(
                metric1Label = "Faltante Total",
                metric1Value = String.format(Locale.US, "%.2f kg", totalShortageKg),
                metric1Sub = "$totalBoxesCount Cajas / Paquetes estimados",
                metric2Label = "Líneas de Pedido",
                metric2Value = "${nonDerivedOrders.size} Cortes",
                metric2Sub = "Listos para surtido central",
                barValues = listOf(0.50f, 0.82f, 0.64f, 0.91f, 0.58f, 0.87f, 0.75f, 0.96f)
            )
        }
    }

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
    val creamPrimary = Color(0xFFF3EFE6)
    val creamWarm = Color(0xFFEEDDC8)
    val textMuted = Color(0xFF8C857B)

    val effectiveUnitType = if (order.unitType.isNotBlank() && order.unitType != "KG") order.unitType else (product?.orderUnitType ?: "KG")
    val boxWeight = product?.weightPerBoxKg ?: 0.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val codeFormatted = String.format(Locale.US, "%03d", order.productCode)

        Text(
            text = codeFormatted,
            modifier = Modifier.weight(0.5f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            color = creamWarm
        )

        Column(modifier = Modifier.weight(1.8f)) {
            Text(
                text = order.productName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = creamPrimary
            )
            val packNote = when (effectiveUnitType) {
                "BARRA" -> "Barra ~${boxWeight} kg"
                "PAQUETE" -> "Paquete ~${boxWeight} kg"
                "BOLSA" -> "Bolsa ~${boxWeight} kg"
                "CAJA" -> "Caja ~${boxWeight} kg"
                "BOTE" -> "Bote ~${boxWeight} kg"
                "PIEZA" -> "Por Pieza"
                else -> null
            }
            if (packNote != null) {
                Text(
                    text = packNote,
                    fontSize = 10.sp,
                    color = textMuted
                )
            }
        }

        val stockDisplay = String.format(Locale.US, "%.2f kg", order.capturedWeight)
        Text(
            text = stockDisplay,
            modifier = Modifier.weight(1.1f),
            fontSize = 11.sp,
            textAlign = TextAlign.End,
            color = textMuted
        )

        val kgFormatted = String.format(Locale.US, "%.2f", order.shortage)
        val unitsToOrder = if (order.orderedUnits > 0) {
            order.orderedUnits
        } else if (boxWeight > 0 && order.shortage > 0) {
            kotlin.math.max(1.0, kotlin.math.ceil(order.shortage / boxWeight))
        } else 0.0

        val toOrderDisplay = when (effectiveUnitType) {
            "CAJA" -> "${unitsToOrder.toInt()} CAJA(S)\n(~$kgFormatted kg)"
            "BARRA" -> "${unitsToOrder.toInt()} BARRA(S)\n(~$kgFormatted kg)"
            "PAQUETE" -> "${unitsToOrder.toInt()} PQTE(S)\n(~$kgFormatted kg)"
            "BOLSA" -> "${unitsToOrder.toInt()} BOLSA(S)\n(~$kgFormatted kg)"
            else -> String.format(Locale.US, "%.2f kg", order.shortage)
        }

        Text(
            text = toOrderDisplay,
            modifier = Modifier.weight(1.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.End,
            color = creamPrimary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onItemClick,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = creamWarm,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
