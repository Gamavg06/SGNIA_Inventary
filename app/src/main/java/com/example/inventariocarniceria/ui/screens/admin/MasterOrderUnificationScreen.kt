package com.example.inventariocarniceria.ui.screens.admin

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

data class MasterConsolidatedItem(
    val productCode: Int,
    val productName: String,
    val pricePerKg: Double,
    val totalShortageKg: Double,
    val totalEstimatedCost: Double,
    val branchBreakdown: List<Pair<String, Double>>
)

/**
 * Pantalla ejecutiva de Unificación de Pedidos Multi-Sucursal (Dueño).
 * Diseñada bajo el estándar "Noir & Crema" Bento Grid: datos ordenados,
 * tipografía monospace de alta legibilidad y botones de exportación táctiles.
 */
@Composable
fun MasterOrderUnificationScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val branchOrders by viewModel.allBranchOrders.collectAsState()
    val context = LocalContext.current

    val consolidatedItems = remember(branchOrders) {
        branchOrders.groupBy { it.productCode }.map { (code, orders) ->
            val first = orders.first()
            val totalKg = orders.sumOf { it.shortageKg }
            val price = first.pricePerKg
            val totalCost = totalKg * price
            val breakdown = orders.map { it.branchName to it.shortageKg }
            MasterConsolidatedItem(
                productCode = code,
                productName = first.productName,
                pricePerKg = price,
                totalShortageKg = totalKg,
                totalEstimatedCost = totalCost,
                branchBreakdown = breakdown
            )
        }.sortedBy { it.productCode }
    }

    val totalMasterKg = consolidatedItems.sumOf { it.totalShortageKg }
    val totalMasterCost = consolidatedItems.sumOf { it.totalEstimatedCost }
    val activeBranchesCount = branchOrders.map { it.branchName }.distinct().size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NoirColors.Background)
            .padding(horizontal = NoirSpace.md, vertical = NoirSpace.sm),
        verticalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
    ) {
        // ENCABEZADO EJECUTIVO
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    NoirLabel("DIRECCIÓN GENERAL · ORDEN MAESTRA", color = NoirColors.Gold)
                    Text(
                        text = "Unificación de Pedidos",
                        style = NoirType.display.copy(fontSize = 24.sp),
                        color = NoirColors.TextPrimary
                    )
                    Text(
                        text = "Consolidación multi-tienda para compras a rastro",
                        style = NoirType.bodySmall,
                        color = NoirColors.TextMuted
                    )
                }

                if (branchOrders.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearBranchOrders() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(NoirRadius.sm))
                            .background(NoirColors.SurfaceRaised)
                            .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm))
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Limpiar Unificación",
                            tint = NoirColors.Danger,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // BENTO CARD HERO: TOTAL MAESTRO DE KILOS E INVERSIÓN
        item {
            BentoCard(
                hero = false,
                contentPadding = PaddingValues(NoirSpace.lg),
                verticalArrangement = Arrangement.spacedBy(NoirSpace.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(
                        text = if (consolidatedItems.isNotEmpty()) "$activeBranchesCount TIENDAS ACTIVAS" else "SIN PEDIDOS",
                        tone = if (consolidatedItems.isNotEmpty()) PillTone.Positive else PillTone.Neutral
                    )
                    Text(
                        text = "${consolidatedItems.size} CORTES",
                        style = NoirType.label,
                        color = NoirColors.TextMuted
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    BigMetric(
                        value = String.format(Locale.US, "%,.2f", totalMasterKg),
                        unit = "KG",
                        label = "VOLUMEN CONSOLIDADO",
                        style = NoirType.metricXL,
                        valueColor = NoirColors.Cream
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        NoirLabel("IMPORTE TOTAL ESTIMADO", color = NoirColors.TextMuted)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.US, "$%,.2f", totalMasterCost),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = NoirColors.TextPrimary
                        )
                    }
                }
            }
        }

        // BOTONES DE EXPORTACIÓN DIRECTA (WHATSAPP & PDF)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
            ) {
                NoirPrimaryButton(
                    text = "WhatsApp Maestro",
                    icon = Icons.Default.Share,
                    enabled = consolidatedItems.isNotEmpty(),
                    onClick = {
                        val text = buildMasterWhatsAppText(consolidatedItems, totalMasterKg, totalMasterCost)
                        WhatsAppExporter.shareRawText(context, text)
                    },
                    modifier = Modifier.weight(1.2f)
                )

                NoirSecondaryButton(
                    text = "PDF VIP",
                    icon = Icons.Default.PictureAsPdf,
                    enabled = consolidatedItems.isNotEmpty(),
                    onClick = {
                        val captures = consolidatedItems.map {
                            InventoryCaptureEntity(
                                productCode = it.productCode,
                                productName = "${it.productName} (UNIFICADO ${it.branchBreakdown.size} SUCURSALES)",
                                capturedWeight = 0.0,
                                maxStock = it.totalShortageKg,
                                shortage = it.totalShortageKg
                            )
                        }
                        PdfExporter.generateAndShareVipInventoryPdf(
                            context = context,
                            orders = captures,
                            allProducts = emptyList(),
                            branchName = "CONSOLIDADO CADENA MULTI-TIENDA",
                            managerName = "Dirección General / Administrador SGNIA"
                        )
                    },
                    modifier = Modifier.weight(0.9f)
                )
            }
        }

        // TABLA BENTO GRID: DETALLE DE CORTES CONSOLIDADO
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(NoirSpace.xs)
            ) {
                // ENCABEZADO DE TABLA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(NoirRadius.sm))
                        .background(NoirColors.SurfaceSunken)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "PLU",
                        modifier = Modifier.weight(0.7f),
                        style = NoirType.label.copy(fontSize = 11.sp),
                        color = NoirColors.TextSecondary
                    )
                    Text(
                        "PRODUCTO / SUCURSALES",
                        modifier = Modifier.weight(2.2f),
                        style = NoirType.label.copy(fontSize = 11.sp),
                        color = NoirColors.TextSecondary
                    )
                    Text(
                        "FALTANTE",
                        modifier = Modifier.weight(1.1f),
                        style = NoirType.label.copy(fontSize = 11.sp),
                        textAlign = TextAlign.End,
                        color = NoirColors.Cream
                    )
                    Text(
                        "COSTO EST.",
                        modifier = Modifier.weight(1.2f),
                        style = NoirType.label.copy(fontSize = 11.sp),
                        textAlign = TextAlign.End,
                        color = NoirColors.TextSecondary
                    )
                }

                if (consolidatedItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Sin pedidos pendientes de sucursales",
                                style = NoirType.title,
                                color = NoirColors.TextPrimary
                            )
                            Text(
                                text = "Las tiendas reportan stock completo o no han enviado pedidos hoy.",
                                style = NoirType.bodySmall,
                                color = NoirColors.TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    consolidatedItems.forEachIndexed { index, item ->
                        MasterConsolidatedItemRow(item)
                        if (index < consolidatedItems.lastIndex) {
                            HorizontalDivider(color = NoirColors.Hairline, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MasterConsolidatedItemRow(item: MasterConsolidatedItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val codeFormatted = String.format(Locale.US, "%03d", item.productCode)
            val totalKgStr = String.format(Locale.US, "%.2f kg", item.totalShortageKg)
            val totalCostStr = String.format(Locale.US, "$%,.2f", item.totalEstimatedCost)

            Text(
                text = codeFormatted,
                modifier = Modifier.weight(0.7f),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = NoirColors.Gold
            )
            Text(
                text = item.productName,
                modifier = Modifier.weight(2.2f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NoirColors.TextPrimary
            )
            Text(
                text = totalKgStr,
                modifier = Modifier.weight(1.1f),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                color = NoirColors.Cream
            )
            Text(
                text = totalCostStr,
                modifier = Modifier.weight(1.2f),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                color = NoirColors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // DESGLOSE POR SUCURSAL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item.branchBreakdown.forEach { (branch, kg) ->
                val shortBranch = branch.replace(" SGNIA", "").replace("Sucursal ", "")
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(NoirRadius.sm))
                        .background(NoirColors.SurfaceRaised)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(NoirColors.TextMuted)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "$shortBranch: ${String.format(Locale.US, "%.2f", kg)} kg",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NoirColors.TextSecondary
                    )
                }
            }
        }
    }
}

