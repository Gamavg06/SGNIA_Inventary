package com.example.inventariocarniceria.ui.screens.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

@Composable
fun ChangeLocalCodeDialog(
    product: ProductEntity,
    allProducts: List<ProductEntity>,
    onDismiss: () -> Unit,
    onConfirmChangeCode: (product: ProductEntity, newLocalCode: Int) -> Unit
) {
    var newCodeText by remember { mutableStateOf(product.localCode.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "✏️ CAMBIAR CÓDIGO LOCAL EN SUCURSAL",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Producto: [${product.localCode}] ${product.name}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Asigna un nuevo número de código exclusivo para esta sucursal (ej. para acoplarlo al teclado de tu báscula):",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = newCodeText,
                    onValueChange = { newCodeText = it.filter { char -> char.isDigit() } },
                    label = { Text("Nuevo Código Local") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                errorMessage?.let { msg ->
                    Text(text = "⚠️ $msg", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val code = newCodeText.toIntOrNull()
                    if (code == null || code <= 0) {
                        errorMessage = "Ingresa un número de código válido"
                        return@Button
                    }
                    val duplicate = allProducts.find { it.localCode == code && it.code != product.code }
                    if (duplicate != null) {
                        errorMessage = "El código #$code ya está en uso por '${duplicate.name}'"
                        return@Button
                    }
                    onConfirmChangeCode(product, code)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Guardar Código Local")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
