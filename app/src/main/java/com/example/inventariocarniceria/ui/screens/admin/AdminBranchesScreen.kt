package com.example.inventariocarniceria.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.BranchEntity
import com.example.inventariocarniceria.ui.components.SgniaCerhanGreetingHeader
import com.example.inventariocarniceria.ui.components.SgniaCerhanNumberedCard
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

@Composable
fun AdminBranchesScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val branches by viewModel.allBranches.collectAsState()
    var editingBranch by remember { mutableStateOf<BranchEntity?>(null) }
    var isAddOpen by remember { mutableStateOf(false) }

    val matteBg = Color(0xFF0F0E0D)
    val cardBg = Color(0xFF181715)
    val cardBorder = Color(0xFF282623)
    val creamPrimary = Color(0xFFF3EFE6)
    val creamWarm = Color(0xFFEEDDC8)
    val textMuted = Color(0xFF8C857B)
    val darkText = Color(0xFF141312)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = matteBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddOpen = true },
                containerColor = creamPrimary,
                contentColor = darkText,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Sucursal")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NUEVA SUCURSAL", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(matteBg)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SgniaCerhanGreetingHeader(
                userName = "Sucursales y Tiendas",
                subtitle = "Gestión de Tiendas, Encargados y PIN de Mostrador"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SgniaCerhanNumberedCard(
                    modifier = Modifier.weight(1f),
                    number = "01.",
                    title = "Tiendas Activas",
                    subtitle = "${branches.size} sucursales registradas",
                    icon = Icons.Default.Storefront,
                    showWaveChart = true
                )
                SgniaCerhanNumberedCard(
                    modifier = Modifier.weight(1f),
                    number = "02.",
                    title = "Accesos PIN",
                    subtitle = "${branches.size} encargados con clave",
                    icon = Icons.Default.Lock,
                    showWaveChart = false
                )
            }

            if (branches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay sucursales activas. Crea una nueva presionando 'NUEVA SUCURSAL'.",
                        color = textMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(branches) { branch ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(branch.branchName, fontWeight = FontWeight.Black, fontSize = 15.sp, color = creamPrimary)
                                Text("Encargado: ${branch.managerName}", fontSize = 12.sp, color = creamWarm, fontWeight = FontWeight.Bold)
                                Text("PIN / Contraseña: '${branch.password}' • Tel: ${branch.phone}", fontSize = 11.sp, color = textMuted)
                            }
                            Row {
                                IconButton(onClick = { editingBranch = branch }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = creamWarm)
                                }
                                IconButton(onClick = { viewModel.deleteBranch(branch) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Borrar", tint = textMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddOpen) {
        var branchName by remember { mutableStateOf("") }
        var managerName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("1234") }

        AlertDialog(
            onDismissRequest = { isAddOpen = false },
            containerColor = cardBg,
            title = { Text("CREAR SUCURSAL Y USUARIO", fontWeight = FontWeight.Black, color = creamPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = branchName, onValueChange = { branchName = it }, label = { Text("Nombre de Sucursal (ej. Sucursal Sur)") }, singleLine = true)
                    OutlinedTextField(value = managerName, onValueChange = { managerName = it }, label = { Text("Nombre del Encargado (ej. Pedro M.)") }, singleLine = true)
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("PIN / Contraseña de Acceso") }, singleLine = true)
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
                    colors = ButtonDefaults.buttonColors(containerColor = creamPrimary, contentColor = darkText)
                ) { Text("Crear Sucursal", fontWeight = FontWeight.Black) }
            },
            dismissButton = { TextButton(onClick = { isAddOpen = false }) { Text("Cancelar", color = creamWarm) } }
        )
    }

    editingBranch?.let { branch ->
        var branchName by remember { mutableStateOf(branch.branchName) }
        var managerName by remember { mutableStateOf(branch.managerName) }
        var phone by remember { mutableStateOf(branch.phone) }
        var password by remember { mutableStateOf(branch.password) }

        AlertDialog(
            onDismissRequest = { editingBranch = null },
            containerColor = cardBg,
            title = { Text("EDITAR SUCURSAL", fontWeight = FontWeight.Black, color = creamPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = branchName, onValueChange = { branchName = it }, label = { Text("Nombre de Sucursal") }, singleLine = true)
                    OutlinedTextField(value = managerName, onValueChange = { managerName = it }, label = { Text("Encargado Asignado") }, singleLine = true)
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("PIN / Contraseña del Encargado") }, singleLine = true)
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
                    colors = ButtonDefaults.buttonColors(containerColor = creamPrimary, contentColor = darkText)
                ) { Text("Guardar Cambios", fontWeight = FontWeight.Black) }
            },
            dismissButton = { TextButton(onClick = { editingBranch = null }) { Text("Cancelar", color = creamWarm) } }
        )
    }
}
