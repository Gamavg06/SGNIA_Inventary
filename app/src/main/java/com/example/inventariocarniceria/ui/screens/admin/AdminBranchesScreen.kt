package com.example.inventariocarniceria.ui.screens.admin

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.licensing.LicenseManager
import com.example.inventariocarniceria.data.local.BranchEntity
import com.example.inventariocarniceria.data.sync.SupabaseSyncManager
import com.example.inventariocarniceria.ui.design.BentoCard
import com.example.inventariocarniceria.ui.design.BigMetric
import kotlinx.coroutines.launch
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirRadius
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

@Composable
fun AdminBranchesScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val branches by viewModel.allBranches.collectAsState()
    var editingBranch by remember { mutableStateOf<BranchEntity?>(null) }
    var isAddOpen by remember { mutableStateOf(false) }
    var showLimitReachedDialog by remember { mutableStateOf(false) }
    var revealedBranchPinIds by remember { mutableStateOf(setOf<Long>()) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val maxAllowedBranches = remember(branches.size) { LicenseManager.getMaxBranchesAllowed(context) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NoirColors.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (branches.size >= maxAllowedBranches) {
                        showLimitReachedDialog = true
                    } else {
                        isAddOpen = true
                    }
                },
                containerColor = NoirColors.Cream,
                contentColor = NoirColors.OnCream,
                shape = RoundedCornerShape(NoirRadius.md)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = NoirSpace.md, vertical = NoirSpace.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Sucursal", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NUEVA SUCURSAL", style = NoirType.label.copy(color = NoirColors.OnCream, fontWeight = FontWeight.Bold))
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
                    NoirLabel("DIRECCIÓN GENERAL · RED DE TIENDAS", color = NoirColors.Gold)
                    Text(
                        text = "Sucursales",
                        style = NoirType.display.copy(fontSize = 24.sp),
                        color = NoirColors.TextPrimary
                    )
                    Text(
                        text = "Gestión de puntos de venta, encargados y PIN de mostrador",
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
                        value = "${branches.size} / $maxAllowedBranches",
                        label = "SUCURSALES",
                        unit = if (branches.size >= maxAllowedBranches) "CUPO LLENO" else "DISPONIBLES",
                        style = NoirType.metricL,
                        valueColor = if (branches.size >= maxAllowedBranches) NoirColors.Gold else NoirColors.Cream
                    )
                    StatusPill(
                        text = if (branches.size >= maxAllowedBranches) "LÍMITE PLAN ($maxAllowedBranches)" else "RED ACTIVA",
                        tone = if (branches.size >= maxAllowedBranches) PillTone.Gold else PillTone.Positive
                    )
                }

                BentoCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(NoirSpace.md)
                ) {
                    BigMetric(
                        value = "${branches.size}",
                        label = "ENCARGADOS",
                        unit = "CON PIN",
                        style = NoirType.metricL,
                        valueColor = NoirColors.TextPrimary
                    )
                    StatusPill("SEGURIDAD ACTIVA", tone = PillTone.Gold)
                }
            }

            // LISTA DE SUCURSALES
            if (branches.isEmpty()) {
                BentoCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(NoirSpace.xl),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("No hay sucursales activas", style = NoirType.title, color = NoirColors.TextPrimary)
                            Text("Presiona '+ NUEVA SUCURSAL' para registrar una tienda.", style = NoirType.bodySmall, color = NoirColors.TextMuted)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(NoirSpace.xs)
                ) {
                    items(branches) { branch ->
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
                                        Text(branch.branchName, style = NoirType.title.copy(fontSize = 15.sp), color = NoirColors.TextPrimary)
                                        Spacer(Modifier.width(8.dp))
                                        StatusPill("ACTIVA", tone = PillTone.Positive, showDot = true)
                                    }

                                    Text(
                                        "Encargado: ${branch.managerName}",
                                        style = NoirType.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = NoirColors.Cream
                                    )

                                    val isPinRevealed = revealedBranchPinIds.contains(branch.id)
                                    val pinDisplay = if (isPinRevealed) branch.password else "••••"
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "PIN: $pinDisplay",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NoirColors.Gold
                                        )
                                        IconButton(
                                            onClick = {
                                                revealedBranchPinIds = if (isPinRevealed) {
                                                    revealedBranchPinIds - branch.id
                                                } else {
                                                    revealedBranchPinIds + branch.id
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPinRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (isPinRevealed) "Ocultar PIN" else "Revelar PIN",
                                                tint = NoirColors.TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            "• Tel: ${branch.phone}",
                                            fontSize = 13.sp,
                                            color = NoirColors.TextMuted
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            val ownerHwId = LicenseManager.getHardwareId(context)
                                            val quickCode = "SGNIA-TIENDA:${branch.branchName}|${branch.managerName}|${branch.password}|$ownerHwId"
                                            val msg = "🥩 *SGNIA CARNICERÍA POS · VINCULACIÓN DE SUCURSAL*\n\n" +
                                                    "🏪 *Sucursal:* ${branch.branchName}\n" +
                                                    "👤 *Encargado:* ${branch.managerName}\n" +
                                                    "🔑 *PIN Mostrador:* ${branch.password}\n\n" +
                                                    "📋 *Código de Vinculación:*\n$quickCode\n\n" +
                                                    "_Pega este código en la app SGNIA Carnicería POS de la tablet para activarla con 1 toque._"
                                            clipboardManager.setText(AnnotatedString(quickCode))
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, msg)
                                                type = "text/plain"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, "Enviar Vinculación a Tablet Tienda")
                                            runCatching { context.startActivity(shareIntent) }.onFailure {
                                                Toast.makeText(context, "Código copiado: $quickCode", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = "Compartir con Tablet", tint = NoirColors.Emerald)
                                    }
                                    IconButton(onClick = { editingBranch = branch }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = NoirColors.Cream)
                                    }
                                    IconButton(onClick = { viewModel.deleteBranch(branch) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Borrar", tint = NoirColors.Danger)
                                    }
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
            containerColor = NoirColors.Surface,
            shape = RoundedCornerShape(NoirRadius.lg),
            title = { Text("CREAR SUCURSAL Y USUARIO", style = NoirType.title, color = NoirColors.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = branchName,
                        onValueChange = { branchName = it },
                        label = { Text("Nombre de Sucursal (ej. Sucursal Sur)", color = NoirColors.TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoirColors.Cream,
                            unfocusedBorderColor = NoirColors.Hairline,
                            focusedTextColor = NoirColors.TextPrimary,
                            unfocusedTextColor = NoirColors.TextPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = managerName,
                        onValueChange = { managerName = it },
                        label = { Text("Nombre del Encargado (ej. Pedro M.)", color = NoirColors.TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoirColors.Cream,
                            unfocusedBorderColor = NoirColors.Hairline,
                            focusedTextColor = NoirColors.TextPrimary,
                            unfocusedTextColor = NoirColors.TextPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("PIN de Acceso", color = NoirColors.TextSecondary) },
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
                        label = { Text("Teléfono de la Tienda", color = NoirColors.TextSecondary) },
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
                        if (branchName.isNotBlank() && managerName.isNotBlank() && password.isNotBlank()) {
                            val bName = branchName.trim()
                            val mName = managerName.trim()
                            val ph = phone.trim()
                            val pass = password.trim()
                            viewModel.addBranch(bName, mName, ph, pass)

                            val ownerHwId = LicenseManager.getHardwareId(context)
                            scope.launch {
                                SupabaseSyncManager.pushBranch(
                                    ownerHwId = ownerHwId,
                                    branch = BranchEntity(
                                        branchName = bName,
                                        managerName = mName,
                                        phone = ph,
                                        password = pass
                                    )
                                )
                            }

                            Toast.makeText(context, "Sucursal '$bName' creada y respaldada en la nube.", Toast.LENGTH_LONG).show()
                            isAddOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirColors.Cream, contentColor = NoirColors.OnCream),
                    shape = RoundedCornerShape(NoirRadius.md)
                ) { Text("Crear Sucursal", style = NoirType.button) }
            },
            dismissButton = {
                TextButton(onClick = { isAddOpen = false }) { Text("Cancelar", color = NoirColors.Cream) }
            }
        )
    }

    editingBranch?.let { branch ->
        var branchName by remember { mutableStateOf(branch.branchName) }
        var managerName by remember { mutableStateOf(branch.managerName) }
        var phone by remember { mutableStateOf(branch.phone) }
        var password by remember { mutableStateOf(branch.password) }

        AlertDialog(
            onDismissRequest = { editingBranch = null },
            containerColor = NoirColors.Surface,
            shape = RoundedCornerShape(NoirRadius.lg),
            title = { Text("EDITAR SUCURSAL", style = NoirType.title, color = NoirColors.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = branchName,
                        onValueChange = { branchName = it },
                        label = { Text("Nombre de Sucursal", color = NoirColors.TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoirColors.Cream,
                            unfocusedBorderColor = NoirColors.Hairline,
                            focusedTextColor = NoirColors.TextPrimary,
                            unfocusedTextColor = NoirColors.TextPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = managerName,
                        onValueChange = { managerName = it },
                        label = { Text("Encargado Asignado", color = NoirColors.TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoirColors.Cream,
                            unfocusedBorderColor = NoirColors.Hairline,
                            focusedTextColor = NoirColors.TextPrimary,
                            unfocusedTextColor = NoirColors.TextPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("PIN de Acceso", color = NoirColors.TextSecondary) },
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
                        label = { Text("Teléfono", color = NoirColors.TextSecondary) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = NoirColors.Cream, contentColor = NoirColors.OnCream),
                    shape = RoundedCornerShape(NoirRadius.md)
                ) { Text("Guardar Cambios", style = NoirType.button) }
            },
            dismissButton = {
                TextButton(onClick = { editingBranch = null }) { Text("Cancelar", color = NoirColors.Cream) }
            }
        )
    }

    if (showLimitReachedDialog) {
        AlertDialog(
            onDismissRequest = { showLimitReachedDialog = false },
            containerColor = NoirColors.Surface,
            shape = RoundedCornerShape(NoirRadius.lg),
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = NoirColors.Gold,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "LÍMITE DE PLAN ALCANZADO",
                    style = NoirType.title.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                    color = NoirColors.TextPrimary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(NoirRadius.sm))
                            .background(NoirColors.SurfaceRaised)
                            .border(1.dp, NoirColors.Gold.copy(alpha = 0.4f), RoundedCornerShape(NoirRadius.sm))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "CUPO CONTRATADO: $maxAllowedBranches SUCURSAL",
                                style = NoirType.label.copy(fontWeight = FontWeight.Bold, color = NoirColors.Gold)
                            )
                            Text(
                                text = "Tu licencia actual autoriza únicamente $maxAllowedBranches sucursal en línea. Ya tienes registrada: '${branches.firstOrNull()?.branchName ?: "Sucursal 1"}'.",
                                style = NoirType.bodySmall,
                                color = NoirColors.TextPrimary
                            )
                        }
                    }

                    Text(
                        text = "Para dar de alta sucursales adicionales, habilitar más terminales POS o ampliar tu red de carnicerías, contacta a tu proveedor autorizado de SGNIA COMPANY:",
                        style = NoirType.bodySmall,
                        color = NoirColors.TextSecondary
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(NoirRadius.sm))
                            .background(NoirColors.Background)
                            .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("SGNIA COMPANY · SOPORTE TÉCNICO", style = NoirType.label, color = NoirColors.Cream)
                            Text("Gamaliel Velázquez", style = NoirType.bodySmall.copy(fontWeight = FontWeight.Bold), color = NoirColors.TextPrimary)
                            Text("+52 276 109 5688", style = NoirType.metricM.copy(fontSize = 14.sp), color = NoirColors.Gold)
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val uri = Uri.parse("https://wa.me/522761095688?text=" + Uri.encode("Hola Gamaliel, tengo la app SGNIA Consola Dueño y deseo ampliar mi plan para agregar más sucursales a mi red."))
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            runCatching { context.startActivity(intent) }.onFailure {
                                Toast.makeText(context, "Contactar al: +52 276 109 5688", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NoirColors.Emerald, contentColor = Color.White),
                        shape = RoundedCornerShape(NoirRadius.sm)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", style = NoirType.label.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+522761095688"))
                            runCatching { context.startActivity(intent) }.onFailure {
                                Toast.makeText(context, "Teléfono: +52 276 109 5688", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NoirColors.Cream, contentColor = NoirColors.OnCream),
                        shape = RoundedCornerShape(NoirRadius.sm)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Llamar", style = NoirType.label.copy(color = NoirColors.OnCream, fontWeight = FontWeight.Bold))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLimitReachedDialog = false }
                ) {
                    Text("Entendido", color = NoirColors.TextMuted)
                }
            }
        )
    }
}
