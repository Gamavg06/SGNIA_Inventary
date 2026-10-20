package com.example.inventariocarniceria.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

@Composable
fun AdminSecurityScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val darkRed = Color(0xFFB71C1C)
    val greenSuccess = Color(0xFF2E7D32)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = darkRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SEGURIDAD Y CAMBIO DE CONTRASEÑA ADMIN",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
            )
        }

        Text(
            text = "Modifica la contraseña del administrador del sistema de Carnicería SGNIA.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = currentPinInput,
                    onValueChange = { currentPinInput = it },
                    label = { Text("Contraseña / PIN Actual") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = { newPinInput = it },
                    label = { Text("Nueva Contraseña / PIN") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = greenSuccess) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPinInput,
                    onValueChange = { confirmPinInput = it },
                    label = { Text("Confirmar Nueva Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = greenSuccess) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { msg ->
                    Text(text = "⚠️ $msg", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                successMessage?.let { msg ->
                    Text(text = "✅ $msg", color = greenSuccess, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        errorMessage = null
                        successMessage = null

                        if (currentPinInput != uiState.adminPin) {
                            errorMessage = "La contraseña actual es incorrecta."
                            return@Button
                        }
                        if (newPinInput.isBlank()) {
                            errorMessage = "Ingresa una contraseña nueva válida."
                            return@Button
                        }
                        if (newPinInput != confirmPinInput) {
                            errorMessage = "Las contraseñas nuevas no coinciden."
                            return@Button
                        }

                        viewModel.updateAdminPin(newPinInput.trim())
                        successMessage = "¡Contraseña de Admin cambiada exitosamente a '$newPinInput'!"
                        currentPinInput = ""
                        newPinInput = ""
                        confirmPinInput = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = darkRed)
                ) {
                    Text("GUARDAR NUEVA CONTRASEÑA ADMIN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
