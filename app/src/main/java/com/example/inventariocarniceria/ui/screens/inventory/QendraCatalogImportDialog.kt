package com.example.inventariocarniceria.ui.screens.inventory

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inventariocarniceria.data.scale.ParsedPluPreview
import com.example.inventariocarniceria.data.scale.ScaleCatalogParser
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirPrimaryButton
import com.example.inventariocarniceria.ui.design.NoirRadius
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill
import com.example.inventariocarniceria.ui.design.sgniaTextFieldColors
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import java.util.Locale

@Composable
fun QendraCatalogImportDialog(
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val scalesList by viewModel.scalesList.collectAsState()
    val scale1Config by viewModel.scale1Config.collectAsState()
    val scale2Config by viewModel.scale2Config.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var rawInputText by remember { mutableStateOf("") }
    var isExtracting by remember { mutableStateOf(false) }
    var extractionMessage by remember { mutableStateOf<String?>(null) }
    var isRawEditorExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val content = stream.bufferedReader(Charsets.UTF_8).readText()
                    rawInputText = content
                    val count = content.lines().count { it.isNotBlank() }
                    extractionMessage = "Archivo cargado exitosamente ($count líneas)."
                }
            } catch (e: Exception) {
                extractionMessage = "Error al abrir archivo: ${e.localizedMessage}"
            }
        }
    }

    val previewList = remember(rawInputText) {
        ScaleCatalogParser.parse(rawInputText)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .navigationBarsPadding()
                .border(BorderStroke(1.dp, NoirColors.HairlineStrong), RoundedCornerShape(NoirRadius.lg)),
            shape = RoundedCornerShape(NoirRadius.lg),
            color = NoirColors.Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = NoirSpace.md, vertical = NoirSpace.sm)
            ) {
                // CABECERA ESTILO NOIR & CREMA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NoirSpace.xs)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(NoirRadius.sm))
                                .background(NoirColors.SurfaceRaised)
                                .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = NoirColors.Cream,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            NoirLabel(text = "INTEGRACIÓN INDUSTRIAL", color = NoirColors.Cream)
                            Text(
                                text = "Sincronización de Básculas & Qendra",
                                style = NoirType.title,
                                fontSize = 16.sp,
                                color = NoirColors.TextPrimary
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(NoirRadius.sm))
                            .background(NoirColors.SurfaceRaised)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = NoirColors.TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = NoirColors.Hairline
                )

                // TARJETA COMPACTA DE ESTADO Y SEGURIDAD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NoirSpace.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NoirColors.EmeraldMuted)
                            .border(1.dp, NoirColors.Emerald.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = NoirColors.Emerald, modifier = Modifier.size(14.dp))
                        Text(
                            text = "${allProducts.size} cortes protegidos (fusión segura)",
                            style = NoirType.bodySmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NoirColors.Emerald
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NoirColors.AmberMuted)
                            .border(1.dp, NoirColors.Amber.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = NoirColors.Amber, modifier = Modifier.size(14.dp))
                        Text(
                            text = "1 TCP x báscula",
                            style = NoirType.bodySmall,
                            fontSize = 11.sp,
                            color = NoirColors.Amber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // BÁSCULAS CONECTADAS EN RED (DINÁMICO PARA 1, 2, 5, 10 O MÁS BÁSCULAS)
                val displayScales = if (scalesList.isNotEmpty()) scalesList else listOf(scale1Config, scale2Config)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BÁSCULAS EN RED (${displayScales.size} DETECTADAS)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NoirColors.Cream
                        )
                        Text(
                            text = "Toca para extraer PLUs",
                            style = NoirType.label,
                            color = NoirColors.TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(displayScales) { scale ->
                            Button(
                                onClick = {
                                    isExtracting = true
                                    extractionMessage = "Consultando ${scale.name} (${scale.ip})..."
                                    viewModel.extractPlusFromScale(scale.id) { plus, msg ->
                                        isExtracting = false
                                        extractionMessage = msg
                                        if (plus.isNotEmpty()) {
                                            val sb = StringBuilder()
                                            plus.forEach { p -> sb.append("${p.code};${p.name};${p.price};${p.section}\n") }
                                            rawInputText = sb.toString()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NoirColors.SurfaceRaised,
                                    contentColor = NoirColors.Cream
                                ),
                                shape = RoundedCornerShape(NoirRadius.sm),
                                border = BorderStroke(1.dp, if (scale.isConnected) NoirColors.Emerald.copy(alpha = 0.6f) else NoirColors.Hairline),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (scale.isConnected) NoirColors.Emerald else NoirColors.Gold)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Icon(Icons.Default.Scale, contentDescription = null, tint = NoirColors.Cream, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${scale.name} (${scale.ip.ifBlank { "TCP" }})",
                                    style = NoirType.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = NoirColors.Cream
                                )
                            }
                        }
                    }
                }

                if (isExtracting) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = NoirColors.Cream, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = extractionMessage ?: "Conectando con la báscula...", style = NoirType.bodySmall, fontSize = 11.sp, color = NoirColors.Cream)
                    }
                } else if (!extractionMessage.isNullOrEmpty()) {
                    Text(
                        text = extractionMessage!!,
                        style = NoirType.bodySmall,
                        fontSize = 11.sp,
                        color = NoirColors.TextSecondary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // ACCIONES DE CARGA Y ARCHIVO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoirColors.SurfaceRaised,
                            contentColor = NoirColors.Cream
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NoirColors.HairlineStrong),
                        modifier = Modifier.weight(1.3f).height(36.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = NoirColors.Cream, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Subir TXT/CSV", style = NoirType.bodySmall, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NoirColors.Cream)
                    }

                    OutlinedButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrBlank()) {
                                rawInputText = clip
                                val count = clip.lines().count { it.isNotBlank() }
                                extractionMessage = "Texto pegado ($count líneas)."
                            } else {
                                extractionMessage = "Portapapeles vacío."
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NoirColors.Hairline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirColors.TextPrimary),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = NoirColors.Cream, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pegar", style = NoirType.bodySmall, fontSize = 11.sp, color = NoirColors.TextPrimary)
                    }

                    OutlinedButton(
                        onClick = {
                            rawInputText = """
1;MENUDO DE RES;120.00;RES
10;PIERNA CON HUESO;98.50;CERDO
20;PIERNA SIN HUESO;115.00;CERDO
21;BISTEC DE CERDO;125.00;CERDO
22;MOLIDA DE CERDO;110.00;CERDO
30;BISTEC DE RES;185.00;RES
31;MOLIDA DE RES;145.00;RES
60;QUESO OAXACA;130.00;LACTEOS
61;JAMON DE PAVO;95.00;LACTEOS
70;MAYONESA BOTE 3.8KG;65.00;ABARROTES
85;BOTANAS SGNIA;25.00;MI INVERSION
                            """.trimIndent()
                            extractionMessage = "Plantilla de ejemplo cargada."
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NoirColors.Hairline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirColors.TextSecondary),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("Ejemplo", style = NoirType.bodySmall, fontSize = 11.sp, color = NoirColors.TextSecondary)
                    }

                    if (rawInputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                rawInputText = ""
                                extractionMessage = null
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NoirColors.SurfaceRaised)
                                .border(1.dp, NoirColors.Hairline, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Limpiar", tint = NoirColors.TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // BARRA COLAPSABLE PARA EDITOR DE TEXTO EN BRUTO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NoirColors.SurfaceRaised)
                        .clickable { isRawEditorExpanded = !isRawEditorExpanded }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = NoirColors.Cream, modifier = Modifier.size(13.dp))
                        Text(
                            text = if (rawInputText.isBlank()) "Editor Manual de PLUs (Opcional)" else "Editar Texto PLUs (${rawInputText.lines().count { it.isNotBlank() }} líneas)",
                            style = NoirType.bodySmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NoirColors.TextPrimary
                        )
                    }
                    Icon(
                        if (isRawEditorExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = NoirColors.Cream,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = isRawEditorExpanded) {
                    OutlinedTextField(
                        value = rawInputText,
                        onValueChange = { rawInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(95.dp)
                            .padding(top = 4.dp),
                        label = { Text("Formato: PLU;Nombre;Precio;Sección", color = NoirColors.TextSecondary, fontSize = 11.sp) },
                        placeholder = { Text("Pega aquí el archivo o extrae de la báscula", color = NoirColors.TextMuted, fontSize = 11.sp) },
                        textStyle = NoirType.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NoirColors.TextPrimary),
                        shape = RoundedCornerShape(NoirRadius.sm),
                        colors = sgniaTextFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // ENCABEZADO DE VISTA PREVIA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.FormatListNumbered,
                            contentDescription = null,
                            tint = NoirColors.Emerald,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Vista Previa (${previewList.size} detectados)",
                            style = NoirType.bodySmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NoirColors.Emerald
                        )
                    }

                    if (rawInputText.isNotBlank()) {
                        StatusPill(
                            text = "${previewList.size} PLUs VÁLIDOS",
                            tone = PillTone.Positive
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // LISTA DE VISTA PREVIA EN BENTO SUNKEN (CON PESO FLEXIBLE)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(NoirRadius.sm))
                        .background(NoirColors.SurfaceSunken)
                        .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm))
                ) {
                    if (previewList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(NoirSpace.sm),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin productos cargados.\nOprime una 'Báscula' arriba o pega datos de Qendra.",
                                style = NoirType.bodySmall,
                                color = NoirColors.TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(NoirSpace.xs)
                        ) {
                            items(previewList) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(NoirColors.SurfaceRaised)
                                                .border(1.dp, NoirColors.Hairline, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "#${item.code}",
                                                style = NoirType.label,
                                                fontSize = 10.sp,
                                                color = NoirColors.Cream,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = item.name,
                                                style = NoirType.bodySmall,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NoirColors.TextPrimary
                                            )
                                            Text(
                                                text = item.section,
                                                style = NoirType.label,
                                                color = NoirColors.TextSecondary,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", item.price)}/kg",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = NoirColors.Emerald
                                    )
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    color = NoirColors.Hairline
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // BOTÓN DE ACCIÓN FINAL EN CREMA (NUNCA CORTADO GRACIAS A NAVIGATION BAR INSETS)
                NoirPrimaryButton(
                    text = if (previewList.isEmpty()) "EXTRAE DE UNA BÁSCULA O SUBE ARCHIVO" else "FUSIONAR ${previewList.size} PRODUCTOS (CONSERVA EXISTENTES)",
                    onClick = {
                        if (previewList.isNotEmpty()) {
                            viewModel.importParsedPlus(previewList)
                        }
                    },
                    enabled = previewList.isNotEmpty(),
                    icon = Icons.Default.CloudDownload,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                )
            }
        }
    }
}
