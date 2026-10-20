package com.example.inventariocarniceria.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.R
import com.example.inventariocarniceria.data.local.BranchEntity
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: InventoryViewModel,
    onLoginSuccess: () -> Unit
) {
    val branches by viewModel.allBranches.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedBranchEntity by remember(branches) { mutableStateOf(branches.firstOrNull()) }
    var selectedRole by remember { mutableStateOf("ENCARGADO") } // ENCARGADO or ADMIN
    var passInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var branchExpanded by remember { mutableStateOf(false) }

    val darkRed = Color(0xFFB71C1C)
    val greenSuccess = Color(0xFF2E7D32)
    val goldAccent = Color(0xFFFFD549)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LOGO & TITLE
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo Carnicería Sgnia",
                    modifier = Modifier.size(80.dp)
                )

                Text(
                    text = "CARNICERÍA SGNIA",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Sistema Móvil Multi-Sucursal, Pesaje BT & Cremería",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // ROLE SELECTOR SEGMENTED BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            selectedRole = "ENCARGADO"
                            passInput = ""
                            errorMessage = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == "ENCARGADO") greenSuccess else Color.Gray.copy(alpha = 0.3f),
                            contentColor = if (selectedRole == "ENCARGADO") Color.White else Color.Gray
                        )
                    ) {
                        Text("Encargado Sucursal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            selectedRole = "ADMIN"
                            passInput = ""
                            errorMessage = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == "ADMIN") darkRed else Color.Gray.copy(alpha = 0.3f),
                            contentColor = if (selectedRole == "ADMIN") Color.White else Color.Gray
                        )
                    ) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dueño / Admin", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // IF ENCARGADO -> SUCURSAL DROPDOWN + SUCURSAL PASSWORD
                if (selectedRole == "ENCARGADO") {
                    ExposedDropdownMenuBox(
                        expanded = branchExpanded,
                        onExpandedChange = { branchExpanded = !branchExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedBranchEntity?.let { "${it.branchName} (${it.managerName})" } ?: "Seleccionar Sucursal",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sucursal a Operar") },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = branchExpanded,
                            onDismissRequest = { branchExpanded = false }
                        ) {
                            if (branches.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No hay sucursales creadas aún. Entra como Admin para crear.") },
                                    onClick = { branchExpanded = false }
                                )
                            } else {
                                branches.forEach { branch ->
                                    DropdownMenuItem(
                                        text = { Text("${branch.branchName} - ${branch.managerName}") },
                                        onClick = {
                                            selectedBranchEntity = branch
                                            branchExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = passInput,
                        onValueChange = { passInput = it },
                        label = { Text("Contraseña de Sucursal") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    // IF ADMIN -> ADMIN PIN FIELD
                    OutlinedTextField(
                        value = passInput,
                        onValueChange = { passInput = it },
                        label = { Text("Contraseña de Administrador (Defecto: ${uiState.adminPin})") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = goldAccent) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                errorMessage?.let { msg ->
                    Text(text = "⚠️ $msg", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // LOGIN ACTION BUTTON
                Button(
                    onClick = {
                        if (selectedRole == "ADMIN") {
                            if (passInput != uiState.adminPin) {
                                errorMessage = "Contraseña de Admin incorrecta."
                                return@Button
                            }
                            viewModel.loginUser("Administración General Matriz", "ADMIN")
                            onLoginSuccess()
                        } else {
                            val branch = selectedBranchEntity
                            if (branch == null) {
                                errorMessage = "Selecciona una sucursal."
                                return@Button
                            }
                            if (passInput != branch.password) {
                                errorMessage = "Contraseña incorrecta para ${branch.branchName}."
                                return@Button
                            }
                            viewModel.loginUser(branch.branchName, "ENCARGADO")
                            onLoginSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedRole == "ADMIN") darkRed else greenSuccess
                    )
                ) {
                    Text(
                        text = if (selectedRole == "ADMIN") "ACCEDER A PANEL DE ADMINISTRADOR" else "INGRESAR A SUCURSAL",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
