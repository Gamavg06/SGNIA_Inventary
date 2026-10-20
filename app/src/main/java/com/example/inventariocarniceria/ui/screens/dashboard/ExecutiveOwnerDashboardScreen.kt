package com.example.inventariocarniceria.ui.screens.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.theme.SgniaTheme
import com.example.inventariocarniceria.theme.SgniaThemeMode
import com.example.inventariocarniceria.ui.components.ShimmerVipButton
import com.example.inventariocarniceria.ui.components.SgniaThemeToggle
import com.example.inventariocarniceria.ui.components.TacticalGlassCard
import com.example.inventariocarniceria.ui.components.TacticalOdometerCounter
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import com.example.inventariocarniceria.utils.PdfExporter
import com.example.inventariocarniceria.utils.WhatsAppExporter
import java.util.Locale

@Composable
fun ExecutiveOwnerDashboardScreen(
    viewModel: InventoryViewModel,
    themeMode: SgniaThemeMode,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onOpenAdminConsole: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val branchOrders by viewModel.allBranchOrders.collectAsState()
    val despieceState by viewModel.despieceState.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val context = LocalContext.current

    // Consolidación de pedidos
    val consolidatedItems = remember(branchOrders) {
        branchOrders.groupBy { it.productCode }.map { (code, orders) ->
            val first = orders.first()
            val totalKg = orders.sumOf { it.shortageKg }
            val price = first.pricePerKg
            val totalCost = totalKg * price
            val breakdown = orders.map { it.branchName to it.shortageKg }
            Triple(first, totalKg, totalCost)
        }.sortedBy { it.first.productCode }
    }

    val totalMasterKg = consolidatedItems.sumOf { it.second }
    val totalMasterCost = consolidatedItems.sumOf { it.third }
    val activeBranchesCount = branchOrders.map { it.branchName }.distinct().size

    val infiniteTransition = rememberInfiniteTransition(label = "warningFlash")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flashAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SgniaTheme.colors.backgroundPrimary)
            .padding(16.dp)
    ) {
        // ==========================================
        // HEADER EJECUTIVO: LOGO, TÍTULO & THEME SWITCH
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SgniaTheme.colors.accentPrimary.copy(alpha = 0.2f))
                        .border(1.dp, SgniaTheme.colors.accentPrimary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = SgniaTheme.colors.accentPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CONSOLA EJECUTIVA DEL DUEÑO",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = SgniaTheme.colors.textPrimary
                    )
                    Text(
                        text = "HUB FINANCIERO Y CONTROL MULTI-TIENDA",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = SgniaTheme.colors.accentPrimary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onOpenAdminConsole != null) {
                    IconButton(onClick = onOpenAdminConsole) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Gestión Administrativa",
                            tint = SgniaTheme.colors.accentPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                SgniaThemeToggle(currentMode = themeMode, onToggle = onToggleTheme)
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir", tint = SgniaTheme.colors.textSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // 1. TARJETAS KPI FINANCIERAS CON ODÓMETROS
            // ==========================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI 1: VOLUMEN CONSOLIDADO
                    TacticalGlassCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Text(
                                text = "VOLUMEN A SURTIR",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = SgniaTheme.colors.accentPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            TacticalOdometerCounter(
                                value = totalMasterKg,
                                prefix = "",
                                suffix = " kg"
                            )
                        }
                    }

                    // KPI 2: INVERSIÓN TOTAL
                    TacticalGlassCard(modifier = Modifier.weight(1.2f)) {
                        Column {
                            Text(
                                text = "INVERSIÓN ESTIMADA",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = SgniaTheme.colors.statusStable
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            TacticalOdometerCounter(
                                value = totalMasterCost,
                                prefix = "$",
                                suffix = " MXN"
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 2. GAUGE DE BALANCE DE MASA EN CANVAS (DESPIECE PIERNA)
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
                                text = "BALANCE DE DESPIECE (PIERNA SIN HUESO)",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = SgniaTheme.colors.accentPrimary
                            )

                            val isWasteHigh = despieceState.wastePercentage > 15.0 && despieceState.initialWeightKg > 0
                            if (isWasteHigh) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SgniaTheme.colors.statusAlert.copy(alpha = flashAlpha))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("MERMA CRÍTICA > 15%", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 9.sp, color = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // GRÁFICO CIRCULAR EN CANVAS
                        val yieldPct = despieceState.yieldPercentage.toFloat()
                        val wastePct = despieceState.wastePercentage.toFloat()
                        val meatColor = SgniaTheme.colors.statusStable
                        val wasteColor = SgniaTheme.colors.statusAlert

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 14.dp.toPx()
                                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                                    // Arco de Carne Vendible
                                    val sweepMeat = (yieldPct / 100f) * 360f
                                    drawArc(
                                        color = meatColor,
                                        startAngle = -90f,
                                        sweepAngle = sweepMeat.coerceIn(0f, 360f),
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )

                                    // Arco de Merma
                                    val sweepWaste = (wastePct / 100f) * 360f
                                    drawArc(
                                        color = wasteColor,
                                        startAngle = -90f + sweepMeat,
                                        sweepAngle = sweepWaste.coerceIn(0f, 360f),
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${despieceState.yieldPercentage}%",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = SgniaTheme.colors.textPrimary
                                    )
                                    Text(
                                        text = "Rendimiento",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = SgniaTheme.colors.textSecondary
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).background(meatColor, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Carne Vendible: ${String.format(Locale.US, "%.1f", despieceState.totalDerivedWeightKg)} kg (${despieceState.yieldPercentage}%)",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = SgniaTheme.colors.textPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).background(wasteColor, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Merma / Grasa: ${String.format(Locale.US, "%.1f", despieceState.totalWasteKg)} kg (${despieceState.wastePercentage}%)",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = SgniaTheme.colors.textPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).background(Color.Gray, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Lote Inicial: ${String.format(Locale.US, "%.1f", despieceState.initialWeightKg)} kg",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = SgniaTheme.colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. DESGLOSE CONSOLIDADO POR CORTE
            // ==========================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONSOLIDADO DE PEDIDO MAYORISTA",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = SgniaTheme.colors.textSecondary
                    )
                    Text(
                        text = "${consolidatedItems.size} Cortes en falta",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = SgniaTheme.colors.accentPrimary
                    )
                }
            }

            items(consolidatedItems) { item ->
                val order = item.first
                val totalKg = item.second
                val totalCost = item.third

                TacticalGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${order.productCode}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = SgniaTheme.colors.accentPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = order.productName,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SgniaTheme.colors.textPrimary
                                )
                            }
                            Text(
                                text = "Surtido total de la cadena",
                                fontSize = 11.sp,
                                color = SgniaTheme.colors.textMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format(Locale.US, "%.3f", totalKg)} kg",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = SgniaTheme.colors.statusAlert
                            )
                            Text(
                                text = "$${String.format(Locale.US, "%,.2f", totalCost)} MXN",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SgniaTheme.colors.statusStable
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ==========================================
        // 4. BOTÓN VIP SHIMMER A 45° PARA EXPORTAR
        // ==========================================
        ShimmerVipButton(
            text = "EXPORTAR PEDIDO UNIFICADO (WHATSAPP / PDF)",
            onClick = {
                val captures = consolidatedItems.map { item ->
                    val o = item.first
                    com.example.inventariocarniceria.data.local.InventoryCaptureEntity(
                        productCode = o.productCode,
                        productName = "${o.productName} (CONSOLIDADO)",
                        capturedWeight = 0.0,
                        maxStock = item.second,
                        shortage = item.second
                    )
                }
                PdfExporter.generateAndShareVipInventoryPdf(
                    context = context,
                    orders = captures,
                    allProducts = allProducts,
                    branchName = "CONSOLIDADO CADENA MULTI-TIENDA",
                    managerName = "Dirección General / Administrador SGNIA"
                )
            },
            icon = {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
