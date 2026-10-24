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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.ui.design.BentoCard
import com.example.inventariocarniceria.ui.design.BigMetric
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirRadius
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

@Composable
fun AdminSuppliersScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val suppliers by viewModel.allSuppliers.collectAsState()
    var isAddOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NoirColors.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddOpen = true },
                containerColor = NoirColors.Cream,
                contentColor = NoirColors.OnCream,
                shape = RoundedCornerShape(NoirRadius.md)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = NoirSpace.md, vertical = NoirSpace.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Proveedor", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NUEVO PROVEEDOR", style = NoirType.label.copy(color = NoirColors.OnCream, fontWeight = FontWeight.Bold))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NoirColors.Background)
                .padding(padding)
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
                    NoirLabel("DIRECCIÓN GENERAL · CADENA DE SUMINISTRO", color = NoirColors.Gold)
                    Text(
                        text = "Proveedores",
                        style = NoirType.display.copy(fontSize = 24.sp),
                        color = NoirColors.TextPrimary
                    )
                    Text(
                        text = "Directorio de frigoríficos, rastros y distribuidores cárnicos",
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
                        value = "${suppliers.size}",
                        label = "EMPRESAS",
                        unit = "ACTIVAS",
                        style = NoirType.metricL,
                        valueColor = NoirColors.Cream
                    )
                    StatusPill("DIRECTORIO MATRIZ", tone = PillTone.Positive)
                }

                BentoCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(NoirSpace.md)
                ) {
                    BigMetric(
                        value = "Rastro",
                        label = "ABASTECIMIENTO",
                        unit = "MAYOREO",
                        style = NoirType.metricL,
                        valueColor = NoirColors.TextPrimary
                    )
                    StatusPill("RES · CERDO · POLLO", tone = PillTone.Neutral)
                }
            }

            // LISTA DE PROVEEDORES
            if (suppliers.isEmpty()) {
                BentoCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(NoirSpace.xl),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Aún no hay proveedores registrados", style = NoirType.title, color = NoirColors.TextPrimary)
                            Text("Agrega introductores y frigoríficos con '+ NUEVO PROVEEDOR'.", style = NoirType.bodySmall, color = NoirColors.TextMuted)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(NoirSpace.xs)
                ) {
                    items(suppliers) { supplier ->
                        BentoCard(
                            contentPadding = PaddingValues(horizontal = NoirSpace.md, vertical = NoirSpace.sm),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(supplier.name, style = NoirType.title.copy(fontSize = 15.sp), color = NoirColors.TextPrimary)
                                        Spacer(Modifier.width(8.dp))
                                        StatusPill("SURTIDOR", tone = PillTone.Neutral, showDot = false)
                                    }

                                    Text(
                                        "Contacto: ${supplier.contactName} (${supplier.phone})",
                                        style = NoirType.bodySmall,
                                        color = NoirColors.TextSecondary
                                    )

                                    Text(
                                        "Líneas: ${supplier.suppliedCategories}",
                                        style = NoirType.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = NoirColors.Cream
                                    )
                                }

                                IconButton(onClick = { viewModel.deleteSupplier(supplier) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Borrar", tint = NoirColors.Danger)
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
            containerColor = NoirColors.Surface,
            shape = RoundedCornerShape(NoirRadius.lg),
            title = { Text("REGISTRAR PROVEEDOR", style = NoirType.title, color = NoirColors.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre Proveedor / Frigorífico", color = NoirColors.TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoirColors.Cream,
                            unfocusedBorderColor = NoirColors.Hairline,
                            focusedTextColor = NoirColors.TextPrimary,
                            unfocusedTextColor = NoirColors.TextPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = contact,
                        onValueChange = { contact = it },
                        label = { Text("Nombre de Contacto", color = NoirColors.TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoirColors.Cream,
                            unfocusedBorderColor = NoirColors.Hairline,
                            focusedTextColor = NoirColors.TextPrimary,
                            unfocusedTextColor = NoirColors.TextPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono de Pedidos", color = NoirColors.TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoirColors.Cream,
                            unfocusedBorderColor = NoirColors.Hairline,
                            focusedTextColor = NoirColors.TextPrimary,
                            unfocusedTextColor = NoirColors.TextPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = categories,
                        onValueChange = { categories = it },
                        label = { Text("Categorías (ej. Res, Cerdo, Pollo)", color = NoirColors.TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoirColors.Cream,
                            unfocusedBorderColor = NoirColors.Hairline,
                            focusedTextColor = NoirColors.TextPrimary,
                            unfocusedTextColor = NoirColors.TextPrimary
                        ),
                        singleLine = true
                    )
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
                    colors = ButtonDefaults.buttonColors(containerColor = NoirColors.Cream, contentColor = NoirColors.OnCream),
                    shape = RoundedCornerShape(NoirRadius.md)
                ) { Text("Guardar Proveedor", style = NoirType.button) }
            },
            dismissButton = {
                TextButton(onClick = { isAddOpen = false }) { Text("Cancelar", color = NoirColors.Cream) }
            }
        )
    }
}
