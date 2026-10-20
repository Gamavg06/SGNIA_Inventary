package com.example.inventariocarniceria.ui.screens.inventory

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

    // Texto limpio sin códigos falsos de simulación
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

    val darkRed = Color(0xFFB71C1C)
    val darkGreen = Color(0xFF2E7D32)
    val accentBlue = Color(0xFF1565C0)

    val previewList = remember(rawInputText) {
        com.example.inventariocarniceria.data.scale.ScaleCatalogParser.parse(rawInputText)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = accentBlue,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SINCRONIZACIÓN DE BÁSCULAS & QENDRA",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = accentBlue
                            )
                            Text(
                                text = "Extracción por socket TCP/IP y carga masiva de PLUs",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // AVISO TÉCNICO LANTRONIX (MONO-CONEXIÓN)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aviso Lantronix: Las básculas Cuora Max solo admiten 1 conexión TCP a la vez. Mantén cerrado el software Qendra en la PC mientras uses la App para evitar el rechazo ECONNREFUSED.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE65100),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // GARANTÍA DE NO BORRADO DEL CATÁLOGO EXISTENTE
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = darkGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fusión Segura: Catálogo actual de ${allProducts.size} productos protegido. Esta acción agregará o actualizará los PLUs sin borrar nada de tu inventario.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkGreen,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Spacer(modifier = Modifier.height(10.dp))

                // BOTÓN PRINCIPAL: SUBIR ARCHIVO EXPORTADO DESDE QENDRA EN PC (.TXT / .CSV)
                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📁 SUBIR ARCHIVO DE QENDRA (.TXT / .CSV)", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ACCIONES SECUNDARIAS: PEGAR TEXTO, LIMPIAR, CARGAR EJEMPLO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pegar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            rawInputText = ""
                            extractionMessage = null
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Limpiar", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            rawInputText = """
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
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Cargar Ejemplo", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // BOTONES DE ESTADO DE CONEXIÓN A BÁSCULAS TCP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Báscula 1 (TCP)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Báscula 2 (TCP)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (isExtracting) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = extractionMessage ?: "Conectando con la báscula...", fontSize = 11.sp, color = accentBlue)
                    }
                } else if (!extractionMessage.isNullOrEmpty()) {
                    Text(
                        text = extractionMessage!!,
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // CAMPO DE TEXTO
                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = { rawInputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    label = { Text("Códigos PLU (Formato: PLU;Nombre;Precio;Sección)") },
                    placeholder = { Text("Pega aquí el archivo de Qendra o extrae directamente con los botones de arriba") },
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // VISTA PREVIA DETECTADA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = null, tint = darkGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Vista Previa (${previewList.size} productos detectados)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = darkGreen
                        )
                    }

                    if (rawInputText.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                rawInputText = ""
                                extractionMessage = null
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Limpiar", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (previewList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin productos cargados. Oprime 'Báscula 1 (TCP)' o pega tus datos de Qendra.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            items(previewList) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "PLU #${item.code}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = accentBlue,
                                            modifier = Modifier.width(65.dp)
                                        )
                                        Column {
                                            Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(text = item.section, fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", item.price)}/kg",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = darkGreen
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = Color.LightGray.copy(alpha = 0.3f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // BOTÓN DE ACCIÓN FINAL
                Button(
                    onClick = {
                        if (previewList.isNotEmpty()) {
                            viewModel.importParsedPlus(previewList)
                        }
                    },
                    enabled = previewList.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FUSIONAR ${previewList.size} PRODUCTOS (CONSERVA EXISTENTES)",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
