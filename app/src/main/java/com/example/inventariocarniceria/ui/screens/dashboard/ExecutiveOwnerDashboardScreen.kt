package com.example.inventariocarniceria.ui.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.theme.SgniaThemeMode
import com.example.inventariocarniceria.ui.design.BentoCard
import com.example.inventariocarniceria.ui.design.BigMetric
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirPrimaryButton
import com.example.inventariocarniceria.ui.design.NoirSecondaryButton
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill
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
    val activeBranchesCount = branchOrders.map { it.branchName }.distinct().size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NoirColors.Background)
            .padding(horizontal = NoirSpace.screen, vertical = NoirSpace.sm),
        verticalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
    ) {
        // ==========================================
        // 1. ENCABEZADO EJECUTIVO
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                NoirLabel("Consola de Dirección General", color = NoirColors.TextMuted)
                Text(
                    text = "Panel Maestro",
                    style = NoirType.display,
                    color = NoirColors.TextPrimary
                )
                Text(
                    text = "Consolidado de compras, unificación multi-sucursal y proveedores.",
                    style = NoirType.bodySmall,
                    color = NoirColors.TextSecondary
                )
            }
        }

        // ==========================================
        // 2. HERO BENTO: VOLUMEN E IMPORTE MAESTRO
        // ==========================================
        item {
            BentoCard(
                modifier = Modifier.fillMaxWidth(),
                hero = true,
                onClick = { onNavigateToSection?.invoke("unificacion") }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NoirLabel("Volumen consolidado a surtir", color = NoirColors.OnCreamSecondary)
                    StatusPill(
                        text = if (consolidatedItems.isNotEmpty()) "$activeBranchesCount tiendas" else "Al día",
                        tone = if (consolidatedItems.isNotEmpty()) PillTone.Pending else PillTone.Positive
                    )
                }

                BigMetric(
                    value = String.format(Locale.US, "%.1f", totalMasterKg),
                    unit = "kg",
                    style = NoirType.metricXL,
                    valueColor = NoirColors.OnCream,
                    labelColor = NoirColors.OnCreamSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Importe estimado de compra",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = NoirColors.OnCreamSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "$%,.2f MXN", totalMasterCost),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NoirColors.OnCream
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "VER PEDIDOS",
                            style = NoirType.label.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = NoirColors.OnCream
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MergeType,
                            contentDescription = null,
                            tint = NoirColors.OnCream,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. SUB-GRID BENTO: SUCURSALES Y RENDIMIENTO
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
            ) {
                BentoCard(
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSection?.invoke("sucursales") }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NoirLabel("Red de Tiendas")
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            tint = NoirColors.Cream,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    BigMetric(
                        value = "${allBranches.size}",
                        unit = if (allBranches.size == 1) "tienda" else "tiendas",
                        style = NoirType.metricL
                    )
                    StatusPill(
                        text = "${allSuppliers.size} proveedores",
                        tone = PillTone.Neutral
                    )
                }

                BentoCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NoirLabel("Rendimiento")
                        StatusPill("Cárnico", PillTone.Positive)
                    }
                    BigMetric(
                        value = String.format(Locale.US, "%.1f", despieceState.yieldPercentage),
                        unit = "%",
                        style = NoirType.metricL,
                        valueColor = NoirColors.Emerald
                    )
                    Text(
                        text = "${consolidatedItems.size} cortes en orden",
                        style = NoirType.bodySmall.copy(fontSize = 12.sp),
                        color = NoirColors.TextSecondary
                    )
                }
            }
        }

        // ==========================================
        // 4. ACCIONES RÁPIDAS EJECUTIVAS
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(NoirSpace.xs)) {
                NoirLabel("Operaciones Maestras", color = NoirColors.TextMuted)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
                ) {
                    NoirPrimaryButton(
                        text = "Unificar Pedidos",
                        icon = Icons.AutoMirrored.Filled.MergeType,
                        onClick = { onNavigateToSection?.invoke("unificacion") },
                        modifier = Modifier.weight(1f)
                    )

                    NoirSecondaryButton(
                        text = "Proveedores",
                        icon = Icons.Default.LocalShipping,
                        onClick = { onNavigateToSection?.invoke("proveedores") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
                ) {
                    NoirSecondaryButton(
                        text = "Reporte PDF VIP",
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
                        },
                        modifier = Modifier.weight(1f)
                    )

                    NoirSecondaryButton(
                        text = "Nueva Tienda",
                        icon = Icons.Default.PersonAdd,
                        onClick = { onNavigateToSection?.invoke("sucursales") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(NoirSpace.lg))
        }
    }
}
