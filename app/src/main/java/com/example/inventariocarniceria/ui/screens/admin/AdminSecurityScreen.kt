package com.example.inventariocarniceria.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.ui.design.BentoCard
import com.example.inventariocarniceria.ui.design.BigMetric
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirPrimaryButton
import com.example.inventariocarniceria.ui.design.NoirRadius
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NoirColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = NoirSpace.md, vertical = NoirSpace.sm),
        verticalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
    ) {
        // ENCABEZADO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                NoirLabel("DIRECCIÓN GENERAL · CONTROL DE ACCESO", color = NoirColors.Gold)
                Text(
                    text = "Seguridad y PIN",
                    style = NoirType.display.copy(fontSize = 24.sp),
                    color = NoirColors.TextPrimary
                )
                Text(
                    text = "Bóveda de credenciales del Administrador SGNIA",
                    style = NoirType.bodySmall,
                    color = NoirColors.TextMuted
                )
            }
        }

        // BENTO HERO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
        ) {
            BentoCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(NoirSpace.md)
            ) {
                BigMetric(
                    value = "ADMIN",
                    label = "ROL ACTIVO",
                    unit = "MASTER",
                    style = NoirType.metricL,
                    valueColor = NoirColors.Cream
                )
                StatusPill("NIVEL EJECUTIVO", tone = PillTone.Positive)
            }

            BentoCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(NoirSpace.md)
            ) {
                BigMetric(
                    value = "LOCAL",
                    label = "ALMACENAMIENTO",
                    unit = "CIFRADO",
                    style = NoirType.metricL,
                    valueColor = NoirColors.TextPrimary
                )
                StatusPill("BÓVEDA PROTEGIDA", tone = PillTone.Gold)
            }
        }

        // FORMULARIO BENTO PARA ACTUALIZACIÓN DE PIN
        BentoCard(
            contentPadding = PaddingValues(NoirSpace.lg),
            verticalArrangement = Arrangement.spacedBy(NoirSpace.sm)
        ) {
            NoirLabel("ACTUALIZAR PIN DE ACCESO MAESTRO", color = NoirColors.Cream)

            OutlinedTextField(
                value = currentPinInput,
                onValueChange = { currentPinInput = it },
                label = { Text("PIN Actual", color = NoirColors.TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NoirColors.TextMuted) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(NoirRadius.sm),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NoirColors.Cream,
                    unfocusedBorderColor = NoirColors.Hairline,
                    focusedTextColor = NoirColors.TextPrimary,
                    unfocusedTextColor = NoirColors.TextPrimary
                )
            )

            OutlinedTextField(
                value = newPinInput,
                onValueChange = { newPinInput = it },
                label = { Text("Nuevo PIN", color = NoirColors.TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NoirColors.Cream) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(NoirRadius.sm),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NoirColors.Cream,
                    unfocusedBorderColor = NoirColors.Hairline,
                    focusedTextColor = NoirColors.TextPrimary,
                    unfocusedTextColor = NoirColors.TextPrimary
                )
            )

            OutlinedTextField(
                value = confirmPinInput,
                onValueChange = { confirmPinInput = it },
                label = { Text("Confirmar Nuevo PIN", color = NoirColors.TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NoirColors.Cream) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(NoirRadius.sm),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NoirColors.Cream,
                    unfocusedBorderColor = NoirColors.Hairline,
                    focusedTextColor = NoirColors.TextPrimary,
                    unfocusedTextColor = NoirColors.TextPrimary
                )
            )

            errorMessage?.let { msg ->
                Text(text = msg, color = NoirColors.Danger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            successMessage?.let { msg ->
                Text(text = msg, color = NoirColors.Emerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            NoirPrimaryButton(
                text = "GUARDAR NUEVO PIN MAESTRO",
                icon = Icons.Default.Save,
                onClick = {
                    errorMessage = null
                    successMessage = null

                    if (currentPinInput != uiState.adminPin) {
                        errorMessage = "El PIN actual es incorrecto."
                        return@NoirPrimaryButton
                    }
                    if (newPinInput.isBlank()) {
                        errorMessage = "Ingresa un PIN nuevo válido."
                        return@NoirPrimaryButton
                    }
                    if (newPinInput != confirmPinInput) {
                        errorMessage = "Los PINs nuevos no coinciden."
                        return@NoirPrimaryButton
                    }

                    viewModel.updateAdminPin(newPinInput.trim())
                    successMessage = "PIN de Dueño actualizado con éxito."
                    currentPinInput = ""
                    newPinInput = ""
                    confirmPinInput = ""
                }
            )
        }
    }
}
