package com.example.inventariocarniceria.ui.screens.admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.inventariocarniceria.ui.components.SgniaCerhanGlobalSummaryCard
import com.example.inventariocarniceria.ui.components.SgniaCerhanGreetingHeader
import com.example.inventariocarniceria.ui.components.SgniaCerhanNumberedCard
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

@Composable
fun MasterOrderUnificationScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val branchOrders by viewModel.allBranchOrders.collectAsState()
    val context = LocalContext.current

    val matteBg = Color(0xFF0F0E0D)
    val cardBg = Color(0xFF181715)
    val cardBorder = Color(0xFF282623)
    val creamPrimary = Color(0xFFF3EFE6)
    val creamWarm = Color(0xFFEEDDC8)
    val textMuted = Color(0xFF8C857B)
    val darkText = Color(0xFF141312)

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
            .background(matteBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SgniaCerhanGreetingHeader(
                        userName = "Unificación Pedidos",
                        subtitle = "Consolidado Multi-Sucursal para Orden Maestra a Proveedor"
                    )
                }
                if (branchOrders.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearBranchOrders() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Limpiar Unificación",
                            tint = creamWarm
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SgniaCerhanNumberedCard(
                    modifier = Modifier.weight(1f),
                    number = "01.",
                    title = "Sucursales",
                    subtitle = String.format(Locale.US, "%d Tiendas • %.2f kg", activeBranchesCount, totalMasterKg),
                    icon = Icons.Default.Storefront,
                    showWaveChart = true
                )
                SgniaCerhanNumberedCard(
                    modifier = Modifier.weight(1f),
                    number = "02.",
                    title = "Importe Maestro",
                    subtitle = String.format(Locale.US, "$%.2f MXN", totalMasterCost),
                    icon = Icons.Default.Hub,
                    showWaveChart = false
                )
            }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF121110))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CÓD", modifier = Modifier.weight(0.6f), fontWeight = FontWeight.Black, fontSize = 11.sp, color = creamWarm)
                    Text("PRODUCTO / DESGLOSE", modifier = Modifier.weight(2f), fontWeight = FontWeight.Black, fontSize = 11.sp, color = creamWarm)
                    Text("TOTAL KG", modifier = Modifier.weight(1.1f), fontWeight = FontWeight.Black, fontSize = 11.sp, textAlign = TextAlign.End, color = creamPrimary)
                    Text("EST. ($)", modifier = Modifier.weight(1.1f), fontWeight = FontWeight.Black, fontSize = 11.sp, textAlign = TextAlign.End, color = creamWarm)
                }

                if (consolidatedItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Sin pedidos pendientes de sucursales",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = creamPrimary
                            )
                            Text(
                                text = "Todas las sucursales tienen existencias completas.",
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }
                    }
                } else {
                    consolidatedItems.forEach { item ->
                        MasterConsolidatedItemRow(item)
                        HorizontalDivider(color = cardBorder)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val text = buildMasterWhatsAppText(consolidatedItems, totalMasterKg, totalMasterCost)
                        WhatsAppExporter.shareRawText(context, text)
                    },
                    enabled = consolidatedItems.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = creamPrimary,
                        contentColor = darkText,
                        disabledContainerColor = Color(0xFF23211E),
                        disabledContentColor = textMuted
                    )
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp Maestro", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val captures = consolidatedItems.map {
                            com.example.inventariocarniceria.data.local.InventoryCaptureEntity(
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
                    enabled = consolidatedItems.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = creamPrimary)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = creamWarm, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF Maestro", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }

        item {
            SgniaCerhanGlobalSummaryCard(
                metric1Label = "Volumen Consolidado",
                metric1Value = String.format(Locale.US, "%.2f kg", totalMasterKg),
                metric1Sub = "Requerimiento total a rastro",
                metric2Label = "Inversión Estimada",
                metric2Value = String.format(Locale.US, "$%.2f", totalMasterCost),
                metric2Sub = "${consolidatedItems.size} cortes unificados",
                barValues = listOf(0.55f, 0.80f, 0.65f, 0.92f, 0.60f, 0.88f, 0.74f, 0.96f)
            )
        }
    }
}

@Composable
fun MasterConsolidatedItemRow(item: MasterConsolidatedItem) {
    val creamPrimary = Color(0xFFF3EFE6)
    val creamWarm = Color(0xFFEEDDC8)
    val textMuted = Color(0xFF8C857B)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val codeFormatted = String.format(Locale.US, "%03d", item.productCode)
            val totalKgStr = String.format(Locale.US, "%.3f kg", item.totalShortageKg)
            val totalCostStr = String.format(Locale.US, "$%.2f", item.totalEstimatedCost)

            Text(codeFormatted, modifier = Modifier.weight(0.6f), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 12.sp, color = creamWarm)
            Text(item.productName, modifier = Modifier.weight(2f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = creamPrimary)
            Text(totalKgStr, modifier = Modifier.weight(1.1f), fontSize = 12.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, color = creamPrimary)
            Text(totalCostStr, modifier = Modifier.weight(1.1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = creamWarm)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item.branchBreakdown.forEach { (branch, kg) ->
                val shortBranch = branch.replace(" SGNIA", "")
                Text(
                    text = "• $shortBranch: ${String.format(Locale.US, "%.2f", kg)} kg",
                    fontSize = 10.sp,
                    color = textMuted
                )
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
        val kgStr = String.format(Locale.US, "%.3f", item.totalShortageKg)
        val costStr = String.format(Locale.US, "$%.2f", item.totalEstimatedCost)

        sb.append("🔹 [Cod. $codeStr] *${item.productName.uppercase()}*\n")
        sb.append("   ▶ TOTAL COMPRA: *$kgStr kg* | Est: $costStr MXN\n")
        sb.append("   📍 Desglose: ")
        val breakdownStr = item.branchBreakdown.joinToString(" + ") { "${it.first.replace(" SGNIA", "")} (${String.format(Locale.US, "%.3f", it.second)}kg)" }
        sb.append(breakdownStr)
        sb.append("\n\n")
    }

    sb.append("------------------------------------------\n")
    sb.append(String.format(Locale.US, "⚖️ *TOTAL COMPRA PROVEEDOR:* %.3f kg\n", totalKg))
    sb.append(String.format(Locale.US, "💰 *IMPORTE ESTIMADO TOTAL:* $%.2f MXN\n\n", totalCost))
    sb.append("Favor de confirmar tiempo de entrega a la sucursal matriz SGNIA. ¡Gracias!")

    return sb.toString()
}
