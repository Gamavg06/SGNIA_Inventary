package com.example.inventariocarniceria.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.inventariocarniceria.theme.SgniaTheme
import com.example.inventariocarniceria.theme.SgniaThemeMode
import com.example.inventariocarniceria.ui.components.SgniaCerhanGlobalSummaryCard
import com.example.inventariocarniceria.ui.components.SgniaCerhanGreetingHeader
import com.example.inventariocarniceria.ui.components.SgniaCerhanNumberedCard
import com.example.inventariocarniceria.ui.components.SgniaCerhanQuickAccessCard
import com.example.inventariocarniceria.ui.components.SgniaQuickActionItem
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import com.example.inventariocarniceria.utils.PdfExporter
import java.util.Locale

@Composable
fun ExecutiveOwnerDashboardScreen(
    viewModel: InventoryViewModel,
    themeMode: SgniaThemeMode,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onOpenAdminConsole: (() -> Unit)? = null,
    onNavigateToSection: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val branchOrders by viewModel.allBranchOrders.collectAsState()
    val despieceState by viewModel.despieceState.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allBranches by viewModel.allBranches.collectAsState()
    val allSuppliers by viewModel.allSuppliers.collectAsState()
    val context = LocalContext.current

    val consolidatedItems = remember(branchOrders) {
        branchOrders.groupBy { it.productCode }.map { (_, orders) ->
            val first = orders.first()
            val totalKg = orders.sumOf { it.shortageKg }
            val price = first.pricePerKg
            val totalCost = totalKg * price
            Triple(first, totalKg, totalCost)
        }.sortedBy { it.first.productCode }
    }

    val totalMasterKg = consolidatedItems.sumOf { it.second }
    val totalMasterCost = consolidatedItems.sumOf { it.third }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SgniaTheme.colors.backgroundPrimary),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // SALUDO PRINCIPAL ESTILO CERHAN
        // ==========================================
        item {
            SgniaCerhanGreetingHeader(
                title = "Buenas tardes, Administrador Principal",
                subtitle = "Desde aquí llevas el control maestro de Carnicería SGNIA: qué cortes hay, pedidos por sucursal y quién los tiene a su cargo."
            )
        }

        // ==========================================
        // 01. INVENTARIO / CATÁLOGO DE CORTES
        // ==========================================
        item {
            SgniaCerhanNumberedCard(
                numberPrefix = "01.",
                title = "Catálogo Maestro de Cortes",
                description = "Catálogo de carnes y abarrotes, búsqueda por PLU o nombre y alta de nuevos productos.",
                bottomMetric = "Total Cortes Registrados: ${allProducts.size}",
                primaryIcon = Icons.Default.Inventory2,
                secondaryIcon = Icons.Default.Search,
                showChart = true,
                onClick = { onNavigateToSection?.invoke("catalogo") }
            )
        }

        // ==========================================
        // 02. UNIFICACIÓN DE PEDIDOS Y SURTIDO
        // ==========================================
        item {
            SgniaCerhanNumberedCard(
                numberPrefix = "02.",
                title = "Unificación de Pedidos y Abasto",
                description = "Qué solicita cada sucursal, consolidado de cajas/kilos y orden maestra para proveedores.",
                bottomMetric = "Volumen a Surtir: ${String.format(Locale.US, "%.1f", totalMasterKg)} kg ($${String.format(Locale.US, "%.0f", totalMasterCost)})",
                primaryIcon = Icons.Default.MergeType,
                secondaryIcon = Icons.Default.Description,
                showChart = false,
                onClick = { onNavigateToSection?.invoke("unificacion") }
            )
        }

        // ==========================================
        // 03. REPORTES Y RENDIMIENTO DE DESPIECE
        // ==========================================
        item {
            SgniaCerhanNumberedCard(
                numberPrefix = "03.",
                title = "Reportes y Rendimiento Cárnico",
                description = "Balance de pierna sin hueso (carne vendible vs merma) y exportación de reportes a PDF.",
                bottomMetric = "Cortes en Pedido: ${consolidatedItems.size} • Rendimiento: ${String.format(Locale.US, "%.1f", despieceState.yieldPercentage)}%",
                primaryIcon = Icons.Default.Assessment,
                secondaryIcon = Icons.Default.PictureAsPdf,
                showChart = true,
                onClick = {
                    if (branchOrders.isNotEmpty()) {
                        val captures = consolidatedItems.map { (first, totalKg, _) ->
                            com.example.inventariocarniceria.data.local.InventoryCaptureEntity(
                                productCode = first.productCode,
                                productName = first.productName,
                                capturedWeight = 0.0,
                                maxStock = totalKg,
                                shortage = totalKg
                            )
                        }
                        PdfExporter.generateAndShareVipInventoryPdf(
                            context = context,
                            orders = captures,
                            allProducts = allProducts,
                            branchName = "CONSOLIDADO CADENA MULTI-TIENDA",
                            managerName = "Dirección General / Administrador SGNIA"
                        )
                    } else {
                        onNavigateToSection?.invoke("unificacion")
                    }
                }
            )
        }

        // ==========================================
        // 04. SUCURSALES, ENCARGADOS Y PROVEEDORES
        // ==========================================
        item {
            SgniaCerhanNumberedCard(
                numberPrefix = "04.",
                title = "Sucursales e Instalaciones",
                description = "Tiendas activas, asignación de encargados, contraseñas y directorio de proveedores.",
                bottomMetric = "Total Sucursales: ${allBranches.size} • Proveedores: ${allSuppliers.size}",
                primaryIcon = Icons.Default.Business,
                secondaryIcon = Icons.Default.LocalShipping,
                showChart = false,
                onClick = { onNavigateToSection?.invoke("sucursales") }
            )
        }

        // ==========================================
        // ACCESO RÁPIDO (CUADRÍCULA 2x2 BOTONES CREMA)
        // ==========================================
        item {
            SgniaCerhanQuickAccessCard(
                title = "Acceso Rápido",
                actions = listOf(
                    SgniaQuickActionItem(
                        label = "Nuevo Corte",
                        icon = Icons.Default.AddCircleOutline,
                        onClick = {
                            viewModel.openNewProductEditor()
                            onNavigateToSection?.invoke("catalogo")
                        }
                    ),
                    SgniaQuickActionItem(
                        label = "Ver Pedidos",
                        icon = Icons.Default.MergeType,
                        onClick = { onNavigateToSection?.invoke("unificacion") }
                    ),
                    SgniaQuickActionItem(
                        label = "Nuevo Reporte",
                        icon = Icons.Default.PictureAsPdf,
                        onClick = {
                            if (branchOrders.isNotEmpty()) {
                                val captures = consolidatedItems.map { (first, totalKg, _) ->
                            com.example.inventariocarniceria.data.local.InventoryCaptureEntity(
                                productCode = first.productCode,
                                productName = first.productName,
                                capturedWeight = 0.0,
                                maxStock = totalKg,
                                shortage = totalKg
                            )
                        }
                        PdfExporter.generateAndShareVipInventoryPdf(
                            context = context,
                            orders = captures,
                            allProducts = allProducts,
                            branchName = "CONSOLIDADO CADENA MULTI-TIENDA",
                            managerName = "Dirección General / Administrador SGNIA"
                        )
                            } else {
                                onNavigateToSection?.invoke("unificacion")
                            }
                        }
                    ),
                    SgniaQuickActionItem(
                        label = "Nueva Sucursal",
                        icon = Icons.Default.PersonAdd,
                        onClick = { onNavigateToSection?.invoke("sucursales") }
                    )
                )
            )
        }

        // ==========================================
        // RESUMEN GLOBAL CON GRÁFICAS VECTORIALES
        // ==========================================
        item {
            SgniaCerhanGlobalSummaryCard(
                title = "Resumen Global",
                firstLabel = "Total Cortes Activos en Catálogo:",
                firstValue = "${allProducts.size}",
                secondLabel = "Total Sucursales y Proveedores:",
                secondValue = "${allBranches.size + allSuppliers.size}"
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
