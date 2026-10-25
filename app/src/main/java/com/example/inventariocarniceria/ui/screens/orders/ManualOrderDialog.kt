package com.example.inventariocarniceria.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.ProductEntity
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualOrderDialog(
    allProducts: List<ProductEntity>,
    initialProduct: ProductEntity? = null,
    onDismiss: () -> Unit,
    onAddManualOrder: (product: ProductEntity, shortageKg: Double, orderedUnits: Double, unitType: String) -> Unit,
    onDeleteOrder: ((product: ProductEntity) -> Unit)? = null
) {
    var selectedProduct by remember { mutableStateOf(initialProduct ?: allProducts.firstOrNull()) }
    var productExpanded by remember { mutableStateOf(false) }

    // Cantidad a pedir (Cajas, Botes, Barras, Paquetes, Bolsas o Kg según corresponda)
    var quantityText by remember { mutableStateOf("1") }
    var orderInKgMode by remember { mutableStateOf(false) }

    // Peso por caja/bote editable dinámicamente
    var editableBoxWeightText by remember(selectedProduct) {
        mutableStateOf(String.format(Locale.US, "%.2f", selectedProduct?.weightPerBoxKg ?: 0.0))
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val greenSuccess = Color(0xFF2E7D32)
    val orangeCajas = Color(0xFFE65100)
    val terraBarra = Color(0xFFD84315)
    val bluePaquete = Color(0xFF0277BD)

    val currentUnitType = selectedProduct?.orderUnitType ?: "KG"
    val isPackaged = currentUnitType in listOf("CAJA", "BOTE", "BARRA", "PAQUETE", "BOLSA", "PIEZA") && !orderInKgMode

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = greenSuccess,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AGREGAR A PEDIDO DE SURTIDO",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = greenSuccess
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. SELECTOR DE PRODUCTO (SIN PRECIO DE VENTA)
                ExposedDropdownMenuBox(
                    expanded = productExpanded,
                    onExpandedChange = { productExpanded = !productExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.let { "#${it.code} ${it.name}" } ?: "Seleccionar producto...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Producto / Corte") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = productExpanded,
                        onDismissRequest = { productExpanded = false }
                    ) {
                        allProducts.forEach { prod ->
                            val unitTag = when (prod.orderUnitType) {
                                "BARRA" -> "🥖 Barra (~${prod.weightPerBoxKg} kg)"
                                "PAQUETE" -> "📦 Paquete (~${prod.weightPerBoxKg} kg)"
                                "BOLSA" -> "🛍️ Bolsa (~${prod.weightPerBoxKg} kg)"
                                "CAJA" -> "📦 Caja (~${prod.weightPerBoxKg} kg)"
                                "BOTE" -> "🫙 Bote (~${prod.weightPerBoxKg} kg)"
                                "PIEZA" -> "🥩 Pieza"
                                else -> "⚖️ Granel (kg)"
                            }
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("#${prod.code} ${prod.name}", fontWeight = FontWeight.Bold)
                                        Text("Presentación: $unitTag", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedProduct = prod
                                    quantityText = "1"
                                    productExpanded = false
                                }
                            )
                        }
                    }
                }

                // 2. TARJETA DE EXISTENCIA OPERATIVA (SIN PRECIO DE VENTA)
                selectedProduct?.let { prod ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "EXISTENCIA EN TIENDA:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.Gray
                                )
                                val badgeColor = when (prod.orderUnitType) {
                                    "BARRA" -> terraBarra
                                    "PAQUETE" -> bluePaquete
                                    "BOLSA" -> greenSuccess
                                    "CAJA" -> orangeCajas
                                    "BOTE" -> Color(0xFFE65100)
                                    else -> Color.DarkGray
                                }
                                Box(
                                    modifier = Modifier
                                        .background(badgeColor, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = prod.orderUnitType,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            when (prod.orderUnitType) {
                                "BARRA" -> {
                                    val stockBars = prod.currentStockUnits.toInt()
                                    val estWeight = if (prod.weightPerBoxKg > 0) prod.currentStockUnits * prod.weightPerBoxKg else 0.0
                                    val estWeightFormatted = String.format(Locale.US, "%.2f", estWeight)
                                    Text(
                                        text = "$stockBars ${if (stockBars == 1) "barra" else "barras"} en stock (~$estWeightFormatted kg)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = terraBarra
                                    )
                                    Text(
                                        text = "🥖 Barra estándar: ~${prod.weightPerBoxKg} kg",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                                "PAQUETE" -> {
                                    val stockPqtes = prod.currentStockUnits.toInt()
                                    val estWeight = if (prod.weightPerBoxKg > 0) prod.currentStockUnits * prod.weightPerBoxKg else 0.0
                                    val estWeightFormatted = String.format(Locale.US, "%.2f", estWeight)
                                    Text(
                                        text = "$stockPqtes paquetes en stock (~$estWeightFormatted kg)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = bluePaquete
                                    )
                                    Text(
                                        text = "📦 Paquete estándar: ~${prod.weightPerBoxKg} kg",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                                "BOLSA" -> {
                                    val stockBolsas = prod.currentStockUnits.toInt()
                                    val estWeight = if (prod.weightPerBoxKg > 0) prod.currentStockUnits * prod.weightPerBoxKg else 0.0
                                    val estWeightFormatted = String.format(Locale.US, "%.2f", estWeight)
                                    val cajasEquiv = if (prod.unitsPerBox > 0) " (${stockBolsas / prod.unitsPerBox} cajas equiv.)" else ""
                                    Text(
                                        text = "$stockBolsas bolsas en stock (~$estWeightFormatted kg)$cajasEquiv",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = greenSuccess
                                    )
                                    val cajasInfo = if (prod.unitsPerBox > 1) " • Caja de ${prod.unitsPerBox} bolsas" else ""
                                    Text(
                                        text = "🛍️ Bolsa de ~${prod.weightPerBoxKg} kg$cajasInfo",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                                "CAJA" -> {
                                    val stockBoxes = prod.currentStockUnits.toInt()
                                    val estWeight = if (prod.weightPerBoxKg > 0) prod.currentStockUnits * prod.weightPerBoxKg else 0.0
                                    val estWeightFormatted = String.format(Locale.US, "%.2f", estWeight)
                                    Text(
                                        text = "$stockBoxes ${if (stockBoxes == 1) "caja" else "cajas"} en stock (~$estWeightFormatted kg)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = orangeCajas
                                    )
                                    val piecesText = if (prod.unitsPerBox > 1) " (${prod.unitsPerBox} piezas/caja)" else ""
                                    Text(
                                        text = "📦 Caja estándar: ~${prod.weightPerBoxKg} kg$piecesText",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                                "BOTE" -> {
                                    val stockBotes = prod.currentStockUnits.toInt()
                                    val estWeight = if (prod.weightPerBoxKg > 0) prod.currentStockUnits * prod.weightPerBoxKg else 0.0
                                    val estWeightFormatted = String.format(Locale.US, "%.2f", estWeight)
                                    Text(
                                        text = "$stockBotes ${if (stockBotes == 1) "bote" else "botes"} en stock (~$estWeightFormatted kg)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = Color(0xFFE65100)
                                    )
                                    Text(
                                        text = "🫙 Bote institucional: ~${prod.weightPerBoxKg} kg",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                                "PIEZA" -> {
                                    val stockPieces = prod.currentStockUnits.toInt()
                                    Text(
                                        text = "$stockPieces piezas en stock",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = Color(0xFF0277BD)
                                    )
                                }
                                else -> {
                                    val stockKgFormatted = String.format(Locale.US, "%.2f", prod.maxStock)
                                    Text(
                                        text = "$stockKgFormatted kg (Meta de stock)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = greenSuccess
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 3. SECCIÓN DE INGRESO DE CANTIDAD A PEDIR SEGÚN PRESENTACIÓN
                selectedProduct?.let { prod ->
                    when {
                        prod.orderUnitType == "BARRA" && !orderInKgMode -> {
                            // MODO BARRA (JAMÓN)
                            Text(
                                text = "🥖 BARRAS A PEDIR (EJ. JAMÓN VIRGINIA / PAVO):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = terraBarra
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1, 2, 3, 5).forEach { num ->
                                    val isSelected = quantityText == num.toString()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) terraBarra else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { quantityText = num.toString() }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+$num ${if (num == 1) "Barra" else "Barras"}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                                label = { Text("Número de Barras a Pedir") },
                                placeholder = { Text("Ej. 1, 2, 3") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            val bars = quantityText.toDoubleOrNull() ?: 0.0
                            val totalKg = bars * prod.weightPerBoxKg
                            val totalKgFormatted = String.format(Locale.US, "%.2f", totalKg)

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Equivalente: ${bars.toInt()} barras x ${prod.weightPerBoxKg} kg = ",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "$totalKgFormatted kg",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = terraBarra
                                    )
                                }
                            }

                            TextButton(onClick = { orderInKgMode = true; quantityText = totalKgFormatted }) {
                                Text("Prefiero pedir en kilos directamente", fontSize = 11.sp)
                            }
                        }

                        prod.orderUnitType == "PAQUETE" && !orderInKgMode -> {
                            // MODO PAQUETE (SALCHICHA)
                            Text(
                                text = "📦 PAQUETES A PEDIR (EJ. SALCHICHA VIENA / PARA ASAR):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = bluePaquete
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1, 2, 5, 10).forEach { num ->
                                    val isSelected = quantityText == num.toString()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) bluePaquete else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { quantityText = num.toString() }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+$num ${if (num == 1) "Pqte" else "Pqtes"}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                                label = { Text("Número de Paquetes a Pedir") },
                                placeholder = { Text("Ej. 5, 10") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            val pqtes = quantityText.toDoubleOrNull() ?: 0.0
                            val totalKg = pqtes * prod.weightPerBoxKg
                            val totalKgFormatted = String.format(Locale.US, "%.2f", totalKg)

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Equivalente: ${pqtes.toInt()} pqtes x ${prod.weightPerBoxKg} kg = ",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "$totalKgFormatted kg",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = bluePaquete
                                    )
                                }
                            }

                            TextButton(onClick = { orderInKgMode = true; quantityText = totalKgFormatted }) {
                                Text("Prefiero pedir en kilos directamente", fontSize = 11.sp)
                            }
                        }

                        prod.orderUnitType == "BOLSA" && !orderInKgMode -> {
                            // MODO BOLSA Y CAJA (EJ. PAPA CONGELADA 2 KG)
                            val perBox = if (prod.unitsPerBox > 0) prod.unitsPerBox else 4
                            Text(
                                text = "🛍️ BOLSAS O CAJAS A PEDIR (PAPA DE 2 KG):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = greenSuccess
                            )

                            // Botones rápidos para bolsas y cajas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    1 to "+1 Bolsa",
                                    2 to "+2 Bolsas",
                                    perBox to "+1 Caja ($perBox b)",
                                    perBox * 2 to "+2 Cajas (${perBox * 2} b)"
                                ).forEach { (num, label) ->
                                    val isSelected = quantityText == num.toString()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) greenSuccess else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { quantityText = num.toString() }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                                label = { Text("Número Total de Bolsas a Pedir") },
                                placeholder = { Text("Ej. 4 bolsas = 1 caja") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            val bolsas = quantityText.toDoubleOrNull() ?: 0.0
                            val totalKg = bolsas * prod.weightPerBoxKg
                            val totalKgFormatted = String.format(Locale.US, "%.2f", totalKg)
                            val cajasCount = (bolsas / perBox).toInt()
                            val cajasRemainder = (bolsas % perBox).toInt()
                            val cajasSummary = if (cajasRemainder == 0) "$cajasCount caja(s)" else "$cajasCount caja(s) y $cajasRemainder bolsa(s)"

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Equivalente: ${bolsas.toInt()} bolsas = ",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = "$totalKgFormatted kg ($cajasSummary)",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = greenSuccess
                                        )
                                    }
                                }
                            }

                            TextButton(onClick = { orderInKgMode = true; quantityText = totalKgFormatted }) {
                                Text("Prefiero pedir en kilos directamente", fontSize = 11.sp)
                            }
                        }

                        prod.orderUnitType == "CAJA" && !orderInKgMode -> {
                            // MODO CAJA (MENUDO, PIERNA)
                            Text(
                                text = "📦 RECUADROS RÁPIDOS DE CAJAS (1-TOQUE):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = orangeCajas
                            )

                            // Fila 1 de Recuadros: 1, 2, 3 Cajas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1, 2, 3).forEach { num ->
                                    val isSelected = quantityText == num.toString()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) orangeCajas else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { quantityText = num.toString() }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$num ${if (num == 1) "Caja" else "Cajas"}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            // Fila 2 de Recuadros: 4, 5, 10 Cajas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(4, 5, 10).forEach { num ->
                                    val isSelected = quantityText == num.toString()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) orangeCajas else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { quantityText = num.toString() }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$num Cajas",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // RECUADRO MANUAL EDITABLE
                            Text(
                                text = "✍️ RECUADRO MANUAL (ESCRIBE CUALQUIER NÚMERO DE CAJAS):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val currentVal = quantityText.toIntOrNull() ?: 1
                                        if (currentVal > 1) quantityText = (currentVal - 1).toString()
                                    },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                ) {
                                    Text("-1", fontWeight = FontWeight.Black, fontSize = 16.sp)
                                }

                                OutlinedTextField(
                                    value = quantityText,
                                    onValueChange = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                                    label = { Text("Recuadro Manual (Cajas)") },
                                    placeholder = { Text("Ej. 1, 2, 7, 12") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                IconButton(
                                    onClick = {
                                        val currentVal = quantityText.toIntOrNull() ?: 0
                                        quantityText = (currentVal + 1).toString()
                                    },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(orangeCajas.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                ) {
                                    Text("+1", fontWeight = FontWeight.Black, fontSize = 16.sp, color = orangeCajas)
                                }
                            }

                            // PESO EDITABLE POR CAJA (POR SI VARÍA, EJ. 30, 32 O 34 KG)
                            OutlinedTextField(
                                value = editableBoxWeightText,
                                onValueChange = { editableBoxWeightText = it },
                                label = { Text("Peso promedio por caja (kg)") },
                                placeholder = { Text("Ej. 32.0, 34.0, 27.24") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            val boxes = quantityText.toDoubleOrNull() ?: 0.0
                            val boxWeight = editableBoxWeightText.toDoubleOrNull() ?: prod.weightPerBoxKg
                            val computedKg = boxes * boxWeight
                            val computedKgFormatted = String.format(Locale.US, "%.2f", computedKg)

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Equivalente: ${boxes.toInt()} cajas x $boxWeight kg = ",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "$computedKgFormatted kg",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = orangeCajas
                                    )
                                }
                            }

                            TextButton(onClick = { orderInKgMode = true; quantityText = computedKgFormatted }) {
                                Text("Prefiero ingresar kilos directamente en vez de cajas", fontSize = 11.sp)
                            }
                        }

                        prod.orderUnitType == "BOTE" && !orderInKgMode -> {
                            // MODO BOTE (MAYONESA, ADEREZOS)
                            Text(
                                text = "🫙 RECUADROS RÁPIDOS DE BOTES (1-TOQUE):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFE65100)
                            )

                            // Fila 1 de Recuadros: 1, 2, 3 Botes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1, 2, 3).forEach { num ->
                                    val isSelected = quantityText == num.toString()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) Color(0xFFE65100) else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { quantityText = num.toString() }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$num ${if (num == 1) "Bote" else "Botes"}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            // Fila 2 de Recuadros: 4, 5, 10 Botes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(4, 5, 10).forEach { num ->
                                    val isSelected = quantityText == num.toString()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) Color(0xFFE65100) else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { quantityText = num.toString() }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$num Botes",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // RECUADRO MANUAL EDITABLE
                            Text(
                                text = "✍️ RECUADRO MANUAL (ESCRIBE NÚMERO DE BOTES):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val currentVal = quantityText.toIntOrNull() ?: 1
                                        if (currentVal > 1) quantityText = (currentVal - 1).toString()
                                    },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                ) {
                                    Text("-1", fontWeight = FontWeight.Black, fontSize = 16.sp)
                                }

                                OutlinedTextField(
                                    value = quantityText,
                                    onValueChange = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                                    label = { Text("Recuadro Manual (Botes)") },
                                    placeholder = { Text("Ej. 1, 2, 4") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                IconButton(
                                    onClick = {
                                        val currentVal = quantityText.toIntOrNull() ?: 0
                                        quantityText = (currentVal + 1).toString()
                                    },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(Color(0xFFE65100).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                ) {
                                    Text("+1", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFFE65100))
                                }
                            }

                            OutlinedTextField(
                                value = editableBoxWeightText,
                                onValueChange = { editableBoxWeightText = it },
                                label = { Text("Peso promedio por bote (kg)") },
                                placeholder = { Text("Ej. 3.9") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            val botes = quantityText.toDoubleOrNull() ?: 0.0
                            val boteWeight = editableBoxWeightText.toDoubleOrNull() ?: prod.weightPerBoxKg
                            val computedKg = botes * boteWeight
                            val computedKgFormatted = String.format(Locale.US, "%.2f", computedKg)

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Equivalente: ${botes.toInt()} botes x $boteWeight kg = ",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "$computedKgFormatted kg",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }

                            TextButton(onClick = { orderInKgMode = true; quantityText = computedKgFormatted }) {
                                Text("Prefiero ingresar kilos directamente en vez de botes", fontSize = 11.sp)
                            }
                        }

                        prod.orderUnitType == "PIEZA" && !orderInKgMode -> {
                            // MODO PIEZA
                            Text(
                                text = "🥩 PIEZAS A PEDIR:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0277BD)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1, 2, 5, 10).forEach { num ->
                                    val isSelected = quantityText == num.toString()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) Color(0xFF0277BD) else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { quantityText = num.toString() }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+$num",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                                label = { Text("Número de Piezas a Pedir") },
                                placeholder = { Text("Ej. 5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        else -> {
                            // MODO KILOS GRANEL
                            Text(
                                text = "⚖️ KILOS A PEDIR (KG):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = greenSuccess
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(5, 10, 20, 50).forEach { num ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                            .clickable {
                                                val current = quantityText.toDoubleOrNull() ?: 0.0
                                                quantityText = if (current <= 1.0) num.toString() else (current + num).toString()
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+$num kg",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { quantityText = it },
                                label = { Text("Kilos a Pedir (kg)") },
                                placeholder = { Text("Ej. 15.500") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            if (prod.orderUnitType != "KG" && orderInKgMode) {
                                TextButton(onClick = { orderInKgMode = false; quantityText = "1" }) {
                                    Text("Volver a pedir por ${prod.orderUnitType}", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                errorMessage?.let { msg ->
                    Text(text = "⚠️ $msg", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val prod = selectedProduct
                    if (prod == null) {
                        errorMessage = "Selecciona un producto"
                        return@Button
                    }
                    val qty = quantityText.toDoubleOrNull()
                    if (qty == null || qty <= 0) {
                        errorMessage = "Ingresa una cantidad válida a pedir"
                        return@Button
                    }

                    if (isPackaged) {
                        val units = qty
                        val boxWeight = editableBoxWeightText.toDoubleOrNull() ?: prod.weightPerBoxKg
                        val totalKg = if (boxWeight > 0) units * boxWeight else units
                        onAddManualOrder(prod, totalKg, units, prod.orderUnitType)
                    } else if (orderInKgMode && prod.orderUnitType != "KG") {
                        val boxWeight = editableBoxWeightText.toDoubleOrNull() ?: prod.weightPerBoxKg
                        val units = if (boxWeight > 0) qty / boxWeight else qty
                        onAddManualOrder(prod, qty, units, prod.orderUnitType)
                    } else {
                        onAddManualOrder(prod, qty, qty, "KG")
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = greenSuccess)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirmar Pedido")
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onDeleteOrder != null && initialProduct != null) {
                    TextButton(
                        onClick = { onDeleteOrder(initialProduct) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar del pedido")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )
}
