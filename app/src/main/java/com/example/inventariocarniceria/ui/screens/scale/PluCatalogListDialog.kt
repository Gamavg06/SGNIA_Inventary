package com.example.inventariocarniceria.ui.screens.scale

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
import com.example.inventariocarniceria.theme.SgniaTheme
import com.example.inventariocarniceria.ui.components.AsyncProductImage
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
    onDeleteAllProducts: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }

    // Estados para edición, creación y eliminación
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
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(20.dp),
            color = SgniaTheme.colors.backgroundSecondary,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // --- 1. HEADER CON CONTADOR Y BOTÓN DE NUEVO PRODUCTO ---
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
                                imageVector = Icons.Default.FormatListNumbered,
                                contentDescription = null,
                                tint = SgniaTheme.colors.accentPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CATÁLOGO DE PLUs Y DERIVADAS",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = SgniaTheme.colors.textPrimary
                            )
                            Text(
                                text = "${products.size} productos registrados • Modificación y cortes",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SgniaTheme.colors.accentPrimary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (products.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { showWipeCatalogConfirm = true },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Vaciar",
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "VACIAR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE53935)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Button(
                            onClick = {
                                editingProduct = null
                                isCreatingProduct = true
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SgniaTheme.colors.accentPrimary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF141210),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "NUEVO PRODUCTO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF141210)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SgniaTheme.colors.surfaceGlass)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = SgniaTheme.colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- 2. BUSCADOR EN TIEMPO REAL ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por código PLU, nombre o derivada (ej. Pierna, Bistec, Menudo)...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = SgniaTheme.colors.accentPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = SgniaTheme.colors.textSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SgniaTheme.colors.accentPrimary,
                        unfocusedBorderColor = SgniaTheme.colors.surfaceGlassBorder,
                        focusedTextColor = SgniaTheme.colors.textPrimary,
                        unfocusedTextColor = SgniaTheme.colors.textPrimary,
                        focusedContainerColor = SgniaTheme.colors.surfaceGlass,
                        unfocusedContainerColor = SgniaTheme.colors.surfaceGlass
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // --- 3. CHIPS DE FILTRO POR CATEGORÍA ---
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SgniaTheme.colors.accentPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = SgniaTheme.colors.surfaceGlass,
                                labelColor = SgniaTheme.colors.textSecondary
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = SgniaTheme.colors.surfaceGlassBorder
                )

                // --- 4. LISTA SCROLLABLE DE PRODUCTOS ---
                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = SgniaTheme.colors.textMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No se encontraron productos con: \"$searchQuery\"",
                                fontSize = 13.sp,
                                color = SgniaTheme.colors.textMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFFD32F2F))
                                            .clickable { productToDelete = prod }
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Eliminar Producto",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
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

                Spacer(modifier = Modifier.height(8.dp))

                // --- 5. FOOTER INFORMATIVO ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mostrando ${filteredProducts.size} de ${products.size} productos",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SgniaTheme.colors.textMuted
                    )
                    Text(
                        text = "💡 Desliza a la izquierda para Eliminar • Toca [Modificar] para derivadas",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SgniaTheme.colors.accentPrimary
                    )
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar un producto individual
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = {
                Text(
                    text = "Eliminar Producto",
                    fontWeight = FontWeight.Bold,
                    color = SgniaTheme.colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "¿Seguro que deseas eliminar '${productToDelete?.name}' [PLU #${productToDelete?.localCode}] del catálogo?",
                    color = SgniaTheme.colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { onDeleteProduct(it) }
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { productToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación para vaciar todo el catálogo
    if (showWipeCatalogConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeCatalogConfirm = false },
            title = {
                Text(
                    text = "⚠️ Vaciar Todo el Catálogo",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            },
            text = {
                Text(
                    text = "Esta acción eliminará todos los ${products.size} productos registrados para dejar el catálogo completamente en blanco. ¿Deseas continuar?",
                    color = SgniaTheme.colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAllProducts()
                        showWipeCatalogConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("SÍ, VACIAR TODO", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showWipeCatalogConfirm = false }) {
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
    // Verificar si es producto padre con derivados
    val childDerivatives = remember(product, allProducts) {
        if (product.parentProductCode == null) {
            allProducts.filter { it.parentProductCode == product.code }
        } else emptyList()
    }

    // Verificar si es un corte derivado de algún padre
    val parentProduct = remember(product, allProducts) {
        if (product.parentProductCode != null) {
            allProducts.find { it.code == product.parentProductCode }
        } else null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SgniaTheme.colors.surfaceGlass),
        border = androidx.compose.foundation.BorderStroke(1.dp, SgniaTheme.colors.surfaceGlassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // FILA SUPERIOR: PLU + IMAGEN + NOMBRE + CATEGORÍA + PRECIO
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BADGE DE CÓDIGO PLU
                Box(
                    modifier = Modifier
                        .size(width = 58.dp, height = 46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SgniaTheme.colors.accentPrimary.copy(alpha = 0.22f))
                        .border(1.5.dp, SgniaTheme.colors.accentPrimary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PLU",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = SgniaTheme.colors.accentPrimary
                        )
                        Text(
                            text = "#${product.localCode}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = SgniaTheme.colors.accentPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // MINIATURA DE IMAGEN ASÍNCRONA
                AsyncProductImage(
                    imageUrl = product.imageUri,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                )

                Spacer(modifier = Modifier.width(10.dp))

                // DETALLES DEL PRODUCTO
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = SgniaTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = product.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SgniaTheme.colors.accentSecondary
                        )

                        // Presentación de unidad si aplica
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
                            fontSize = 10.sp,
                            color = SgniaTheme.colors.textMuted
                        )
                    }

                    if (product.synonyms.isNotBlank()) {
                        Text(
                            text = "Sinónimos: ${product.synonyms}",
                            fontSize = 9.sp,
                            color = SgniaTheme.colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // PRECIO POR KILO
                Text(
                    text = "$${String.format(Locale.US, "%.2f", product.price)}/kg",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = SgniaTheme.colors.statusStable
                )
            }

            // BADGE DE RELACIÓN DE DERIVADAS
            Spacer(modifier = Modifier.height(8.dp))
            if (product.parentProductCode != null) {
                // Es un derivado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE65100).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFFE65100).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "↳ Corte Derivado de: #${product.parentProductCode} ${parentProduct?.name ?: "Materia Prima Base"} (Al pesarse, descuenta kilos del padre)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB74D)
                    )
                }
            } else if (childDerivatives.isNotEmpty()) {
                // Es materia prima con derivadas vinculadas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SgniaTheme.colors.accentPrimary.copy(alpha = 0.12f))
                        .border(1.dp, SgniaTheme.colors.accentPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val namesPreview = childDerivatives.take(4).joinToString(", ") { it.name }
                    val extra = if (childDerivatives.size > 4) " (+${childDerivatives.size - 4} más)" else ""
                    Text(
                        text = "🍗 Materia Prima Base • ${childDerivatives.size} derivadas vinculadas: $namesPreview$extra",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SgniaTheme.colors.accentPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BOTONES DE ACCIÓN: [✏️ Modificar y Derivadas] Y [⚡ Pesar]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Eliminar",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SgniaTheme.colors.surfaceGlassBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = SgniaTheme.colors.textPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Modificar",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SgniaTheme.colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSelectToWeigh,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SgniaTheme.colors.accentPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = null,
                        tint = Color(0xFF141210),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pesar",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = Color(0xFF141210)
                    )
                }
            }
        }
    }
}
