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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
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
import com.example.inventariocarniceria.ui.components.SgniaCerhanGreetingHeader
import com.example.inventariocarniceria.ui.components.SgniaCerhanNumberedCard
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

@Composable
fun AdminSuppliersScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val suppliers by viewModel.allSuppliers.collectAsState()
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
                    Icon(Icons.Default.Add, contentDescription = "Agregar Proveedor")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NUEVO PROVEEDOR", fontWeight = FontWeight.Black, fontSize = 12.sp)
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
                userName = "Proveedores y Rastro",
                subtitle = "Directorio de Frigoríficos, Carnes y Embutidos"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SgniaCerhanNumberedCard(
                    modifier = Modifier.weight(1f),
                    number = "01.",
                    title = "Proveedores",
                    subtitle = "${suppliers.size} empresas activas",
                    icon = Icons.Default.LocalShipping,
                    showWaveChart = true
                )
                SgniaCerhanNumberedCard(
                    modifier = Modifier.weight(1f),
                    number = "02.",
                    title = "Abastecimiento",
                    subtitle = "Res, Cerdo, Pollo y Lácteos",
                    icon = Icons.Default.Inventory2,
                    showWaveChart = false
                )
            }

            if (suppliers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aún no hay proveedores registrados.", color = textMuted)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suppliers) { supplier ->
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
                                Text(supplier.name, fontWeight = FontWeight.Black, fontSize = 15.sp, color = creamPrimary)
                                Text("Contacto: ${supplier.contactName} (${supplier.phone})", fontSize = 12.sp, color = textMuted)
                                Text("Líneas: ${supplier.suppliedCategories}", fontSize = 12.sp, color = creamWarm, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { viewModel.deleteSupplier(supplier) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Borrar", tint = textMuted)
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
            containerColor = cardBg,
            title = { Text("REGISTRAR PROVEEDOR", fontWeight = FontWeight.Black, color = creamPrimary) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = creamPrimary, contentColor = darkText)
                ) { Text("Guardar Proveedor", fontWeight = FontWeight.Black) }
            },
            dismissButton = { TextButton(onClick = { isAddOpen = false }) { Text("Cancelar", color = creamWarm) } }
        )
    }
}
