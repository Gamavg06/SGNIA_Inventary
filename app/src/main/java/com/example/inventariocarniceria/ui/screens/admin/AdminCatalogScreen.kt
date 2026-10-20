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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.ProductEntity
import com.example.inventariocarniceria.ui.components.SgniaCerhanGreetingHeader
import com.example.inventariocarniceria.ui.components.SgniaCerhanNumberedCard
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

    val matteBg = Color(0xFF0F0E0D)
    val cardBorder = Color(0xFF282623)
    val creamPrimary = Color(0xFFF3EFE6)
    val creamWarm = Color(0xFFEEDDC8)
    val textMuted = Color(0xFF8C857B)
    val darkText = Color(0xFF141312)

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
        containerColor = matteBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openNewProductEditor() },
                containerColor = creamPrimary,
                contentColor = darkText,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Corte")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NUEVO CORTE", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(matteBg)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SgniaCerhanGreetingHeader(
                userName = "Catálogo de Cortes",
                subtitle = "Gestión de PLUs, Precios por Kilo y Presentaciones"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SgniaCerhanNumberedCard(
                    modifier = Modifier.weight(1f),
                    number = "01.",
                    title = "Total PLUs",
                    subtitle = "${allProducts.size} cortes registrados",
                    icon = Icons.Default.Inventory2,
                    showWaveChart = true
                )
                SgniaCerhanNumberedCard(
                    modifier = Modifier.weight(1f),
                    number = "02.",
                    title = "Filtrados",
                    subtitle = "${filteredProducts.size} en vista ($selectedCategoryFilter)",
                    icon = Icons.Default.Category,
                    showWaveChart = false
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por código (ej. 24) o nombre...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = creamWarm) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = creamPrimary,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = creamPrimary,
                    unfocusedTextColor = creamPrimary,
                    focusedPlaceholderColor = textMuted,
                    unfocusedPlaceholderColor = textMuted
                )
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = cat },
                        label = {
                            Text(
                                text = cat,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = creamPrimary,
                            selectedLabelColor = darkText,
                            containerColor = Color(0xFF181715),
                            labelColor = textMuted
                        )
                    )
                }
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron productos en el catálogo.",
                        color = textMuted
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
    val cardBg = Color(0xFF181715)
    val cardBorder = Color(0xFF282623)
    val creamPrimary = Color(0xFFF3EFE6)
    val creamWarm = Color(0xFFEEDDC8)
    val textMuted = Color(0xFF8C857B)
    val darkText = Color(0xFF141312)

    val priceFormatted = String.format(Locale.US, "$%.2f", product.price)
    val maxStockFormatted = String.format(Locale.US, "%.2f", product.maxStock)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(creamPrimary, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "#${product.code}",
                        color = darkText,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = creamPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Precio: $priceFormatted/kg",
                    fontSize = 12.sp,
                    color = creamWarm,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Máx: $maxStockFormatted kg",
                    fontSize = 12.sp,
                    color = textMuted
                )
                Text(
                    text = product.category,
                    fontSize = 12.sp,
                    color = creamPrimary
                )
            }

            if (product.orderUnitType != "KG" || product.parentProductCode != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (product.orderUnitType != "KG") {
                        Text(
                            text = "${product.orderUnitType} (~${product.weightPerBoxKg}kg)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = creamWarm
                        )
                    }
                    if (product.parentProductCode != null) {
                        Text(
                            text = "Derivado de #${product.parentProductCode}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMuted
                        )
                    }
                }
            }
        }

        Row {
            IconButton(onClick = onEdit) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = creamWarm)
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = textMuted)
            }
        }
    }
}
