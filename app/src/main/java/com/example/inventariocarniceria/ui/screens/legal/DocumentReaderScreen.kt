package com.example.inventariocarniceria.ui.screens.legal

import android.content.Context
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.ui.design.BentoCard
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirRadius
import com.example.inventariocarniceria.ui.design.NoirSecondaryButton
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill

enum class DocumentType {
    TERMS, PRIVACY
}

data class LegalSection(
    val number: String,
    val title: String,
    val content: String
)

/**
 * PANTALLA: LECTOR DE DOCUMENTO LEGAL (Términos o Aviso de Privacidad).
 *
 * DECISIONES DE DISEÑO:
 * 1. Tipografía generosa con interlineado holgado (lineHeight 22sp) para permitir lectura cómoda en cualquier condición de luz.
 * 2. Títulos de sección en crema cálido (#EEDDC8) con numeración de estilo placa técnica para guiar la lectura secuencial.
 * 3. Botón de pie de página para consulta web externa blindado con runCatching y advertencia clara de requerimiento de red.
 */
@Composable
fun DocumentReaderScreen(
    documentType: DocumentType,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val title = if (documentType == DocumentType.TERMS) "Términos y Condiciones" else "Aviso de Privacidad"
    val version = "v1.0.0"
    val lastUpdate = "24 de Octubre de 2026"

    val sections = if (documentType == DocumentType.TERMS) {
        listOf(
            LegalSection(
                number = "01.",
                title = "Objeto del Software y Licencia de Uso",
                content = "SGNIA Carnicería & Balanza Industrial POS es una solución tecnológica desarrollada por [NOMBRE DEL RESPONSABLE] (SGNIA COMPANY) para la gestión local de inventario cárnico, lectura de balanzas y unificación de pedidos. El titular de la carnicería adquiere una licencia de uso no exclusiva, intransferible y sujeta a los términos aquí descritos."
            ),
            LegalSection(
                number = "02.",
                title = "Operación Offline y Hardware de Pesaje",
                content = "La aplicación opera bajo arquitectura Offline-First. La precisión del pesaje depende de la calibración, nivelación física y mantenimiento del hardware de pesaje (Systel Cuora) y de la infraestructura de red local. [NOMBRE DEL RESPONSABLE] no asume responsabilidad por discrepancias resultantes de manipulación indebida de la tara, falta de calibración física o cortes de suministro eléctrico."
            ),
            LegalSection(
                number = "03.",
                title = "Captura de Precios y Rendimiento",
                content = "Los precios por kilo, mermas de despiece cárnico y límites de stock son configurados autónomamente por el personal de la tienda. El usuario es el único responsable de la exactitud de los valores numéricos introducidos y de las transacciones comerciales ejecutadas con sus clientes."
            ),
            LegalSection(
                number = "04.",
                title = "Propiedad Intelectual y Restricciones",
                content = "El código fuente, arquitectura, diseño gráfico e identidad Noir & Crema son propiedad intelectual exclusiva de SGNIA COMPANY. Queda prohibida la descompilación, ingeniería inversa, duplicación o comercialización no autorizada del software a terceros."
            ),
            LegalSection(
                number = "05.",
                title = "Contacto y Soporte",
                content = "Para aclaraciones sobre esta licencia de uso, diríjase a [DOMICILIO] o comuníquese a través de [CORREO PARA DERECHOS ARCO]."
            )
        )
    } else {
        listOf(
            LegalSection(
                number = "01.",
                title = "Identidad y Domicilio del Responsable",
                content = "[NOMBRE DEL RESPONSABLE], con domicilio de operaciones en [DOMICILIO], en estricto cumplimiento de la Ley Federal de Protección de Datos Personales, hace de su conocimiento que los datos recabados en este dispositivo son tratados de manera confidencial."
            ),
            LegalSection(
                number = "02.",
                title = "Datos Tratados y Almacenamiento Local",
                content = "La app almacena de forma local (en la base de datos interna de la tablet/dispositivo): nombres de sucursales, nombres de encargados, números de teléfono para pedidos y catálogo de proveedores cárnicos. No se realiza transferencia de bases de datos a servidores externos sin autorización explícita."
            ),
            LegalSection(
                number = "03.",
                title = "Finalidad del Tratamiento",
                content = "La información recabada tiene como única finalidad: (a) autenticar el acceso por rol (Dueño / Encargado), (b) consolidar órdenes de compra a proveedores, y (c) permitir la exportación de pedidos vía WhatsApp y reportes PDF solicitados directamente por el usuario."
            ),
            LegalSection(
                number = "04.",
                title = "Ejercicio de Derechos ARCO",
                content = "Usted tiene derecho de Acceder, Rectificar, Cancelar u Oponerse al tratamiento de sus datos personales. Para ejercer estos derechos, puede limpiar la base de datos desde los ajustes del sistema o enviar una solicitud por escrito a [CORREO PARA DERECHOS ARCO]."
            ),
            LegalSection(
                number = "05.",
                title = "Modificaciones al Aviso de Privacidad",
                content = "Cualquier modificación futura a este aviso estará disponible en la aplicación y en el portal web oficial https://sgnia.xyz."
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NoirColors.Background)
            .padding(horizontal = NoirSpace.md, vertical = NoirSpace.sm),
        verticalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
    ) {
        // BARRA SUPERIOR
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(NoirRadius.sm))
                        .background(NoirColors.SurfaceRaised)
                        .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = NoirColors.Cream
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    NoirLabel("DOCUMENTO LEGAL OFICIAL", color = NoirColors.Gold)
                    Text(
                        text = title,
                        style = NoirType.display.copy(fontSize = 20.sp),
                        color = NoirColors.TextPrimary
                    )
                }
            }
        }

        // TARJETA DE VERSIÓN Y METADATOS
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (documentType == DocumentType.TERMS) Icons.Default.Description else Icons.Default.PrivacyTip,
                            contentDescription = null,
                            tint = NoirColors.Cream,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (documentType == DocumentType.TERMS) "TÉRMINOS DE SERVICIO" else "AVISO DE PRIVACIDAD",
                            style = NoirType.label,
                            color = NoirColors.Cream
                        )
                    }

                    StatusPill(text = version, tone = PillTone.Gold)
                }

                Text(
                    text = "Última actualización: $lastUpdate",
                    style = NoirType.bodySmall,
                    color = NoirColors.TextMuted
                )
            }
        }

        // SECCIONES DEL DOCUMENTO
        items(sections) { section ->
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.lg),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = section.number,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = NoirColors.Gold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = section.title,
                        style = NoirType.title.copy(fontSize = 15.sp),
                        color = NoirColors.Cream
                    )
                }

                Text(
                    text = section.content,
                    style = NoirType.bodySmall.copy(fontSize = 13.sp, lineHeight = 21.sp),
                    color = NoirColors.TextPrimary
                )
            }
        }

        // PIE DE PÁGINA CON ENLACE WEB
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = NoirSpace.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(color = NoirColors.Hairline, thickness = 1.dp)

                Text(
                    text = "Ver versión web completa (requiere internet):",
                    style = NoirType.bodySmall.copy(fontSize = 12.sp),
                    color = NoirColors.TextMuted
                )

                NoirSecondaryButton(
                    text = "Visitar sgnia.xyz/legal",
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    height = 50.dp,
                    onClick = {
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sgnia.xyz")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }.onFailure {
                            Toast.makeText(context, "No se pudo abrir el navegador (Requiere internet)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0E0D)
@Composable
fun DocumentReaderScreenPreview() {
    DocumentReaderScreen(
        documentType = DocumentType.TERMS,
        onBack = {}
    )
}
