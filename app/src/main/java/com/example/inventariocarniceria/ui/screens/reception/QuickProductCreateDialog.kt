package com.example.inventariocarniceria.ui.screens.reception

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.ProductEntity

private val NoirSurface = Color(0xFF181715)
private val NoirSurfaceSunken = Color(0xFF121110)
private val NoirHairline = Color(0xFF2A2723)
private val NoirCream = Color(0xFFEEDDC8)
private val NoirOnCream = Color(0xFF141312)
private val NoirTextPrimary = Color(0xFFF3EFE6)
private val NoirTextSecondary = Color(0xFFA8A196)
private val NoirEmerald = Color(0xFF4ADE80)
private val NoirDanger = Color(0xFFE07A6F)

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
        containerColor = NoirSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    text = "RECEPCIÓN RÁPIDA",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NoirCream
                )
                Text(
                    text = "Crear Producto al Vuelo",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = NoirTextPrimary
                )
                Text(
                    text = "Código no encontrado. Regístralo en 1 paso para continuar la recepción de carne.",
                    fontSize = 11.sp,
                    color = NoirTextSecondary
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
                    label = { Text("Código (1-107)", color = NoirTextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedColors()
                )

                // Nombre
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Nombre del Corte / Producto", color = NoirTextSecondary) },
                    placeholder = { Text("Ej. Cecina de Res Yecapixtla", color = NoirTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedColors()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Precio por kg
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Precio / kg ($)", color = NoirTextSecondary) },
                        placeholder = { Text("195.00", color = NoirTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedColors()
                    )

                    // Kilos Iniciales que entran
                    OutlinedTextField(
                        value = initialWeightText,
                        onValueChange = { initialWeightText = it },
                        label = { Text("Kilos Entrada", color = NoirTextSecondary) },
                        placeholder = { Text("15.500", color = NoirTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedColors()
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
                        label = { Text("Categoría", color = NoirTextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedColors()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier
                            .background(NoirSurface)
                            .border(1.dp, NoirHairline, RoundedCornerShape(12.dp))
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, fontWeight = FontWeight.Bold, color = NoirTextPrimary) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                },
                                colors = MenuDefaults.itemColors(textColor = NoirTextPrimary)
                            )
                        }
                    }
                }

                // Notas Proveedor / Lote
                OutlinedTextField(
                    value = supplierNotesText,
                    onValueChange = { supplierNotesText = it },
                    label = { Text("Proveedor / Lote (Opcional)", color = NoirTextSecondary) },
                    placeholder = { Text("Ej. Camión Proveedor SGNIA", color = NoirTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedColors()
                )

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "⚠️ $msg", color = NoirDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                colors = ButtonDefaults.buttonColors(containerColor = NoirCream, contentColor = NoirOnCream)
            ) {
                Text("Crear y Acreditar Entrada", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = NoirTextSecondary)
            }
        }
    )
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NoirCream,
    unfocusedBorderColor = NoirHairline,
    focusedContainerColor = NoirSurfaceSunken,
    unfocusedContainerColor = NoirSurfaceSunken,
    focusedTextColor = NoirTextPrimary,
    unfocusedTextColor = NoirTextPrimary
)
