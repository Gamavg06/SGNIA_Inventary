package com.example.inventariocarniceria.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.ProductEntity

private val NoirSurface = Color(0xFF181715)
private val NoirSurfaceRaised = Color(0xFF22201D)
private val NoirSurfaceSunken = Color(0xFF121110)
private val NoirHairline = Color(0xFF2C2824)
private val NoirCream = Color(0xFFEEDDC8)
private val NoirOnCream = Color(0xFF141312)
private val NoirTextPrimary = Color(0xFFF3EFE6)
private val NoirTextSecondary = Color(0xFFA8A196)
private val NoirEmerald = Color(0xFF4ADE80)
private val NoirAmber = Color(0xFFD9A441)
private val NoirDanger = Color(0xFFE07A6F)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductEditDialog(
    product: ProductEntity?,
    allProducts: List<ProductEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit,
    onLinkDerivative: (childCode: Int, parentCode: Int?) -> Unit = { _, _ -> }
) {
    var codeText by remember { mutableStateOf(product?.code?.toString() ?: "") }
    var nameText by remember { mutableStateOf(product?.name ?: "") }
    var synonymsText by remember { mutableStateOf(product?.synonyms ?: "") }
    var priceText by remember { mutableStateOf(product?.price?.let { if (it > 0) it.toString() else "" } ?: "") }
    var maxStockText by remember { mutableStateOf(product?.maxStock?.let { if (it > 0) it.toString() else "" } ?: "") }
    var selectedCategory by remember { mutableStateOf(product?.category ?: "Res") }

    // Unidad y empaque: "KG", "CAJA", "BARRA", "PAQUETE", "BOLSA", "PIEZA"
    var selectedUnitType by remember { mutableStateOf(product?.orderUnitType ?: "KG") }
    var weightPerBoxText by remember {
        mutableStateOf(product?.weightPerBoxKg?.let { if (it > 0) it.toString() else "" } ?: "")
    }
    var unitsPerBoxText by remember {
        mutableStateOf(product?.unitsPerBox?.let { if (it > 0) it.toString() else "1" } ?: "1")
    }
    var currentStockUnitsText by remember {
        mutableStateOf(product?.currentStockUnits?.let { if (it > 0) it.toString() else "" } ?: "")
    }

    // Padre y derivadas
    var selectedParentCode by remember { mutableStateOf(product?.parentProductCode) }
    var parentDropdownExpanded by remember { mutableStateOf(false) }
    var addDerivativeExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Res", "Cerdo", "Pollo", "Embutidos", "Quesos", "Jamones", "Salchichas", "Otros")
    var categoryExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Derivados actuales si este producto es padre
    val currentDerivatives = remember(allProducts, product) {
        if (product != null) {
            allProducts.filter { it.parentProductCode == product.code }
        } else emptyList()
    }

    // Posibles padres (excluyendo a sí mismo)
    val eligibleParents = remember(allProducts, product) {
        allProducts.filter { it.code != product?.code }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NoirSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = NoirCream,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CATÁLOGO DE PRODUCTOS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoirCream
                    )
                    Text(
                        text = if (product == null) "Nuevo Producto / Corte" else "Editar Producto #${product.code}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = NoirTextPrimary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ==========================================
                // 1. INFORMACIÓN GENERAL
                // ==========================================
                Text(
                    text = "1. INFORMACIÓN GENERAL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                // Código
                OutlinedTextField(
                    value = codeText,
                    onValueChange = { codeText = it.filter { char -> char.isDigit() } },
                    label = { Text("Código PLU / Base") },
                    placeholder = { Text("Ej. 20, 75, 85, 110") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = product == null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Nombre
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Nombre del Producto / Corte") },
                    placeholder = { Text("Ej. Jamón Virginia, Salchicha Viena, Papa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Sinónimos
                OutlinedTextField(
                    value = synonymsText,
                    onValueChange = { synonymsText = it },
                    label = { Text("Sinónimos / Alias (separados por coma)") },
                    placeholder = { Text("Ej. jamón, barra, bolsa, francesa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Precio por kg
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Precio Venta / kg ($)") },
                        placeholder = { Text("120.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Categoría
                    Box(modifier = Modifier.weight(1f)) {
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
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
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
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // ==========================================
                // 2. FORMATO DE PRESENTACIÓN Y EMPAQUE
                // ==========================================
                Text(
                    text = "2. PRESENTACIÓN DE LLEGADA Y PEDIDO",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Selecciona cómo llega y se cuenta el producto (Barras para jamón, Paquetes para salchicha, Bolsas/Cajas para papa, etc.):",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                // Selector de tipos de presentación
                val unitOptions = listOf(
                    "KG" to "⚖️ Granel / Kg",
                    "BARRA" to "🥖 Por Barra",
                    "PAQUETE" to "📦 Por Paquete",
                    "BOLSA" to "🛍️ Por Bolsa",
                    "CAJA" to "📦 Por Caja",
                    "BOTE" to "🫙 Por Bote",
                    "PIEZA" to "🥩 Por Pieza"
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    unitOptions.forEach { (type, label) ->
                        val isSelected = selectedUnitType == type
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) NoirCream else NoirSurfaceRaised,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (isSelected) NoirCream else NoirHairline, RoundedCornerShape(8.dp))
                                .clickable { selectedUnitType = type }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NoirOnCream else NoirTextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Configuración dinámica según la presentación seleccionada
                when (selectedUnitType) {
                    "BARRA" -> {
                        // MODO BARRA (JAMONES, QUESO BARRA)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NoirSurfaceRaised),
                            border = BorderStroke(1.dp, NoirHairline),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "🥖 Configuración de Barras (Jamón / Queso Barra):",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD84315)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = weightPerBoxText,
                                        onValueChange = { weightPerBoxText = it },
                                        label = { Text("Peso Promedio / Barra (kg)") },
                                        placeholder = { Text("Ej. 4.0 o 3.5") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = currentStockUnitsText,
                                        onValueChange = { currentStockUnitsText = it },
                                        label = { Text("Barras en Existencia") },
                                        placeholder = { Text("Ej. 3") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                OutlinedTextField(
                                    value = maxStockText,
                                    onValueChange = { maxStockText = it },
                                    label = { Text("Meta de Stock en Tienda (kg)") },
                                    placeholder = { Text("Ej. 12.0") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    "PAQUETE" -> {
                        // MODO PAQUETE (SALCHICHAS, TOCINO)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NoirSurfaceRaised),
                            border = BorderStroke(1.dp, NoirHairline),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "📦 Configuración de Paquetes (Salchicha, Embutidos Empacados):",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0277BD)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = weightPerBoxText,
                                        onValueChange = { weightPerBoxText = it },
                                        label = { Text("Peso / Paquete (kg)") },
                                        placeholder = { Text("Ej. 1.0 o 0.5") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = currentStockUnitsText,
                                        onValueChange = { currentStockUnitsText = it },
                                        label = { Text("Paquetes en Existencia") },
                                        placeholder = { Text("Ej. 10") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                OutlinedTextField(
                                    value = maxStockText,
                                    onValueChange = { maxStockText = it },
                                    label = { Text("Meta de Stock en Tienda (kg)") },
                                    placeholder = { Text("Ej. 10.0") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    "BOLSA" -> {
                        // MODO BOLSA Y CAJA (EJ. PAPA CONGELADA)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NoirSurfaceRaised),
                            border = BorderStroke(1.dp, NoirHairline),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "🛍️ Configuración de Bolsas y Cajas (Ej. Papa de 2 kg en cajas de 3-4 bolsas):",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NoirEmerald
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = weightPerBoxText,
                                        onValueChange = { weightPerBoxText = it },
                                        label = { Text("Peso / Bolsa (kg)") },
                                        placeholder = { Text("Ej. 2.0") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = unitsPerBoxText,
                                        onValueChange = { unitsPerBoxText = it.filter { c -> c.isDigit() } },
                                        label = { Text("Bolsas / Caja") },
                                        placeholder = { Text("Ej. 3 o 4") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = currentStockUnitsText,
                                        onValueChange = { currentStockUnitsText = it },
                                        label = { Text("Bolsas en Existencia") },
                                        placeholder = { Text("Ej. 8") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = maxStockText,
                                        onValueChange = { maxStockText = it },
                                        label = { Text("Meta de Stock (kg)") },
                                        placeholder = { Text("Ej. 16.0") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    "CAJA" -> {
                        // MODO CAJA (MENUDO, PIERNA)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NoirSurfaceRaised),
                            border = BorderStroke(1.dp, NoirHairline),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "📦 Configuración de Cajas (Ej. Menudo ~27.24 kg | Pierna 3 piezas ~30-34 kg):",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = weightPerBoxText,
                                        onValueChange = { weightPerBoxText = it },
                                        label = { Text("Peso / Caja (kg)") },
                                        placeholder = { Text("Ej. 27.24 o 32.0") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = unitsPerBoxText,
                                        onValueChange = { unitsPerBoxText = it.filter { c -> c.isDigit() } },
                                        label = { Text("Piezas / Caja") },
                                        placeholder = { Text("Ej. 3 o 1") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = currentStockUnitsText,
                                        onValueChange = { currentStockUnitsText = it },
                                        label = { Text("Cajas en Existencia") },
                                        placeholder = { Text("Ej. 2") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = maxStockText,
                                        onValueChange = { maxStockText = it },
                                        label = { Text("Meta Stock (kg)") },
                                        placeholder = { Text("Ej. 64.0") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    "BOTE" -> {
                        // MODO BOTE (MAYONESA, ADEREZOS, CUBETAS)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NoirSurfaceRaised),
                            border = BorderStroke(1.dp, NoirHairline),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "🫙 Configuración de Botes (Mayonesa, Aderezos, Cubetas):",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NoirCream
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = weightPerBoxText,
                                        onValueChange = { weightPerBoxText = it },
                                        label = { Text("Peso / Bote (kg)") },
                                        placeholder = { Text("Ej. 3.9 o 4.0") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = currentStockUnitsText,
                                        onValueChange = { currentStockUnitsText = it },
                                        label = { Text("Botes en Existencia") },
                                        placeholder = { Text("Ej. 3") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                OutlinedTextField(
                                    value = maxStockText,
                                    onValueChange = { maxStockText = it },
                                    label = { Text("Meta Stock (kg)") },
                                    placeholder = { Text("Ej. 12.0") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    "PIEZA" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = currentStockUnitsText,
                                onValueChange = { currentStockUnitsText = it },
                                label = { Text("Existencia (Piezas)") },
                                placeholder = { Text("Ej. 10") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = maxStockText,
                                onValueChange = { maxStockText = it },
                                label = { Text("Stock Máx (kg/unid)") },
                                placeholder = { Text("Ej. 20.0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    else -> {
                        // KG GRANEL NORMAL
                        OutlinedTextField(
                            value = maxStockText,
                            onValueChange = { maxStockText = it },
                            label = { Text("Stock Máximo / Meta (kg)") },
                            placeholder = { Text("Ej. 25.0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // ==========================================
                // 3. MATERIA PRIMA BASE Y DERIVADAS
                // ==========================================
                Text(
                    text = "3. MATERIA PRIMA BASE Y DERIVADAS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Cuando se pese o capture un corte derivado, los kilos se descontarán automáticamente del producto padre.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                // Selector de Padre
                val parentProd = eligibleParents.find { it.code == selectedParentCode }
                ExposedDropdownMenuBox(
                    expanded = parentDropdownExpanded,
                    onExpandedChange = { parentDropdownExpanded = !parentDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = parentProd?.let { "#${it.code} ${it.name}" } ?: "Ninguno (Producto Independiente / Base)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Materia Prima Padre") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = parentDropdownExpanded,
                        onDismissRequest = { parentDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ninguno (Producto Independiente / Base)") },
                            onClick = {
                                selectedParentCode = null
                                parentDropdownExpanded = false
                            }
                        )
                        eligibleParents.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("#${p.code} ${p.name}") },
                                onClick = {
                                    selectedParentCode = p.code
                                    parentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Si este producto es materia prima (no tiene padre y ya existe), mostrar sus derivadas
                if (product != null && selectedParentCode == null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NoirSurfaceRaised),
                        border = BorderStroke(1.dp, NoirHairline),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CORTES DERIVADOS VINCULADOS (${currentDerivatives.size}):",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )

                                Box {
                                    TextButton(onClick = { addDerivativeExpanded = true }) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Vincular corte", fontSize = 12.sp)
                                    }

                                    DropdownMenu(
                                        expanded = addDerivativeExpanded,
                                        onDismissRequest = { addDerivativeExpanded = false }
                                    ) {
                                        eligibleParents.filter { it.parentProductCode != product.code }.forEach { candidate ->
                                            DropdownMenuItem(
                                                text = { Text("#${candidate.code} ${candidate.name}") },
                                                onClick = {
                                                    onLinkDerivative(candidate.code, product.code)
                                                    addDerivativeExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (currentDerivatives.isEmpty()) {
                                Text(
                                    text = "No hay derivadas vinculadas aún. Puedes vincular Bistec, Molida, Maciza, Pastor, Árabe, etc.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    currentDerivatives.forEach { deriv ->
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "#${deriv.code} ${deriv.name}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Desvincular",
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable {
                                                            onLinkDerivative(deriv.code, null)
                                                        },
                                                    tint = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "⚠️ $msg", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val code = codeText.toIntOrNull()
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val weightBox = weightPerBoxText.toDoubleOrNull() ?: 0.0
                    val unitsBox = unitsPerBoxText.toIntOrNull() ?: 1
                    val stockUnits = currentStockUnitsText.toDoubleOrNull() ?: 0.0
                    val maxStock = if (maxStockText.isNotBlank()) {
                        maxStockText.toDoubleOrNull() ?: 0.0
                    } else if (stockUnits > 0 && weightBox > 0) {
                        stockUnits * weightBox
                    } else 20.0

                    if (code == null || code <= 0) {
                        errorMessage = "Ingresa un código numérico válido"
                        return@Button
                    }
                    if (nameText.isBlank()) {
                        errorMessage = "Ingresa el nombre del producto"
                        return@Button
                    }

                    val updatedProduct = ProductEntity(
                        code = code,
                        localCode = product?.localCode ?: code,
                        name = nameText.trim(),
                        synonyms = synonymsText.trim(),
                        price = price,
                        maxStock = maxStock,
                        category = selectedCategory,
                        imageUri = product?.imageUri,
                        orderUnitType = selectedUnitType,
                        weightPerBoxKg = weightBox,
                        unitsPerBox = unitsBox,
                        parentProductCode = selectedParentCode,
                        currentStockUnits = stockUnits
                    )
                    onSave(updatedProduct)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NoirCream, contentColor = NoirOnCream)
            ) {
                Text("Guardar Producto", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = NoirTextSecondary)
            }
        }
    )
}
