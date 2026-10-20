package com.example.inventariocarniceria.ui.screens.manager

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.LocalProductEntity
import com.example.inventariocarniceria.ui.components.SgniaThemeToggle
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerInvestmentScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localProducts by viewModel.localProducts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var isAddOrEditDialogOpen by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<LocalProductEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<LocalProductEntity?>(null) }

    var selectedInvestmentFilter by remember { mutableStateOf("Todas") }
    var searchQuery by remember { mutableStateOf("") }
    var isTableViewMode by remember { mutableStateOf(true) } // true = Tablita ejecutiva, false = Tarjetas

    val greenSuccess = Color(0xFF2E7D32)
    val amberWarning = Color(0xFFE65100)
    val purpleSgnia = Color(0xFF6A1B9A)
    val darkRed = Color(0xFFB71C1C)

    // Inversiones registradas únicas para filtros
    val investmentNames = listOf("Todas") + localProducts.map { it.investmentName.trim() }.distinct().filter { it.isNotEmpty() }

    // Filtrar productos
    val filteredProducts = localProducts.filter { prod ->
        val matchesInvestment = (selectedInvestmentFilter == "Todas" || prod.investmentName.equals(selectedInvestmentFilter, ignoreCase = true))
        val matchesSearch = searchQuery.isBlank() ||
                prod.name.contains(searchQuery, ignoreCase = true) ||
                prod.investmentName.contains(searchQuery, ignoreCase = true) ||
                prod.code.toString().contains(searchQuery)
        matchesInvestment && matchesSearch
    }

    // Métricas ejecutivas
    val totalToMakeBags = filteredProducts.sumOf { it.quantityToMake }
    val totalToMakePieces = filteredProducts.sumOf { it.totalPiecesToMake }
    val totalStockBags = filteredProducts.sumOf { it.quantityInStock }
    val totalStockPieces = filteredProducts.sumOf { it.totalPiecesInStock }
    val totalValueToMake = filteredProducts.sumOf { it.totalValueToMake }
    val totalValueInStock = filteredProducts.sumOf { it.totalValueInStock }
    val totalProjectedValue = totalValueToMake + totalValueInStock

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    itemToEdit = null
                    isAddOrEditDialogOpen = true
                },
                containerColor = greenSuccess,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Botana / Inversión")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NUEVA INVERSIÓN", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = purpleSgnia.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    tint = purpleSgnia,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MI INVERSIÓN LOCAL",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                        )
                    }
                    Text(
                        text = "Venta por unidad/bolsita (Botanas SGNIA, Dulces, Abarrotes)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                SgniaThemeToggle(
                    onToggle = { viewModel.toggleTheme() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- EXECUTIVE DASHBOARD METRICS (KPIs) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PRODUCCIÓN Y CONTROL DE BOTANAS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Sucursal: ${uiState.currentBranch}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        // Botón de WhatsApp para compartir plan de producción
                        OutlinedButton(
                            onClick = {
                                shareProductionPlan(
                                    context = context,
                                    branch = uiState.currentBranch,
                                    investmentFilter = selectedInvestmentFilter,
                                    items = filteredProducts
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = greenSuccess),
                            border = BorderStroke(1.dp, greenSuccess),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compartir Plan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tarjetas de conteo de piezas y bolsas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Card: Por Hacer (Bolsitas y Piezas exactas)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = amberWarning.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = amberWarning, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("POR HACER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = amberWarning)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$totalToMakeBags bolsitas",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "🍬 $totalToMakePieces piezas a embolsar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = amberWarning
                                )
                            }
                        }

                        // Card: Existencia Lista en Mostrador
                        Card(
                            colors = CardDefaults.cardColors(containerColor = greenSuccess.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = greenSuccess, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("EXISTENCIA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = greenSuccess)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$totalStockBags bolsitas",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "📦 $totalStockPieces piezas listas",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = greenSuccess
                                )
                            }
                        }

                        // Card: Valor Total
                        Card(
                            colors = CardDefaults.cardColors(containerColor = purpleSgnia.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("VALOR TOTAL ($)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = purpleSgnia)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format(Locale.US, "$%.2f", totalProjectedValue),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = purpleSgnia
                                )
                                Text(
                                    text = "Venta unitaria",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- FILTERS & SEARCH ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar botana o código...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                // Toggle entre vista de Tablita y vista de Tarjetas
                IconButton(
                    onClick = { isTableViewMode = !isTableViewMode },
                    modifier = Modifier
                        .background(
                            color = if (isTableViewMode) purpleSgnia.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isTableViewMode) Icons.Default.TableChart else Icons.Default.ViewAgenda,
                        contentDescription = "Cambiar vista",
                        tint = if (isTableViewMode) purpleSgnia else Color.DarkGray
                    )
                }
            }

            // Chips de filtro por Nombre de Inversión
            if (investmentNames.size > 2) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(investmentNames) { invName ->
                        FilterChip(
                            selected = selectedInvestmentFilter == invName,
                            onClick = { selectedInvestmentFilter = invName },
                            label = { Text(invName, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = purpleSgnia,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- CONTENT AREA ---
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No se encontraron botanas con '$searchQuery'" else "Aún no has registrado botanas en esta inversión.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                itemToEdit = null
                                isAddOrEditDialogOpen = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = greenSuccess)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agregar Primera Botana")
                        }
                    }
                }
            } else {
                if (isTableViewMode) {
                    // --- TABLITA EJECUTIVA DE PRODUCCIÓN (HORIZONTALLY SCROLLABLE) ---
                    ExecutiveProductionTable(
                        modifier = Modifier.weight(1f),
                        items = filteredProducts,
                        onIncrementToMake = { item -> viewModel.updateLocalProductQuantityToMake(item, item.quantityToMake + 1) },
                        onDecrementToMake = { item -> viewModel.updateLocalProductQuantityToMake(item, (item.quantityToMake - 1).coerceAtLeast(0)) },
                        onIncrementStock = { item -> viewModel.updateLocalProductQuantity(item, item.quantityInStock + 1) },
                        onDecrementStock = { item -> viewModel.updateLocalProductQuantity(item, (item.quantityInStock - 1).coerceAtLeast(0)) },
                        onEdit = { item ->
                            itemToEdit = item
                            isAddOrEditDialogOpen = true
                        },
                        onDelete = { item -> itemToDelete = item }
                    )
                } else {
                    // --- VISTA DE TARJETAS ---
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProducts) { item ->
                            LocalProductItemCard(
                                item = item,
                                onIncrementToMake = { viewModel.updateLocalProductQuantityToMake(item, item.quantityToMake + 1) },
                                onDecrementToMake = { viewModel.updateLocalProductQuantityToMake(item, (item.quantityToMake - 1).coerceAtLeast(0)) },
                                onIncrementStock = { viewModel.updateLocalProductQuantity(item, item.quantityInStock + 1) },
                                onDecrementStock = { viewModel.updateLocalProductQuantity(item, (item.quantityInStock - 1).coerceAtLeast(0)) },
                                onEdit = {
                                    itemToEdit = item
                                    isAddOrEditDialogOpen = true
                                },
                                onDelete = { itemToDelete = item }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal para Agregar o Editar Producto de Inversión
    if (isAddOrEditDialogOpen) {
        AddEditLocalProductDialog(
            existingItem = itemToEdit,
            existingInvestments = investmentNames.filter { it != "Todas" },
            onDismiss = {
                isAddOrEditDialogOpen = false
                itemToEdit = null
            },
            onSave = { code, invName, prodName, cat, pzasPerBolsita, qtyToMake, qtyInStock, cost, sale ->
                if (itemToEdit != null) {
                    viewModel.updateLocalProduct(
                        itemToEdit!!.copy(
                            code = code,
                            investmentName = invName,
                            name = prodName,
                            category = cat,
                            piecesPerBolsita = pzasPerBolsita,
                            quantityToMake = qtyToMake,
                            quantityInStock = qtyInStock,
                            costPrice = cost,
                            salePrice = sale
                        )
                    )
                } else {
                    viewModel.addLocalProduct(
                        code = code,
                        investmentName = invName,
                        name = prodName,
                        category = cat,
                        piecesPerBolsita = pzasPerBolsita,
                        quantityToMake = qtyToMake,
                        quantityInStock = qtyInStock,
                        costPrice = cost,
                        salePrice = sale
                    )
                }
                isAddOrEditDialogOpen = false
                itemToEdit = null
            }
        )
    }

    // Diálogo de Confirmación de Eliminación
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Eliminar de la Inversión", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Estás seguro de que deseas eliminar '${item.name}' (${item.investmentName})? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLocalProduct(item)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = darkRed)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * TABLITA EJECUTIVA DE PRODUCCIÓN
 * Permite ver en una tabla profesional:
 * - CÓD (85)
 * - INVERSIÓN (Botanas SGNIA)
 * - PRODUCTO / BOTANA (Gomita Manguito Enchilado)
 * - PZAS X BOLSITA (6 pzas)
 * - POR HACER (10 bolsitas con +/-)
 * - TOTAL PIEZAS A ELABORAR (60 pzas destacado)
 * - EXISTENCIA (5 bolsitas con +/-)
 * - PRECIO ($15.00)
 * - VALOR TOTAL ($150.00)
 * - ACCIONES (Editar, Eliminar)
 */
@Composable
fun ExecutiveProductionTable(
    modifier: Modifier = Modifier,
    items: List<LocalProductEntity>,
    onIncrementToMake: (LocalProductEntity) -> Unit,
    onDecrementToMake: (LocalProductEntity) -> Unit,
    onIncrementStock: (LocalProductEntity) -> Unit,
    onDecrementStock: (LocalProductEntity) -> Unit,
    onEdit: (LocalProductEntity) -> Unit,
    onDelete: (LocalProductEntity) -> Unit
) {
    val greenSuccess = Color(0xFF2E7D32)
    val amberWarning = Color(0xFFE65100)
    val purpleSgnia = Color(0xFF6A1B9A)
    val darkRed = Color(0xFFB71C1C)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        // Envoltura con desplazamiento horizontal para garantizar que todas las columnas sean legibles
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
        ) {
            LazyColumn(
                modifier = Modifier.width(1080.dp)
            ) {
                // ENCABEZADO DE LA TABLITA
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CÓD", modifier = Modifier.width(60.dp), fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("INVERSIÓN", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("PRODUCTO / BOTANA", modifier = Modifier.width(200.dp), fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("PZAS X BOLSA", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Black, fontSize = 12.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("POR HACER", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Black, fontSize = 12.sp, textAlign = TextAlign.Center, color = amberWarning)
                            Text("TOTAL A PREPARAR", modifier = Modifier.width(140.dp), fontWeight = FontWeight.Black, fontSize = 12.sp, textAlign = TextAlign.Center, color = amberWarning)
                            Text("EXISTENCIA", modifier = Modifier.width(130.dp), fontWeight = FontWeight.Black, fontSize = 12.sp, textAlign = TextAlign.Center, color = greenSuccess)
                            Text("PRECIO", modifier = Modifier.width(80.dp), fontWeight = FontWeight.Black, fontSize = 12.sp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("TOTAL $", modifier = Modifier.width(90.dp), fontWeight = FontWeight.Black, fontSize = 12.sp, textAlign = TextAlign.End, color = purpleSgnia)
                            Text("ACCIONES", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Black, fontSize = 12.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // FILAS DE PRODUCTOS DE LA TABLITA
                items(items) { item ->
                    val isEven = items.indexOf(item) % 2 == 0
                    val rowBg = if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

                    Surface(
                        color = rowBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Código PLU / Venta
                            Surface(
                                color = purpleSgnia.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.width(55.dp)
                            ) {
                                Text(
                                    text = "#${item.code}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = purpleSgnia,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(5.dp))

                            // Nombre de la Inversión (ej. Botanas SGNIA)
                            Column(modifier = Modifier.width(130.dp)) {
                                Text(
                                    text = item.investmentName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.category,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }

                            // Nombre del Producto / Botana (ej. Gomita Manguito Enchilado)
                            Column(modifier = Modifier.width(200.dp)) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Piezas por Bolsita (ej. 6 pzas)
                            Surface(
                                color = Color.Gray.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.width(90.dp)
                            ) {
                                Text(
                                    text = "${item.piecesPerBolsita} pzas/bolsa",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))

                            // Por Hacer (Bolsitas con botones táctiles - y +)
                            Row(
                                modifier = Modifier.width(130.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onDecrementToMake(item) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Restar por hacer", modifier = Modifier.size(16.dp))
                                }
                                Surface(
                                    color = amberWarning.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = "${item.quantityToMake}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = amberWarning,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onIncrementToMake(item) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Sumar por hacer", tint = amberWarning, modifier = Modifier.size(16.dp))
                                }
                            }

                            // Total de Piezas a Preparar (ej. 60 pzas = 6 pzas x 10 bolsas)
                            Surface(
                                color = amberWarning.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.width(130.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${item.totalPiecesToMake} piezas",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = amberWarning
                                    )
                                    Text(
                                        text = "(${item.quantityToMake} × ${item.piecesPerBolsita})",
                                        fontSize = 10.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))

                            // En Existencia (Bolsitas terminadas con botones - y +)
                            Row(
                                modifier = Modifier.width(130.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onDecrementStock(item) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Restar existencia", modifier = Modifier.size(16.dp))
                                }
                                Surface(
                                    color = greenSuccess.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = "${item.quantityInStock}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = greenSuccess,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onIncrementStock(item) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Sumar existencia", tint = greenSuccess, modifier = Modifier.size(16.dp))
                                }
                            }

                            // Precio de venta unitario
                            Text(
                                text = String.format(Locale.US, "$%.2f", item.salePrice),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(80.dp)
                            )

                            // Valor total venta
                            Text(
                                text = String.format(Locale.US, "$%.2f", item.totalValueToMake + item.totalValueInStock),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = purpleSgnia,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(90.dp)
                            )

                            // Botones de Acción (Editar y Eliminar)
                            Row(
                                modifier = Modifier.width(100.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                IconButton(
                                    onClick = { onEdit(item) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = purpleSgnia, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { onDelete(item) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = darkRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * TARJETA DE PRODUCTO LOCAL (Vista alternativa)
 */
@Composable
fun LocalProductItemCard(
    item: LocalProductEntity,
    onIncrementToMake: () -> Unit,
    onDecrementToMake: () -> Unit,
    onIncrementStock: () -> Unit,
    onDecrementStock: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val greenSuccess = Color(0xFF2E7D32)
    val amberWarning = Color(0xFFE65100)
    val purpleSgnia = Color(0xFF6A1B9A)
    val darkRed = Color(0xFFB71C1C)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Fila superior: Código, Inversión, Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = purpleSgnia.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "CÓD. #${item.code}",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = purpleSgnia,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.investmentName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = purpleSgnia, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = darkRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Nombre de la botana / producto
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )

            // Detalle de piezas y precio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📦 ${item.piecesPerBolsita} piezas por bolsita",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                Text(
                    text = String.format(Locale.US, "$%.2f c/u", item.salePrice),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = greenSuccess
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila de Control: Por Hacer vs En Existencia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Caja Por Hacer
                Surface(
                    color = amberWarning.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("POR HACER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = amberWarning)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDecrementToMake, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = "${item.quantityToMake} bolsitas",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = amberWarning
                            )
                            IconButton(onClick = onIncrementToMake, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = amberWarning, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(
                            text = "= ${item.totalPiecesToMake} pzas a embolsar",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = amberWarning
                        )
                    }
                }

                // Caja En Existencia
                Surface(
                    color = greenSuccess.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("EXISTENCIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = greenSuccess)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDecrementStock, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = "${item.quantityInStock} bolsitas",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = greenSuccess
                            )
                            IconButton(onClick = onIncrementStock, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = greenSuccess, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(
                            text = "= ${item.totalPiecesInStock} pzas listas",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = greenSuccess
                        )
                    }
                }
            }
        }
    }
}

/**
 * MODAL COMPLETO PARA AGREGAR O EDITAR PRODUCTOS DE INVERSIÓN
 * Permite editar:
 * - Código PLU (default: 85)
 * - Nombre de Inversión (default: Botanas SGNIA)
 * - Nombre del Producto / Botana (ej. Gomita Manguito Enchilado)
 * - Piezas por bolsita (ej. 6)
 * - Cantidad por hacer (ej. 10)
 * - Cantidad en existencia (ej. 5)
 * - Precio de venta ($15.00)
 * - Costo (opcional)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLocalProductDialog(
    existingItem: LocalProductEntity? = null,
    existingInvestments: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        code: Int,
        investmentName: String,
        name: String,
        category: String,
        piecesPerBolsita: Int,
        quantityToMake: Int,
        quantityInStock: Int,
        cost: Double,
        sale: Double
    ) -> Unit
) {
    val isEditing = existingItem != null

    var codeText by remember { mutableStateOf(existingItem?.code?.toString() ?: "85") }
    var investmentNameText by remember { mutableStateOf(existingItem?.investmentName ?: "Botanas SGNIA") }
    var nameText by remember { mutableStateOf(existingItem?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(existingItem?.category ?: "Botanas") }
    var piecesPerBolsitaText by remember { mutableStateOf(existingItem?.piecesPerBolsita?.toString() ?: "6") }
    var quantityToMakeText by remember { mutableStateOf(existingItem?.quantityToMake?.toString() ?: "10") }
    var quantityInStockText by remember { mutableStateOf(existingItem?.quantityInStock?.toString() ?: "0") }
    var costText by remember { mutableStateOf(existingItem?.costPrice?.toString() ?: "0.0") }
    var saleText by remember { mutableStateOf(existingItem?.salePrice?.toString() ?: "15.0") }

    val categories = listOf("Botanas", "Dulces", "Ropa", "Abarrotes", "Otros")
    var categoryExpanded by remember { mutableStateOf(false) }

    // Cálculo dinámico para vista previa
    val pzas = piecesPerBolsitaText.toIntOrNull() ?: 0
    val toMake = quantityToMakeText.toIntOrNull() ?: 0
    val inStock = quantityInStockText.toIntOrNull() ?: 0
    val saleVal = saleText.toDoubleOrNull() ?: 0.0
    val totalPiecesNeeded = pzas * toMake
    val totalEstimatedValue = (toMake + inStock) * saleVal

    val purpleSgnia = Color(0xFF6A1B9A)
    val amberWarning = Color(0xFFE65100)
    val greenSuccess = Color(0xFF2E7D32)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint = purpleSgnia
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "EDITAR INVERSIÓN" else "NUEVA INVERSIÓN / BOTANA",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Fila 1: Código PLU y Nombre de la Inversión
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = codeText,
                        onValueChange = { codeText = it.filter { c -> c.isDigit() } },
                        label = { Text("Código PLU") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.35f)
                    )

                    OutlinedTextField(
                        value = investmentNameText,
                        onValueChange = { investmentNameText = it },
                        label = { Text("Nombre de Inversión") },
                        placeholder = { Text("ej. Botanas SGNIA") },
                        singleLine = true,
                        modifier = Modifier.weight(0.65f)
                    )
                }

                // Sugerencias rápidas de inversiones si existen
                if (existingInvestments.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        existingInvestments.take(3).forEach { inv ->
                            Surface(
                                color = purpleSgnia.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                onClick = { investmentNameText = inv }
                            ) {
                                Text(
                                    text = inv,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = purpleSgnia,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Fila 2: Nombre del Producto / Botana
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Nombre del Producto / Botana") },
                    placeholder = { Text("ej. Gomita Manguito Enchilado, Panditas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Fila 3: Categoría
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Fila 4: Piezas por bolsita y Bolsitas por hacer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = piecesPerBolsitaText,
                        onValueChange = { piecesPerBolsitaText = it.filter { c -> c.isDigit() } },
                        label = { Text("Pzas x Bolsita") },
                        placeholder = { Text("ej. 6 u 11") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = quantityToMakeText,
                        onValueChange = { quantityToMakeText = it.filter { c -> c.isDigit() } },
                        label = { Text("Bolsas por Hacer") },
                        placeholder = { Text("ej. 10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fila 5: Bolsitas en existencia y Precio de venta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityInStockText,
                        onValueChange = { quantityInStockText = it.filter { c -> c.isDigit() } },
                        label = { Text("Bolsas Listas (Stock)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = saleText,
                        onValueChange = { saleText = it },
                        label = { Text("Precio Venta ($)") },
                        placeholder = { Text("15.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Cuadro de Resumen en Tiempo Real de Elaboración
                Card(
                    colors = CardDefaults.cardColors(containerColor = purpleSgnia.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, purpleSgnia.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🧮 CÁLCULO DE PRODUCCIÓN:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = purpleSgnia
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "• $toMake bolsitas × $pzas pzas c/u = $totalPiecesNeeded piezas de producto a embolsar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalPiecesNeeded > 0) amberWarning else Color.Gray
                        )
                        Text(
                            text = "• Venta esperada total: $toMake por hacer + $inStock en stock = ${toMake + inStock} bolsas = $${String.format(Locale.US, "%.2f", totalEstimatedValue)} MXN",
                            fontSize = 11.sp,
                            color = greenSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val code = codeText.toIntOrNull() ?: 85
                    val inv = investmentNameText.trim().ifEmpty { "Botanas SGNIA" }
                    val name = nameText.trim()
                    val pzasVal = piecesPerBolsitaText.toIntOrNull() ?: 6
                    val toMakeVal = quantityToMakeText.toIntOrNull() ?: 0
                    val inStockVal = quantityInStockText.toIntOrNull() ?: 0
                    val cost = costText.toDoubleOrNull() ?: 0.0
                    val sale = saleText.toDoubleOrNull() ?: 15.0

                    if (name.isNotBlank()) {
                        onSave(code, inv, name, selectedCategory, pzasVal, toMakeVal, inStockVal, cost, sale)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = greenSuccess)
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Guardar Inversión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * Función para compartir el plan de producción por WhatsApp o mensaje de texto.
 */
fun shareProductionPlan(
    context: android.content.Context,
    branch: String,
    investmentFilter: String,
    items: List<LocalProductEntity>
) {
    val totalToMakeBags = items.sumOf { it.quantityToMake }
    val totalToMakePieces = items.sumOf { it.totalPiecesToMake }
    val totalValue = items.sumOf { it.totalValueToMake }

    val sb = StringBuilder()
    sb.append("📋 *PLAN DE PRODUCCIÓN - ")
    sb.append(if (investmentFilter == "Todas") "INVERSIONES SGNIA" else investmentFilter.uppercase())
    sb.append("*\n")
    sb.append("🏢 Sucursal: $branch\n")
    sb.append("━━━━━━━━━━━━━━━━━━━━\n\n")

    items.forEach { item ->
        sb.append("🍬 *${item.name}* (Cód. #${item.code})\n")
        sb.append("   • Inversión: ${item.investmentName}\n")
        sb.append("   • Bolsitas por hacer: *${item.quantityToMake} bolsitas*\n")
        sb.append("   • Piezas por bolsita: ${item.piecesPerBolsita} pzas\n")
        sb.append("   • 🎯 *TOTAL A EMBOLSAR: ${item.totalPiecesToMake} PIEZAS*\n")
        sb.append("   • Existencia lista: ${item.quantityInStock} bolsitas\n")
        sb.append("   • Precio de venta: $${String.format(Locale.US, "%.2f", item.salePrice)} c/u\n\n")
    }

    sb.append("━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("📦 *TOTAL BOLSITAS A ELABORAR:* $totalToMakeBags\n")
    sb.append("🍬 *TOTAL PIEZAS A CONTAR:* $totalToMakePieces pzas\n")
    sb.append("💰 *VALOR ESTIMADO:* $${String.format(Locale.US, "%.2f", totalValue)} MXN\n")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartir Plan de Producción"))
}
