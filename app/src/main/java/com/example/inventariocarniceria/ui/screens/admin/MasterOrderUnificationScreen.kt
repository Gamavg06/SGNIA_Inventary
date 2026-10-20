package com.example.inventariocarniceria.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.BranchOrderEntity
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
    val branchBreakdown: List<Pair<String, Double>> // BranchName -> Kilos
)

@Composable
fun MasterOrderUnificationScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val branchOrders by viewModel.allBranchOrders.collectAsState()
    val context = LocalContext.current

    val greenSuccess = Color(0xFF2E7D32)
    val darkRed = Color(0xFFB71C1C)
    val whatsAppGreen = Color(0xFF25D366)

    // Consolidate orders by productCode across all branches
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- 1. HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = Color(0xFFFFD549))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HUB UNIFICACIÓN MULTI-SUCURSAL",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                    )
                }
                Text(
                    text = "Consolidado de Requerimientos para Orden Maestra a Proveedor",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (branchOrders.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearBranchOrders() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Limpiar Unificación",
                        tint = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 2. EXECUTIVE MASTER KPI BANNER ---
        if (consolidatedItems.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp),
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
                        Text("SUCURSALES UNIFICADAS", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "$activeBranchesCount Sucursales",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = String.format(Locale.US, "%.3f kg totales", totalMasterKg),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("IMPORTE MAESTRO PROVEEDOR", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = String.format(Locale.US, "$%.2f MXN", totalMasterCost),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = greenSuccess
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // --- 3. CONSOLIDATED TABLE HEADER ---
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
                Text("CÓD", modifier = Modifier.weight(0.6f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("PRODUCTO / DESGLOSE SUCURSALES", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("TOTAL KG", modifier = Modifier.weight(1.1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End, color = darkRed)
                Text("EST. ($)", modifier = Modifier.weight(1.1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End, color = greenSuccess)
            }
        }

        if (consolidatedItems.isEmpty()) {
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
                        text = "Sin pedidos pendientes de sucursales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Todas las sucursales tienen existencias completas o no han enviado requerimientos.",
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
                items(consolidatedItems) { item ->
                    MasterConsolidatedItemRow(item)
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 4. MASTER EXPORT BUTTONS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    // Export Master Order to WhatsApp
                    val text = buildMasterWhatsAppText(consolidatedItems, totalMasterKg, totalMasterCost)
                    WhatsAppExporter.shareRawText(context, text)
                },
                enabled = consolidatedItems.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = whatsAppGreen,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Orden Maestra WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = {
                    // Export Master Order to PDF
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
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = darkRed)
                Spacer(modifier = Modifier.width(6.dp))
                Text("PDF Maestro", fontWeight = FontWeight.Bold, color = darkRed, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun MasterConsolidatedItemRow(item: MasterConsolidatedItem) {
    val darkRed = Color(0xFFB71C1C)
    val greenSuccess = Color(0xFF2E7D32)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val codeFormatted = String.format(Locale.US, "%03d", item.productCode)
            val totalKgStr = String.format(Locale.US, "%.3f kg", item.totalShortageKg)
            val totalCostStr = String.format(Locale.US, "$%.2f", item.totalEstimatedCost)

            Text(codeFormatted, modifier = Modifier.weight(0.6f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(item.productName, modifier = Modifier.weight(2f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(totalKgStr, modifier = Modifier.weight(1.1f), fontSize = 13.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, color = darkRed)
            Text(totalCostStr, modifier = Modifier.weight(1.1f), fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = greenSuccess)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Breakdown per branch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item.branchBreakdown.forEach { (branch, kg) ->
                val shortBranch = branch.replace(" SGNIA", "")
                Text(
                    text = "• $shortBranch: ${String.format(Locale.US, "%.3f", kg)} kg",
                    fontSize = 11.sp,
                    color = Color.Gray
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
