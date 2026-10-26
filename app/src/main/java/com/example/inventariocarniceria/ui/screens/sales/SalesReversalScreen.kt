package com.example.inventariocarniceria.ui.screens.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.data.local.SaleTicketEntity
import com.example.inventariocarniceria.ui.components.CameraBarcodeScannerDialog
import com.example.inventariocarniceria.ui.design.BentoCard
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirPrimaryButton
import com.example.inventariocarniceria.ui.design.NoirRadius
import com.example.inventariocarniceria.ui.design.NoirSecondaryButton
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesReversalScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val allTickets by viewModel.allSaleTickets.collectAsState()
    val haptic = LocalHapticFeedback.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODOS") } // "TODOS", "COMPLETADOS", "INVERTIDOS"
    var selectedTicket by remember { mutableStateOf<SaleTicketEntity?>(null) }
    var isConfirmDialogOpen by remember { mutableStateOf(false) }
    var isCameraScannerOpen by remember { mutableStateOf(false) }

    // Motivos predefinidos para la baja / inversión
    val presetReasons = listOf(
        "Equivocación en cobro o pesaje",
        "Reembolso solicitado por el cliente",
        "Cancelación de compra en mostrador",
        "Corte devuelto por inconformidad",
        "Otro motivo operativo"
    )
    var selectedReason by remember { mutableStateOf(presetReasons[0]) }
    var customNotes by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

    // Filtrar la lista según búsqueda y estado
    val filteredTickets = remember(allTickets, searchQuery, selectedFilter) {
        val q = searchQuery.trim().lowercase()
        allTickets.filter { ticket ->
            val matchesFilter = when (selectedFilter) {
                "COMPLETADOS" -> ticket.status == "COMPLETADA"
                "INVERTIDOS" -> ticket.status == "INVERTIDA"
                else -> true
            }
            val matchesQuery = if (q.isEmpty()) true else {
                ticket.operationNumber.lowercase().contains(q) ||
                ticket.ticketBarcode.lowercase().contains(q) ||
                ticket.productName.lowercase().contains(q) ||
                ticket.productCode.toString().contains(q)
            }
            matchesFilter && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NoirColors.Background)
            .padding(horizontal = NoirSpace.md, vertical = NoirSpace.sm),
        verticalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
    ) {
        // 1. ENCABEZADO
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    NoirLabel("DEVOLUCIONES Y REEMBOLSOS · ${uiState.currentBranch.uppercase()}", color = NoirColors.Gold)
                    Text(
                        text = "Invertir Venta / Bajas",
                        style = NoirType.display.copy(fontSize = 24.sp),
                        color = NoirColors.TextPrimary
                    )
                    Text(
                        text = "Localiza el ticket por número de operación o código de barras para cancelarlo",
                        style = NoirType.bodySmall,
                        color = NoirColors.TextMuted
                    )
                }
            }
        }

        // 2. BARRA DE BÚSQUEDA Y ESCÁNER DE CÓDIGO DE BARRAS
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(NoirSpace.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Número de Operación o Código de Barras", color = NoirColors.TextSecondary) },
                        placeholder = { Text("Ej. OP-10024 o 2000001010005", color = NoirColors.TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = NoirColors.Cream)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = NoirColors.TextSecondary)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                val match = allTickets.find {
                                    it.operationNumber.equals(searchQuery.trim(), ignoreCase = true) ||
                                    it.ticketBarcode.equals(searchQuery.trim(), ignoreCase = true)
                                }
                                if (match != null) {
                                    selectedTicket = match
                                }
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(NoirRadius.sm),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoirColors.Cream,
                            unfocusedBorderColor = NoirColors.Hairline,
                            focusedTextColor = NoirColors.TextPrimary,
                            unfocusedTextColor = NoirColors.TextPrimary,
                            focusedContainerColor = NoirColors.SurfaceSunken,
                            unfocusedContainerColor = NoirColors.SurfaceSunken
                        )
                    )

                    NoirSecondaryButton(
                        text = "Escanear",
                        icon = Icons.Default.QrCodeScanner,
                        onClick = { isCameraScannerOpen = true },
                        modifier = Modifier.height(56.dp)
                    )
                }

                // Chips de filtrado rápido
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("TODOS", "COMPLETADOS", "INVERTIDOS").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = when (filter) {
                                        "TODOS" -> "Todos (${allTickets.size})"
                                        "COMPLETADOS" -> "Activos (${allTickets.count { it.status == "COMPLETADA" }})"
                                        else -> "Invertidos (${allTickets.count { it.status == "INVERTIDA" }})"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NoirColors.Cream,
                                selectedLabelColor = NoirColors.Background,
                                containerColor = NoirColors.SurfaceRaised,
                                labelColor = NoirColors.TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) NoirColors.Cream else NoirColors.Hairline
                            )
                        )
                    }
                }
            }
        }

        // 3. TARJETA DE DETALLE DEL TICKET SELECCIONADO (SI HAY UNO)
        item {
            val ticket = selectedTicket ?: filteredTickets.firstOrNull()
            if (ticket != null) {
                val isReversed = ticket.status == "INVERTIDA"
                BentoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.5.dp,
                            color = if (isReversed) NoirColors.Danger.copy(alpha = 0.8f) else NoirColors.Cream.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(NoirRadius.lg)
                        ),
                    contentPadding = PaddingValues(NoirSpace.lg),
                    verticalArrangement = Arrangement.spacedBy(NoirSpace.md)
                ) {
                    // Cabecera del ticket
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = if (isReversed) NoirColors.Danger else NoirColors.Cream,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Operación #${ticket.operationNumber}",
                                    style = NoirType.title.copy(fontSize = 18.sp),
                                    color = NoirColors.TextPrimary
                                )
                            }
                            Text(
                                text = "Código de barras: ${ticket.ticketBarcode}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = NoirColors.TextMuted
                            )
                        }

                        StatusPill(
                            text = if (isReversed) "VENTA INVERTIDA / DADA DE BAJA" else "VENTA COMPLETADA",
                            tone = if (isReversed) PillTone.Danger else PillTone.Positive
                        )
                    }

                    // Cuadro de información del producto y pesaje
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(NoirRadius.md))
                            .background(NoirColors.SurfaceSunken)
                            .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.md))
                            .padding(NoirSpace.md)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🥩 ${ticket.productName}",
                                        style = NoirType.title.copy(fontSize = 17.sp),
                                        color = NoirColors.Cream
                                    )
                                    Text(
                                        text = "PLU #${ticket.productCode} · $${String.format(Locale.US, "%.2f", ticket.pricePerKg)}/kg",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        color = NoirColors.TextSecondary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = String.format(Locale.US, "%.3f kg", ticket.weightKg),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = NoirColors.TextPrimary
                                    )
                                    Text(
                                        text = "Importe: $${String.format(Locale.US, "%,.2f", ticket.totalAmount)} MXN",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = NoirColors.Gold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Fecha: ${dateFormat.format(Date(ticket.timestamp))}",
                                    fontSize = 12.sp,
                                    color = NoirColors.TextMuted
                                )
                                Text(
                                    text = "Cajero: ${ticket.cashierName}",
                                    fontSize = 12.sp,
                                    color = NoirColors.TextMuted
                                )
                            }
                        }
                    }

                    // Motivo de inversión si ya fue invertida
                    if (isReversed) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(NoirRadius.sm))
                                .background(NoirColors.Danger.copy(alpha = 0.12f))
                                .border(1.dp, NoirColors.Danger.copy(alpha = 0.4f), RoundedCornerShape(NoirRadius.sm))
                                .padding(NoirSpace.sm)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = NoirColors.Danger, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Motivo de la baja / reembolso:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = NoirColors.Danger
                                    )
                                }
                                Text(
                                    text = ticket.reversalReason ?: "Cancelación manual de ticket",
                                    fontSize = 13.sp,
                                    color = NoirColors.TextPrimary
                                )
                                if (ticket.reversalTimestamp != null) {
                                    Text(
                                        text = "Invertida el: ${dateFormat.format(Date(ticket.reversalTimestamp))}",
                                        fontSize = 11.sp,
                                        color = NoirColors.TextMuted
                                    )
                                }
                                Text(
                                    text = "✅ Se reingresaron ${String.format(Locale.US, "%.3f", ticket.weightKg)} kg al inventario de ${ticket.productName}.",
                                    fontSize = 12.sp,
                                    color = NoirColors.Emerald
                                )
                            }
                        }
                    } else {
                        // Botón de acción para dar de baja / invertir la venta
                        NoirPrimaryButton(
                            text = "INVERTIR VENTA / DAR DE BAJA ESTE TICKET",
                            icon = Icons.Default.AssignmentReturn,
                            onClick = {
                                selectedTicket = ticket
                                isConfirmDialogOpen = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Estado vacío si no hay tickets
                BentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(NoirSpace.lg),
                    verticalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = NoirSpace.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = NoirColors.TextMuted, modifier = Modifier.size(36.dp))
                        Text(
                            text = "No se encontraron tickets registrados",
                            style = NoirType.title.copy(fontSize = 16.sp),
                            color = NoirColors.TextPrimary
                        )
                        Text(
                            text = "Las ventas capturadas en báscula aparecerán aquí automáticamente.",
                            style = NoirType.bodySmall,
                            color = NoirColors.TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.seedSampleSaleTicket()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirColors.Cream),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(NoirColors.Cream))
                        ) {
                            Text("Generar Tickets de Prueba (#OP-10025 Cuero S/G)")
                        }
                    }
                }
            }
        }

        // 4. HISTORIAL DE TICKETS RECIENTES
        if (filteredTickets.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = NoirColors.Gold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Historial de Operaciones Recientes",
                            style = NoirType.title.copy(fontSize = 16.sp),
                            color = NoirColors.TextPrimary
                        )
                    }
                    Text(
                        text = "${filteredTickets.size} tickets",
                        fontSize = 12.sp,
                        color = NoirColors.TextMuted
                    )
                }
            }

            items(filteredTickets, key = { it.id }) { itemTicket ->
                val isSelected = selectedTicket?.id == itemTicket.id
                val isItemReversed = itemTicket.status == "INVERTIDA"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(NoirRadius.md))
                        .background(NoirColors.Surface)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) NoirColors.Cream else if (isItemReversed) NoirColors.Danger.copy(alpha = 0.4f) else NoirColors.Hairline,
                            shape = RoundedCornerShape(NoirRadius.md)
                        )
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            selectedTicket = itemTicket
                        }
                        .padding(NoirSpace.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${itemTicket.operationNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isItemReversed) NoirColors.Danger else NoirColors.TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusPill(
                                    text = if (isItemReversed) "INVERTIDO" else "ACTIVO",
                                    tone = if (isItemReversed) PillTone.Danger else PillTone.Positive
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🥩 ${itemTicket.productName} · ${String.format(Locale.US, "%.3f", itemTicket.weightKg)} kg",
                                fontSize = 13.sp,
                                color = NoirColors.Cream
                            )
                            Text(
                                text = "Código: ${itemTicket.ticketBarcode} · ${dateFormat.format(Date(itemTicket.timestamp))}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = NoirColors.TextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$${String.format(Locale.US, "%,.2f", itemTicket.totalAmount)}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isItemReversed) NoirColors.TextMuted else NoirColors.Gold
                            )
                            Text(
                                text = if (isSelected) "SELECCIONADO" else "TOCAR DETALLE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NoirColors.Cream else NoirColors.TextMuted
                            )
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO MODAL DE CONFIRMACIÓN DE INVERSIÓN / BAJA
    if (isConfirmDialogOpen && selectedTicket != null) {
        val target = selectedTicket!!
        AlertDialog(
            onDismissRequest = { isConfirmDialogOpen = false },
            containerColor = NoirColors.SurfaceRaised,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AssignmentReturn,
                        contentDescription = null,
                        tint = NoirColors.Danger,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Invertir Venta y Dar de Baja",
                        style = NoirType.title.copy(fontSize = 18.sp),
                        color = NoirColors.TextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Se cancelará el cobro del ticket #${target.operationNumber} ($${String.format(Locale.US, "%,.2f", target.totalAmount)} MXN) y se reingresarán automáticamente ${String.format(Locale.US, "%.3f", target.weightKg)} kg de ${target.productName} al inventario.",
                        style = NoirType.bodySmall,
                        color = NoirColors.TextSecondary
                    )

                    Text(
                        text = "Selecciona el motivo del reembolso / baja:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NoirColors.Cream
                    )

                    presetReasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                        ) {
                            RadioButton(
                                selected = (selectedReason == reason),
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NoirColors.Cream,
                                    unselectedColor = NoirColors.Hairline
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = reason,
                                fontSize = 13.sp,
                                color = if (selectedReason == reason) NoirColors.TextPrimary else NoirColors.TextSecondary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customNotes,
                        onValueChange = { customNotes = it },
                        label = { Text("Notas u observaciones adicionales (opcional)", color = NoirColors.TextSecondary) },
                        placeholder = { Text("Ej. Cliente regresó el corte en mostrador", color = NoirColors.TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(NoirRadius.sm),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoirColors.Cream,
                            unfocusedBorderColor = NoirColors.Hairline,
                            focusedTextColor = NoirColors.TextPrimary,
                            unfocusedTextColor = NoirColors.TextPrimary,
                            focusedContainerColor = NoirColors.SurfaceSunken,
                            unfocusedContainerColor = NoirColors.SurfaceSunken
                        )
                    )
                }
            },
            confirmButton = {
                NoirPrimaryButton(
                    text = "CONFIRMAR INVERSIÓN Y BAJA",
                    icon = Icons.Default.CheckCircle,
                    onClick = {
                        val fullReason = if (customNotes.isNotBlank()) "$selectedReason: $customNotes" else selectedReason
                        viewModel.reverseSaleTicket(target.id, fullReason)
                        isConfirmDialogOpen = false
                        selectedTicket = target.copy(
                            status = "INVERTIDA",
                            reversalReason = fullReason,
                            reversalTimestamp = System.currentTimeMillis()
                        )
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { isConfirmDialogOpen = false }) {
                    Text("Cancelar", color = NoirColors.TextSecondary)
                }
            }
        )
    }

    // ESCÁNER DE CÁMARA PARA TICKETS O ETIQUETAS
    if (isCameraScannerOpen) {
        CameraBarcodeScannerDialog(
            onDismiss = { isCameraScannerOpen = false },
            onBarcodeScanned = { scannedCode ->
                isCameraScannerOpen = false
                searchQuery = scannedCode
                val match = allTickets.find {
                    it.ticketBarcode.equals(scannedCode.trim(), ignoreCase = true) ||
                    it.operationNumber.equals(scannedCode.trim(), ignoreCase = true)
                }
                if (match != null) {
                    selectedTicket = match
                }
            }
        )
    }
}
