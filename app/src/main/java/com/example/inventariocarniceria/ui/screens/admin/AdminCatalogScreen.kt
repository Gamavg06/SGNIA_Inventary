package com.example.inventariocarniceria.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.ProductCategories
import com.example.inventariocarniceria.data.local.ProductEntity
import com.example.inventariocarniceria.ui.design.BentoCard
import com.example.inventariocarniceria.ui.design.BigMetric
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirRadius
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill
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

    val categories = ProductCategories.ALL

    val filteredProducts = remember(allProducts, searchQuery, selectedCategoryFilter) {
        allProducts.filter { product ->
            val matchesSearch = searchQuery.isBlank() ||
                    product.code.toString().contains(searchQuery, ignoreCase = true) ||
                    product.name.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategoryFilter) {
                "Todas" -> true
                "Otros" -> product.category.equals("Otros", ignoreCase = true) ||
                        product.category.equals("Carnicería", ignoreCase = true) ||
                        product.category.equals("General", ignoreCase = true)
                "Quesos / Lácteos" -> product.category.equals("Quesos / Lácteos", ignoreCase = true) ||
                        product.category.equals("Quesos", ignoreCase = true) ||
                        product.category.equals("Lácteos", ignoreCase = true)
                else -> product.category.equals(selectedCategoryFilter, ignoreCase = true)
            }

            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NoirColors.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openNewProductEditor() },
                containerColor = NoirColors.Cream,
                contentColor = NoirColors.OnCream,
                shape = RoundedCornerShape(NoirRadius.md)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = NoirSpace.md, vertical = NoirSpace.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Corte", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NUEVO CORTE", style = NoirType.label.copy(color = NoirColors.OnCream, fontWeight = FontWeight.Bold))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NoirColors.Background)
                .padding(padding)
                .padding(horizontal = NoirSpace.md, vertical = NoirSpace.sm),
            verticalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
        ) {
            // ENCABEZADO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    NoirLabel("ADMINISTRACIÓN DE TIENDA · CATÁLOGO LOCAL", color = NoirColors.Gold)
                    Text(
                        text = "Catálogo de Cortes",
                        style = NoirType.display.copy(fontSize = 24.sp),
                        color = NoirColors.TextPrimary
                    )
                    Text(
                        text = "Códigos PLU, precios por kilo y límites de stock en báscula",
                        style = NoirType.bodySmall,
                        color = NoirColors.TextMuted
                    )
                }
            }

            // BENTO HERO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
            ) {
                BentoCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(NoirSpace.md)
                ) {
                    BigMetric(
                        value = "${allProducts.size}",
                        label = "TOTAL PLUs",
                        unit = "CORTES",
                        style = NoirType.metricL,
                        valueColor = NoirColors.Cream
                    )
                    StatusPill("LOCAL + NUBE", tone = PillTone.Positive)
                }

                BentoCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(NoirSpace.md)
                ) {
                    BigMetric(
                        value = "${filteredProducts.size}",
                        label = "EN PANTALLA",
                        unit = selectedCategoryFilter.uppercase(),
                        style = NoirType.metricL,
                        valueColor = NoirColors.TextPrimary
                    )
                    StatusPill("FILTRO ACTIVO", tone = PillTone.Neutral)
                }
            }

            // BOTONES DE SINCRONIZACIÓN Y RECLASIFICACIÓN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
            ) {
                Button(
                    onClick = { viewModel.syncAllCatalogToSupabase() },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirColors.SurfaceRaised, contentColor = NoirColors.Gold),
                    border = BorderStroke(1.dp, NoirColors.Gold.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(NoirRadius.sm),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = NoirColors.Gold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("SUBIR A SUPABASE", style = NoirType.label, color = NoirColors.Gold)
                }

                Button(
                    onClick = { viewModel.fixCategoriesManually() },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirColors.SurfaceRaised, contentColor = NoirColors.Cream),
                    border = BorderStroke(1.dp, NoirColors.HairlineStrong),
                    shape = RoundedCornerShape(NoirRadius.sm),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = NoirColors.Cream, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("AUTO-CLASIFICAR", style = NoirType.label, color = NoirColors.Cream)
                }
            }

            // BUSCADOR BENTO
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por código PLU o corte...", color = NoirColors.TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NoirColors.Cream) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(NoirRadius.md),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NoirColors.Cream,
                    unfocusedBorderColor = NoirColors.Hairline,
                    focusedTextColor = NoirColors.TextPrimary,
                    unfocusedTextColor = NoirColors.TextPrimary,
                    focusedContainerColor = NoirColors.Surface,
                    unfocusedContainerColor = NoirColors.Surface
                )
            )

            // SELECTOR DE CATEGORÍA
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    val bg = if (isSelected) NoirColors.Cream else NoirColors.SurfaceRaised
                    val fg = if (isSelected) NoirColors.OnCream else NoirColors.TextSecondary
                    val border = if (isSelected) NoirColors.Cream else NoirColors.Hairline

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(NoirRadius.pill))
                            .background(bg)
                            .border(1.dp, border, RoundedCornerShape(NoirRadius.pill))
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat.uppercase(),
                            style = NoirType.label.copy(fontSize = 11.sp),
                            color = fg
                        )
                    }
                }
            }

            // LISTA DE CORTES
            if (filteredProducts.isEmpty()) {
                BentoCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(NoirSpace.xl),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("No se encontraron cortes", style = NoirType.title, color = NoirColors.TextPrimary)
                            Text("Prueba con otro término de búsqueda o agrega un nuevo corte.", style = NoirType.bodySmall, color = NoirColors.TextMuted)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(NoirSpace.xs)
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
    val priceFormatted = String.format(Locale.US, "$%.2f", product.price)
    val maxStockFormatted = String.format(Locale.US, "%.2f", product.maxStock)

    BentoCard(
        contentPadding = PaddingValues(horizontal = NoirSpace.md, vertical = NoirSpace.sm),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NoirColors.SurfaceRaised)
                            .border(1.dp, NoirColors.Hairline, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "PLU %03d", product.code),
                            color = NoirColors.Gold,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = product.name,
                        style = NoirType.title.copy(fontSize = 15.sp),
                        color = NoirColors.TextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$priceFormatted/kg",
                        style = NoirType.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = NoirColors.Cream
                    )
                    Text(
                        text = "Máx: $maxStockFormatted kg",
                        fontSize = 12.sp,
                        color = NoirColors.TextMuted
                    )
                    StatusPill(product.category.uppercase(), tone = PillTone.Neutral, showDot = false)
                }

                if (product.orderUnitType != "KG" || product.parentProductCode != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (product.orderUnitType != "KG") {
                            Text(
                                text = "Caja: ~${product.weightPerBoxKg}kg",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = NoirColors.Cream
                            )
                        }
                        if (product.parentProductCode != null) {
                            Text(
                                text = "• Derivado de #${product.parentProductCode}",
                                fontSize = 11.sp,
                                color = NoirColors.TextSecondary
                            )
                        }
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = NoirColors.Cream)
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = NoirColors.Danger)
                }
            }
        }
    }
}
