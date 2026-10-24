package com.example.inventariocarniceria.ui.screens.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
        containerColor = NoirSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    text = "CATÁLOGO DE SUCURSAL",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NoirCream
                )
                Text(
                    text = "Cambiar Código Local PLU",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = NoirTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Corte: [PLU #${product.localCode}] ${product.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NoirTextPrimary
                )
                Text(
                    text = "Asigna un número de tecla rápida exclusivo para acoplarlo al teclado de tu báscula en esta tienda:",
                    fontSize = 11.sp,
                    color = NoirTextSecondary
                )

                OutlinedTextField(
                    value = newCodeText,
                    onValueChange = { newCodeText = it.filter { char -> char.isDigit() } },
                    label = { Text("Nuevo Código Local (PLU)", color = NoirTextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoirCream,
                        unfocusedBorderColor = NoirHairline,
                        focusedContainerColor = NoirSurfaceSunken,
                        unfocusedContainerColor = NoirSurfaceSunken,
                        focusedTextColor = NoirTextPrimary,
                        unfocusedTextColor = NoirTextPrimary
                    )
                )

                errorMessage?.let { msg ->
                    Text(text = "⚠️ $msg", color = NoirDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                colors = ButtonDefaults.buttonColors(containerColor = NoirCream, contentColor = NoirOnCream)
            ) {
                Text("Guardar Código Local", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = NoirTextSecondary)
            }
        }
    )
}
