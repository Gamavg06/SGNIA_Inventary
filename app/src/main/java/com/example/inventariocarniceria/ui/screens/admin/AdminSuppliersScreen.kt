package com.example.inventariocarniceria.ui.screens.admin

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
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
import com.example.inventariocarniceria.data.local.SupplierEntity
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

@Composable
fun AdminSuppliersScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val suppliers by viewModel.allSuppliers.collectAsState()
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
                Icon(Icons.Default.Add, contentDescription = "Agregar Proveedor")
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
                Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = greenSuccess)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GESTIÓN DE PROVEEDORES",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )
            }
            Text(
                text = "Catálogo de proveedores de carne, embutidos y materia prima",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (suppliers.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aún no hay proveedores registrados.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suppliers) { supplier ->
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
                                    Text(supplier.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Contacto: ${supplier.contactName} (${supplier.phone})", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text("Líneas: ${supplier.suppliedCategories}", style = MaterialTheme.typography.bodySmall, color = greenSuccess, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = { viewModel.deleteSupplier(supplier) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Borrar", tint = darkRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddOpen) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var contact by remember { mutableStateOf("") }
        var categories by remember { mutableStateOf("Res, Cerdo") }

        AlertDialog(
            onDismissRequest = { isAddOpen = false },
            title = { Text("✨ REGISTRAR PROVEEDOR", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre Proveedor / Frigorífico") }, singleLine = true)
                    OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Nombre de Contacto") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono de Pedidos") }, singleLine = true)
                    OutlinedTextField(value = categories, onValueChange = { categories = it }, label = { Text("Categorías que surte (ej. Res, Cerdo)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addSupplier(name.trim(), phone.trim(), contact.trim(), categories.trim())
                            isAddOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = greenSuccess)
                ) { Text("Guardar Proveedor") }
            },
            dismissButton = { TextButton(onClick = { isAddOpen = false }) { Text("Cancelar") } }
        )
    }
}
