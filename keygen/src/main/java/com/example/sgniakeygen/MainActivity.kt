package com.example.sgniakeygen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

// Paleta Noir & Crema idéntica al ecosistema SGNIA
val NoirBg = Color(0xFF0F0E0D)
val NoirSurface = Color(0xFF181715)
val NoirSurfaceRaised = Color(0xFF1F1D1A)
val NoirSurfaceSunken = Color(0xFF121110)
val NoirHairline = Color(0xFF2A2723)
val NoirHairlineStrong = Color(0xFF3A352F)
val NoirCream = Color(0xFFEEDDC8)
val NoirOnCream = Color(0xFF141312)
val NoirTextPrimary = Color(0xFFF3EFE6)
val NoirTextSecondary = Color(0xFFA8A196)
val NoirEmerald = Color(0xFF4ADE80)
val NoirAmber = Color(0xFFD9A441)

private const val HMAC_SECRET_SEED = "SGNIA_ENTERPRISE_KEY_2026_SECURITY_SALT_GAMAVEL_MEAT_SCALE"

enum class KeygenTier(val code: String, val label: String, val badge: String) {
    DEMO_10M("10M", "10 Minutos", "⏱️ DEMO EN VIVO"),
    TRIAL_7D("07D", "7 Días", "📅 PRUEBA COMERCIAL"),
    RENTAL_1M("01M", "1 Mes (30 días)", "📆 RENTA MENSUAL"),
    RENTAL_1Y("01Y", "1 Año (365 días)", "🗓️ RENTA ANUAL"),
    LIFETIME("LIFE", "De por vida", "♾️ COMPRA PERPETUA")
}

data class GeneratedKeyRecord(
    val hardwareId: String,
    val clientName: String,
    val tier: KeygenTier,
    val key: String,
    val timestamp: Long
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = NoirBg) {
                KeygenAppScreen()
            }
        }
    }
}

