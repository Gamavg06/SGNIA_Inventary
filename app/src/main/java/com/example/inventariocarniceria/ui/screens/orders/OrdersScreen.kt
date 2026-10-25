package com.example.inventariocarniceria.ui.screens.orders

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.InventoryCaptureEntity
import com.example.inventariocarniceria.data.local.ProductCategories
import com.example.inventariocarniceria.data.local.ProductEntity
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
                cat.equals(selectedCategoryFilter, ignoreCase = true)
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
                    NoirLabel("MOSTRADOR · ${uiState.currentBranch.uppercase()}", color = NoirColors.Gold)
                    Text(
                        text = "Pedidos de Surtido",
                        style = NoirType.display.copy(fontSize = 24.sp),
                        color = NoirColors.TextPrimary
                    )
                    Text(
                        text = "Requisición inteligente de faltantes a rastro y mayoreo",
                        style = NoirType.bodySmall,
                        color = NoirColors.TextMuted
                    )
                }

                if (nonDerivedOrders.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearAllCaptures() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(NoirRadius.sm))
                            .background(NoirColors.SurfaceRaised)
                            .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm))
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Limpiar Pedido",
                            tint = NoirColors.Danger,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // BENTO HERO
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
            ) {
                BentoCard(
                    modifier = Modifier.weight(1.1f),
                    contentPadding = PaddingValues(NoirSpace.md)
                ) {
                    BigMetric(
                        value = String.format(Locale.US, "%.1f", totalShortageKg),
                        label = "FALTANTE TOTAL",
                        unit = "KG",
                        style = NoirType.metricL,
                        valueColor = NoirColors.Cream
                    )
                    StatusPill("$totalBoxesCount CAJAS ESTIMADAS", tone = PillTone.Positive)
                }

                BentoCard(
                    modifier = Modifier.weight(0.9f),
                    contentPadding = PaddingValues(NoirSpace.md)
                ) {
                    BigMetric(
                        value = "${nonDerivedOrders.size}",
                        label = "EN REQUISICIÓN",
                        unit = "LÍNEAS",
                        style = NoirType.metricL,
                        valueColor = NoirColors.TextPrimary
                    )
                    StatusPill("POR ENVIAR", tone = PillTone.Pending)
                }
            }
        }

        // BOTÓN AGREGAR MANUAL + FILTROS
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(NoirSpace.sm)
            ) {
                NoirPrimaryButton(
                    text = "Agregar Corte / Caja al Pedido",
                    icon = Icons.Default.Add,
                    onClick = {
                        selectedOrderProduct = null
                        isManualOrderOpen = true
                    }
                )

                val orderCategories = ProductCategories.ALL
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(orderCategories) { cat ->
                        val isSelected = cat == selectedCategoryFilter
                        val bg = if (isSelected) NoirColors.Cream else NoirColors.SurfaceRaised
                        val fg = if (isSelected) NoirColors.OnCream else NoirColors.TextSecondary
                        val border = if (isSelected) NoirColors.Cream else NoirColors.Hairline

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(NoirRadius.pill))
                                .background(bg)
                                .border(1.dp, border, RoundedCornerShape(NoirRadius.pill))
                                .clickable { selectedCategoryFilter = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat.uppercase(),
                                style = NoirType.label.copy(fontSize = 10.sp),
                                color = fg
                            )
                        }
                    }
                }
            }
        }

        // TABLA BENTO GRID: REQUISICIÓN DE PEDIDO
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(NoirSpace.xs)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(NoirRadius.sm))
                        .background(NoirColors.SurfaceSunken)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PLU", modifier = Modifier.weight(0.6f), style = NoirType.label.copy(fontSize = 11.sp), color = NoirColors.TextSecondary)
                    Text("PRODUCTO", modifier = Modifier.weight(1.8f), style = NoirType.label.copy(fontSize = 11.sp), color = NoirColors.TextSecondary)
                    Text("EXISTE", modifier = Modifier.weight(1.0f), style = NoirType.label.copy(fontSize = 11.sp), textAlign = TextAlign.End, color = NoirColors.TextMuted)
                    Text("A PEDIR", modifier = Modifier.weight(1.4f), style = NoirType.label.copy(fontSize = 11.sp), textAlign = TextAlign.End, color = NoirColors.Cream)
                    Spacer(modifier = Modifier.width(60.dp))
                }

                if (displayOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (nonDerivedOrders.isEmpty()) "No hay pedidos pendientes" else "Sin pedidos en esta categoría",
                                style = NoirType.title,
                                color = NoirColors.TextPrimary
                            )
                            Text(
                                text = "Usa 'Agregar Corte / Caja al Pedido' arriba para ingresar faltantes.",
                                style = NoirType.bodySmall,
                                color = NoirColors.TextMuted
                            )
                        }
                    }
                } else {
                    displayOrders.forEachIndexed { index, order ->
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
                        if (index < displayOrders.lastIndex) {
                            HorizontalDivider(color = NoirColors.Hairline, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // ACCIONES DE ENVÍO Y REPORTES
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
            ) {
                NoirPrimaryButton(
                    text = "WhatsApp Tienda",
                    icon = Icons.Default.Share,
                    enabled = nonDerivedOrders.isNotEmpty(),
                    onClick = { WhatsAppExporter.shareToWhatsApp(context, nonDerivedOrders) },
                    modifier = Modifier.weight(1.2f)
                )

                NoirSecondaryButton(
                    text = "PDF Pedido",
                    icon = Icons.Default.PictureAsPdf,
                    enabled = nonDerivedOrders.isNotEmpty(),
                    onClick = {
                        PdfExporter.generateAndShareVipInventoryPdf(
                            context = context,
                            orders = nonDerivedOrders,
                            allProducts = allProducts,
                            branchName = uiState.currentBranch,
                            managerName = "Encargado (${uiState.currentBranch})"
                        )
                    },
                    modifier = Modifier.weight(0.9f)
                )
            }
        }

        item {
            NoirSecondaryButton(
                text = "GENERAR CORTE DE VENTAS (PDF)",
                icon = Icons.Default.PictureAsPdf,
                enabled = nonDerivedOrders.isNotEmpty() || recentCaptures.isNotEmpty(),
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
                modifier = Modifier.fillMaxWidth()
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
    val effectiveUnitType = if (order.unitType.isNotBlank() && order.unitType != "KG") order.unitType else (product?.orderUnitType ?: "KG")
    val boxWeight = product?.weightPerBoxKg ?: 0.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val codeFormatted = String.format(Locale.US, "%03d", order.productCode)

        Text(
            text = codeFormatted,
            modifier = Modifier.weight(0.6f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = NoirColors.Gold
        )

        Column(modifier = Modifier.weight(1.8f)) {
            Text(
                text = order.productName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NoirColors.TextPrimary
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
                    fontSize = 11.sp,
                    color = NoirColors.TextMuted
                )
            }
        }

        val stockDisplay = String.format(Locale.US, "%.1f kg", order.capturedWeight)
        Text(
            text = stockDisplay,
            modifier = Modifier.weight(1.0f),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            color = NoirColors.TextMuted
        )

        val kgFormatted = String.format(Locale.US, "%.1f", order.shortage)
        val unitsToOrder = if (order.orderedUnits > 0) {
            order.orderedUnits
        } else if (boxWeight > 0 && order.shortage > 0) {
            kotlin.math.max(1.0, kotlin.math.ceil(order.shortage / boxWeight))
        } else 0.0

        val toOrderDisplay = when (effectiveUnitType) {
            "CAJA" -> "${unitsToOrder.toInt()} CAJAS\n(~$kgFormatted kg)"
            "BARRA" -> "${unitsToOrder.toInt()} BARRAS\n(~$kgFormatted kg)"
            "PAQUETE" -> "${unitsToOrder.toInt()} PQTES\n(~$kgFormatted kg)"
            "BOLSA" -> "${unitsToOrder.toInt()} BOLSAS\n(~$kgFormatted kg)"
            else -> String.format(Locale.US, "%.1f kg", order.shortage)
        }

        Text(
            text = toOrderDisplay,
            modifier = Modifier.weight(1.4f),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            color = NoirColors.Cream
        )

        Row(
            modifier = Modifier.width(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onItemClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = NoirColors.Cream,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = NoirColors.Danger,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
