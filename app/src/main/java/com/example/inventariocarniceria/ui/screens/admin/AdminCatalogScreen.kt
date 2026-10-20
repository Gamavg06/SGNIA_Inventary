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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.ProductEntity
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import java.util.Locale

@Composable
fun AdminCatalogScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Todas") }

    val categories = listOf("Todas", "Res", "Cerdo", "Pollo", "Embutidos", "Otros")

    val filteredProducts = remember(allProducts, searchQuery, selectedCategoryFilter) {
        allProducts.filter { product ->
            val matchesSearch = searchQuery.isBlank() ||
                    product.code.toString().contains(searchQuery, ignoreCase = true) ||
                    product.name.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategoryFilter == "Todas" || product.category.equals(selectedCategoryFilter, ignoreCase = true)

            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openNewProductEditor() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
            }
        }
    ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "ADMINISTRACIÓN DE CATÁLOGO",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                        )
                        Text(
                            text = "Gestión de productos, códigos, precios y existencias máximas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Buscador
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar por código (ej. 24) o nombre...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Categorías FilterChips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedCategoryFilter == cat,
                                    onClick = { selectedCategoryFilter = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Lista de productos
                        if (filteredProducts.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No se encontraron productos en el catálogo.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredProducts) { product ->
                                    AdminProductItem(
                                        product = product,
                                        onEdit = { viewModel.openProductEditor(product) },
                                        onDelete = { viewModel.deleteProduct(product) }
                                    )
                                }
                            }
                        }
                    }
        // Modal Dialog for adding/editing product
        if (uiState.isEditorOpen) {
            ProductEditDialog(
                product = uiState.editingProduct,
                allProducts = allProducts,
                onDismiss = { viewModel.closeProductEditor() },
                onSave = { updatedProduct -> viewModel.saveProduct(updatedProduct) },
                onLinkDerivative = { childCode, parentCode -> viewModel.linkDerivative(childCode, parentCode) }
            )
        }
    }
}

@Composable
fun AdminProductItem(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val darkRed = Color(0xFFB71C1C)
    val priceFormatted = String.format(Locale.US, "$%.2f", product.price)
    val maxStockFormatted = String.format(Locale.US, "%.2f", product.maxStock)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "#${product.code}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Precio: $priceFormatted/kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Max: $maxStockFormatted kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Packaging and Derivative Badges
                if (product.orderUnitType != "KG" || product.parentProductCode != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        when (product.orderUnitType) {
                            "BARRA" -> {
                                val wBar = if (product.weightPerBoxKg > 0) "~${product.weightPerBoxKg}kg" else ""
                                val stock = if (product.currentStockUnits > 0) " • ${product.currentStockUnits.toInt()} barras" else ""
                                Text(
                                    text = "🥖 BARRA $wBar$stock",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD84315)
                                )
                            }
                            "PAQUETE" -> {
                                val wPqte = if (product.weightPerBoxKg > 0) "~${product.weightPerBoxKg}kg" else ""
                                val stock = if (product.currentStockUnits > 0) " • ${product.currentStockUnits.toInt()} pqtes" else ""
                                Text(
                                    text = "📦 PAQUETE $wPqte$stock",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0277BD)
                                )
                            }
                            "BOLSA" -> {
                                val wBolsa = if (product.weightPerBoxKg > 0) "~${product.weightPerBoxKg}kg" else ""
                                val perBox = if (product.unitsPerBox > 1) " (${product.unitsPerBox} b/caja)" else ""
                                val stock = if (product.currentStockUnits > 0) " • ${product.currentStockUnits.toInt()} bolsas" else ""
                                Text(
                                    text = "🛍️ BOLSA $wBolsa$perBox$stock",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            "CAJA" -> {
                                val wBox = if (product.weightPerBoxKg > 0) "~${product.weightPerBoxKg}kg" else ""
                                val pzas = if (product.unitsPerBox > 1) " (${product.unitsPerBox} pzas)" else ""
                                val stock = if (product.currentStockUnits > 0) " • ${product.currentStockUnits.toInt()} cajas" else ""
                                Text(
                                    text = "📦 CAJA $wBox$pzas$stock",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                            "PIEZA" -> {
                                Text(
                                    text = "🥩 POR PIEZA (${product.currentStockUnits.toInt()} en exist.)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0277BD)
                                )
                            }
                        }
                        if (product.parentProductCode != null) {
                            Text(
                                text = "🧬 Derivado de #${product.parentProductCode}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6A1B9A)
                            )
                        }
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = darkRed)
                }
            }
        }
    }
}