@Composable
fun KeygenAppScreen() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var hardwareIdInput by remember { mutableStateOf("") }
    var clientNameInput by remember { mutableStateOf("") }
    var selectedTier by remember { mutableStateOf(KeygenTier.RENTAL_1M) }
    var generatedKey by remember { mutableStateOf<String?>(null) }

    val history = remember { mutableStateListOf<GeneratedKeyRecord>() }

    fun generateKey() {
        val cleanHw = hardwareIdInput.trim().uppercase(Locale.US)
            .replace("SGN", "")
            .replace("-", "")
            .replace(" ", "")

        if (cleanHw.length < 8) {
            Toast.makeText(context, "El Hardware ID debe contener al menos 8 caracteres (ej. SGN-8F2A-3C91)", Toast.LENGTH_SHORT).show()
            return
        }

        val chars = "0123456789ABCDEF"
        val token = (1..4).map { chars.random() }.joinToString("")
        val payload = "$cleanHw:${selectedTier.code}:$token"
        val hmac = calculateHmacSha256(payload, HMAC_SECRET_SEED).take(8).uppercase(Locale.US)
        val hmacPart1 = hmac.substring(0, 4)
        val hmacPart2 = hmac.substring(4, 8)

        val fullKey = "SGN-${selectedTier.code}-$token-$hmacPart1-$hmacPart2"
        generatedKey = fullKey

        history.add(
            0,
            GeneratedKeyRecord(
                hardwareId = hardwareIdInput.trim().uppercase(Locale.US),
                clientName = clientNameInput.trim().ifBlank { "Cliente SGNIA" },
                tier = selectedTier,
                key = fullKey,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NoirBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CABECERA SGNIA KEYGEN ADMIN
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NoirSurfaceRaised)
                            .border(1.dp, NoirHairline, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = NoirCream, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text(
                            text = "SGNIA COMPANY",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp,
                            color = NoirCream
                        )
                        Text(
                            text = "Keygen Admin Industrial",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NoirTextPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(NoirEmerald.copy(alpha = 0.15f))
                        .border(1.dp, NoirEmerald.copy(alpha = 0.4f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("ADMIN OFFLINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NoirEmerald)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NoirHairline)
        }

        // TARJETA 1: HARDWARE ID DEL DISPOSITIVO DEL CLIENTE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NoirSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NoirHairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. IDENTIFICADOR DE LA TABLET (HARDWARE ID)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoirCream
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = hardwareIdInput,
                            onValueChange = { hardwareIdInput = it.uppercase(Locale.US) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("ej. SGN-8F2A-3C91", color = NoirTextSecondary) },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NoirTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NoirCream,
                                unfocusedBorderColor = NoirHairline,
                                focusedContainerColor = NoirSurfaceSunken,
                                unfocusedContainerColor = NoirSurfaceSunken
                            )
                        )

                        OutlinedButton(
                            onClick = {
                                val text = clipboardManager.getText()?.text
                                if (!text.isNullOrBlank()) {
                                    hardwareIdInput = text.trim().uppercase(Locale.US)
                                } else {
                                    Toast.makeText(context, "Portapapeles vacío", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, NoirHairline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirCream),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, tint = NoirCream, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pegar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = clientNameInput,
                        onValueChange = { clientNameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nombre del cliente o carnicería (opcional)", color = NoirTextSecondary) },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = NoirTextPrimary),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NoirCream,
                            unfocusedBorderColor = NoirHairline,
                            focusedContainerColor = NoirSurfaceSunken,
                            unfocusedContainerColor = NoirSurfaceSunken
                        )
                    )
                }
            }
        }

        // TARJETA 2: DURACIÓN DE LA LICENCIA
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NoirSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NoirHairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "2. SELECCIONA LA DURACIÓN DEL CONTRATO",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoirCream
                    )

                    KeygenTier.entries.forEach { tier ->
                        val isSelected = selectedTier == tier
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NoirCream else NoirSurfaceRaised)
                                .border(
                                    1.dp,
                                    if (isSelected) NoirCream else NoirHairline,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedTier = tier }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tier.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) NoirOnCream else NoirTextPrimary
                                )
                                Text(
                                    text = tier.badge,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color(0xFF4A4640) else NoirTextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) NoirOnCream else Color.Transparent)
                                    .border(2.dp, if (isSelected) NoirOnCream else NoirHairline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(NoirCream)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // BOTÓN PRINCIPAL DE GENERACIÓN
        item {
            Button(
                onClick = { generateKey() },
                colors = ButtonDefaults.buttonColors(containerColor = NoirCream, contentColor = NoirOnCream),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = NoirOnCream, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GENERAR CLAVE DE ACTIVACIÓN",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // RESULTADO DE CLAVE GENERADA
        if (generatedKey != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NoirSurfaceRaised),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.5.dp, NoirCream)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CLAVE GENERADA CON ÉXITO",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = NoirEmerald
                            )
                            Text(
                                text = selectedTier.badge,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NoirCream
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NoirSurfaceSunken)
                                .border(1.dp, NoirHairlineStrong, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = generatedKey!!,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = NoirCream,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(generatedKey!!))
                                    Toast.makeText(context, "Clave copiada al portapapeles", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, NoirHairline),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirTextPrimary),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NoirCream, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copiar Clave", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val client = clientNameInput.trim().ifBlank { "Cliente" }
                                    val msg = """
🥩 *SGNIA CARNICERÍA POS · LICENCIA OFFLINE*
Hola $client, aquí tienes tu clave de activación:

🔑 *CLAVE:* `${generatedKey!!}`
📱 *EQUIPO:* `${hardwareIdInput.trim().uppercase(Locale.US)}`
⏳ *DURACIÓN:* ${selectedTier.label} (${selectedTier.badge})

*Instrucciones:*
1. Abre la app en la tablet.
2. Ingresa a 'Licencia de Operación'.
3. Pega la clave y toca 'ACTIVAR LICENCIA'.

_SGNIA COMPANY • Software que Protege y Hace Crecer tu Negocio_
https://sgnia.xyz
                                    """.trimIndent()

                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, msg)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Enviar Clave a Cliente"))
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366),
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.weight(1.3f).height(46.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enviar por WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        // HISTORIAL DE CLAVES GENERADAS
        if (history.isNotEmpty()) {
            item {
                Text(
                    text = "HISTORIAL RECIENTE (${history.size})",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NoirCream
                )
            }

            items(history) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = NoirSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NoirHairline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${item.clientName} (${item.hardwareId})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NoirTextPrimary
                            )
                            Text(
                                text = "${item.tier.label} • ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.timestamp))}",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = NoirTextSecondary
                                )
                            )
                            Text(
                                text = item.key,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = NoirCream
                            )
                        }

                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(item.key))
                            Toast.makeText(context, "Copiado: ${item.key}", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NoirCream, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun calculateHmacSha256(data: String, key: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
    mac.init(secretKey)
    val bytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02X".format(it) }
}
