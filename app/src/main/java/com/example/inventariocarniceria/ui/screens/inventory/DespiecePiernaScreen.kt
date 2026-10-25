package com.example.inventariocarniceria.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import java.util.Locale

@Composable
fun DespiecePiernaDialog(
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    val despieceState by viewModel.despieceState.collectAsState()
    val scaleState by viewModel.scaleState.collectAsState()

    var manualInitialWeightText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf(despieceState.notes) }

    val darkRed = Color(0xFFB71C1C)
    val darkGreen = Color(0xFF2E7D32)
    val warningOrange = Color(0xFFE65100)
    val accentBlue = Color(0xFF1565C0)

    val currentScaleWeight = scaleState.currentWeight
    val formattedScaleWeight = String.format(Locale.US, "%.3f", currentScaleWeight)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.95f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // ==========================================
                // 1. HEADER CON INDICADOR DE BÁSCULA VINCULADA
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(darkRed, shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ContentCut, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DESPIECE Y RENDIMIENTO",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = darkRed
                            )
                            Text(
                                text = "Transformación de Pierna sin hueso a cortes derivados",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    // WIDGET MINI BÁSCULA EN VIVO
                    Card(
                        colors = CardDefaults.cardColors(containerColor = darkGreen.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, darkGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (scaleState.isStable) darkGreen else Color.Gray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$formattedScaleWeight kg",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = darkGreen
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ==========================================
                    // PASO 1: CAPTURA DE PESO INICIAL DE LA PIERNA
                    // ==========================================
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "PASO 1:",
                                            fontWeight = FontWeight.Black,
                                            color = darkRed,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Peso Inicial Pierna sin Hueso",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    if (despieceState.initialWeightKg > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = darkGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${String.format(Locale.US, "%.3f", despieceState.initialWeightKg)} kg Lote",
                                                fontWeight = FontWeight.Black,
                                                color = darkGreen,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // BOTÓN 1-TOQUE DESDE BÁSCULA
                                    Button(
                                        onClick = {
                                            viewModel.setDespieceInitialWeight(currentScaleWeight)
                                            manualInitialWeightText = String.format(Locale.US, "%.3f", currentScaleWeight)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = darkRed),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Icon(Icons.Default.Scale, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tomar de Báscula ($formattedScaleWeight kg)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    // ENTRADA MANUAL ALTERNATIVA
                                    OutlinedTextField(
                                        value = manualInitialWeightText,
                                        onValueChange = {
                                            manualInitialWeightText = it
                                            val w = it.toDoubleOrNull()
                                            if (w != null && w > 0) {
                                                viewModel.setDespieceInitialWeight(w)
                                            }
                                        },
                                        placeholder = { Text("Manual kg") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .width(130.dp)
                                            .height(52.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ==========================================
                    // PASO 2: PESAJE DE CADA DERIVADO (1-TOQUE)
                    // ==========================================
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PASO 2: Pesar y Capturar Cortes Derivados",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Coloca el corte en la báscula y presiona capturar",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    items(despieceState.items) { item ->
                        val isMerma = item.isWaste
                        val itemColor = if (isMerma) warningOrange else darkGreen
                        val isCaptured = item.isCaptured && item.weightKg > 0

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCaptured) itemColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCaptured) itemColor else Color.LightGray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isMerma) "MERMA" else "#${item.productCode}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = if (isMerma) warningOrange else accentBlue
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item.productName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    val yield = if (despieceState.initialWeightKg > 0) {
                                        (item.weightKg / despieceState.initialWeightKg) * 100.0
                                    } else 0.0

                                    Text(
                                        text = if (isCaptured) "Pesado: ${String.format(Locale.US, "%.3f", item.weightKg)} kg  (${String.format(Locale.US, "%.1f", yield)}% del lote)"
                                               else "Pendiente de pesar",
                                        fontSize = 12.sp,
                                        color = if (isCaptured) itemColor else Color.Gray,
                                        fontWeight = if (isCaptured) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // BOTÓN CAPTURAR PESO ACTUAL DE BÁSCULA EN 1 TOQUE
                                    Button(
                                        onClick = {
                                            viewModel.captureDespieceItemWeight(item.productCode, currentScaleWeight)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = itemColor),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(42.dp)
                                    ) {
                                        Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Capturar $formattedScaleWeight kg", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // PASO 3: BALANCE Y RENDIMIENTO EN TIEMPO REAL
                    // ==========================================
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "PASO 3: Balance de Masa y Rendimiento",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // BARRA DE BALANCE: CAPTURADO VS INICIAL
                                val initial = despieceState.initialWeightKg
                                val captured = despieceState.totalCapturedKg
                                val progress = if (initial > 0) (captured / initial).toFloat().coerceIn(0f, 1f) else 0f

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp)),
                                    color = if (despieceState.balanceDiscrepancyKg == 0.0) darkGreen else warningOrange,
                                    trackColor = Color.LightGray.copy(alpha = 0.5f)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Carne Vendible", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            "${String.format(Locale.US, "%.3f", despieceState.totalDerivedWeightKg)} kg (${despieceState.yieldPercentage}%)",
                                            fontWeight = FontWeight.Black,
                                            color = darkGreen,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Merma/Grasa", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            "${String.format(Locale.US, "%.3f", despieceState.totalWasteKg)} kg (${despieceState.wastePercentage}%)",
                                            fontWeight = FontWeight.Black,
                                            color = if (despieceState.wastePercentage > 15.0) darkRed else warningOrange,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Diferencia / Faltante", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            "${String.format(Locale.US, "%.3f", despieceState.balanceDiscrepancyKg)} kg",
                                            fontWeight = FontWeight.Black,
                                            color = if (despieceState.balanceDiscrepancyKg == 0.0) darkGreen else darkRed,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                // ALERTA DE MERMA ELEVADA
                                if (despieceState.wastePercentage > 15.0 && despieceState.initialWeightKg > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(darkRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = darkRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "¡Alerta! Porcentaje de merma superior al 15%. Revise exceso de grasa o hueso.",
                                            fontSize = 11.sp,
                                            color = darkRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = notesText,
                                    onValueChange = {
                                        notesText = it
                                        viewModel.updateDespieceNotes(it)
                                    },
                                    label = { Text("Notas de lote (ej. Proveedor Frigorífico Central, Lote #89)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ==========================================
                // BOTONES INFERIORES: REINICIAR & CONFIRMAR DESPIECE
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.resetDespiece()
                            manualInitialWeightText = ""
                            notesText = ""
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reiniciar")
                    }

                    val canFinalize = despieceState.initialWeightKg > 0 && despieceState.totalCapturedKg > 0

                    Button(
                        onClick = { viewModel.finalizeDespiece() },
                        enabled = canFinalize,
                        colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FINALIZAR DESPIECE Y ACTUALIZAR STOCK",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
