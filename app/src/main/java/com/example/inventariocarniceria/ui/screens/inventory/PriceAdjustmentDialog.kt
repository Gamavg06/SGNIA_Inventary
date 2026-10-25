package com.example.inventariocarniceria.ui.screens.inventory

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inventariocarniceria.data.local.ProductEntity
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceAdjustmentDialog(
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val recentAudits by viewModel.recentPriceAudits.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(allProducts.firstOrNull()) }
    var newPriceText by remember { mutableStateOf("") }

    val darkRed = Color(0xFFB71C1C)
    val darkGreen = Color(0xFF2E7D32)
    val goldAccent = Color(0xFFFFD549)

    LaunchedEffect(selectedProduct) {
        selectedProduct?.let {
            newPriceText = String.format(Locale.US, "%.2f", it.price)
        }
    }

    val filteredProducts = remember(searchQuery, allProducts) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) allProducts
        else {
            allProducts.filter {
                it.localCode.toString().contains(q) ||
                it.code.toString().contains(q) ||
                it.name.lowercase().contains(q) ||
                it.synonyms.lowercase().contains(q)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = darkRed,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AJUSTE RÁPIDO DE PRECIOS",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = darkRed
                            )
                            Text(
                                text = "Actualización en mostrador & Exportación a Balanzas",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // BUSCADOR DE PRODUCTOS
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Buscar por Código PLU o Nombre...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // SELECTOR HORIZONTAL RÁPIDO DE PRODUCTOS FILTRADOS
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(6.dp)) {
                        items(filteredProducts) { prod ->
                            val isSelected = selectedProduct?.code == prod.code
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) darkRed.copy(alpha = 0.15f) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedProduct = prod }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${prod.localCode}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) darkRed else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.width(55.dp)
                                    )
                                    Text(
                                        text = prod.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", prod.price)}/kg",
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) darkGreen else Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PANEL DE EDICIÓN DEL PRODUCTO SELECCIONADO
                selectedProduct?.let { product ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "[PLU #${product.localCode}] ${product.name}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Categoría: ${product.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Precio Actual",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", product.price)}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                        color = darkRed
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // CAMPO NUMÉRICO GRANDE DE NUEVO PRECIO
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newPriceText,
                                    onValueChange = { newPriceText = it },
                                    label = { Text("Nuevo Precio por Kilo ($)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = darkGreen,
                                        focusedLabelColor = darkGreen
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = {
                                        val p = newPriceText.toDoubleOrNull()
                                        if (p != null && p > 0) {
                                            viewModel.updateProductPrice(product, p)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Guardar", fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // BOTONES TÁCTILES RÁPIDOS (+/- $5, +/- $10) PARA USO CON GUANTES
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(-10.0, -5.0, 5.0, 10.0).forEach { delta ->
                                    val label = if (delta > 0) "+$${delta.toInt()}" else "-$${kotlin.math.abs(delta).toInt()}"
                                    val btnColor = if (delta > 0) darkGreen else darkRed
                                    OutlinedButton(
                                        onClick = {
                                            val current = newPriceText.toDoubleOrNull() ?: product.price
                                            val next = (current + delta).coerceAtLeast(1.0)
                                            newPriceText = String.format(Locale.US, "%.2f", next)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(label, fontWeight = FontWeight.Bold, color = btnColor)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // BOTÓN ACCIÓN RÁPIDA: EXPORTAR A BÁSCULAS SYSTEL/QENDRA
                Button(
                    onClick = {
                        scope.launch {
                            val content = viewModel.getSystelPluExportContent()
                            try {
                                val file = File(context.cacheDir, "precios_systel_qendra.txt")
                                FileOutputStream(file).use { it.write(content.toByteArray()) }

                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Catálogo de Precios Balanzas Systel/Qendra")
                                    putExtra(Intent.EXTRA_TEXT, content)
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Exportar Precios a Básculas")
                                context.startActivity(shareIntent)
                            } catch (_: Exception) { }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXPORTAR PRECIOS A BÁSCULAS (SYSTEL / QENDRA)", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // BITÁCORA DE AUDITORÍA RECIENTE
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bitácora de Auditoría de Cambios de Precio", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(6.dp))

                val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (recentAudits.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay cambios de precios registrados recientemente", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            items(recentAudits) { audit ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "[#${audit.productCode}] ${audit.productName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "${dateFormat.format(Date(audit.timestamp))} • ${audit.managerName}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$${String.format(Locale.US, "%.2f", audit.oldPrice)}",
                                            fontSize = 12.sp,
                                            color = darkRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(" ➔ ", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            text = "$${String.format(Locale.US, "%.2f", audit.newPrice)}",
                                            fontSize = 13.sp,
                                            color = darkGreen,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = Color.LightGray.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }
}
