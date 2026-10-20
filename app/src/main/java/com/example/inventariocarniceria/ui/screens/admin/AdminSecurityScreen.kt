package com.example.inventariocarniceria.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.ui.components.SgniaCerhanGlobalSummaryCard
import com.example.inventariocarniceria.ui.components.SgniaCerhanGreetingHeader
import com.example.inventariocarniceria.ui.components.SgniaCerhanNumberedCard
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

    val matteBg = Color(0xFF0F0E0D)
    val cardBg = Color(0xFF181715)
    val cardBorder = Color(0xFF282623)
    val creamPrimary = Color(0xFFF3EFE6)
    val creamWarm = Color(0xFFEEDDC8)
    val textMuted = Color(0xFF8C857B)
    val darkText = Color(0xFF141312)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(matteBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SgniaCerhanGreetingHeader(
            userName = "Seguridad y PIN",
            subtitle = "Bóveda de Credenciales del Administrador SGNIA"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SgniaCerhanNumberedCard(
                modifier = Modifier.weight(1f),
                number = "01.",
                title = "PIN Maestro",
                subtitle = "Protección de Consola Dueño",
                icon = Icons.Default.AdminPanelSettings,
                showWaveChart = true
            )
            SgniaCerhanNumberedCard(
                modifier = Modifier.weight(1f),
                number = "02.",
                title = "Cifrado Local",
                subtitle = "Acceso blindado por rol",
                icon = Icons.Default.Security,
                showWaveChart = false
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "03. Actualizar PIN / Contraseña del Dueño",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = creamPrimary
            )

            OutlinedTextField(
                value = currentPinInput,
                onValueChange = { currentPinInput = it },
                label = { Text("Contraseña / PIN Actual") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = creamWarm) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = creamPrimary,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = creamPrimary,
                    unfocusedTextColor = creamPrimary,
                    focusedLabelColor = creamWarm,
                    unfocusedLabelColor = textMuted
                )
            )

            OutlinedTextField(
                value = newPinInput,
                onValueChange = { newPinInput = it },
                label = { Text("Nueva Contraseña / PIN") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = creamPrimary) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = creamPrimary,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = creamPrimary,
                    unfocusedTextColor = creamPrimary,
                    focusedLabelColor = creamWarm,
                    unfocusedLabelColor = textMuted
                )
            )

            OutlinedTextField(
                value = confirmPinInput,
                onValueChange = { confirmPinInput = it },
                label = { Text("Confirmar Nueva Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = creamPrimary) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = creamPrimary,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = creamPrimary,
                    unfocusedTextColor = creamPrimary,
                    focusedLabelColor = creamWarm,
                    unfocusedLabelColor = textMuted
                )
            )

            errorMessage?.let { msg ->
                Text(text = msg, color = Color(0xFFE57373), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            successMessage?.let { msg ->
                Text(text = msg, color = creamWarm, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(4.dp))

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
                    successMessage = "Contraseña de Dueño actualizada a '$newPinInput'."
                    currentPinInput = ""
                    newPinInput = ""
                    confirmPinInput = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = creamPrimary, contentColor = darkText)
            ) {
                Text("GUARDAR NUEVA CONTRASEÑA ADMIN", fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }

        SgniaCerhanGlobalSummaryCard(
            metric1Label = "Nivel de Acceso",
            metric1Value = "Dueño / Admin",
            metric1Sub = "Control total de sucursales",
            metric2Label = "Estado de Bóveda",
            metric2Value = "Activa",
            metric2Sub = "PIN de 4 dígitos habilitado",
            barValues = listOf(0.80f, 0.85f, 0.90f, 0.95f, 0.88f, 0.92f, 0.96f, 1.0f)
        )
    }
}
