package com.example.inventariocarniceria.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.BranchEntity
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

@Composable
fun AdminBranchesScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val branches by viewModel.allBranches.collectAsState()
    var editingBranch by remember { mutableStateOf<BranchEntity?>(null) }
    var isAddOpen by remember { mutableStateOf(false) }

    val greenSuccess = Color(0xFF2E7D32)
    val darkRed = Color(0xFFB71C1C)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddOpen = true },
                containerColor = greenSuccess,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Sucursal")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = greenSuccess)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SUCURSALES Y USUARIOS ENCARGADOS",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )
            }
            Text(
                text = "Crea sucursales, asigna encargados y gestiona sus contraseñas de acceso",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (branches.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay sucursales activas. Crea una nueva presionando '+'.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(branches) { branch ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(branch.branchName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Encargado: ${branch.managerName}", style = MaterialTheme.typography.bodySmall, color = greenSuccess, fontWeight = FontWeight.Bold)
                                    Text("Contraseña Acceso: '${branch.password}'", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Row {
                                    IconButton(onClick = { editingBranch = branch }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteBranch(branch) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Borrar", tint = darkRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Crear Nueva Sucursal
    if (isAddOpen) {
        var branchName by remember { mutableStateOf("") }
        var managerName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("1234") }

        AlertDialog(
            onDismissRequest = { isAddOpen = false },
            title = { Text("✨ CREAR SUCURSAL Y USUARIO", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = branchName, onValueChange = { branchName = it }, label = { Text("Nombre de Sucursal (ej. Sucursal Sur)") }, singleLine = true)
                    OutlinedTextField(value = managerName, onValueChange = { managerName = it }, label = { Text("Nombre del Encargado (ej. Pedro M.)") }, singleLine = true)
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña de Acceso para el Encargado") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono de la Tienda") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (branchName.isNotBlank() && managerName.isNotBlank() && password.isNotBlank()) {
                            viewModel.addBranch(branchName.trim(), managerName.trim(), phone.trim(), password.trim())
                            isAddOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = greenSuccess)
                ) { Text("Crear Sucursal") }
            },
            dismissButton = { TextButton(onClick = { isAddOpen = false }) { Text("Cancelar") } }
        )
    }

    // Modal Editar Sucursal Existente
    editingBranch?.let { branch ->
        var branchName by remember { mutableStateOf(branch.branchName) }
        var managerName by remember { mutableStateOf(branch.managerName) }
        var phone by remember { mutableStateOf(branch.phone) }
        var password by remember { mutableStateOf(branch.password) }

        AlertDialog(
            onDismissRequest = { editingBranch = null },
            title = { Text("✏️ EDITAR SUCURSAL", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = branchName, onValueChange = { branchName = it }, label = { Text("Nombre de Sucursal") }, singleLine = true)
                    OutlinedTextField(value = managerName, onValueChange = { managerName = it }, label = { Text("Encargado Asignado") }, singleLine = true)
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña del Encargado") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (branchName.isNotBlank() && managerName.isNotBlank() && password.isNotBlank()) {
                            viewModel.updateBranch(
                                branch.copy(
                                    branchName = branchName.trim(),
                                    managerName = managerName.trim(),
                                    phone = phone.trim(),
                                    password = password.trim()
                                )
                            )
                            editingBranch = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Guardar Cambios") }
            },
            dismissButton = { TextButton(onClick = { editingBranch = null }) { Text("Cancelar") } }
        )
    }
}
