package com.example.inventariocarniceria.ui.screens.inventory

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentPaste
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.inventariocarniceria.ui.design.sgniaTextFieldColors
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
import java.util.Locale

@Composable
fun QendraCatalogImportDialog(
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val scale1Config by viewModel.scale1Config.collectAsState()
    val scale2Config by viewModel.scale2Config.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var rawInputText by remember { mutableStateOf("") }
    var isExtracting by remember { mutableStateOf(false) }
    var extractionMessage by remember { mutableStateOf<String?>(null) }

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
                    extractionMessage = "Archivo cargado exitosamente ($count líneas detectadas). Revisa la vista previa y pulsa 'Guardar Catálogo'."
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
                .fillMaxHeight(0.95f)
                .border(BorderStroke(1.dp, NoirColors.HairlineStrong), RoundedCornerShape(NoirRadius.lg)),
            shape = RoundedCornerShape(NoirRadius.lg),
            color = NoirColors.Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(NoirSpace.lg)
            ) {
                // CABECERA ESTILO NOIR & CREMA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(NoirRadius.sm))
                                .background(NoirColors.SurfaceRaised)
                                .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = NoirColors.Cream,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            NoirLabel(text = "INTEGRACIÓN INDUSTRIAL", color = NoirColors.Cream)
                            Text(
                                text = "Sincronización de Básculas & Qendra",
                                style = NoirType.title,
                                color = NoirColors.TextPrimary
                            )
                            Text(
                                text = "Extracción por socket TCP/IP y carga masiva de PLUs",
                                style = NoirType.bodySmall,
                                color = NoirColors.TextSecondary
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(RoundedCornerShape(NoirRadius.sm))
                            .background(NoirColors.SurfaceRaised)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = NoirColors.TextPrimary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = NoirSpace.sm),
                    color = NoirColors.Hairline
                )

                // TARJETA AVISO TÉCNICO LANTRONIX (MONO-CONEXIÓN)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(NoirRadius.sm))
                        .background(NoirColors.AmberMuted)
                        .border(1.dp, NoirColors.Amber.copy(alpha = 0.4f), RoundedCornerShape(NoirRadius.sm))
                        .padding(horizontal = NoirSpace.md, vertical = NoirSpace.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NoirSpace.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NoirColors.Amber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Aviso Lantronix: Las básculas Cuora Max admiten 1 conexión TCP a la vez. Mantén cerrado Qendra en la PC mientras sincronices.",
                        style = NoirType.bodySmall,
                        color = NoirColors.Amber,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(NoirSpace.xs))

                // TARJETA DE GARANTÍA DE NO BORRADO (FUSIÓN SEGURA)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(NoirRadius.sm))
                        .background(NoirColors.EmeraldMuted)
                        .border(1.dp, NoirColors.Emerald.copy(alpha = 0.4f), RoundedCornerShape(NoirRadius.sm))
                        .padding(horizontal = NoirSpace.md, vertical = NoirSpace.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NoirSpace.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = NoirColors.Emerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Fusión Segura: Catálogo actual de ${allProducts.size} cortes protegido. Los PLUs se añadirán o actualizarán sin borrar existencias.",
                        style = NoirType.bodySmall,
                        color = NoirColors.Emerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(NoirSpace.sm))

                // BOTÓN PRINCIPAL: SUBIR ARCHIVO EXPORTADO DESDE QENDRA
                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoirColors.SurfaceRaised,
                        contentColor = NoirColors.Cream
                    ),
                    shape = RoundedCornerShape(NoirRadius.sm),
                    border = BorderStroke(1.dp, NoirColors.HairlineStrong),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = NoirColors.Cream, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(NoirSpace.xs))
                    Text(
                        text = "SUBIR ARCHIVO DE QENDRA (.TXT / .CSV)",
                        style = NoirType.button,
                        fontSize = 12.sp,
                        color = NoirColors.Cream
                    )
                }

                Spacer(modifier = Modifier.height(NoirSpace.xs))

                // ACCIONES SECUNDARIAS: PEGAR TEXTO, LIMPIAR, CARGAR EJEMPLO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NoirSpace.xs)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrBlank()) {
                                rawInputText = clip
                                val count = clip.lines().count { it.isNotBlank() }
                                extractionMessage = "Texto pegado ($count líneas). Revisa la vista previa abajo."
                            } else {
                                extractionMessage = "El portapapeles está vacío."
                            }
                        },
                        shape = RoundedCornerShape(NoirRadius.sm),
                        border = BorderStroke(1.dp, NoirColors.Hairline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirColors.TextPrimary),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = NoirColors.Cream, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pegar", style = NoirType.bodySmall, fontWeight = FontWeight.Bold, color = NoirColors.TextPrimary)
                    }

                    OutlinedButton(
                        onClick = {
                            rawInputText = ""
                            extractionMessage = null
                        },
                        shape = RoundedCornerShape(NoirRadius.sm),
                        border = BorderStroke(1.dp, NoirColors.Hairline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirColors.TextSecondary),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Limpiar", style = NoirType.bodySmall, color = NoirColors.TextSecondary)
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
                            extractionMessage = "Plantilla de ejemplo cargada. Puedes editar los textos antes de guardar."
                        },
                        shape = RoundedCornerShape(NoirRadius.sm),
                        border = BorderStroke(1.dp, NoirColors.Hairline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirColors.TextSecondary),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Cargar Ejemplo", style = NoirType.bodySmall, color = NoirColors.TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(NoirSpace.xs))

                // BOTONES DE CONEXIÓN A BÁSCULAS TCP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NoirSpace.xs)
                ) {
                    Button(
                        onClick = {
                            isExtracting = true
                            extractionMessage = "Consultando Báscula 1 (${scale1Config.ip})..."
                            viewModel.extractPlusFromScale(1) { plus, msg ->
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
                        border = BorderStroke(1.dp, NoirColors.Hairline),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.Scale, contentDescription = null, tint = NoirColors.Cream, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Báscula 1 (${scale1Config.ip.ifBlank { "TCP" }})", style = NoirType.bodySmall, fontWeight = FontWeight.Bold, color = NoirColors.Cream)
                    }

                    Button(
                        onClick = {
                            isExtracting = true
                            extractionMessage = "Consultando Báscula 2 (${scale2Config.ip})..."
                            viewModel.extractPlusFromScale(2) { plus, msg ->
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
                        border = BorderStroke(1.dp, NoirColors.Hairline),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.Scale, contentDescription = null, tint = NoirColors.Cream, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Báscula 2 (${scale2Config.ip.ifBlank { "TCP" }})", style = NoirType.bodySmall, fontWeight = FontWeight.Bold, color = NoirColors.Cream)
                    }
                }

                if (isExtracting) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = NoirSpace.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NoirColors.Cream, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(NoirSpace.xs))
                        Text(text = extractionMessage ?: "Conectando con la báscula...", style = NoirType.bodySmall, color = NoirColors.Cream)
                    }
                } else if (!extractionMessage.isNullOrEmpty()) {
                    Text(
                        text = extractionMessage!!,
                        style = NoirType.bodySmall,
                        color = NoirColors.TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(NoirSpace.xs))

                // CAMPO DE TEXTO EDITABLE
                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = { rawInputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    label = { Text("Códigos PLU (PLU;Nombre;Precio;Sección)", color = NoirColors.TextSecondary) },
                    placeholder = { Text("Pega aquí el archivo o extrae de la báscula", color = NoirColors.TextMuted) },
                    textStyle = NoirType.bodySmall.copy(fontFamily = FontFamily.Monospace, color = NoirColors.TextPrimary),
                    shape = RoundedCornerShape(NoirRadius.sm),
                    colors = sgniaTextFieldColors()
                )

                Spacer(modifier = Modifier.height(NoirSpace.xs))

                // VISTA PREVIA DETECTADA
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
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Vista Previa (${previewList.size} detectados)",
                            style = NoirType.bodySmall,
                            fontWeight = FontWeight.Bold,
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

                Spacer(modifier = Modifier.height(NoirSpace.xxs))

                // LISTA DE VISTA PREVIA EN BENTO SUNKEN
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
                            modifier = Modifier.fillMaxSize().padding(NoirSpace.md),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin productos cargados. Oprime 'Báscula 1 (TCP)' o pega datos de Qendra.",
                                style = NoirType.bodySmall,
                                color = NoirColors.TextMuted,
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
                                        .padding(horizontal = NoirSpace.xs, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(NoirSpace.xs)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NoirColors.SurfaceRaised)
                                                .border(1.dp, NoirColors.Hairline, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "#${item.code}",
                                                style = NoirType.label,
                                                color = NoirColors.Cream,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = item.name,
                                                style = NoirType.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = NoirColors.TextPrimary
                                            )
                                            Text(
                                                text = item.section,
                                                style = NoirType.label,
                                                color = NoirColors.TextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", item.price)}/kg",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = NoirColors.Emerald
                                    )
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = NoirSpace.xs),
                                    color = NoirColors.Hairline
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(NoirSpace.sm))

                // BOTÓN DE ACCIÓN FINAL EN CREMA
                NoirPrimaryButton(
                    text = if (previewList.isEmpty()) "CARGA UN ARCHIVO O EXTRAE DE LA BÁSCULA" else "FUSIONAR ${previewList.size} PRODUCTOS (CONSERVA EXISTENTES)",
                    onClick = {
                        if (previewList.isNotEmpty()) {
                            viewModel.importParsedPlus(previewList)
                        }
                    },
                    enabled = previewList.isNotEmpty(),
                    icon = Icons.Default.CloudDownload,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