private fun buildMasterWhatsAppText(
    items: List<MasterConsolidatedItem>,
    totalKg: Double,
    totalCost: Double
): String {
    val sb = StringBuilder()
    sb.append("🥩 *ORDEN MAESTRA DE COMPRA - CARNICERÍA SGNIA*\n")
    sb.append("📌 *UNIFICACIÓN DE REQUERIMIENTOS MULTI-SUCURSAL*\n")
    sb.append("------------------------------------------\n\n")

    items.forEach { item ->
        val codeStr = String.format(Locale.US, "%03d", item.productCode)
        val kgStr = String.format(Locale.US, "%.2f", item.totalShortageKg)
        val costStr = String.format(Locale.US, "$%,.2f", item.totalEstimatedCost)

        sb.append("🔹 [PLU $codeStr] *${item.productName.uppercase()}*\n")
        sb.append("   ▶ TOTAL COMPRA: *$kgStr kg* | Est: $costStr MXN\n")
        sb.append("   📍 Desglose: ")
        val breakdownStr = item.branchBreakdown.joinToString(" + ") {
            "${it.first.replace(" SGNIA", "")} (${String.format(Locale.US, "%.2f", it.second)}kg)"
        }
        sb.append(breakdownStr)
        sb.append("\n\n")
    }

    sb.append("------------------------------------------\n")
    sb.append(String.format(Locale.US, "⚖️ *TOTAL COMPRA PROVEEDOR:* %.2f kg\n", totalKg))
    sb.append(String.format(Locale.US, "💰 *IMPORTE ESTIMADO TOTAL:* $%,.2f MXN\n\n", totalCost))
    sb.append("Favor de confirmar tiempo de entrega a la sucursal matriz SGNIA. ¡Gracias!")

    return sb.toString()
}
