package com.example.inventariocarniceria.ui.screens.reception

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.inventariocarniceria.data.local.ProductEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickProductCreateDialog(
    suggestedCode: Int?,
    initialWeightFromScale: Double,
    onDismiss: () -> Unit,
    onSaveAlVuelo: (product: ProductEntity, initialKilos: Double, supplierNotes: String?) -> Unit
) {
    var codeText by remember { mutableStateOf(suggestedCode?.toString() ?: "") }
    var nameText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var initialWeightText by remember { mutableStateOf(if (initialWeightFromScale > 0) String.format(java.util.Locale.US, "%.3f", initialWeightFromScale) else "") }
    var supplierNotesText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Res") }

    val categories = listOf("Res", "Cerdo", "Pollo", "Embutidos", "Otros")
    var categoryExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "✨ CREAR PRODUCTO AL VUELO",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "Código no encontrado. Regístralo en 1 paso para continuar la recepción.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
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
                // Código
                OutlinedTextField(
                    value = codeText,
                    onValueChange = { codeText = it.filter { char -> char.isDigit() } },
                    label = { Text("Código (1-107)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Nombre
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Nombre del Corte / Producto") },
                    placeholder = { Text("Ej. Cecina de Res Yecapixtla") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Precio por kg
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Precio / kg ($)") },
                        placeholder = { Text("195.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Kilos Iniciales que entran
                    OutlinedTextField(
                        value = initialWeightText,
                        onValueChange = { initialWeightText = it },
                        label = { Text("Kilos Entrada") },
                        placeholder = { Text("15.500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Categoría
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
                            .fillMaxWidth(),
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

                // Notas Proveedor / Lote
                OutlinedTextField(
                    value = supplierNotesText,
                    onValueChange = { supplierNotesText = it },
                    label = { Text("Proveedor / Lote (Opcional)") },
                    placeholder = { Text("Ej. Camión Proveedor SGNIA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

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
                    val kilos = initialWeightText.toDoubleOrNull() ?: 0.0

                    if (code == null || code <= 0) {
                        errorMessage = "Ingresa un código numérico válido"
                        return@Button
                    }
                    if (nameText.isBlank()) {
                        errorMessage = "Ingresa el nombre del corte"
                        return@Button
                    }

                    val newProduct = ProductEntity(
                        code = code,
                        name = nameText.trim(),
                        price = price,
                        maxStock = (kilos * 1.5).coerceAtLeast(15.0),
                        category = selectedCategory
                    )
                    onSaveAlVuelo(newProduct, kilos, supplierNotesText.ifBlank { null })
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Crear y Acreditar Entrada")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
