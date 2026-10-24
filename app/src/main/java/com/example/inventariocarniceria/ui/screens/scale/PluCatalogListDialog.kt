package com.example.inventariocarniceria.ui.screens.scale

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inventariocarniceria.data.local.ProductEntity
import com.example.inventariocarniceria.ui.components.AsyncProductImage
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirRadius
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill
import com.example.inventariocarniceria.ui.screens.admin.ProductEditDialog
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluCatalogListDialog(
    products: List<ProductEntity>,
    onSelectProduct: (ProductEntity) -> Unit,
    onDismiss: () -> Unit,
    onSaveProduct: (ProductEntity) -> Unit = {},
    onLinkDerivative: (childCode: Int, parentCode: Int?) -> Unit = { _, _ -> },
    onDeleteProduct: (ProductEntity) -> Unit = {},
    onDeleteAllProducts: () -> Unit = {},
    onImportQendra: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }

    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var isCreatingProduct by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }
    var showWipeCatalogConfirm by remember { mutableStateOf(false) }

    val categories = remember(products) {
        val unique = products.map { it.category.trim() }.filter { it.isNotBlank() }.distinct().sorted()
        listOf("Todas") + unique
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()
        products.filter { prod ->
            val matchesCategory = selectedCategory == "Todas" || prod.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = q.isEmpty() ||
                    prod.localCode.toString().contains(q) ||
                    prod.code.toString().contains(q) ||
                    prod.name.lowercase().contains(q) ||
                    prod.synonyms.lowercase().contains(q) ||
                    prod.category.lowercase().contains(q)
            matchesCategory && matchesQuery
        }.sortedBy { it.localCode }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.95f)
                .border(BorderStroke(1.dp, NoirColors.HairlineStrong), RoundedCornerShape(NoirRadius.lg)),
            shape = RoundedCornerShape(NoirRadius.lg),
            color = NoirColors.Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(NoirSpace.lg)
            ) {
                // --- 1. HEADER NOIR & CREMA ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(NoirRadius.sm))
                                .background(NoirColors.SurfaceRaised)
                                .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListNumbered,
                                contentDescription = null,
                                tint = NoirColors.Cream,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            NoirLabel(text = "MAESTRO DE ARTÍCULOS", color = NoirColors.Cream)
                            Text(
                                text = "Catálogo de PLUs y Derivadas",
                                style = NoirType.title,
                                color = NoirColors.TextPrimary
                            )
                            Text(
                                text = "${products.size} productos registrados • Modificación y cortes",
                                style = NoirType.bodySmall,
                                color = NoirColors.TextSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NoirSpace.xs)
                    ) {
                        if (products.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { showWipeCatalogConfirm = true },
                                shape = RoundedCornerShape(NoirRadius.sm),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirColors.Danger),
                                border = BorderStroke(1.dp, NoirColors.Danger.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Vaciar",
                                    tint = NoirColors.Danger,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "VACIAR",
                                    style = NoirType.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NoirColors.Danger
                                )
                            }
                        }

                        if (onImportQendra != null) {
                            Button(
                                onClick = onImportQendra,
                                shape = RoundedCornerShape(NoirRadius.sm),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NoirColors.SurfaceRaised,
                                    contentColor = NoirColors.Cream
                                ),
                                border = BorderStroke(1.dp, NoirColors.Hairline),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = NoirColors.Cream, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("IMPORTAR BÁSCULA", style = NoirType.bodySmall, fontWeight = FontWeight.Bold, color = NoirColors.Cream)
                            }
                        }

                        Button(
                            onClick = {
                                editingProduct = null
                                isCreatingProduct = true
                            },
                            shape = RoundedCornerShape(NoirRadius.sm),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NoirColors.Cream,
                                contentColor = NoirColors.OnCream
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = NoirColors.OnCream,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "NUEVO PRODUCTO",
                                style = NoirType.bodySmall,
                                fontWeight = FontWeight.Black,
                                color = NoirColors.OnCream
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(NoirRadius.sm))
                                .background(NoirColors.SurfaceRaised)
                                .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = NoirColors.TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = NoirSpace.sm),
                    color = NoirColors.Hairline
                )

                // --- 2. BUSCADOR EN TIEMPO REAL ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por código PLU, nombre o derivada (ej. Pierna, Bistec, Menudo)...", color = NoirColors.TextMuted, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = NoirColors.Cream
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = NoirColors.TextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(NoirRadius.sm),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoirColors.Cream,
                        unfocusedBorderColor = NoirColors.Hairline,
                        focusedTextColor = NoirColors.TextPrimary,
                        unfocusedTextColor = NoirColors.TextPrimary,
                        focusedContainerColor = NoirColors.SurfaceSunken,
                        unfocusedContainerColor = NoirColors.SurfaceSunken
                    )
                )

                Spacer(modifier = Modifier.height(NoirSpace.xs))

                // --- 3. CHIPS DE FILTRO POR CATEGORÍA ---
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NoirSpace.xs),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                val count = if (cat == "Todas") products.size else products.count { it.category.equals(cat, ignoreCase = true) }
                                Text(
                                    text = "$cat ($count)",
                                    style = NoirType.label,
                                    color = if (isSelected) NoirColors.OnCream else NoirColors.TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NoirColors.Cream,
                                selectedLabelColor = NoirColors.OnCream,
                                containerColor = NoirColors.SurfaceRaised,
                                labelColor = NoirColors.TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) NoirColors.Cream else NoirColors.Hairline,
                                selectedBorderColor = NoirColors.Cream
                            ),
                            shape = RoundedCornerShape(NoirRadius.pill)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(NoirSpace.xs))

                // --- 4. LISTA SCROLLABLE DE PRODUCTOS ---
                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(NoirRadius.sm))
                            .background(NoirColors.SurfaceSunken)
                            .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = NoirColors.TextMuted
                            )
                            Spacer(modifier = Modifier.height(NoirSpace.xs))
                            Text(
                                text = "No se encontraron productos con: \"$searchQuery\"",
                                style = NoirType.bodySmall,
                                color = NoirColors.TextMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(NoirSpace.xs)
                    ) {
                        items(filteredProducts, key = { it.code }) { prod ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        productToDelete = prod
                                        false
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(NoirRadius.sm))
                                            .background(NoirColors.DangerMuted)
                                            .border(1.dp, NoirColors.Danger, RoundedCornerShape(NoirRadius.sm))
                                            .clickable { productToDelete = prod }
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Eliminar Producto",
                                                color = NoirColors.Danger,
                                                fontWeight = FontWeight.Bold,
                                                style = NoirType.bodySmall
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = NoirColors.Danger,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            ) {
                                ProductPluItemCard(
                                    product = prod,
                                    allProducts = products,
                                    onSelectToWeigh = { onSelectProduct(prod) },
                                    onEdit = { editingProduct = prod },
                                    onDelete = { productToDelete = prod }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(NoirSpace.xs))

                // --- 5. FOOTER INFORMATIVO ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mostrando ${filteredProducts.size} de ${products.size} productos",
                        style = NoirType.label,
                        fontFamily = FontFamily.Monospace,
                        color = NoirColors.TextMuted
                    )
                    Text(
                        text = "💡 Desliza a la izquierda para Eliminar • Modificar para derivadas",
                        style = NoirType.label,
                        color = NoirColors.Cream
                    )
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar un producto individual
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            containerColor = NoirColors.Surface,
            titleContentColor = NoirColors.TextPrimary,
            textContentColor = NoirColors.TextSecondary,
            title = {
                Text(
                    text = "Eliminar Producto",
                    style = NoirType.title,
                    color = NoirColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = "¿Seguro que deseas eliminar '${productToDelete?.name}' [PLU #${productToDelete?.localCode}] del catálogo?",
                    style = NoirType.bodySmall,
                    color = NoirColors.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { onDeleteProduct(it) }
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirColors.Danger)
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { productToDelete = null },
                    border = BorderStroke(1.dp, NoirColors.Hairline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirColors.TextSecondary)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación para vaciar todo el catálogo
    if (showWipeCatalogConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeCatalogConfirm = false },
            containerColor = NoirColors.Surface,
            titleContentColor = NoirColors.Danger,
            textContentColor = NoirColors.TextSecondary,
            title = {
                Text(
                    text = "⚠️ Vaciar Todo el Catálogo",
                    style = NoirType.title,
                    color = NoirColors.Danger
                )
            },
            text = {
                Text(
                    text = "Esta acción eliminará todos los ${products.size} productos registrados para dejar el catálogo completamente en blanco. ¿Deseas continuar?",
                    style = NoirType.bodySmall,
                    color = NoirColors.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAllProducts()
                        showWipeCatalogConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirColors.Danger)
                ) {
                    Text("SÍ, VACIAR TODO", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showWipeCatalogConfirm = false },
                    border = BorderStroke(1.dp, NoirColors.Hairline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirColors.TextSecondary)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de edición / creación de producto y gestión de derivados
    if (editingProduct != null || isCreatingProduct) {
        ProductEditDialog(
            product = editingProduct,
            allProducts = products,
            onDismiss = {
                editingProduct = null
                isCreatingProduct = false
            },
            onSave = { updatedProduct ->
                onSaveProduct(updatedProduct)
                editingProduct = null
                isCreatingProduct = false
            },
            onLinkDerivative = { childCode, parentCode ->
                onLinkDerivative(childCode, parentCode)
            }
        )
    }
}

@Composable
private fun ProductPluItemCard(
    product: ProductEntity,
    allProducts: List<ProductEntity>,
    onSelectToWeigh: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit = {}
) {
    val childDerivatives = remember(product, allProducts) {
        if (product.parentProductCode == null) {
            allProducts.filter { it.parentProductCode == product.code }
        } else emptyList()
    }

    val parentProduct = remember(product, allProducts) {
        if (product.parentProductCode != null) {
            allProducts.find { it.code == product.parentProductCode }
        } else null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(NoirRadius.sm),
        colors = CardDefaults.cardColors(containerColor = NoirColors.Surface),
        border = BorderStroke(1.dp, NoirColors.Hairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NoirSpace.sm)
        ) {
            // FILA SUPERIOR: PLU + IMAGEN + NOMBRE + CATEGORÍA + PRECIO
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BADGE DE CÓDIGO PLU
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 44.dp)
                        .clip(RoundedCornerShape(NoirRadius.sm))
                        .background(NoirColors.SurfaceRaised)
                        .border(1.dp, NoirColors.Cream.copy(alpha = 0.5f), RoundedCornerShape(NoirRadius.sm)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PLU",
                            style = NoirType.label,
                            fontSize = 8.sp,
                            color = NoirColors.Cream
                        )
                        Text(
                            text = "#${product.localCode}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = NoirColors.Cream
                        )
                    }
                }

                Spacer(modifier = Modifier.width(NoirSpace.xs))

                AsyncProductImage(
                    imageUrl = product.imageUri,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(NoirRadius.sm))
                )

                Spacer(modifier = Modifier.width(NoirSpace.xs))

                // DETALLES DEL PRODUCTO
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = NoirType.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = NoirColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = product.category,
                            style = NoirType.label,
                            color = NoirColors.Cream
                        )

                        val presLabel = when (product.orderUnitType) {
                            "BARRA" -> " • 🥖 Barra ~${product.weightPerBoxKg}kg"
                            "PAQUETE" -> " • 📦 Pqte ~${product.weightPerBoxKg}kg"
                            "BOLSA" -> " • 🛍️ Bolsa ~${product.weightPerBoxKg}kg"
                            "CAJA" -> " • 📦 Caja ~${product.weightPerBoxKg}kg"
                            "BOTE" -> " • 🫙 Bote ~${product.weightPerBoxKg}kg"
                            "PIEZA" -> " • 🥩 Pieza"
                            else -> " • ⚖️ Granel/Kg"
                        }
                        Text(
                            text = presLabel,
                            style = NoirType.label,
                            color = NoirColors.TextSecondary
                        )
                    }

                    if (product.synonyms.isNotBlank()) {
                        Text(
                            text = "Sinónimos: ${product.synonyms}",
                            style = NoirType.label,
                            color = NoirColors.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(NoirSpace.xs))

                // PRECIO POR KILO EN VERDE ESMERALDA
                Text(
                    text = "$${String.format(Locale.US, "%.2f", product.price)}/kg",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = NoirColors.Emerald
                )
            }

            // BADGE DE RELACIÓN DE DERIVADAS
            if (product.parentProductCode != null) {
                Spacer(modifier = Modifier.height(NoirSpace.xs))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(NoirColors.AmberMuted)
                        .border(1.dp, NoirColors.Amber.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "↳ Corte Derivado de: #${product.parentProductCode} ${parentProduct?.name ?: "Materia Prima Base"} (Al pesarse, descuenta del padre)",
                        style = NoirType.bodySmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoirColors.Amber
                    )
                }
            } else if (childDerivatives.isNotEmpty()) {
                Spacer(modifier = Modifier.height(NoirSpace.xs))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(NoirColors.SurfaceRaised)
                        .border(1.dp, NoirColors.Hairline, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val namesPreview = childDerivatives.take(4).joinToString(", ") { it.name }
                    val extra = if (childDerivatives.size > 4) " (+${childDerivatives.size - 4} más)" else ""
                    Text(
                        text = "🍗 Materia Prima Base • ${childDerivatives.size} derivadas: $namesPreview$extra",
                        style = NoirType.bodySmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoirColors.Cream
                    )
                }
            }

            Spacer(modifier = Modifier.height(NoirSpace.xs))

            // BOTONES DE ACCIÓN: [Eliminar], [Modificar], [Pesar]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(NoirRadius.sm),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirColors.Danger),
                    border = BorderStroke(1.dp, NoirColors.Danger.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = NoirColors.Danger,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Eliminar",
                        style = NoirType.bodySmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoirColors.Danger
                    )
                }

                Spacer(modifier = Modifier.width(NoirSpace.xs))

                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(NoirRadius.sm),
                    border = BorderStroke(1.dp, NoirColors.Hairline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirColors.TextPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = NoirColors.TextPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Modificar",
                        style = NoirType.bodySmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoirColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(NoirSpace.xs))

                Button(
                    onClick = onSelectToWeigh,
                    shape = RoundedCornerShape(NoirRadius.sm),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoirColors.Cream,
                        contentColor = NoirColors.OnCream
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = null,
                        tint = NoirColors.OnCream,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pesar",
                        style = NoirType.bodySmall,
                        fontWeight = FontWeight.Black,
                        color = NoirColors.OnCream
                    )
                }
            }
        }
    }
}
